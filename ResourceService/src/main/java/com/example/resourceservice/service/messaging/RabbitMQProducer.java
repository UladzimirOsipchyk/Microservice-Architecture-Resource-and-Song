package com.example.resourceservice.service.messaging;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMQProducer {

  @Value("${rabbitmq.resource-queue-name}")
  private String queueName;

  @Autowired
  private AmqpTemplate amqpTemplate;

  public void processObjectKeyToQueue(String messageData) {
    amqpTemplate.convertAndSend(queueName, messageData);
  }

}
