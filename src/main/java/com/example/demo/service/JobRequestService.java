package com.example.demo.service;

import com.example.demo.model.JobRequest;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

@Service
// This service interface is responsible for handling job requests

public class JobRequestService {

  public String createJobId(JobRequest jobRequest) {
    // Logic to create a unique job ID based on the job request

    UUID uuid = UUID.randomUUID();
    String uuidAsString = uuid.toString();
    storeMetadata(uuidAsString, jobRequest); // Store metadata for the job request

    return "jobId-" + uuidAsString;
  }

  public void storeMetadata(String uuid, JobRequest jobRequest) {
    // Logic to store metadata related to the job request

    HashMap<String, String> storeJobMetadata = new HashMap<>();
    storeJobMetadata.put(uuid, jobRequest.toString());
  }
}
