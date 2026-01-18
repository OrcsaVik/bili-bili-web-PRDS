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


package com.github.bilibili.auth.domain.dataobject.runner;

import com.github.bilibili.auth.domain.dataobject.auth.AuthElementOperation;
import com.github.bilibili.auth.domain.dataobject.auth.AuthMenu;
import com.github.bilibili.auth.domain.dataobject.auth.AuthRole;
import com.github.bilibili.auth.domain.dataobject.auth.AuthRoleElementOperation;
import com.github.bilibili.auth.domain.dataobject.auth.AuthRoleMenu;
import com.github.bilibili.auth.domain.dataobject.config.AuthCacheKey;
import com.github.bilibili.auth.domain.dataobject.mapper.AuthElementOperationMapper;
import com.github.bilibili.auth.domain.dataobject.mapper.AuthMenuMapper;
import com.github.bilibili.auth.domain.dataobject.mapper.AuthRoleElementOperationMapper;
import com.github.bilibili.auth.domain.dataobject.mapper.AuthRoleMapper;
import com.github.bilibili.auth.domain.dataobject.mapper.AuthRoleMenuMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Vik
 * @date 2025-12-19
 * @description cache for the auth Date in the redis
 *              but not make req for authService to imple
 */

@Component
@AllArgsConstructor
@Slf4j
public class PushPermissionRedisCacheTask implements CommandLineRunner {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthRoleMapper roleMapper;
    private final AuthMenuMapper menuMapper;
    private final AuthElementOperationMapper elementMapper;
    private final AuthRoleMenuMapper roleMenuMapper;
    private final AuthRoleElementOperationMapper roleElementMapper;

    @Override
    public void run(String... args) {
        // 1. Atomic Lock to prevent multi-node collision
        Boolean canPush = redisTemplate.opsForValue().setIfAbsent(AuthCacheKey.PUSH_LOCK_FLAG, "1", 1, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(canPush)) {
            log.info("Permission cache already initialized by another instance.");
            return;
        }

        log.info("Starting permission cache synchronization...");

        // 2. Fetch all raw data once. Do NOT query in a loop.
        List<AuthRole> roles = roleMapper.selectList(null);
        List<AuthMenu> menus = menuMapper.selectList(null);
        List<AuthElementOperation> elements = elementMapper.selectList(null);
        List<AuthRoleMenu> roleMenuRelations = roleMenuMapper.selectList(null);
        List<AuthRoleElementOperation> roleElementRelations = roleElementMapper.selectList(null);

        // 3. Create ID-to-Code Lookup Maps (Memory is cheap, DB roundtrips are
        // expensive)
        Map<Long, String> menuIdToCode = menus.stream().collect(Collectors.toMap(AuthMenu::getId, AuthMenu::getCode));
        Map<Long, String> elementIdToCode = elements.stream()
                .collect(Collectors.toMap(AuthElementOperation::getId, AuthElementOperation::getElementCode));

        // 4. Group relations by RoleId
        Map<Long, List<AuthRoleMenu>> menuRelationsByRole = roleMenuRelations.stream()
                .collect(Collectors.groupingBy(AuthRoleMenu::getRoleId));
        Map<Long, List<AuthRoleElementOperation>> elementRelationsByRole = roleElementRelations.stream()
                .collect(Collectors.groupingBy(AuthRoleElementOperation::getRoleId));

        // 5. Pipeline the Redis writes
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (AuthRole role : roles) {
                String roleCode = role.getCode();
                Long roleId = role.getId();

                // Cache Menus for this Role
                List<AuthRoleMenu> rMenus = menuRelationsByRole.getOrDefault(roleId, Collections.emptyList());
                if (!rMenus.isEmpty()) {
                    String key = String.format(AuthCacheKey.ROLE_MENUS, roleCode);
                    String[] codes = rMenus.stream()
                            .map(rm -> menuIdToCode.get(rm.getMenuId()))
                            .filter(Objects::nonNull)
                            .toArray(String[]::new);
                    if (codes.length > 0)
                        redisTemplate.opsForSet().add(key, (Object[]) codes);
                }

                // Cache Elements for this Role
                List<AuthRoleElementOperation> rElements = elementRelationsByRole.getOrDefault(roleId, Collections.emptyList());
                if (!rElements.isEmpty()) {
                    String key = String.format(AuthCacheKey.ROLE_ELEMENTS, roleCode);
                    String[] codes = rElements.stream()
                            .map(re -> elementIdToCode.get(re.getElementOperationId()))
                            .filter(Objects::nonNull)
                            .toArray(String[]::new);
                    if (codes.length > 0)
                        redisTemplate.opsForSet().add(key, (Object[]) codes);
                }
            }
            return null;
        });

        log.info("Permission cache synchronization complete.");
    }
}