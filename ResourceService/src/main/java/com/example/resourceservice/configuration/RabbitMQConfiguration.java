package com.example.resourceservice.configuration;

import lombok.Setter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Setter
@Configuration
public class RabbitMQConfiguration {
  @Value("${rabbitmq.host}")
  private String host;

  @Value("${rabbitmq.resource-queue-name}")
  private String queueName;

  @Value("${rabbitmq.username}")
  private String userName;

  @Value("${rabbitmq.password}")
  private String password;

  @Bean
  public Queue queue() {
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
    cachingConnectionFactory.setUsername(userName);
    cachingConnectionFactory.setPassword(password);

    Queue queue = new Queue(queueName, false);

    RabbitAdmin rabbitAdmin = new RabbitAdmin(cachingConnectionFactory);
    rabbitAdmin.declareQueue(queue);

    return queue;
  }
}
