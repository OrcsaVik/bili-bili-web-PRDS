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


package com.github.bilibili.user.model.vo.coin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CoinHistoryRespVO {

    /**
     * 记录ID（主键）
     */
    private String id;

    /**
     * 操作类型
     * 例如：THROW（投币）、RECEIVE（收币）、RECHARGE（充值）、WITHDRAW（提现）
     */
    private String type;

    /**
     * 硬币数量（正数：增加，负数：减少）
     */
    private Integer amount;

    /**
     * 操作后的余额
     */
    private Integer balance;

    /**
     * 关联内容类型
     * 例如：VIDEO（视频）、ARTICLE（文章）、MOMENT（动态）、DANMU（弹幕）
     */
    private String relatedType;

    /**
     * 关联内容ID（视频ID、文章ID等）
     */
    private String relatedId;

    /**
     * 关联内容标题（用于前端展示）
     */
    private String relatedTitle;

    /**
     * 操作描述
     * 例如："给视频【xxxx】投币"、"充值到账"、"提现成功"
     */
    private String description;

    /**
     * 操作时间（前端序列化格式）
     * JSON格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
}