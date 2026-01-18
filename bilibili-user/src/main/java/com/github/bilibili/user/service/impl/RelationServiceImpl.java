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


package com.github.bilibili.user.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.bilibili.framework.coonvention.ClientException;
import com.github.bilibili.user.constants.RedisConstatnts;
import com.github.bilibili.user.domain.FollowingGroup;
import com.github.bilibili.user.domain.PageResponse;
import com.github.bilibili.user.domain.UserFollowing;
import com.github.bilibili.user.enums.GroupType;
import com.github.bilibili.user.mapper.FollowingGroupMapper;
import com.github.bilibili.user.mapper.UserFollowingMapper;
import com.github.bilibili.user.model.vo.FollowPageReq;
import com.github.bilibili.user.model.vo.FollowReqVO;
import com.github.bilibili.user.service.UserRelationService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.bilibili.user.enums.GroupType.USER_DEFINED;

/**
 * @author Vik
 * @date 2025-12-21
 * @description
 */
@Service
@AllArgsConstructor
@Slf4j
public class RelationServiceImpl extends ServiceImpl<UserFollowingMapper, UserFollowing>
        implements
            UserRelationService {

    // 1L is default for group
    private static final Long DEFAULT_GROUP_ID = 1L;
    private static final int MAX_CACHE_PAGE = 20;

    private final ThreadPoolTaskExecutor userTaskExecutor;

    private final FollowingGroupMapper followingGroupMapper;

    private final RedisTemplate<String, Object> redisTemplate; // Spring Data Redis
    private final UserFollowingMapper userFollowingMapper;

    /**
     * @param followReqVO
     */
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public void follow(FollowReqVO followReqVO) {
        // 1. Validate
        Long currentUserId = followReqVO.getCurrentUserId();
        Long targetUserId = followReqVO.getTargetUserId();
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot follow self");
        }
        String followkey = String.format(RedisConstatnts.FOLLOWING_KEY, currentUserId);
        // 2. Check Redis first (fast path for "already following")
        Boolean alreadyFollowingInRedis = redisTemplate.opsForSet()
                .isMember(followkey, targetUserId.toString());

        if (Boolean.TRUE.equals(alreadyFollowingInRedis)) {
            // Might still need to check DB if Redis could be stale
            // But Redis is our source of truth for "is following" queries
            throw new ClientException("already for targetUser is followed");
        }
        Integer groupType = followReqVO.getGroupType();
        // 3. Update DB with optimistic locking or unique constraint
        UserFollowing follow = new UserFollowing();
        follow.setUserId(currentUserId);
        follow.setFollowingId(targetUserId);

        // set the gorup and when foolow

        GroupType groupEnum = GroupType.fromCode(groupType);
        // set the groupId
        Long groupId = -1L;
        if (groupEnum == USER_DEFINED) {
            // insert the mappeer to get groupId

            FollowingGroup followingGroup = new FollowingGroup();
            followingGroup.setUserId(currentUserId);
            followingGroup.setName(followReqVO.getName());
            followingGroup.setType(String.valueOf(followReqVO.getGroupType()));
            followingGroupMapper.insert(followingGroup);
            // four sign
            groupId = followingGroup.getId();

        }

        follow.setGroupId(Long.valueOf(followReqVO.getGroupType()));

        // insert IF wrong can condition eq -1
        if (groupId == -1) {
            throw new ClientException("insert the group appear wrong");
        }

        // or the keep the siuation

        // This will fail if (user_id, following_id) unique constraint violated
        boolean dbSuccess = save(follow);

        if (!dbSuccess) {
            // Insert failed - probably already following
            throw new ClientException("follow insert  is insert fail");
        }
        String followerKey = String.format(RedisConstatnts.FOLLOWER_KEY, targetUserId);
        // 4. Update Redis
        redisTemplate.opsForSet().add(followkey, targetUserId.toString());
        redisTemplate.opsForSet().add(followerKey, currentUserId.toString());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long targetUserId) {
        // 1. Basic validation
        if (followerId == null || targetUserId == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }

        if (followerId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot follow self");
        }

        // 2. Prepare entity WITHOUT timestamps - let database or MyBatis-Plus handle
        // them
        UserFollowing follow = new UserFollowing();
        follow.setUserId(followerId);
        follow.setFollowingId(targetUserId);
        // Use default group - your requirement says don't include groupId
        follow.setGroupId(DEFAULT_GROUP_ID); // Define this constant somewhere

        try {
            // 3. Insert into DB - let unique constraint be the guard
            // MyBatis-Plus will auto-fill create_time/update_time if configured
            boolean inserted = save(follow);

            if (!inserted) {
                // This shouldn't happen with a normal insert
                // Unless you have some interceptors that might prevent it
                throw new ClientException("Failed to create follow relationship");
            }

            String followingKey = String.format(RedisConstatnts.FOLLOWING_KEY, followerId);
            String followerKey = String.format(RedisConstatnts.FOLLOWER_KEY, targetUserId);

            redisTemplate.opsForSet().add(followingKey, targetUserId.toString());
            redisTemplate.opsForSet().add(followerKey, followerId.toString());

        } catch (Exception e) {
            // Log and rethrow
            throw new ClientException("Follow operation failed" + e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long targetUserId) {
        if (followerId == null || targetUserId == null) {
            throw new ClientException("the params not exist for followerId or targetUserId ");
        }

        boolean deleted = remove(new QueryWrapper<UserFollowing>()
                .eq("user_id", followerId)
                .eq("following_id", targetUserId));

        // Async Redis update whether we deleted or not
        // (handles case where Redis had stale data)
        updateRedisAsync(followerId, targetUserId, false);

        if (!deleted) {
            // Wasn't following - this is OK (idempotent)
            log.debug("Unfollow called for non-existent relationship {} -> {}",
                    followerId, targetUserId);
        }
    }

    /**
     * @param followerId
     * @param targetUserId
     * @param isFollow     to condition that unfollow
     */
    private void updateRedisAsync(Long followerId, Long targetUserId, boolean isFollow) {
        CompletableFuture.runAsync(() -> {
            try {
                String followingKey = String.format(RedisConstatnts.FOLLOWING_KEY, followerId);
                String followerKey = String.format(RedisConstatnts.FOLLOWER_KEY, targetUserId);

                if (isFollow) {
                    redisTemplate.opsForSet().add(followingKey, targetUserId.toString());
                    redisTemplate.opsForSet().add(followerKey, followerId.toString());
                } else {
                    redisTemplate.opsForSet().remove(followingKey, targetUserId.toString());
                    redisTemplate.opsForSet().remove(followerKey, followerId.toString());
                }
            } catch (Exception e) {
                log.warn("Async Redis update failed for {} {} -> {}",
                        isFollow ? "follow" : "unfollow", followerId, targetUserId, e);
                // Will be repaired by periodic sync job
            }
        });
    }

    @Override
    @Async("userTakExecutor")
    public boolean isFollowing(Long followerId, Long targetUserId) {
        // 1. Basic null checks
        if (followerId == null || targetUserId == null) {
            return false;
        }

        // 2. Quick self-check
        if (followerId.equals(targetUserId)) {
            return false; // Or true, depending on your business logic
        }

        // 3. Check Redis FIRST - it's fast
        String followingKey = String.format(RedisConstatnts.FOLLOWING_KEY, followerId);
        Boolean inRedis = redisTemplate.opsForSet().isMember(followingKey, targetUserId.toString());

        if (Boolean.TRUE.equals(inRedis)) {
            return true;
        }

        // 4. Redis says no, but Redis might be stale - check DB
        // Use EXISTS query, not SELECT * - it's cheaper
        Long count = Long.valueOf(lambdaQuery()
                .eq(UserFollowing::getUserId, followerId)
                .eq(UserFollowing::getFollowingId, targetUserId)
                .count());

        boolean isFollowingInDb = count != null && count > 0;

        // 5. If DB says yes but Redis says no, fix Redis (eventually)
        if (isFollowingInDb) {
            // Async repair - don't block the response
            CompletableFuture.runAsync(() -> {
                redisTemplate.opsForSet().add(followingKey, targetUserId.toString());
            }, userTaskExecutor);
        }

        return isFollowingInDb;
    }

    @Override
    public List<Long> findInfluencersFollowersByIdV2(Long userId) {

        if (userId == null) {
            return Collections.emptyList();

        }

        List<Long> influencersFollowedBy = userFollowingMapper.findInfluencersFollowedBy(userId);
        return influencersFollowedBy;
    }

    @Override
    public List<Long> findFollowingById(Long userId) {
        LambdaQueryWrapper<UserFollowing> eq = Wrappers.lambdaQuery(UserFollowing.class)
                .eq(UserFollowing::getUserId, userId);
        List<UserFollowing> userFollowings = this.baseMapper.selectList(eq);
        List<Long> result = userFollowings.stream().map(each -> each.getFollowingId()).collect(Collectors.toList());
        // if is empty so still empty List
        return result;

    }

    @Override
    public Boolean hasFollowings(Long userId) {
        LambdaQueryWrapper<UserFollowing> eq = Wrappers.lambdaQuery(UserFollowing.class)
                .eq(UserFollowing::getUserId, userId);
        Integer count = this.baseMapper.selectCount(eq);
        return count >= 1 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public PageResponse<UserFollowing> listFans(FollowPageReq req) {
        return getRelationPage(req, false); // false = I am the target, show me followers
    }

    @Override
    public PageResponse<UserFollowing> listFollowing(FollowPageReq req) {
        return getRelationPage(req, true); // true = I am the user, show who I follow
    }

    public static final TypeReference<PageResponse<UserFollowing>> PAGE_RESPONSE_USER_FOLLOWING =
            new TypeReference<PageResponse<UserFollowing>>() {
            };

    /**
     * The Core Logic.
     * Unified method because the logic is identical, just the direction changes.
     */
    private PageResponse<UserFollowing> getRelationPage(FollowPageReq req, boolean isFollowingList) {
        long userId = req.getUserId();
        int page = req.getPage();
        int size = req.getSize();

        // 1. CACHE LAYER (The "BigV" protection)
        // We only cache the first few pages to protect DB from 'refresh spam'
        String cacheKey = isFollowingList
                ? String.format(RedisConstatnts.FOLLOWING_PAGE_KEY, userId, page, size)
                : String.format(RedisConstatnts.FAN_PAGE_KEY, userId, page, size);

        if (page <= MAX_CACHE_PAGE) {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey).toString();
            if (StringUtils.hasText(cachedJson)) {
                // CACHE HIT: Deserialize and return
                // We stored only IDs and basic Metadata to keep it small
                // TODO here show red tip - convert and not find suit method
                @SuppressWarnings("RedundantArguments")
                PageResponse<UserFollowing> response = JSON.parseObject(cachedJson,
                        (Type) PAGE_RESPONSE_USER_FOLLOWING);

                // CRITICAL: Re-hydrate real-time UserInfo (Avatar/Name)
                // populateUserInfo(response.getRecords(), isFollowingList);
                return response;
            }
        }

        // 2. DB LAYER (Fallback)
        Page<UserFollowing> myBatisPage = new Page<>(page, size);
        LambdaQueryWrapper<UserFollowing> wrapper = new LambdaQueryWrapper<>();

        if (isFollowingList) {
            wrapper.eq(UserFollowing::getUserId, userId);
        } else {
            wrapper.eq(UserFollowing::getFollowingId, userId);
        }

        // Sorting: Newest relationships first
        wrapper.orderByDesc(UserFollowing::getCreateTime);

        IPage<UserFollowing> dbResult = this.page(myBatisPage, wrapper);

        // 3. CONSTRUCT RESPONSE
        PageResponse<UserFollowing> response = PageResponse.of(dbResult);

        // 4. WRITE BACK TO CACHE (Async is better, but Sync is safer for consistency
        // here)
        // Only cache if we are within the "hot" zone
        if (page <= MAX_CACHE_PAGE && !response.getRecords().isEmpty()) {
            // We serialize the response BEFORE populating UserInfo.
            // Why? Because we want the Cache to be pure IDs + Time.
            // UserInfo should always be fetched fresh.
            saveToCache(cacheKey, response);
        }

        // 5. HYDRATE INFO (For the final return)
        // populateUserInfo(response.getRecords(), isFollowingList);

        return response;
    }

    private void saveToCache(String key, PageResponse<UserFollowing> response) {
        try {
            String json = JSONObject.toJSONString(response);
            // Randomize TTL slightly to prevent "Cache Avalanche" (all keys expiring at
            // once)
            // cache boom
            long ttl = RedisConstatnts.CACHE_TTL_SECONDS + (long) (Math.random() * 10);
            // 30
            redisTemplate.opsForValue().set(key, json, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis serialization failed", e);
        }
    }
}
