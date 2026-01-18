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


package com.github.bilibili.comments.constants;

public class MomentConstants {

    /**
     * 影响力用户阈值
     */
    public static final long INFLUENCER_THRESHOLD = 100_000;

    /**
     * 用户关注者数量Redis Key模式
     */
    public static final String KEY_FOLLOWER_COUNT = "user:stats:%d:followers";

    /**
     * 动态内容Redis Key模式
     */
    public static final String KEY_MOMENT_CONTENT = "moment:content:%d";

    /**
     * 用户收件箱Redis Key模式
     */
    public static final String KEY_INBOX = "timeline:inbox:%d";

    /**
     * 用户发件箱Redis Key模式
     */
    public static final String KEY_OUTBOX = "timeline:outbox:%d";

    /**
     * 批量操作大小
     */
    public static final int BATCH_SIZE = 500;
}