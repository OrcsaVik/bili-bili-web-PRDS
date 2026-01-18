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


package com.github.bilibili.auth.domain.dataobject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailHelper {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:OrcasVik}")
    private String sender;
    /**
     * Sends an HTML verification code email asynchronously.
     *
     * @param to   The recipient email address.
     * @param code The 6-digit verification code.
     */
    @Async("SmsTaskExecutor") // Use the ThreadPool we defined earlier
    public void sendVerifyCode(String to, String code) {
        log.info("Preparing to send verification code to: {}", to);

        try {
            // 1. Prepare the Context (Variables for the HTML)
            Context context = new Context();
            context.setVariable("code", code);

            // 2. Process the Template
            // Looks for src/main/resources/templates/email/verify-code.html
            String htmlContent = templateEngine.process("verify-code", context);

            // 3. Create the MIME Message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject("[Moments] Your Verification Code: " + code);
            helper.setText(htmlContent, true); // true = isHtml

            // 4. Send
            mailSender.send(message);
            log.info("Verification email sent successfully to {}", to);

        } catch (MessagingException e) {
            // In production, you might want to retry or alert admin
            log.error("Failed to send email to {}", to, e);
        }
    }
}