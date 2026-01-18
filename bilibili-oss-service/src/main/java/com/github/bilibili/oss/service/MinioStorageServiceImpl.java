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


package com.github.bilibili.oss.service;

import com.github.bilibili.oss.config.StorageProperties;
import com.github.bilibili.oss.exception.StorageException;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements StorageServiceInterface {

    private final MinioClient minioClient;
    private final StorageProperties props;

    @Override
    public String uploadDirect(String objectName, InputStream stream, String contentType, long size) {

        try {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getPublicBucket())
                            .object(objectName)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build());
            return getPublicUrl(objectName);
        } catch (Exception e) {
            throw new StorageException("Failed to upload direct file: \n" + objectName + e.getMessage());
        }
    }

    @Override
    public String getPresignedUploadUrl(String objectName, String contentType) {
        try {
            // Generates a PUT URL valid for 1 hour
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(props.getRawBucket())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL" + e.getMessage());
        }
    }

    @Override
    public String getPublicUrl(String objectName) {
        // Assuming public bucket policy is set to read-only for world
        return props.getEndpoint() + "/" + props.getPublicBucket() + "/" + objectName;
    }

    @Override
    public InputStream getFileStream(String bucket, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new StorageException("Failed to download stream" + e.getMessage());
        }
    }

    @Override
    public void uploadProcessed(String objectName, String localFilePath, String contentType) {
        try (InputStream is = new FileInputStream(localFilePath)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getPublicBucket())
                            .object(objectName)
                            .stream(is, -1, 10485760) // 10MB part size
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new StorageException("Failed to upload processed file" + e.getMessage());
        }
    }
}