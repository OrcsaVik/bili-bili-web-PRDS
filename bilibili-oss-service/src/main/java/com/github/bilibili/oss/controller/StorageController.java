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


package com.github.bilibili.oss.controller;

import com.github.bilibili.framework.JsonResponse;
import com.github.bilibili.framework.Results;
import com.github.bilibili.oss.constants.ApiConstants;
import com.github.bilibili.oss.events.VideoUploadedEvent;
import com.github.bilibili.oss.service.StorageServiceInterface;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstants.PREFIX)
@RequiredArgsConstructor
public class StorageController {

    private final StorageServiceInterface storageService;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 1. Simple Upload (Images/Avatars).
     * The file goes through the server. Limit this to 2MB in application.yml.
     */
    @PostMapping("/image")
    @SneakyThrows
    public JsonResponse<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Results.fail("File is empty");
        }

        try {
            String ext = getSafeExtension(file.getOriginalFilename());

            // Path: images/a1/b2/uuid.jpg
            String objectKey = generatePath("images", ext);

            String url = storageService.uploadDirect(
                    objectKey,
                    file.getInputStream(),
                    file.getContentType(),
                    file.getSize());

            return Results.success(new UploadResponse(url, objectKey));

        } catch (IOException e) {
            log.error("Upload failed", e);
            return Results.fail("Internal storage error");
        }
    }

    /**
     * 2. Get Upload Token (Videos).
     * The server does NOT touch the video file. It just signs a permission slip.
     * the function not need the ObjectKey d
     * the inner will generate it
     */
    @GetMapping("/video/token")
    @Deprecated
    public JsonResponse<?> getVideoToken(@RequestParam("extension") String extension) {
        if (!isValidVideoExtension(extension)) {
            return Results.fail("Invalid video format");
        }

        // Path: videos/raw/c4/d9/uuid.mov
        String objectKey = generatePath("videos/raw", extension);

        // QuickTime mapping for iOS compatibility, otherwise standard
        String contentType = "video/" + (extension.equals("mov") ? "quicktime" : extension);

        String uploadUrl = storageService.getPresignedUploadUrl(objectKey, contentType);

        return Results.success(new PresignedUrlResponse(uploadUrl, objectKey));
    }

    @GetMapping("/video/token")
    public JsonResponse<?> getVideoToken(String objectKey, String contentType) {
        if (!isValidVideoExtension(contentType)) {
            return Results.fail("Invalid video format");
        }

        String uploadUrl = storageService.getPresignedUploadUrl(objectKey, contentType);

        return Results.success(new PresignedUrlResponse(uploadUrl, objectKey));
    }

    /**
     * 3. Callback (The Trigger).
     * The Frontend calls this AFTER they finish uploading to MinIO.
     * This wakes up the Transcoder.
     */
    @PostMapping("/video/complete")
    public JsonResponse<String> completeVideoUpload(@RequestBody VideoCallbackRequest request) {
        log.info("Client claims upload finished for: {}", request.getObjectName());

        // Ideally: Check MinIO here to verify file exists (optional but recommended)

        // Fire the Event. The Controller's job is done.
        // The VideoTranscodeListener will pick this up.
        eventPublisher.publishEvent(new VideoUploadedEvent(request.getVideoId(), request.getObjectName()));

        return Results.success("Processing started");
    }

    private String generatePath(String root, String extension) {
        // 128 bit base64 is base on the 6 bit to make one letter
        String uuid = UUID.randomUUID().toString().replace("-", ""); // Clean hex

        // Take the first 4 random characters for directory sharding
        String shard1 = uuid.substring(0, 2);
        String shard2 = uuid.substring(2, 4);

        return String.format("%s/%s/%s/%s.%s", root, shard1, shard2, uuid, extension);
    }

    private String getSafeExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "bin"; // Default to binary if unknown
        }
        // Strip dots and lowercase to prevent "file.EXE" madness
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    // --- Helper Methods & DTOs ---

    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }

    private boolean isValidVideoExtension(String ext) {
        return "mp4".equalsIgnoreCase(ext) || "mov".equalsIgnoreCase(ext) || "avi".equalsIgnoreCase(ext);
    }

    @Data
    public static class UploadResponse {

        private final String url;
        private final String objectName;
    }

    @Data
    public static class PresignedUrlResponse {

        private final String uploadUrl;
        private final String objectName;
    }

    @Data
    public static class VideoCallbackRequest {

        private String videoId; // The ID in your database
        private String objectName; // The path in MinIO
    }
}
