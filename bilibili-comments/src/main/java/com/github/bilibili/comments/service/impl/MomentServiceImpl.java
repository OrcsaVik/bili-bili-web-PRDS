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


package com.github.bilibili.comments.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.bilibili.comments.domain.UserMoments;
import com.github.bilibili.comments.enums.mapper.UserMomentMapper;
import com.github.bilibili.comments.model.vo.GetUserSubscriPageMoment;
import com.github.bilibili.comments.mq.MomentCreatedEvent;
import com.github.bilibili.comments.mq.RabbitMQSender;
import com.github.bilibili.comments.service.MomentService;
import com.github.bilibili.comments.service.RedisService;
import com.github.bilibili.context.LoginUserContextHolder;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import static com.github.bilibili.comments.constants.MomentConstants.*;

/**
 * @author Vik
 * @date 2025-12-21
 * @description
 */
@Service
@AllArgsConstructor
@Slf4j
public class MomentServiceImpl extends ServiceImpl<UserMomentMapper, UserMoments>
        implements
            MomentService,
            ApplicationContextAware {

    private ApplicationContext applicationContext;
    private FeedService feedService;

    private final RedisService redisService;
    private final RabbitMQSender rabbitMQSender; // Wrapper for RabbitTemplate

    private Integer page = 1;
    private static final Integer size = 10;

    /**
     * link for userMoments
     * by the Event Publish machines
     *
     * @param userMoment
     * @return
     */
    @Override
    public JsonResponse<?> addUserMoments(UserMoments userMoment) {
        Long userId = LoginUserContextHolder.getUserId();
        // 1. Persistence (Source of Truth)
        UserMoments moment = UserMoments.builder()
                .userId(userId)
                .type(userMoment.getType()) // In real app, content is save
                // d to separate table/blob store, returning ID
                // assume the front will return
                .contentId(userMoment.getContentId())
                .build();

        this.baseMapper.insert(moment);

        // 2. Cache Warm-up (Availability enhancement)
        // We cache the content immediately so consumers don't hit DB during hydration
        String cacheKey = String.format(KEY_MOMENT_CONTENT, moment.getId());
        redisService.setEx(cacheKey, moment, 24, java.util.concurrent.TimeUnit.HOURS);

        // 3. Logic: Check Influencer Status (FR-002)
        // check out the cur user is infulencer
        // TODO cal the num of the usrId
        String countKey = String.format(KEY_FOLLOWER_COUNT, userId);
        Object countObj = redisService.get(countKey);
        long followerCount = countObj != null ? Long.parseLong(countObj.toString()) : 0;

        boolean isInfluencer = followerCount >= INFLUENCER_THRESHOLD;

        // 4. Async Dispatch (T-202)
        // We decouple the "Write" from the "Fan-out" using MQ
        MomentCreatedEvent event = MomentCreatedEvent.builder()
                .momentId(moment.getId())
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .isInfluencer(isInfluencer) // Consumers rely on this flag
                .build();

        rabbitMQSender.sendMomentEvent(event);

        log.info("Published Moment ID: " + moment.getId() + " | Strategy: " +
                (isInfluencer ? "PULL (Influencer)" : "PUSH (Regular)"));

        return Results.success();
    }

    @Override
    public JsonResponse<?> getUserSubscribeMoments() {
        Long userId = LoginUserContextHolder.getUserId();
        return feedService.getUserSubscribeMoments(userId, page, size);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public JsonResponse<?> getUserSubscribeMoments(@RequestBody GetUserSubscriPageMoment getUserSubscriPageMoment) {

        Long userId = LoginUserContextHolder.getUserId();
        Integer _size = Math.min(getUserSubscriPageMoment.getSize(), size);
        return feedService.getUserSubscribeMoments(userId, getUserSubscriPageMoment.getPage(), _size);
    }
}
