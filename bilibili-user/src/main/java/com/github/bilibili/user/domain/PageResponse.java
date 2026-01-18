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


package com.github.bilibili.user.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The Data List
     * Named 'records' to match your snippet: response.getRecords()
     */
    private List<T> records;

    /**
     * Total Item Count
     */
    private long total;

    /**
     * Page Size
     */
    private long size;

    /**
     * Current Page Number
     */
    private long current;

    /**
     * Total Pages
     */
    private long pages;

    /**
     * Factory Method: Convert MyBatis Plus IPage to PageResponse
     * This supports your code: PageResponse.of(dbResult)
     */
    public static <T> PageResponse<T> of(IPage<T> page) {
        return PageResponse.<T>builder()
                .records(page.getRecords())
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .pages(page.getPages())
                .build();
    }

    public static <R, T> PageResponse<R> of(IPage<T> page, List<R> convertList) {
        return PageResponse.<R>builder()
                .records(convertList)
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .pages(page.getPages())
                .build();
    }

    /**
     * Factory Method: Create an Empty Page (Safe Fallback)
     */
    public static <T> PageResponse<T> empty(long current, long size) {
        return PageResponse.<T>builder()
                .records(Collections.emptyList())
                .total(0)
                .size(size)
                .current(current)
                .pages(0)
                .build();
    }

    /**
     * Helper: Manually set records (useful for hydration steps)
     */
    public void setRecords(List<T> records) {
        this.records = records;
    }
}