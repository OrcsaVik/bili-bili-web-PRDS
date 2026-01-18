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


package com.github.bilibili.comments.rpc;

import com.github.bilibili.framework.JsonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Vik
 * @date 2025-12-21
 * @description
 */
@FeignClient(name = ApiConstants.USER_CALL)
public interface UserFollowRpcService {

    public static final String PREFIX = "/api/user";

    /**
     * find the all following for currentId
     * @param userId
     * @return
     */
    @GetMapping(PREFIX + "/follower/get")
    public JsonResponse<List<Long>> findFollowerIdsByUserId(Long userId);

    /**
     * search the currentId follow  infulencer like BigV
     * @param userId
     * @return
     */
    @GetMapping(PREFIX + "/influencers/getFollowCount")
    JsonResponse<List<Long>> findInfluencersFollowersByIdV2(@RequestParam Long userId);

    @GetMapping(PREFIX + "/find/following")
    List<Long> findFollowingById(Long userId);

    @GetMapping(PREFIX + "/following/hasFollow")
    Boolean hasFollowings(Long currentUserId);

}
