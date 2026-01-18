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


package com.github.bilibili.comments.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.bilibili.comments.domain.Video;
import com.github.bilibili.comments.enums.VideoStatus;
import com.github.bilibili.comments.enums.mapper.VideoMapper;
import com.github.bilibili.comments.service.VideoService;
import com.github.bilibili.framework.coonvention.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    private final VideoMapper videoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initVideoUpload(String title, String rawKey) {
        Video video = Video.builder()
                .title(title)
                .ossRawKey(rawKey)
                .status(VideoStatus.CREATED.getCode())
                .build();

        videoMapper.insert(video);
        log.info("初始化视频上传成功, videoId: {}", video.getId());
        return video.getId().toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsUploaded(String videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在, videoId: " + videoId);
        }

        // 严格状态检查
        if (VideoStatus.CREATED.getCode() != (video.getStatus())) {
            throw new IllegalStateException(
                    String.format("视频状态不正确, 期望: %s, 实际: %s",
                            VideoStatus.CREATED.getCode(), video.getStatus()));
        }

        video.setStatus(VideoStatus.UPLOADED.getCode());
        videoMapper.updateById(video);
        log.info("标记视频为已上传状态, videoId: {}", videoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String videoId, VideoStatus status) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在, videoId: " + videoId);
        }

        video.setStatus(status.getCode());
        videoMapper.updateById(video);
        log.info("更新视频状态, videoId: {}, 新状态: {}", videoId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeProcessing(String videoId, String hlsUrl, String coverUrl) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在, videoId: " + videoId);
        }

        video.setOssHlsManifest(hlsUrl);
        video.setCoverUrl(coverUrl);
        video.setStatus(VideoStatus.PUBLISHED.getCode());
        videoMapper.updateById(video);

        log.info("完成视频处理, videoId: {}, hlsUrl: {}", videoId, hlsUrl);
    }

    @Override

    public Video getVideoById(String videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new RuntimeException("视频不存在, videoId: " + videoId);
        }
        return video;
    }

    @Override
    public List<Video> getVideosByUserId(Long userId) {
        return videoMapper.selectList(
                new QueryWrapper<Video>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at"));
    }

    @Override
    public void failProcessing(String videoId) {
        Video video = getVideoById(videoId);
        if (video == null || video.getStatus() != VideoStatus.FAILED.getCode()) {
            throw new ClientException("the video progrss statu exist wrong");
        }
        // if need to find entity and to
        // OTHER IS TO DIRECT UPDATE
        video.setStatus(VideoStatus.PUBLISHED.getCode());
        this.updateById(video);

        log.warn("视频处理失败, videoId: {}, title: {}", videoId, video.getTitle());
    }
}