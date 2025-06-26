package com.example.demo.controller;

import com.example.demo.model.JobRequest;
import com.example.demo.service.KafkaProducerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JobController {

  // This is takes in the job request api parameters and sends it to Kafka
  private final KafkaProducerService kafkaProducerService;

  public JobController(KafkaProducerService kafkaProducerService) {
    this.kafkaProducerService = kafkaProducerService;
  }

  @PostMapping("/submitJob")
  public String submitJob(@RequestBody @Valid JobRequest jobRequest) {
    // Process the job request
    if (
      jobRequest.getJobType().isEmpty() ||
      jobRequest.getKeyword().isEmpty() ||
      jobRequest.getDataSetPath().isEmpty()
    ) {
      throw new IllegalArgumentException(
        "Invalid job request: Missing required parameters."
      );
    }
    if (jobRequest.getTopN() < 1) {
      throw new IllegalArgumentException(
        "Invalid job request: topN must be a positive integer."
      );
    }

    kafkaProducerService.sendJobRequest(jobRequest); // Assuming KafkaProducerService is properly set up to send job requests

    return "Valid job to send to Kafka";
  }
}
