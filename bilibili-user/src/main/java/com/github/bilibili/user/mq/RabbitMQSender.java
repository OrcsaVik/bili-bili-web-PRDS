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


package com.github.bilibili.user.mq;

import com.github.bilibili.user.config.CoinRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Asynchronously send a Stat Update Event.
     * This is "Fire and Forget" - we don't wait for the Stats Service to finish.
     * this is coin for rabbitMQSender
     * 
     * @param event The payload containing bizId, type, and amount.
     */
    public void sendStatEvent(StatEvent event) {
        try {
            log.info("[MQ] Sending Stat Event: BizID={}, Type={}, Delta={}",
                    event.getBizId(), event.getType(), event.getDelta());

            rabbitTemplate.convertAndSend(
                    CoinRabbitConfig.EXCHANGE_INTERACTION,
                    CoinRabbitConfig.ROUTING_KEY_COIN,
                    event);
        } catch (Exception e) {
            // AV Design: If MQ fails, do we rollback the Coin Throw?
            // NO. The user paid, the record is in DB.
            // Stats being out of sync (showing 99 coins instead of 100) is acceptable.
            // We just log the error. In a Pro system, we might write to a local fallback
            // log.
            log.error("[MQ] Failed to send Stat Event! Stats will be eventually inconsistent.", e);
        }
    }
}