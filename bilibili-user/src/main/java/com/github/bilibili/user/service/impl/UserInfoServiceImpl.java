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


package com.github.bilibili.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.bilibili.context.LoginUserContextHolder;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.framework.coonvention.ClientException;
import com.github.bilibili.user.domain.PageResponse;
import com.github.bilibili.user.domain.UserInfo;
import com.github.bilibili.user.mapper.UserInfoMapper;
import com.github.bilibili.user.model.vo.UserInfoPageReqVO;
import com.github.bilibili.user.service.UserInfoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Vik
 * @date 2025-12-17
 * @description
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    private final UserInfoMapper userInfoMapper;

    @Override
    public JsonResponse<?> updateUserInfo(UserInfo userInfo) {

        Long userId = LoginUserContextHolder.getUserId();
        userInfo.setUserId(userId);
        userInfoMapper.updateUserInfoByUserId(userInfo.getUserId(), userInfo);
        return Results.success();

    }

    @Override
    public JsonResponse<PageResponse<UserInfo>> pageListUserInfoByNick(UserInfoPageReqVO userInfoPageReqVO) {

        if (userInfoPageReqVO == null || userInfoPageReqVO.getPage() == null ||
                userInfoPageReqVO.getSize() == null)
            throw new RuntimeException("分页参数不能为空");
        Integer page = userInfoPageReqVO.getPage();
        Integer size = userInfoPageReqVO.getSize();
        Page<UserInfo> userInfoPage = new Page<>((page - 1) * size, size);
        if (userInfoPageReqVO.getNickName() != null) {
            // 按照创建时间用户 倒叙排序
            LambdaQueryWrapper<UserInfo> eq = Wrappers.lambdaQuery(UserInfo.class)
                    .eq(UserInfo::getNick, userInfoPageReqVO.getNickName())
                    .orderByDesc(UserInfo::getCreateTime);

            Page<UserInfo> userInfos = userInfoMapper.selectPage(userInfoPage, eq);

            PageResponse<UserInfo> userInfoPageResponse = PageResponse.of(userInfos);
            return Results.success(userInfoPageResponse);
        } else {
            LambdaQueryWrapper<UserInfo> eq = Wrappers.lambdaQuery(UserInfo.class)
                    .orderByDesc(UserInfo::getCreateTime);

            Page<UserInfo> userInfos = userInfoMapper.selectPage(userInfoPage, eq);
            PageResponse<UserInfo> userInfoPageResponse = PageResponse.of(userInfos);
            // 如果为空 返回空白数据
            return Results.success(userInfoPageResponse);
        }
    }

    public PageResponse<UserInfo> getUserInfoByUserIdsPageList(List<Long> userIds, Integer page, Integer size) {
        if (userIds == null || userIds.isEmpty())
            return new PageResponse<>();

        Page<UserInfo> userInfoPage = new Page<>(page, size);

        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = Wrappers.lambdaQuery(UserInfo.class)
                .in(UserInfo::getUserId, userIds);

        Page<UserInfo> userInfos = userInfoMapper.selectPage(userInfoPage, lambdaQueryWrapper);

        PageResponse<UserInfo> result = PageResponse.of(userInfos);

        return result;

    }

    @Override
    public UserInfo getUserInfoByUserId(Long userId) {
        if (userId == null) {
            throw new ClientException(
                    "the userId not allow to empty");
        }

        return this.getById(userId);
    }

}
