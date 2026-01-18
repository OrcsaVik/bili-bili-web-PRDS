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


package com.github.bilibili.comments.controller;

import com.github.bilibili.comments.domain.MomentFeedDTO;
import com.github.bilibili.comments.domain.UserMoments;
import com.github.bilibili.comments.service.MomentService;
import com.github.bilibili.comments.service.impl.FeedService;
import com.github.bilibili.context.LoginUserContextHolder;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.framework.annoations.ApiLimitedRole;
import com.github.bilibili.framework.constants.AuthRoleConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Vik
 * @date 2025-12-21
 * @description
 */
@RestController
@Slf4j
@AllArgsConstructor
public class MomentController {

    private final MomentService momentService;
    private final FeedService feedService;

    // here base on DB show to select the constants
    @ApiLimitedRole(limitedOperationCodeList = {AuthRoleConstant.ROLE_LV0})
    @PostMapping("/user-moments")
    public JsonResponse<?> addUserMoments(@RequestBody UserMoments userMoments) throws Exception {
        Long userId = LoginUserContextHolder.getUserId();
        userMoments.setUserId(userId);
        momentService.addUserMoments(userMoments);
        return Results.success();
    }

    // 获取关注用户的动态
    @GetMapping("/feed")
    public JsonResponse<List<MomentFeedDTO>> getUserSubscribeMoments(
                                                                     @RequestHeader("X-User-Id") Long userId, // Mocking Auth
                                                                     @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {

        return feedService.getUserSubscribeMoments(userId, page, size);
    }

}
