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


package com.github.bilibili.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.bilibili.user.domain.PageResponse;
import com.github.bilibili.user.domain.UserFollowing;
import com.github.bilibili.user.model.vo.FollowPageReq;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface UserRelationService extends IService<UserFollowing> {

    // Action
    void follow(Long currentUserId, Long targetUserId);

    void unfollow(Long currentUserId, Long targetUserId);

    /**
     * get the follower of userId
     * 
     * @param req
     * @return
     */
    PageResponse<UserFollowing> listFollowing(FollowPageReq req);

    /**
     * get the fans of userId
     * 
     * @param req
     * @return
     */
    PageResponse<UserFollowing> listFans(FollowPageReq req);

    // Query Status (Crucial for UI)
    boolean isFollowing(Long currentUserId, Long targetUserId);

    default List<Long> findFollowerIdsByUserId(Long userId) {

        if (userId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<UserFollowing> eq = Wrappers.lambdaQuery(UserFollowing.class)
                .eq(UserFollowing::getFollowingId, userId);

        List<UserFollowing> userFollowings = this.getBaseMapper().selectList(eq);

        List<Long> result = userFollowings.stream().map(userFollowing -> userFollowing.getUserId())
                .collect(Collectors.toList());

        return result;

    }

    /**
     * get all follwer like big V form currentUswerId
     * 
     * @param userId
     * @return
     */
    List<Long> findInfluencersFollowersByIdV2(Long userId);

    List<Long> findFollowingById(Long userId);

    Boolean hasFollowings(Long userId);
}