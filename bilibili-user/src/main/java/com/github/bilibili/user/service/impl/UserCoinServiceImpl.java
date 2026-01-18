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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.bilibili.context.LoginUserContextHolder;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.framework.coonvention.ClientException;
import com.github.bilibili.user.domain.CoinRecord;
import com.github.bilibili.user.domain.PageResponse;
import com.github.bilibili.user.mapper.CoinRecordMapper;
import com.github.bilibili.user.model.vo.CoinReqVO;
import com.github.bilibili.user.model.vo.coin.CoinDailyRemainingRespVO;
import com.github.bilibili.user.model.vo.coin.CoinHistoryReqVO;
import com.github.bilibili.user.model.vo.coin.CoinHistoryRespVO;
import com.github.bilibili.user.model.vo.coin.CoinSearchReqVO;
import com.github.bilibili.user.model.vo.coin.CoinSearchRespVO;
import com.github.bilibili.user.model.vo.coin.CoinUpIncomeReqVO;
import com.github.bilibili.user.model.vo.coin.CoinUpIncomeRespVO;
import com.github.bilibili.user.model.vo.coin.CoinVideoStatusRespVO;
import com.github.bilibili.user.mq.RabbitMQSender;
import com.github.bilibili.user.mq.StatEvent;
import com.github.bilibili.user.rpc.WalletRpcService;
import com.github.bilibili.user.service.UserCoinService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vik
 * @date 2025-12-18
 * @description
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserCoinServiceImpl extends ServiceImpl<CoinRecordMapper, CoinRecord> implements UserCoinService {

    private final WalletRpcService walletRpcService;

    private final CoinRecordMapper coinMapper;

    private final RabbitMQSender rabbitMQSender;

    /**
     * what time for get
     * 
     * @param userId
     * @return
     */
    @Override
    public Integer getUserCoinAmount(Long userId) {
        LambdaQueryWrapper<CoinRecord> eq = Wrappers.lambdaQuery(CoinRecord.class).eq(CoinRecord::getUserId, userId);
        CoinRecord userCoin = this.baseMapper.selectOne(eq);
        if (userCoin == null)
            throw new ClientException("not exist coin record for currentUerId");

        Integer amount = userCoin.getAmount();

        return amount;
    }

    /**
     * when condtion get and call this fucntion
     * update the conin
     *
     * way :
     * 1. time to get
     * 2/ acitivity to get
     * 3/ other-give coin to up
     * 
     * @param coins
     */

    @Transactional(rollbackFor = Exception.class)
    public void updateCoinAmount(Long coins, Long userId) {

        if (coins == null || userId == null)
            throw new ClientException("the params not allow to empty");

        try {
            int result = this.baseMapper.updateCoinAmountAtomic(userId, coins);
            if (result != 1)
                throw new ClientException("update error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public CoinVideoStatusRespVO getVideoCoinStatus(String videoId, Long currentUserId) {
        return null;
    }

    @Override
    public CoinDailyRemainingRespVO getDailyRemainingCoins(Long userId) {
        return null;
    }

    @Override
    public PageResponse<CoinHistoryRespVO> getCoinHistory(CoinHistoryReqVO reqVO) {

        if (reqVO == null || reqVO.getType() == null || reqVO.getPage() == null) {
            throw new ClientException("the param not allow to NULL ");
        }
        // get history form
        Long userId = LoginUserContextHolder.getUserId();
        LambdaQueryWrapper<CoinRecord> coinRecordLambdaQueryWrapper = Wrappers.lambdaQuery(CoinRecord.class)
                .eq(CoinRecord::getUserId, userId)
                .ge(CoinRecord::getCreateTime, reqVO.getStartDate())
                .le(CoinRecord::getCreateTime, reqVO.getEndDate())
                .orderByDesc(CoinRecord::getCreateTime);

        Page<CoinRecord> page = new Page<>(reqVO.getPage(), reqVO.getSize());
        Page<CoinRecord> coinRecordPage = coinMapper.selectPage(page, coinRecordLambdaQueryWrapper);
        List<CoinHistoryRespVO> collect = coinRecordPage.getRecords().stream()
                .map(each -> BeanUtil.toBean(each, CoinHistoryRespVO.class)).collect(Collectors.toList());

        PageResponse<CoinHistoryRespVO> coinRecordPageResponse = PageResponse.of(coinRecordPage, collect);

        return coinRecordPageResponse;

    }

    @Override
    public CoinUpIncomeRespVO getUpCoinIncome(CoinUpIncomeReqVO reqVO) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse<?> throwCoin(CoinReqVO req) {

        // 1. (Optional) Validation: Check if bizId exists
        // For high performance, we often SKIP this check and let the stats accumulate.
        // If you really need it, inject a ContentRpcService here.

        // default for ignore
        // 2. RPC: Deduct Money (The Financial Risk)
        boolean success = walletRpcService.deductCoin(req.getUserId(), req.getAmount());
        if (!success) {
            return Results.fail("Insufficient Coins Balance");
        }

        // 3. DB: Insert Record (The Idempotency Check)
        try {
            CoinRecord record = CoinRecord.builder()
                    .userId(req.getUserId())
                    .bizId(req.getBizId())
                    .bizType(req.getBizType())
                    .amount(req.getAmount())
                    .createTime(new Date())
                    .build();

            // Relies on UNIQUE KEY (user_id, biz_id, biz_type) in MySQL
            coinMapper.insert(record);

        } catch (DuplicateKeyException e) {
            log.warn("Duplicate Coin Throw: User {} -> Biz {}", req.getUserId(), req.getBizId());

            // CRITICAL: Compensating Transaction
            // We took the money in Step 2, but the record failed. Give it back.
            // extend advice : use the sega distributed transaction
            walletRpcService.refundCoin(req.getUserId(), req.getAmount());

            return Results.fail("You have already thrown coins for this content!");
        }

        // 4. MQ: Async Stats Update (Fire and Forget)
        // This updates the "View/Like/Coin" counts in the Stats Service
        StatEvent event = new StatEvent(req.getBizId(), req.getBizType(), "COIN", req.getAmount());
        rabbitMQSender.sendStatEvent(event);

        log.info("Coin Thrown Successfully: User {} -> Biz {}", req.getUserId(), req.getBizId());
        return Results.success();
    }

    @Override
    public PageResult<CoinSearchRespVO> searchCoinRecords(CoinSearchReqVO reqVO) {
        return null;
    }

}
