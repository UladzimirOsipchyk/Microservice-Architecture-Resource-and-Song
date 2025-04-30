package com.example.resourceservice.component;

import com.example.resourceservice.model.Resource;
import com.example.resourceservice.repository.ResourceRepository;
import com.example.resourceservice.service.metadata.MetadataExtractorService;
import com.example.resourceservice.service.resource.ResourceService;
import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class ResourceServiceComponentTest {

  @Autowired
  private ResourceService resourceService;

  @Autowired
  private ResourceRepository resourceRepository;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @MockBean
  private MetadataExtractorService metadataExtractorService;

  @Container
  static GenericContainer<?> minio =
      new GenericContainer<>("minio/minio:latest")
          .withEnv("MINIO_ACCESS_KEY", "minioadmin")
          .withEnv("MINIO_SECRET_KEY", "minioadmin")
          .withCommand("server /data")
          .withExposedPorts(9000);

  @Container
  @ServiceConnection
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management")
    .withExposedPorts(5672, 15672);


  @BeforeAll
  static void initContainers() {
    System.setProperty("cloud.aws.s3.endpoint", "http://localhost:" + minio.getMappedPort(9000));
    System.setProperty("rabbitmq.host", "localhost");
    System.setProperty("rabbitmq.port", String.valueOf(rabbitmq.getHttpPort()));
  }

  @Test
  public void rabbitmq_shouldBeUp() {
    assertTrue(rabbitmq.isRunning());
    System.out.println(rabbitmq.getHttpUrl());
    System.out.println(rabbitmq.getHttpPort());
  }

  @Test
  @Order(1)
  void createResourceShouldStoreResourceAndSendMessage() throws Exception {
    byte[] mp3Data = getTestMp3Bytes();

    String contentType = "audio/mpeg";

    Metadata metadata = new Metadata();
    metadata.set("dc:title", "test-song.mp3");

    when(metadataExtractorService.extractMetadata(mp3Data)).thenReturn(metadata);

    Resource resource = resourceService.createResource(mp3Data, contentType);

    assertThat(resource.getId()).isNotNull();
    assertThat(resource.getFileName()).isEqualTo("test-song.mp3");

    Resource persisted = resourceRepository.findById(resource.getId()).orElseThrow();
    assertThat(persisted.getFileUrl()).contains("test-song");
  }

  @Test
  @Order(2)
  void getResourceBinaryByIdShouldReturnFileBytes() throws Exception {
    Resource resource = resourceRepository.findAll().get(0);

    byte[] actualBytes = resourceService.getResourceBinaryById(resource.getId());

    assertThat(actualBytes).isNotNull();
    assertThat(actualBytes.length).isGreaterThan(1000);
  }

  @Test
  @Order(3)
  void deleteResourceShouldRemoveFilesAndDatabaseEntries() throws Exception {
    Resource resource = resourceRepository.findAll().get(0);
    Long id = resource.getId();

    List<Long> deletedIds = resourceService.deleteResource(id.toString());

    assertThat(deletedIds).contains(id);
    assertThat(resourceRepository.findById(id)).isEmpty();
  }

  private byte[] getTestMp3Bytes() throws IOException {
    return new ClassPathResource("sample.mp3").getInputStream().readAllBytes();
  }
}