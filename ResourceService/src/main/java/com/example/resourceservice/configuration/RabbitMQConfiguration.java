package com.example.resourceservice.configuration;

import lombok.Setter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory factory = new CachingConnectionFactory(host);
    factory.setUsername(userName);
    factory.setPassword(password);
    return factory;
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public Queue queue() {
    return new Queue(queueName, false);
  }
}
