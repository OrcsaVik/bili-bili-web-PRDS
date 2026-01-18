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


package com.github.bilibili.framework;

import com.github.bilibili.framework.coonvention.AbstractException;
import com.github.bilibili.framework.coonvention.BaseErrorCode;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * @author Vik
 * @date 2025-12-17
 * @description
 */
@UtilityClass
public class Results<R> {

    /**
     * 成功响应，带数据
     */
    public static <T> JsonResponse<T> success(T data) {
        return new JsonResponse<>(data);
    }

    /**
     * 成功响应，无数据（Void）
     */
    public static JsonResponse<Void> success() {
        return new JsonResponse<>(null);
    }

    /**
     * 失败响应，使用默认错误码和消息
     */
    public static JsonResponse<?> fail() {
        return JsonResponse.builder()
                .code(BaseErrorCode.SERVICE_ERROR.code())
                .msg(BaseErrorCode.SERVICE_ERROR.message())
                .build();
    }

    /**
     * 失败响应，根据异常构建
     */
    public static JsonResponse<?> fail(AbstractException ex) {
        return JsonResponse.builder()
                .code(Optional.ofNullable(ex.getErrorCode()).orElse(null))
                .msg(ex.errorMessage)
                .build();
    }

    public static JsonResponse<?> fail(String message) {
        return JsonResponse.builder()
                .code(BaseErrorCode.CLIENT_ERROR.code())
                .msg(Optional.ofNullable(message).orElse("the default wrong meesage"))

                .build();
    }

    /**
     * 自定义失败响应
     */
    public static JsonResponse<?> fail(String code, String msg) {
        return JsonResponse.builder()
                .code(code)
                .msg(msg).build();

    }

    /**
     * 成功响应，自定义消息（通常用于提示）
     */
    public static <T> JsonResponse<T> success(T data, String msg) {
        JsonResponse<T> response = new JsonResponse<>(data);
        response.setMsg(msg);
        return response;
    }

    /**
     * 成功响应，自定义状态码和消息
     */
    public static <T> JsonResponse<T> success(T data, String code, String msg) {
        JsonResponse<T> response = new JsonResponse<>(data);
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
}
