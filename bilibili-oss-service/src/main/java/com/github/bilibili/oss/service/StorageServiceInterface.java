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

import java.io.InputStream;

public interface StorageServiceInterface {

    /**
     * For small files (Images, Avatars).
     * Server uploads directly.
     */
    String uploadDirect(String objectName, InputStream stream, String contentType, long size);

    /**
     * For large files (Videos).
     * Returns a URL that the Frontend uses to PUT the file.
     * Valid for 1 hour.
     */
    String getPresignedUploadUrl(String objectName, String contentType);

    /**
     * Get the public view URL.
     */
    String getPublicUrl(String objectName);

    /**
     * Download file stream (used by Transcoder).
     */
    InputStream getFileStream(String bucket, String objectName);

    /**
     * Upload processed file (used by Transcoder).
     */
    void uploadProcessed(String objectName, String filePath, String contentType);
}