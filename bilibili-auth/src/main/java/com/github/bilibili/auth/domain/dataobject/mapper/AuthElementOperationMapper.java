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


package com.github.bilibili.auth.domain.dataobject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.bilibili.auth.domain.dataobject.auth.AuthElementOperation;
import org.apache.ibatis.annotations.Mapper;

/**
* @author laiyuejia
* @description 针对表【t_auth_element_operation(权限控制--页面元素操作表)】的数据库操作Mapper
* @createDate 2023-01-24 17:07:46
* @Entity cn.laiyuejia.bilibili.domain.auth.AuthElementOperation
*/
@Mapper
public interface AuthElementOperationMapper extends BaseMapper<AuthElementOperation> {

}
