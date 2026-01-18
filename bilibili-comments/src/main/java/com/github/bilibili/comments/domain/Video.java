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


package com.github.bilibili.comments.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@TableName("videos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Video implements Serializable {

    /**
     * 主键ID (BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY)
     * 可以使用AUTO自增，也可以考虑使用ASSIGN_ID（雪花算法）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID (Owner)
     */
    private Long userId;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 区域 (默认"beijing")
     */
    @Builder.Default
    private String area = "beijing";

    /**
     * 状态机 (最重要的列)
     * 0: 已创建 (预上传)
     * 1: 已上传 (原始文件在OSS，等待队列)
     * 2: 处理中 (转码进行中)
     * 3: 已发布 (可播放)
     * 4: 失败 (转码错误)
     * 5: 封禁 (违反政策)
     */
    @Builder.Default
    private Integer status = 0;

    /**
     * OSS存储桶
     */
    private String ossBucket;

    /**
     * OSS原始文件路径 (原始MP4上传路径)
     */
    private String ossRawKey;

    /**
     * OSS HLS清单路径 (主.m3u8文件路径，处理前为NULL)
     */
    private String ossHlsManifest;

    /**
     * 视频时长 (秒)
     */
    @Builder.Default
    private Integer duration = 0;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
