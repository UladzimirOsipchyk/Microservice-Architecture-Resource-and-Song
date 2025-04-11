package com.example.resourceservice.contract.cdc;

import com.example.resourceservice.ResourceServiceApplication;
import com.example.resourceservice.controller.ResourceController;
import com.example.resourceservice.dto.DeletedResourceDTO;
import com.example.resourceservice.dto.ResourceDTO;
import com.example.resourceservice.model.Resource;
import com.example.resourceservice.repository.ResourceRepository;
import com.example.resourceservice.service.RabbitMQProducer;
import com.example.resourceservice.service.ResourceService;
import com.example.resourceservice.service.S3StorageService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.utils.test.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = ResourceServiceApplication.class)
@Testcontainers
@AutoConfigureMessageVerifier
public abstract class CdcBaseClass {

  @Autowired
  private RabbitMQProducer rabbitMQProducer;

  private ResourceController resourceController;

  @Container
  @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

  @BeforeEach
  public void setup() {
    resourceController = new ResourceController() {

      @Override
      public ResponseEntity<ResourceDTO> uploadResource(byte[] fileData, String contentType) throws Exception {
        String message = "{id=1, key=sample.mp3, action=extract_meta}";

        rabbitMQProducer.processObjectKeyToQueue(message);

        Resource resource = prepareResourceForTest();
        return new ResponseEntity<>(new ResourceDTO(resource.getId(), resource.getFileUrl()), HttpStatus.OK);
      }

      @Override
      public ResponseEntity<DeletedResourceDTO> deleteResource(String ids) throws Exception {
        String message = "{id=1,2,3, key=null, action=remove_meta}";

        rabbitMQProducer.processObjectKeyToQueue(message);

        return new ResponseEntity<>(new DeletedResourceDTO(List.of(1L, 2L, 3L)), HttpStatus.OK);
      }
    };
  }

  @Test
  public void triggerResourceCreatedEvent() throws Exception {
    resourceController.uploadResource(getTestMp3Bytes(), "audio/mpeg");
  }

  @Test
  public void triggerResourceDeletedEvent() throws Exception {
    resourceController.deleteResource("1,2,3");
  }


  private byte[] getTestMp3Bytes() throws IOException {
    return new ClassPathResource("sample.mp3").getInputStream().readAllBytes();
  }

  private Resource prepareResourceForTest() {
    Resource resource = new Resource();
    resource.setId(1L);
    resource.setFileUrl("https//some.file.url");
    resource.setFileName("Some Interesting song");
    return resource;
  }
}

@Configuration
class TestConfig {

  @Bean
  RabbitMessageVerifier rabbitTemplateMessageVerifier() {
    return new RabbitMessageVerifier();
  }

  @Bean
  ContractVerifierMessaging<Message> rabbitContractVerifierMessaging(RabbitMessageVerifier messageVerifier) {
    return new ContractVerifierMessaging<>(new NoOpStubMessages<>(), messageVerifier) {

      @Override
      protected ContractVerifierMessage convert(Message message) {
        if (message == null) {
          return null;
        }
        return new ContractVerifierMessage(message.getPayload(), message.getHeaders());
      }

    };
  }
}

class RabbitMessageVerifier implements MessageVerifierReceiver<Message> {


  private final LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>();

  @Override
  public Message receive(String destination, long timeout, TimeUnit timeUnit, @Nullable YamlContract contract) {
    try {
      return queue.poll(timeout, timeUnit);
    }
    catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @RabbitListener(id = "foo", queues = "ResourceQueue")
  public void listen(Message message) {
    queue.add(message);
  }

  @Override
  public Message receive(String destination, YamlContract contract) {
    return receive(destination, 1, TimeUnit.SECONDS, contract);
  }

}


