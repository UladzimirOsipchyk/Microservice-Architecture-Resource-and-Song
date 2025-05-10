package com.example.resourceprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMQConsumer {

  @Autowired
  private ResourceProcessorService resourceProcessorService;


  @RabbitListener(queues = {"ResourceQueue"})
  public void receiveAndProcessObjectKey(String message) throws Exception {
    Map<String, String> messageData = getMessageAsMap(message);
    switch (messageData.get("action")) {
      case "extract_meta":
        resourceProcessorService.executeMetaDataExtraction(messageData);
        break;

      case "remove_meta":
        resourceProcessorService.executeMedaDataDeletion(messageData);
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
