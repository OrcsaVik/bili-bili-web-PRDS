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

import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.framework.coonvention.BaseErrorCode;
import com.github.bilibili.framework.coonvention.ClientException;
import com.github.bilibili.user.constants.UserApiConstants;
import com.github.bilibili.user.model.vo.LoginReq;
import com.github.bilibili.user.model.vo.UserRegisterReqVO;
import com.github.bilibili.user.model.vo.UserRegisterRspVO;
import com.github.bilibili.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(UserApiConstants.PREFIX)

public class UserContoller {

    private final UserService userService;

    // 用户注册
    @PostMapping("/register")
    public JsonResponse<UserRegisterRspVO> register(@RequestBody UserRegisterReqVO user) {
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        UserRegisterRspVO register = userService.register(user);
        return Results.success(register);
    }

    // 登录 生成token

    // 登录接口 - 生成Token

    @PostMapping("/login")
    public JsonResponse<?> login(
                                 @RequestBody LoginReq loginReq,
                                 // Extract Headers Here
                                 @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                 @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        // Fail fast if device ID is missing (enforce security)
        if (deviceId == null) {
            return Results.fail("Missing X-Device-Id header");
        }

        // Pass them as arguments to the service
        return Results.success(userService.login(loginReq, deviceId, userAgent));
    }

    @GetMapping("/refresh")
    public JsonResponse<?> refreshToken(
                                        @RequestHeader("Authorization") String refreshToken,
                                        @RequestHeader(value = "X-Device-Id", required = false) String device,
                                        @RequestHeader(value = "User-Agent", required = false) String userAgent) throws Exception {
        if (refreshToken == null || device == null || userAgent == null) {
            return Results.fail(new ClientException("Missing required headers"));
        }
        return Results.success(userService.refreshAccessToken(refreshToken, device, userAgent));
    }

}
