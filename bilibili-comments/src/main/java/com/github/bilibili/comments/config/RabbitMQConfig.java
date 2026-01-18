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


package com.github.bilibili.comments.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Constants ---
    public static final String EXCHANGE_MOMENTS = "moments.exchange";
    public static final String QUEUE_DISTRIBUTION = "moments.distribution.queue";
    public static final String ROUTING_KEY_CREATED = "moment.created";

    // --- DLX Constants (For Reliability) ---
    public static final String EXCHANGE_DLX = "moments.dlx";
    public static final String QUEUE_DLQ = "moments.dead.letter.queue";
    public static final String ROUTING_KEY_DLX = "moment.dlx.routing";

    /**
     * 1. Main Business Exchange (Topic)
     * Allows routing based on keys like 'moment.created' or 'moment.deleted'
     */
    @Bean
    public TopicExchange momentsExchange() {
        return new TopicExchange(EXCHANGE_MOMENTS, true, false);
    }

    /**
     * 2. Main Distribution Queue
     * Configured with arguments to forward failed messages to the DLX.
     */
    @Bean
    public Queue distributionQueue() {
        return QueueBuilder.durable(QUEUE_DISTRIBUTION)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLX)
                // Optional: TTL (Time To Live) if needed, e.g., 24 hours
                .build();
    }

    /**
     * 3. Binding: Connect Main Queue to Main Exchange
     */
    @Bean
    public Binding bindingDistribution() {
        return BindingBuilder.bind(distributionQueue())
                .to(momentsExchange())
                .with(ROUTING_KEY_CREATED);
    }

    /**
     * RabbitAdmin - 负责声明队列、交换机和绑定
     */
    // @Bean
    // public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    // RabbitAdmin admin = new RabbitAdmin(connectionFactory);
    // admin.setAutoStartup(true); // 应用启动时自动声明
    // admin.setIgnoreDeclarationExceptions(true); // 忽略已存在的声明异常
    // return admin;
    // }

    // ==========================================
    // Dead Letter Strategy (The Safety Net)
    // ==========================================

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(QUEUE_DLQ);
    }

    @Bean
    public Binding bindingDLQ() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(ROUTING_KEY_DLX);
    }

    // ==========================================
    // Serialization (JSON)
    // ==========================================

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Essential: Converts your Java Objects to JSON payload automatically
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        // Enable Mandatory to ensure returns are handled if routing fails
        template.setMandatory(true);
        return template;
    }
}