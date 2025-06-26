/* Need to publish the job request to the Kafka topic */

package com.example.demo.service;

import com.example.demo.config.KafkaConfig;
import com.example.demo.model.JobRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import org.apache.hadoop.shaded.org.jline.utils.InputStreamReader;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class KafkaProducerService {

  // Create a Kafka producer

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendJobRequest(JobRequest jobRequest) { // Validate the job request
    // Send the job request to Kafka topic

    //kafkaTemplate.send("topicName", jobRequest.toString());

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String json = objectMapper.writeValueAsString(jobRequest);
      kafkaTemplate.send("topicName", json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @KafkaListener(
    topics = "topicName",
    groupId = "your-group-id",
    containerFactory = "kafkaListenerContainerFactory"
  )
  public void listen(String jobRequest)
    throws IOException, InterruptedException {
    System.out.println("Received job request: " + jobRequest);

    // Need to connect to the pyspark job server to submit the job request

    ObjectMapper objectMapper = new ObjectMapper();
    JobRequest job = objectMapper.readValue(jobRequest, JobRequest.class);

    ProcessBuilder processBuilder = new ProcessBuilder(
      "python3",
      "/Users/swetharanga/Desktop/Cloud-Native/spark/SparkJob.py",
      job.getDataSetPath(),
      job.getKeyword(),
      job.getJobType(),
      String.valueOf(job.getTopN())
    );
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    // Read the output from the process
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(process.getInputStream())
    );
    String line;
    while ((line = reader.readLine()) != null) {
      System.out.println("[Python output] " + line);
    }

    int exitCode = process.waitFor();
    System.out.println("Python process exited with code: " + exitCode);
  }
}
