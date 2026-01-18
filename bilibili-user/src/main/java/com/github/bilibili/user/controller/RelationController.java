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


package com.github.bilibili.user.controller;

import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.user.constants.UserApiConstants;
import com.github.bilibili.user.domain.PageResponse;
import com.github.bilibili.user.domain.UserFollowing;
import com.github.bilibili.user.model.vo.FollowPageReq;
import com.github.bilibili.user.service.UserRelationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(UserApiConstants.PREFIX + "/relation")
@AllArgsConstructor
public class RelationController {

    private UserRelationService relationService;

    @PostMapping("/following/list")
    public JsonResponse<PageResponse<UserFollowing>> listFollowing(@RequestBody FollowPageReq req) {
        // One line. No logic. The way it should be.
        return new JsonResponse<>(relationService.listFollowing(req));
    }

    @PostMapping("/fans/list")
    public JsonResponse<PageResponse<UserFollowing>> listFans(@RequestBody FollowPageReq req) {
        return new JsonResponse<>(relationService.listFans(req));
    }
    // ---------------------------------------------------------------

    // here is for the rpc service interface

    @GetMapping("/follower/get")
    public JsonResponse<List<Long>> findFollowerIdsByUserId(Long currentId) {

        return Results.success(relationService.findFollowerIdsByUserId(currentId));
    }

    @GetMapping("/influencers/getFollowCount")
    public JsonResponse<List<Long>> findInfluencersFollowersByIdV2(@RequestParam Long userId) {
        return Results.success(relationService.findInfluencersFollowersByIdV2(userId));
    }

    @GetMapping("/find/following")
    public JsonResponse<List<Long>> findFollowingById(Long userId) {
        return Results.success(relationService.findFollowingById(userId));
    }

    @GetMapping("/following/hasFollow")
    public JsonResponse<Boolean> hasFollowings(Long userId) {
        return Results.success(relationService.hasFollowings(userId));
    }

}