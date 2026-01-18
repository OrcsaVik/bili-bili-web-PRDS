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


package com.github.bilibili.user.constants;

/**
 * @author Vik
 * @date 2025-12-21
 * @description
 */
public interface RedisConstatnts {

    String FOLLOWING_KEY = "user:following:%s";

    String FOLLOWER_KEY = "user:followers:%s";

    // Format: rel:fans:{userId}:{page}:{size}
    // We include size to avoid index misalignment
    String FAN_PAGE_KEY = "rel:fans:%s:%d:%d";

    // Format: rel:following:{userId}:{page}:{size}
    String FOLLOWING_PAGE_KEY = "rel:follow:%s:%d:%d";

    // TTL in seconds (Short for freshness)
    long CACHE_TTL_SECONDS = 30;

    String VERIFY_CODE_KEY = "verifycode:";

    // Separate prefixes. Logic is cleaner.
    String BL_ACCESS = "bl:acc:";
    String BL_REFRESH = "bl:ref:";

}
