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


package com.github.bilibili.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Vik
 * @date 2025-12-21
 * @description
 */
@AllArgsConstructor
@Getter
public enum GroupType {

    SPECIAL(0, "特别关注"),
    QUIET(1, "悄悄关注"),
    DEFAULT(2, "默认关注"),
    USER_DEFINED(3, "用户自定义");

    private final Integer code;
    private final String description;

    public static GroupType fromCode(Integer code) {
        if (code == null)
            return DEFAULT;

        for (GroupType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return DEFAULT;
    }

    public static boolean isSystemDefined(Integer code) {
        GroupType type = fromCode(code);
        return type != USER_DEFINED;
    }

    public boolean isUserDefined() {
        return this == USER_DEFINED;
    }
}