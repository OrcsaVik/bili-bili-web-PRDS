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


package com.github.bilibili.user.config;

import com.github.bilibili.context.LoginUserContextHolder;
import com.github.bilibili.framework.annoations.ApiLimitedRole;
import com.github.bilibili.framework.coonvention.ClientException;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Aspect
@AllArgsConstructor
@Component
public class ApiLimitedPermissonAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    private final LoginUserContextHolder loginUserContextHolder;

    // 定义切点
    @Pointcut("@annotation(com.github.bilibili.framework.annoations.ApiLimitedRole)")
    public void limit() {

    }

    // Keep keys central.
    private static final String KEY_ROLE = "auth:user:%s:role";
    private static final String KEY_ROLE_MENUS = "auth:role:%s:menus";
    private static final String KEY_ROLE_OPS = "auth:role:%s:elements";

    @Before("@annotation(limit)")
    public void checkPermission(ApiLimitedRole limit) {
        Long userId = loginUserContextHolder.getUserId();
        String roleKey = String.format(KEY_ROLE, userId);

        // 1. Fast fail: No role? No access.
        String roleCode = redisTemplate.opsForValue().get(roleKey).toString();
        if (roleCode == null) {
            throw new ClientException("Access Denied: No Role Assigned.");
        }

        // 2. Check Menus
        if (!hasIntersection(limit.limitedMenuCodeList(), KEY_ROLE_MENUS, roleCode)) {
            throw new ClientException("Access Denied: Missing Menu Permission.");
        }

        // 3. Check Operations (Elements)
        if (!hasIntersection(limit.limitedOperationCodeList(), KEY_ROLE_OPS, roleCode)) {
            throw new ClientException("Access Denied: Missing Operation Permission.");
        }
    }

    /**
     * returns true if specific codes intersect with the user's role permissions.
     * If 'requiredCodes' is null/empty, we assume no restriction -> return true.
     */
    private boolean hasIntersection(String[] requiredCodes, String keyFormat, String roleCode) {
        if (requiredCodes == null || requiredCodes.length == 0) {
            return true;
        }

        String redisKey = String.format(keyFormat, roleCode);
        Set<Object> ownedParams = redisTemplate.opsForSet().members(redisKey);
        // need to convert to String to make the equals
        if (ownedParams == null || ownedParams.isEmpty()) {
            return false;
        }

        // "Good Taste": Collections.disjoint returns true if NO elements in common.
        // We want the opposite (at least one match).
        // Using Set.of() for the array is cleaner than streams.
        return !Collections.disjoint(ownedParams, of(requiredCodes));
    }

    // donont use the stream to cal because the complex
    private static Set<String> of(String[] codes) {
        HashSet<String> stringHashSet = new HashSet<String>(codes.length);
        for (String code : codes) {
            stringHashSet.add(code);
        }
        return stringHashSet;
    }
}
