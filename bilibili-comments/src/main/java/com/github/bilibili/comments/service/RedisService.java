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


package com.github.bilibili.comments.service;

import com.github.bilibili.comments.constants.MomentConstants;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * ZADD: Add a moment ID to a timeline (Inbox or Outbox).
     * Time complexity: O(log(N))
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * ZREVRANGE: Get the latest moment IDs (High score/timestamp to Low).
     * Time complexity: O(log(N)+M)
     */
    public Set<Object> zRevRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * ZREM: Remove a specific moment (Retraction/Delete support).
     */
    public Long zRem(String key, Object value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * MGET (Wrapper): Bulk retrieve Moment objects by their keys.
     * Assumes Moment content is stored as Value (String/JSON) or Serialized Object.
     */
    public List<Object> mGet(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * Helper: Get specific scalar value (e.g., Follower Count).
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Helper: Cache the actual Moment content (for hydration later).
     */
    public void setEx(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void pipelineFanout(List<Long> followerIds, Long momentId, long timestamp) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] rawMomentId = String.valueOf(momentId).getBytes();

            for (Long followerId : followerIds) {
                // Key: timeline:inbox:{follower_id}

                String key = MomentConstants.KEY_INBOX + followerId;
                byte[] rawKey = key.getBytes();

                // ZADD key score member
                connection.zSetCommands().zAdd(rawKey, timestamp, rawMomentId);
            }
            return null; // Return value is ignored in pipeline
        });
    }

    public Set<ZSetOperations.TypedTuple<Object>> zRevRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    public Long hIncr(Long userId, String field, long delta) {
        String key = "user:stats:" + userId;
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /**
     * Get a specific stat (used for Influencer Check).
     */
    public Long getStat(Long userId, String field) {
        String key = "user:stats:" + userId;
        Object value = redisTemplate.opsForHash().get(key, field);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    /**
     * Init stats from MySQL if Cache Miss.
     */
    public void setStats(Long userId, Map<String, Object> stats) {
        String key = "user:stats:" + userId;
        redisTemplate.opsForHash().putAll(key, stats);
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // Optional TTL
    }

    public void mSetOptimized(Map<String, Object> map, long timeout, TimeUnit unit) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
            RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                byte[] keyBytes = keySerializer.serialize(key);
                byte[] valueBytes = valueSerializer.serialize(value);
                connection.setEx(keyBytes, unit.toSeconds(timeout), valueBytes);
            }
            return null;
        });
    }

    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

}