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


package com.github.bilibili.auth.domain.dataobject.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.github.bilibili.auth.domain.dataobject.moel.vo.req.SendEmailVerifiyCodeReqVO;
import com.github.bilibili.auth.domain.dataobject.moel.vo.req.SendPhoneVerifiyCodeReqVO;
import com.github.bilibili.auth.domain.dataobject.service.AliyunSmsHelper;
import com.github.bilibili.auth.domain.dataobject.service.AuthSmsService;
import com.github.bilibili.auth.domain.dataobject.service.EmailHelper;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.framework.coonvention.BaseErrorCode;
import com.github.bilibili.framework.coonvention.ClientException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Vik
 * @date 2025-12-20
 * @description
 */
@Service
@AllArgsConstructor
@Slf4j
public class AuthSmsServiceImpl implements AuthSmsService {

    private final AliyunSmsHelper aliyunSmsHelper;

    private final RedisTemplate<String, Object> redisTemplate;

    private final EmailHelper emailHelper;

    private static final String VERIFY_CODE_KEY = "verifycode:%s";

    @Override
    public JsonResponse sendVerifyCodeByPhone(SendPhoneVerifiyCodeReqVO sendPhoneVerifiyCodeReqVO) {

        if (sendPhoneVerifiyCodeReqVO.getPhone() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        String key = String.format(VERIFY_CODE_KEY, sendPhoneVerifiyCodeReqVO.getPhone());

        Object hasKey = redisTemplate.opsForValue().get(key);

        if (hasKey != null) {
            throw new RuntimeException("wait for time, please donnot try many times");
        }

        String code = RandomUtil.randomNumbers(6);

        log.info("this time to send of code is  {}-----------", code);

        // send the request
        String signName = "阿里云短信测试";
        String templateCode = "SMS_154950909";
        String templateParam = String.format("{\"code\":\"%s\"}", code);
        aliyunSmsHelper.sendMessage(signName, templateCode, sendPhoneVerifiyCodeReqVO.getPhone(), templateParam);

        redisTemplate.opsForValue().set(key, code, 3, TimeUnit.MINUTES);

        return Results.success();

    }

    @Override
    public JsonResponse sendVerifyCodeByEmail(SendEmailVerifiyCodeReqVO sendEmailVerifiyCodeReqVO) {

        if (sendEmailVerifiyCodeReqVO.getEmail() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        String key = String.format(VERIFY_CODE_KEY, sendEmailVerifiyCodeReqVO.getEmail());

        Object hasKey = redisTemplate.opsForValue().get(key);

        if (hasKey != null) {
            throw new RuntimeException("wait for time, please donnot try many times");
        }

        String code = RandomUtil.randomNumbers(6);

        log.info("this time to send of code is  {}-----------", code);

        emailHelper.sendVerifyCode(sendEmailVerifiyCodeReqVO.getEmail(), code);

        redisTemplate.opsForValue().set(key, code, 3, TimeUnit.MINUTES);

        return Results.success();

    }
}
