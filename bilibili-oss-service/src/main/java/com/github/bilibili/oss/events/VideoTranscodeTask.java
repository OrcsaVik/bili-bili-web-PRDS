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


package com.github.bilibili.oss.events;

import com.github.bilibili.oss.config.StorageProperties;
import com.github.bilibili.oss.rpc.TranscodeResultDTO;
import com.github.bilibili.oss.rpc.VideoServiceClient;
import com.github.bilibili.oss.service.StorageServiceInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTranscodeTask {

    private final StorageServiceInterface storageEngine;
    private final StorageProperties props;
    private final StringRedisTemplate redisTemplate;
    private final VideoServiceClient videoClient; // HTTP Client, not DB Service

    public static final String SECRET = "my-super-secret-key";

    public void processVideo(String videoId, String rawObjectKey) {
        String lockKey = "lock:transcode:" + videoId;

        // 1. Atomic Lock. Fails fast if another worker is busy.
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", 30, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(acquired))
            return;

        File tempRaw = null;
        File tempDir = null;

        try {
            log.info("Task started: {}", videoId);
            // Notify Video Service: "I am working"
            // We use 'false' for success flag to indicate "In Progress" or a specific status endpoint
            notifyProgress(videoId, "PROCESSING", null, null);

            // 2. Prepare Workspace
            tempRaw = File.createTempFile("raw_" + videoId, ".tmp");
            tempDir = Files.createTempDirectory("hls_" + videoId).toFile();

            // 3. Download Raw File
            Files.copy(
                    storageEngine.getFileStream(props.getRawBucket(), rawObjectKey),
                    tempRaw.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // 4. Generate Thumbnail
            File thumbFile = new File(tempDir, "cover.jpg");
            runFfmpeg("-i", tempRaw.getAbsolutePath(), "-ss", "00:00:01", "-vframes", "1", thumbFile.getAbsolutePath());

            // 5. Transcode to HLS
            File m3u8File = new File(tempDir, "index.m3u8");
            runFfmpeg(
                    "-i", tempRaw.getAbsolutePath(),
                    "-c:v", "libx264", "-preset", "veryfast", "-g", "60",
                    "-c:a", "aac", "-b:a", "128k",
                    "-hls_time", "10", "-hls_list_size", "0", "-f", "hls",
                    m3u8File.getAbsolutePath());

            // 6. Upload Everything
            String basePath = rawObjectKey.replace("raw/", "hls/").replaceFirst("[.][^.]+$", "");

            // Upload Cover
            String coverKey = basePath + "/cover.jpg";
            storageEngine.uploadDirect(coverKey, Files.newInputStream(thumbFile.toPath()), "image/jpeg", thumbFile.length());

            // Upload HLS segments
            for (File f : tempDir.listFiles()) {
                if (f.isDirectory())
                    continue;
                String contentType = f.getName().endsWith(".m3u8") ? "application/x-mpegURL" : "video/MP2T";
                storageEngine.uploadDirect(basePath + "/" + f.getName(), Files.newInputStream(f.toPath()), contentType, f.length());
            }

            // 7. Success Callback
            String playlistUrl = basePath + "/index.m3u8";
            notifyProgress(videoId, "SUCCESS", playlistUrl, coverKey);

            log.info("Task success: {}", videoId);

        } catch (Exception e) {
            log.error("Task failed: {}", videoId, e);
            // 8. Failure Callback
            notifyProgress(videoId, "FAILED", null, null);
            redisTemplate.delete(lockKey); // Release lock strictly on error
        } finally {
            cleanup(tempRaw);
            cleanupDir(tempDir);
            // Do not release lock on success immediately; let it expire to prevent rapid re-triggers
        }
    }

    @AllArgsConstructor
    @Getter
    public enum TranscodeStatus {

        SCUESS("SUCCESS"),
        FAILDER("FAILED");

        private final String sign;

        private static TranscodeStatus fromCode(String code) {
            for (TranscodeStatus status : values()) {
                if (status.sign.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("No matching TranscodeStatus for code: " + code);
        }

        public boolean isSuccess() {
            return SCUESS.equals(this);
        }

        public boolean isFailed() {
            return FAILDER.equals(this);
        }
    }

    private void notifyProgress(String videoId, String status, String url, String cover) {
        try {
            // This is an HTTP call. It might fail.
            // In production, wrap this in a Retry mechanism.
            TranscodeStatus transcodeStatus = TranscodeStatus.fromCode(status);
            if (transcodeStatus.isSuccess()) {
                TranscodeResultDTO resultDTO = new TranscodeResultDTO().builder().videoId(videoId)
                        .success(true)
                        .coverUrl(url)
                        .coverUrl(cover).build();

                videoClient.sendCallback(SECRET, resultDTO);
            } else if (transcodeStatus.isFailed()) {
                TranscodeResultDTO resultDTO = new TranscodeResultDTO().builder().videoId(videoId)
                        .success(false)
                        .coverUrl(url)
                        .coverUrl(cover).build();

                videoClient.sendCallback(SECRET, resultDTO);
            }
        } catch (Exception e) {
            log.error("Failed to call back Video Service for {}", videoId, e);
        }
    }

    private void runFfmpeg(String... args) throws Exception {
        // Prepend the binary path
        String[] command = new String[args.length + 1];
        command[0] = props.getFfmpegPath();
        System.arraycopy(args, 0, command, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(command);

        // CRITICAL: Merge stdout/stderr and pipe to parent process.
        // If you do not do this, the OS buffer fills up and FFmpeg hangs (Deadlock).
        pb.inheritIO();

        Process p = pb.start();
        if (p.waitFor() != 0) {
            throw new RuntimeException("FFmpeg exited with error");
        }
    }

    private void cleanup(File f) {
        if (f != null)
            f.delete();
    }

    private void cleanupDir(File d) {
        if (d != null && d.exists()) {
            File[] files = d.listFiles();
            if (files != null)
                for (File f : files)
                    f.delete();
            d.delete();
        }
    }
}