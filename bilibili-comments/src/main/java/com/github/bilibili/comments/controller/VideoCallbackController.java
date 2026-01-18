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


package com.github.bilibili.comments.controller;

import com.github.bilibili.comments.model.vo.TranscodeResultDTO;
import com.github.bilibili.comments.service.VideoService;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/v1/callbacks")
@RequiredArgsConstructor
public class VideoCallbackController {

    private final VideoService videoService;

    /**
     * The Webhook.
     * The OSS Service calls this when FFmpeg finishes.
     */
    @PostMapping("/transcode-result")
    public JsonResponse<?> handleTranscodeCallback(
                                                   @RequestHeader("X-Internal-Secret") String secret,
                                                   @RequestBody TranscodeResultDTO result) {

        // 1. Security Check (Simple Shared Secret)
        if (!"my-super-secret-key".equals(secret)) {
            log.warn("Unauthorized callback attempt");
            return Results.fail("Forbidden"); // 403
        }

        log.info("Received callback for video: {} status: {}", result.getVideoId(),
                result.getCoverUrl());

        if (result.isSuccess()) {
            // Update DB: Status -> PUBLISHED, Save HLS URL
            videoService.completeProcessing(
                    result.getVideoId(),
                    result.getHlsUrl(),
                    result.getCoverUrl());
        } else {
            // Update DB: Status -> FAILED
            videoService.failProcessing(result.getVideoId());
        }

        return Results.success();
    }
}