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

import com.github.bilibili.comments.domain.MomentFeedDTO;
import com.github.bilibili.comments.domain.UserMoments;
import com.github.bilibili.comments.enums.mapper.UserMomentMapper;
import com.github.bilibili.comments.rpc.UserFollowRpcService;
import com.github.bilibili.comments.service.RedisService;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.framework.coonvention.ClientException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FeedService {

    private final RedisService redisService;
    private final UserFollowRpcService userFollowRpcService;
    private final ThreadPoolTaskExecutor momentsTaskExecutor;
    private final UserMomentMapper userMomentMapper;

    private static final String KEY_INBOX = "timeline:inbox:%d";
    private static final String KEY_OUTBOX = "timeline:outbox:%d";
    private static final String KEY_CONTENT = "moment:content:%d";

    /**
     * Optimized High-Concurrency Feed Retrieval
     */
    public JsonResponse<List<MomentFeedDTO>> getUserSubscribeMoments(Long currentUserId, int page, int size) {
        long start = (long) (page - 1) * size;
        long end = start + size + 10; // Buffer

        // =================================================================
        // 1. ASYNC TASK A: Fetch Regular Inbox (Push Data)
        // =================================================================
        CompletableFuture<Set<ZSetOperations.TypedTuple<Object>>> inboxFuture = CompletableFuture.supplyAsync(() -> {
            String inboxKey = String.format(KEY_INBOX, currentUserId);
            return redisService.zRevRangeWithScores(inboxKey, 0, end);
        }, momentsTaskExecutor);

        // =================================================================
        // 2. ASYNC TASK B: Fetch Influencers & Their Outboxes (Pull Data)
        // ========================================f========================
        CompletableFuture<List<TypedTuple<Object>>> influencerFuture = CompletableFuture.supplyAsync(() -> {
            // 2.1 Get Influencer List
            List<Long> influencerIds = userFollowRpcService.findInfluencersFollowersByIdV2(currentUserId).getData();
            if (influencerIds == null || influencerIds.isEmpty())
                return new ArrayList<>();

            // 2.2 Optimization: Use Parallel Stream or Redis Pipeline here
            // Parallel Stream allows concurrent Redis calls within this thread's scope
            return influencerIds.parallelStream()
                    .map(infId -> {
                        String outboxKey = String.format(KEY_OUTBOX, infId);
                        // Fetch range
                        return redisService.zRevRangeWithScores(outboxKey, 0, end);
                    })
                    .filter(Objects::nonNull)
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
        }, momentsTaskExecutor);

        // =================================================================
        // 3. JOIN & MERGE (Barrier)
        // =================================================================
        List<TypedTuple<Object>> combinedList = new ArrayList<>();
        try {
            // Wait for both tasks to finish (Blocking only for the longest task)
            CompletableFuture.allOf(inboxFuture, influencerFuture).join();

            // Collect Results
            if (inboxFuture.get() != null)
                combinedList.addAll(inboxFuture.get());
            if (influencerFuture.get() != null)
                combinedList.addAll(influencerFuture.get());

        } catch (Exception e) {
            log.error("Async Feed Error", e);
            throw new ClientException("\"Async Feed Error\", e");
        }

        // =================================================================
        // 4. SORT & PAGING (CPU Bound - Fast)
        // =================================================================
        // if all cache is all empty to make the recache
        if (combinedList.isEmpty()) {
            // Check if user actually follows anyone
            boolean hasFollowings = userFollowRpcService.hasFollowings(currentUserId);
            if (hasFollowings) {
                log.warn("[Feed] Cache Miss for User {}. Fallback to DB Pull.", currentUserId);
                return getFeedFromDatabaseFallback(currentUserId, page, size);
            }
            return Results.success(Collections.emptyList());
        }

        combinedList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        List<TypedTuple<Object>> pagedIds = combinedList.stream()
                .skip(start)
                .limit(size)
                .collect(Collectors.toList());

        // =================================================================
        // 5. HYDRATION with CACHE MISS HANDLING
        // =================================================================
        return Results.success(hydrateContent(pagedIds));
    }

    /**
     * Handles retrieving content from Redis -> Fallback to DB -> Async Re-cache
     */
    private List<MomentFeedDTO> hydrateContent(List<TypedTuple<Object>> pagedIds) {

        // 1. Prepare Keys
        List<Long> momentIds = pagedIds.stream()
                .map(t -> Long.valueOf(t.getValue().toString()))
                .collect(Collectors.toList());

        List<String> cacheKeys = momentIds.stream()
                .map(id -> String.format(KEY_CONTENT, id))
                .collect(Collectors.toList());

        // 2. Batch Get from Redis
        List<Object> cachedObjects = redisService.mGet(cacheKeys);
        List<MomentFeedDTO> finalResult = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();

        // 3. Check for Misses
        for (int i = 0; i < momentIds.size(); i++) {
            Object obj = cachedObjects.get(i);
            if (obj != null) {
                finalResult.add(convertToDto((UserMoments) obj));
            } else {
                // Detected Cache Miss
                missingIds.add(momentIds.get(i));
            }
        }

        // 4. Handle Misses (Query DB + Async Re-populate)
        if (!missingIds.isEmpty()) {
            log.warn("Cache Miss for {} items. Querying DB...", missingIds.size());

            // A. Batch Query DB (Source of Truth)
            List<UserMoments> dbMoments = userMomentMapper.selectBatchIds(missingIds);

            // B. Add to Result
            dbMoments.forEach(m -> finalResult.add(convertToDto(m)));

            // C. ASYNC: Re-populate Cache (Send to separate thread)
            CompletableFuture.runAsync(() -> {
                repopulateCache(dbMoments);
            }, momentsTaskExecutor);
        }

        // 5. Re-sort final result because DB returns might be out of order
        finalResult.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return finalResult;
    }

    /**
     * Async Cache Warm-up logic
     */
    private void repopulateCache(List<UserMoments> moments) {
        Map<String, Object> cacheMap = new HashMap<>();
        for (UserMoments m : moments) {
            cacheMap.put(String.format(KEY_CONTENT, m.getId()), m);
        }
        // Use mSet (multi-set) for efficiency
        redisService.mSetOptimized(cacheMap, 24, TimeUnit.HOURS);
        log.info("Async Cache Warm-up completed for {} items", moments.size());
    }

    private JsonResponse<List<MomentFeedDTO>> getFeedFromDatabaseFallback(Long userId, int page, int size) {
        // 1. Who do I follow?
        List<Long> followingIds = userFollowRpcService.findFollowingById(userId);
        followingIds.add(userId); // Include myself

        // 2. Query DB directly (The "Slow" Query)
        // SELECT * FROM t_user_moments WHERE user_id IN (...) ORDER BY create_time DESC LIMIT ...
        // You need to add this method to your Mapper
        List<UserMoments> dbMoments = userMomentMapper.selectMomentsByBa(followingIds, page, size).getRecords();

        // 3. Convert to DTO
        List<MomentFeedDTO> dtos = dbMoments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 4. Async: Rebuild the Redis Inbox?
        // Optional. Usually, we just let the next "Push" event start filling the inbox again.

        return Results.success(dtos);
    }

    private MomentFeedDTO convertToDto(UserMoments entity) {
        return MomentFeedDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .mediaUrl(String.valueOf(entity.getContentId())) // In real app, resolve this to URL
                .timestamp(entity.getCreateTime().getTime())
                .build();
    }
}