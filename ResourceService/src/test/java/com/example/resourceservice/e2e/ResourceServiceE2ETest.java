package com.example.resourceservice.e2e;

import com.jayway.jsonpath.JsonPath;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.minio.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.profiles.active=test"
)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class ResourceServiceE2ETest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0")
      .withDatabaseName("testdb")
      .withUsername("user")
      .withPassword("password");

  @Container
  static GenericContainer<?> minio =
      new GenericContainer<>("minio/minio:latest")
          .withEnv("MINIO_ACCESS_KEY", "minioadmin")
          .withEnv("MINIO_SECRET_KEY", "minioadmin")
          .withCommand("server /data")
          .withExposedPorts(9000);


  @Container
  static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.13.1")
      .withExposedPorts(5672, 15672);
  
  @DynamicPropertySource
  static void overrideRabbitProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.discovery.enabled", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    registry.add("rabbitmq.host", rabbitMQ::getHost);
    registry.add("rabbitmq.port", () -> rabbitMQ.getAmqpPort());
    registry.add("rabbitmq.username", rabbitMQ::getAdminUsername);
    registry.add("rabbitmq.password", rabbitMQ::getAdminPassword);

    registry.add("cloud.aws.s3.endpoint", () -> "http://localhost:" + minio.getMappedPort(9000));
  }

  @Autowired
  private MockMvc mockMvc;

  private static MinioClient minioClient;
  @BeforeAll
  static void initContainers() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitMQ.getHost());
    factory.setPort(rabbitMQ.getMappedPort(5672));
    factory.setUsername("guest");
    factory.setPassword("guest");

    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
      channel.queueDeclare("ResourceQueue", true, false, false, null);
    }

    minioClient = MinioClient.builder()
        .endpoint("http://" + minio.getHost() + ":" + minio.getMappedPort(9000))
        .credentials("minioadmin", "minioadmin")
        .build();

    createBucketIfNotExists(minioClient, "staging-song");
    createBucketIfNotExists(minioClient, "permanent-song");
  }

  @Test
  void checkSuccessContainersRunning() {
    assertThat(postgres.isCreated()).isTrue();
    assertThat(postgres.isRunning()).isTrue();
    assertThat(minio.isRunning()).isTrue();
    assertThat(minio.isRunning()).isTrue();
    assertThat(rabbitMQ.isCreated()).isTrue();
    assertThat(rabbitMQ.isRunning()).isTrue();

  }

  @Test
  void testUploadAndSaveFileLocation() throws Exception {

    byte[] fileContent = getTestMp3Bytes();

    final Integer[] id = new Integer[1];

    mockMvc.perform(post("/resources")
        .content(fileContent)
        .contentType(MediaType.valueOf("audio/mpeg"))
        .header("Content-Type", MediaType.valueOf("audio/mpeg")))
        .andDo(result -> {
          String responseBody = result.getResponse().getContentAsString();
          id[0] = JsonPath.read(responseBody, "$.id");
        })

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id[0]))
        .andExpect(jsonPath("$.fileUrl").value(containsString("/staging-song/Some%20Interesting%20song")));
  }

  @Test
  void testGetResourceBytes() throws Exception {
    byte[] expectedContent = getTestMp3Bytes();

    mockMvc.perform(post("/resources")
            .content(expectedContent)
            .contentType("audio/mpeg"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andDo(result -> {

          String fileName = "Some Interesting song"; // или получи из id/response

          MinioClient client = MinioClient.builder()
              .endpoint("http://" + minio.getHost() + ":" + minio.getMappedPort(9000))
              .credentials("minioadmin", "minioadmin")
              .build();

          await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .bucket("permanent-song")
                    .object(fileName)
                    .source(
                        CopySource.builder()
                            .bucket("staging-song")
                            .object(fileName)
                            .build()
                    )
                    .build()
            );
          });

          String responseBody = result.getResponse().getContentAsString();
          Integer id = JsonPath.read(responseBody, "$.id");

          byte[] actualContent = mockMvc.perform(MockMvcRequestBuilders.get("/resources/" + id))
              .andExpect(status().isOk())
              .andExpect(content().contentType("audio/mpeg"))
              .andReturn()
              .getResponse()
              .getContentAsByteArray();

          assertThat(actualContent).isEqualTo(expectedContent);
        });
  }

  @Test
  void testDeleteResource() throws Exception {
    byte[] expectedContent = getTestMp3Bytes();
    List<Integer> createdIds = new ArrayList<>();

    mockMvc.perform(post("/resources")
            .content(expectedContent)
            .contentType("audio/mpeg"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andDo(result -> {
          String responseBody = result.getResponse().getContentAsString();
          createdIds.add(JsonPath.read(responseBody, "$.id"));
        });

    mockMvc.perform(post("/resources")
            .content(expectedContent)
            .contentType("audio/mpeg"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andDo(result -> {
          String responseBody = result.getResponse().getContentAsString();
          createdIds.add(JsonPath.read(responseBody, "$.id"));
        });

    String csvIds = createdIds.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(","));

    mockMvc.perform(delete("/resources")
            .param("ids", csvIds)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.ids[0]").value(createdIds.get(0)))
        .andExpect(jsonPath("$.ids[1]").value(createdIds.get(1)));
  }


  private byte[] getTestMp3Bytes() throws IOException {
    return new ClassPathResource("sample.mp3").getInputStream().readAllBytes();
  }

  private static void createBucketIfNotExists(MinioClient client, String bucket) throws Exception {
    boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    if (!exists) {
      client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }
}
