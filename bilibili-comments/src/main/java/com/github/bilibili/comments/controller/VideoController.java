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

import com.alibaba.fastjson2.JSONObject;
import com.github.bilibili.comments.domain.Video;
import com.github.bilibili.comments.enums.VideoStatus;
import com.github.bilibili.comments.rpc.StorageServiceInterface;
import com.github.bilibili.comments.service.VideoService;
import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final StorageServiceInterface storageService;

    /**
     * 1. Add Video (Init Upload)
     * Creates the metadata row and issues the upload ticket.
     */
    @PostMapping
    public JsonResponse<?> addVideo(@RequestBody VideoCreateRequest request) {
        // Validation
        if (request.getTitle() == null || request.getExtension() == null) {
            return Results.fail("Title and Extension are required");
        }

        // 1. Generate Sharded Path (OSS Layout)
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String objectKey = String.format("videos/raw/%s/%s/%s.%s",
                uuid.substring(0, 2), uuid.substring(2, 4), uuid, request.getExtension());

        // 2. Create DB Row (State: CREATED)
        String videoId = videoService.initVideoUpload(request.getTitle(), objectKey);

        // 3. Get Permission Slip (Presigned URL)
        String contentType = "video/" + (request.getExtension().equals("mov") ? "quicktime" : request.getExtension());
        String data = storageService.getVideoToken(objectKey, contentType).getData().toString();
        // json For

        PresignedUrlResponse presignedUrlResponse = JSONObject.parseObject(data, PresignedUrlResponse.class);
        // prove the key or ignore
        if (presignedUrlResponse.getObjectName() != objectKey) {
            throw new IllegalArgumentException("objectKey was not matched maybe rewrite by evil");
        }
        return Results.success(new VideoInitResponse(videoId, presignedUrlResponse.getUploadUrl(), objectKey));
    }

    /**
     * 2. Get Video Info (Metadata)
     */
    @GetMapping("/{id}")
    public JsonResponse<VideoResponse> getVideo(@PathVariable String id) {
        Video video = videoService.getVideoById(id);

        // Map Entity to DTO (Don't leak internal columns like ossRawKey to frontend)
        VideoResponse response = new VideoResponse(
                video.getId().toString(),
                video.getTitle(),
                video.getDescription(),
                video.getCoverUrl(),
                video.getStatus(),
                video.getDuration());

        return Results.success(response);
    }

    /**
     * 3. Get Video View (The Player URL)
     * Enforces strict state checks.
     */
    @GetMapping("/{id}/view")
    public JsonResponse<?> getVideoView(@PathVariable String id) {
        Video video = videoService.getVideoById(id);

        // Strict State Check
        if (video.getStatus() != VideoStatus.PUBLISHED.getCode()) {
            // Edge Case: If processing, return specific code so frontend shows "Processing..." spinner
            if (video.getStatus() == VideoStatus.PROCESSING.getCode()) {
                return Results.fail("Video is still processing");
            }
            return Results.fail("Video is not available");
        }
        // TODO CDN
        // Return the M3U8 for the player
        // In real prod, this might need a CDN domain prepended
        String playUrl = "https://your-oss-cdn.com/" + video.getOssHlsManifest();

        return Results.success(new VideoPlayResponse(playUrl, video.getCoverUrl()));
    }

    // --- DTOs (Data Transfer Objects) ---
    // Keep them static and simple.

    @Data
    public static class VideoCreateRequest {

        private String title;
        private String description;
        private String extension; // e.g., "mp4"
    }

    @Data
    public static class VideoInitResponse {

        private final String videoId;
        private final String uploadUrl; // PUT this url
        private final String objectKey; // Send this back in /complete callback
    }

    @Data
    public static class VideoResponse {

        private final String id;
        private final String title;
        private final String description;
        private final String coverUrl;
        private final Integer status;
        private final Integer duration;
    }

    @Data
    public static class VideoPlayResponse {

        private final String url; // The .m3u8 link
        private final String cover;
    }

    @Data
    public static class PresignedUrlResponse {

        private final String uploadUrl;
        private final String objectName;
    }
}