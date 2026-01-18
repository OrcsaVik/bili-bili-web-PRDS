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


package com.github.bilibili.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.bilibili.user.domain.UserRole;
import com.github.bilibili.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 用户角色缓存问题诊断测试
 */
@SpringBootTest // 加载完整的Spring上下文
// 如果只想测试Mapper层，可以使用 @MybatisPlusTest
// @MybatisPlusTest
public class UserRoleCacheDiagnosticTest {

    @Autowired
    private UserRoleMapper userRoleMapper;

    /**
     * 测试1：直接查询数据库，验证数据是否存在
     */
    @Test
    public void testDirectQuery() {
        Long userId = 2L;
        System.out.println("=== 测试1：直接查询数据库 ===");
        System.out.println("查询 userId = " + userId);

        // 方法1：使用selectMaps查看原始数据
        List<Map<String, Object>> resultList = userRoleMapper.selectMaps(
                new QueryWrapper<UserRole>()
                        .select("id", "user_id", "role_id", "create_time", "role_code")
                        .eq("user_id", userId));

        UserRole userRole = null;
        LambdaQueryWrapper<UserRole> eq = Wrappers.lambdaQuery(UserRole.class)
                .eq(UserRole::getUserId, userId);
        userRole = userRoleMapper.selectOne(eq);

        System.out.println("查询结果行数: " + resultList.size());
        if (!resultList.isEmpty()) {
            System.out.println("第一行数据详情:");
            Map<String, Object> row = resultList.get(0);
            row.forEach((key, value) -> System.out.println("  " + key + " = " + value + " (类型: " +
                    (value != null ? value.getClass().getSimpleName() : "null") + ")"));
        } else {
            System.out.println("⚠️ 警告：未查询到任何数据！");
        }
        System.out.println();
    }

    @Test
    public void testQueryWrapperConstruction() {
        System.out.println("=== 测试2：验证LambdaQueryWrapper构建 ===");

        // 手动构建Wrapper，查看生成的SQL
        Long testUserId = 2L;
        LambdaQueryWrapper<UserRole> eq = Wrappers.lambdaQuery(UserRole.class)
                .eq(UserRole::getUserId, testUserId);

        // 执行查询
        UserRole userRole = userRoleMapper.selectOne(eq);

        System.out.println("查询结果: " + userRole);
        if (userRole != null) {
            System.out.println("用户角色详情:");
            System.out.println("  id: " + userRole.getId());
            System.out.println("  userId: " + userRole.getUserId());
            System.out.println("  roleId: " + userRole.getRoleId());
            System.out.println("  roleCode: " + userRole.getRoleCode());
        }

        // 断言验证
        assertNotNull(userRole, "用户角色不应为空");
        assertEquals(testUserId, userRole.getUserId(), "用户ID应匹配");
        assertNotNull(userRole.getRoleCode(), "角色编码不应为空");

        System.out.println("✅ 查询包装器测试通过！");
    }

}