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


package com.github.bilibili.comments.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.bilibili.comments.domain.Video;
import com.github.bilibili.comments.enums.VideoStatus;

import java.util.List;

public interface VideoService extends IService<Video> {

    /**
     * 初始化视频上传
     * @param title 视频标题
     * @param rawKey 原始文件路径
     * @return 视频ID
     */
    String initVideoUpload(String title, String rawKey);

    /**
     * 标记视频为已上传状态
     * @param videoId 视频ID
     */
    void markAsUploaded(String videoId);

    /**
     * 更新视频状态
     * @param videoId 视频ID
     * @param status 状态
     */
    void updateStatus(String videoId, VideoStatus status);

    /**
     * 完成视频处理
     * @param videoId 视频ID
     * @param hlsUrl HLS地址
     * @param coverUrl 封面地址
     */
    void completeProcessing(String videoId, String hlsUrl, String coverUrl);

    /**
     * 获取视频信息
     * @param videoId 视频ID
     * @return 视频对象
     */
    Video getVideoById(String videoId);

    /**
     * 根据用户ID获取视频列表
     * @param userId 用户ID
     * @return 视频列表
     */
    List<Video> getVideosByUserId(Long userId);

    void failProcessing(String videoId);
}