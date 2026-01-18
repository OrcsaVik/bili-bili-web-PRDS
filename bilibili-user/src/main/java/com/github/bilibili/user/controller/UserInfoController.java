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


package com.github.bilibili.user.controller;

import com.github.bilibili.context.LoginUserContextHolder;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.user.constants.UserApiConstants;
import com.github.bilibili.user.domain.PageResponse;
import com.github.bilibili.user.domain.UserInfo;
import com.github.bilibili.user.model.vo.UserInfoPageReqVO;
import com.github.bilibili.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(UserApiConstants.PREFIX)
public class UserInfoController {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserInfoService userInfoService;

    @PutMapping("/user-infos")
    public JsonResponse<?> updateUserInfos(@RequestBody UserInfo userInfo) {
        Long userId = LoginUserContextHolder.getUserId();
        userInfo.setUserId(userId);
        return userInfoService.updateUserInfo(userInfo);

    }

    // 用户分页查询 for query navigation bar to
    // best to choice the ES to imple
    @GetMapping("/user-infos")
    public JsonResponse<PageResponse<UserInfo>> pageListUserInfos(@RequestBody UserInfoPageReqVO userInfoPageReqVO) {

        return userInfoService.pageListUserInfoByNick(userInfoPageReqVO);

    }

    @GetMapping("/batch")
    public JsonResponse<PageResponse<UserInfo>> getUserInfoByUserIdsPageList(
                                                                             @RequestParam List<Long> userIds,
                                                                             @RequestParam Integer page,
                                                                             @RequestParam Integer size) {
        PageResponse<UserInfo> result = userInfoService.getUserInfoByUserIdsPageList(userIds, page, size);
        return Results.success(result);
    }

    /**
     * Get user info by user ID
     * 
     * @param userId User ID to query
     * @return User info
     */
    @GetMapping("/{userId}")
    public JsonResponse<UserInfo> getUserInfoByUserId(@PathVariable Long userId) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        return Results.success(userInfo);
    }

}
