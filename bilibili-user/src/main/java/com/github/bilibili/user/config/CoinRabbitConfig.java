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


package com.github.bilibili.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoinRabbitConfig {

    // --- Constants (Public for Sender to use) ---
    public static final String EXCHANGE_INTERACTION = "interaction.exchange";
    public static final String ROUTING_KEY_COIN = "interaction.coin";

    // The Queue that the Stats Service will listen to
    // (Defining it here ensures it exists even if Stats Service is offline)
    public static final String QUEUE_STATS_COIN = "stats.coin.queue";

    /**
     * 1. Topic Exchange
     * Why Topic? Because later you will have 'interaction.like', 'interaction.fav'.
     * A Topic Exchange allows consumers to listen to 'interaction.*' or specific keys.
     */
    @Bean
    public TopicExchange interactionExchange() {
        return new TopicExchange(EXCHANGE_INTERACTION, true, false);
    }

    /**
     * 2. Queue (Durable)
     * This holds the "Add Coin" events until the Stats Service processes them.
     */
    @Bean
    public Queue statsCoinQueue() {
        return QueueBuilder.durable(QUEUE_STATS_COIN).build();
    }

    /**
     * 3. Binding
     * Connects the Queue to the Exchange specifically for COIN events.
     */
    @Bean
    public Binding bindingStatsCoin() {
        return BindingBuilder.bind(statsCoinQueue())
                .to(interactionExchange())
                .with(ROUTING_KEY_COIN);
    }

    /**
     * 4. JSON Serializer
     * CRITICAL: Without this, RabbitMQ sends unreadable Java Serialization bytes.
     * This ensures your messages are readable JSON: {"bizId":100, "amount":2...}
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 5. Template Configuration
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}