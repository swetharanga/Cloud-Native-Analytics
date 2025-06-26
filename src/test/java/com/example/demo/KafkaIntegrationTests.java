package com.example.demo;

import com.example.demo.config.KafkaConfig;
import com.example.demo.model.JobRequest;
import com.example.demo.service.KafkaProducerService;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.shaded.com.google.protobuf.Value;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "topicName" })
public class KafkaIntegrationTests {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  private TestKafkaConsumer testKafkaConsumer;

  @Autowired
  private KafkaProducerService kafkaProducerService;

  @DynamicPropertySource
  static void overrideKafkaBootstrapServers(DynamicPropertyRegistry registry) {
    registry.add(
      "spring.kafka.bootstrap-servers",
      () -> System.getProperty("spring.embedded.kafka.brokers")
    );
  }

  @Test
  public void testKafkaSendandReceive() throws Exception {
    // Can your producer send valid messages to Kafka?
    // 	Is the broker available and accepting messages?
    // Can your app or test code read back the message

    JobRequest jobRequest = new JobRequest(
      "test/path/to/dataset.csv",
      "search",
      "keyword",
      5
    );

    //kafkaTemplate.send("topicName", jobRequest.toString());
    String json = String.format(
      "{\"dataSetPath\":\"%s\",\"jobType\":\"%s\",\"keyword\":\"%s\",\"topN\":%d}",
      jobRequest.getDataSetPath(),
      jobRequest.getJobType(),
      jobRequest.getKeyword(),
      jobRequest.getTopN()
    );
    kafkaTemplate.send("topicName", json);

    // Verify that the message was sent successfully

    Awaitility
      .await()
      .atMost(Duration.ofSeconds(10))
      .until(() -> testKafkaConsumer.getMessage().get() != null);

    Assertions.assertTrue(
      testKafkaConsumer.getMessage().get().contains("test/path/to/dataset.csv")
    );
    Assertions.assertTrue(
      testKafkaConsumer.getMessage().get().contains("search")
    );
    Assertions.assertTrue(
      testKafkaConsumer.getMessage().get().contains("keyword")
    );
    Assertions.assertTrue(testKafkaConsumer.getMessage().get().contains("5"));
    System.out.println(
      "Test passed: Message received in Kafka topic with correct content."
    );
  }

  @TestConfiguration
  static class TestKafkaConsumer {

    private final AtomicReference<String> message = new AtomicReference<>();

    @KafkaListener(topics = "topicName", groupId = "your-group-id")
    public void listen(String message) {
      System.out.println("Received job request: " + message);
      this.message.set(message);
    }

    public AtomicReference<String> getMessage() {
      return message;
    }
  }
}
