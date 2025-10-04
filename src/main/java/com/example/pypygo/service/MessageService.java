package com.example.pypygo.service;

import com.example.pypygo.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author: tcsow
 * @date: 2025/10/4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final RabbitTemplate rabbitTemplate;

    // 發送訊息
    public void sendMessage(Object message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
        log.info("Message sent: {}", message);
    }

    // 接收訊息
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(Object message) {
        log.info("Message received: {}", message);
        // 處理訊息邏輯
    }
}