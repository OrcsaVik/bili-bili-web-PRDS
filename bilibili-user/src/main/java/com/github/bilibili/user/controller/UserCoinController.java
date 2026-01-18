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

import cn.hutool.db.PageResult;
import com.github.bilibili.context.LoginUserContextHolder;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.user.constants.UserApiConstants;
import com.github.bilibili.user.domain.PageResponse;
import com.github.bilibili.user.model.vo.CoinReqVO;
import com.github.bilibili.user.model.vo.coin.CoinDailyRemainingRespVO;
import com.github.bilibili.user.model.vo.coin.CoinHistoryReqVO;
import com.github.bilibili.user.model.vo.coin.CoinHistoryRespVO;
import com.github.bilibili.user.model.vo.coin.CoinSearchReqVO;
import com.github.bilibili.user.model.vo.coin.CoinSearchRespVO;
import com.github.bilibili.user.model.vo.coin.CoinUpIncomeReqVO;
import com.github.bilibili.user.model.vo.coin.CoinUpIncomeRespVO;
import com.github.bilibili.user.model.vo.coin.CoinVideoStatusRespVO;
import com.github.bilibili.user.service.UserCoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Vik
 * @date 2025-12-20
 * @description
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(UserApiConstants.PREFIX)
@Slf4j
public class UserCoinController {

    private final UserCoinService userCoinService;

    @GetMapping("/coin/get/{userId}")
    public JsonResponse<Integer> getUserCoinAmount(@PathVariable("userId") Long userId) {

        return Results.success(userCoinService.getUserCoinAmount(userId));

    }

    @PostMapping("/coin/update")
    public JsonResponse<?> addCoinUpdateAmount(@RequestParam("coins") Long coins, @RequestParam("userId") Long userId) {

        userCoinService.updateCoinAmount(coins, userId);
        return Results.success();
    }

    @GetMapping("/video/{videoId}/status")
    public JsonResponse<CoinVideoStatusRespVO> getVideoCoinStatus(@PathVariable("videoId") String videoId) {
        // ThreadLocal get currentId
        return null;
    }

    /**
     * @return
     *         search current remain to cast the coin num
     */
    @GetMapping("/daily/remaining")
    public JsonResponse<CoinDailyRemainingRespVO> getDailyRemainingCoins() {
        return null;
    }

    /**
     * @param reqVO
     * @return
     */
    @GetMapping("/history")
    public JsonResponse<PageResponse<CoinHistoryRespVO>> getCoinHistory(CoinHistoryReqVO reqVO) {
        return null;
    }

    /**
     * get the up of coin num
     * 
     * @param reqVO
     * @return
     */
    @GetMapping("/up/income")
    public JsonResponse<CoinUpIncomeRespVO> getUpCoinIncome(CoinUpIncomeReqVO reqVO) {
        return null;
    }

    @PostMapping("/throw")
    public JsonResponse<?> throwCoin(@RequestBody CoinReqVO req) {
        // Always get UserID from the secure context (Token), never from the JSON body
        Long userId = LoginUserContextHolder.getUserId();
        req.setUserId(userId);

        return userCoinService.throwCoin(req);
    }

    /**
     * base on the condition to search
     * 
     * @param reqVO
     * @return
     */
    @GetMapping("/search")
    public JsonResponse<PageResult<CoinSearchRespVO>> searchCoinRecords(CoinSearchReqVO reqVO) {
        return null;
    }

}
