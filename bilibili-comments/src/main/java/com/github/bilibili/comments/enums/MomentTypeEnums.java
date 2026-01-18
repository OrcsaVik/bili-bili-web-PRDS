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


package com.github.bilibili.comments.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Getter
public enum MomentTypeEnums {

    VIDEO(0, "视频", "video"),
    LIVE(1, "直播", "live"),
    COLUMN(2, "动态专栏", "column"),
    ARTICLE(3, "文章", "article"),
    AUDIO(4, "音频", "audio"),
    TOPIC(5, "话题", "topic"),
    SHARE(6, "分享", "share");

    private final Integer code;
    private final String desc;
    private final String alias;

    MomentTypeEnums(Integer code, String desc, String alias) {
        this.code = code;
        this.desc = desc;
        this.alias = alias;
    }

    /**
     * 根据code获取枚举
     */
    public static MomentTypeEnums fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MomentTypeEnums type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据alias获取枚举
     */
    public static MomentTypeEnums fromAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            return null;
        }
        for (MomentTypeEnums type : values()) {
            if (type.getAlias().equalsIgnoreCase(alias)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查code是否有效
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 获取所有code列表
     */
    public static List<Integer> getAllCodes() {
        return Arrays.stream(values())
                .map(MomentTypeEnums::getCode)
                .collect(Collectors.toList());
    }
}
