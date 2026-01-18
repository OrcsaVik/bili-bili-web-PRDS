/*
 * MIT License
 *
 * Copyright (c) [2025] [OrcasVik]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *<link>https://github.com/OrcsaVik</link>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.github.bilibili.comments.mq;

import com.github.bilibili.comments.config.RabbitMQConfig;
import com.github.bilibili.comments.constants.MomentConstants;
import com.github.bilibili.comments.rpc.UserFollowRpcService;
import com.github.bilibili.comments.service.RedisService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.bilibili.comments.constants.MomentConstants.BATCH_SIZE;

/**
 * is to for the push moment for the followers
 * 
 * by the inbox and outbox
 * 
 * if is influencer by the use the oubox
 * and the user to get outbox and update the moment
 * 
 * and outbox is design for single user
 */
@Slf4j
@Component
public class MomentDistributionWorker {

    private final RedisService redisService;
    private final UserFollowRpcService userFollowRpcService;

    public MomentDistributionWorker(RedisService redisService, UserFollowRpcService userFollowRpcService) {
        this.redisService = redisService;
        this.userFollowRpcService = userFollowRpcService;
    }

    /**
     * T-301: Async Consumer
     * Listens to the 'moments.distribution.queue'
     * Uses MANUAL ACK mode to ensure reliability.
     *
     * to add the ipemoptentKey condition
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_DISTRIBUTION, ackMode = "MANUAL")
    public void onMessage(@Payload MomentCreatedEvent event,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        long start = System.currentTimeMillis();
        log.info("[Worker] Received Moment ID: {} from User: {}", event.getMomentId(), event.getUserId());

        // use the uuid if the update event for it
        String idempotencyKey = "proceessed:dist" + UUID.randomUUID().toString();

        // TTL is 3s consider for the redis progress
        Boolean isNew = redisService.setIfAbsent(idempotencyKey, "1", 3, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("[Idempotency] Duplicate message detected: {}. Skipping.", event.getMomentId());
            // ACK immediately because we already did this work before
            channel.basicAck(tag, false);
        }

        try {
            if (event.isInfluencer()) {
                // T-303: Strategy B - Pull (Optimization)
                handleInfluencerStrategy(event);
            } else {
                // T-302: Strategy A - Push (Fan-out)
                handleRegularStrategy(event);
            }

            // T-301: Success - Acknowledge the message
            // multiple=false means only ack this specific tag
            channel.basicAck(tag, false);
            log.info("[Worker] Processed Moment ID: {} in {}ms", event.getMomentId(), (System.currentTimeMillis() - start));

        } catch (Exception e) {
            // T-304: Dead Letter Handling
            log.error("[Worker] Failed to process Moment ID: {}. Sending to DLX.", event.getMomentId(), e);

            // CRITICAL: If logic fails, we must DELETE the idempotency key
            // so that the retry (or DLX) can actually try again!
            redisService.delete(idempotencyKey);

            log.error("Failed to process...", e);
            channel.basicNack(tag, false, false);
        }
    }

    /**
     * Strategy B: Pull Logic
     * Just write to the Influencer's OWN Outbox.
     * Consumers will "Pull" from here and merge with their inbox at read-time.
     */
    private void handleInfluencerStrategy(MomentCreatedEvent event) {
        String outboxKey = String.format(MomentConstants.KEY_OUTBOX, event.getUserId());

        // ZADD timeline:outbox:{uid} timestamp momentId
        redisService.zAdd(outboxKey, event.getMomentId(), event.getTimestamp());

        log.info(">> [PULL] Updated Outbox for Influencer ID: {}", event.getUserId());
    }

    /**
     * Strategy A: Push Logic (Fan-out)
     * Get all followers and write to THEIR Inboxes.
     */
    private void handleRegularStrategy(MomentCreatedEvent event) {
        // 1. Fetch Followers (Source of Truth)
        List<Long> followerIds = userFollowRpcService.findFollowerIdsByUserId(event.getUserId()).getData();

        if (followerIds.isEmpty()) {
            log.info(">> [PUSH] User has no followers. Skipping fan-out.");
            return;
        }

        // 2. Batch Processing (to handle memory limits if list is semi-large, e.g. 50k)
        // Note: For <100k, we can usually process in one go, but batching is safer for
        // Redis buffers.
        int total = followerIds.size();
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(total, i + BATCH_SIZE);
            List<Long> batch = followerIds.subList(i, end);

            // 3. Pipeline Write (Redis Optimization)
            redisService.pipelineFanout(batch, event.getMomentId(), event.getTimestamp());
        }

        log.info(">> [PUSH] Fanned out to {} followers (Batch size: {})", total, BATCH_SIZE);
    }
}