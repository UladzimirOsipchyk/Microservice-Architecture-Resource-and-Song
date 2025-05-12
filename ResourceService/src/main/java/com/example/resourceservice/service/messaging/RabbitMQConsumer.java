package com.example.resourceservice.service.messaging;

import com.example.resourceservice.service.resource.ResourceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMQConsumer {

  @Autowired
  private ResourceService resourceService;


  @RabbitListener(queues = {"ResourceQueue"})
  public void receiveAndProcessObjectKey(String message) throws Exception {
    Map<String, String> messageData = getMessageAsMap(message);
    switch (messageData.get("action")) {
      case "complete_resource":
        resourceService.processCompleteResourceProcessing(messageData);
        break;

      default:
        break;
    }
  }

  private Map<String, String> getMessageAsMap(String message) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(message, Map.class);
  }
}
