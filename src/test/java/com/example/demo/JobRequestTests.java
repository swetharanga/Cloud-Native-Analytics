package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.model.JobRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

// J unit test library

@SpringBootTest
class JobRequestTests {

  @ParameterizedTest
  @ValueSource(strings = { "s3://mybucket/mydataset.csv" })
  void validDataSetPath(String dataSetPath) {
    // Test for valid dataset path  s3://<bucket-name>/<key>
    JobRequest jobRequest = new JobRequest();

    jobRequest.setDataSetPath(dataSetPath);

    assertNotNull(jobRequest.getDataSetPath());
    assertTrue(dataSetPath.matches("s3://[^/]+/[^/]+"));
    assertTrue(dataSetPath.startsWith("s3://"));
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "",
      "s3://",
      "s3://mybucket/",
      "s3://mybucket/mydataset.csv/extra",
      "s3://example-bucket/data/file.txt",
    }
  )
  void invalidDataSetPath(String dataSetPath) {
    if (
      !dataSetPath.matches("s3://[^/]+/[^/]+") ||
      !dataSetPath.startsWith(("s3://"))
    ) {
      throw new IllegalArgumentException(
        "Invalid dataset path: " + dataSetPath
      );
    }
    Assertions.fail(
      "Expected invalid path to throw exception, but it didn't: " + dataSetPath
    );
  }

  @ParameterizedTest
  @NullSource
  void nullDataSetPath(String dataSetPath) {
    if (dataSetPath == null) {
      throw new NullPointerException("Dataset path cannot be null");
    } else {
      Assertions.fail("Expected NullPointerException for null dataset path");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = { "top_subreddits", "top_likes", "DATA_CLEANING" })
  void validJobType(String jobType) {
    // Test for null or empty job type
    // Test for non alphabets in job type

    assertNotNull(jobType);
    assertTrue(jobType.matches("[a-zA-Z_]+"));
  }

  @ParameterizedTest
  @ValueSource(
    strings = { "top_subreddits1", "top_subreddits1", "DATA_CLEANING23" }
  )
  void invalidJobType(String jobType) {
    // Test for null or empty job type
    // Test for non alphabets in job type

    assertFalse(jobType.matches("^[a-zA-Z_]+$"));
  }

  @ParameterizedTest
  @NullSource
  void nullJobType(String jobType) {
    if (jobType == null) {
      throw new NullPointerException("Job type cannot be null");
    } else {
      Assertions.fail("Expected NullPointerException for null job type");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = { "openai", "machinelearning", "data_science" })
  void Validkeyword(String keyword) {
    // Test for null or empty keyword
    // Test for non-alphanumeric characters in keyword

    assertNotNull(keyword);
    assertTrue((keyword.matches("[a-zA-Z_]+")));
  }

  @ParameterizedTest
  @ValueSource(strings = { "", "openai!", "machine learning", "ihop-32@" })
  void Invalidkeyword(String keyword) {
    // Test for null or empty keyword
    // Test for non-alphanumeric characters in keyword

    assertFalse((keyword.matches("^[a-zA-Z_]+$")));
  }

  @ParameterizedTest
  @NullSource
  void nullKeyword(String keyword) {
    if (keyword == null) {
      throw new NullPointerException("Keyword cannot be null");
    } else {
      Assertions.fail("Expected NullPointerException for null keyword");
    }
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 5, 10, 50, 100 })
  void validTopN(int topN) {
    // Test for null or empty topN
    // Test for valid topN (positive integer)
    // Test for invalid topN (zero or negative)

    assertNotNull(topN);
    assertTrue(topN > 0);
    assertTrue(topN <= 10000);
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, -1, -5, -10, -50, -100 })
  void invalidTopN(int topN) {
    // Test for null or empty topN

    // Test for valid topN (positive integer)

    // Test for invalid topN (zero or negative)

    assertFalse((topN > 0 && topN <= 100));
  }

  @ParameterizedTest
  @NullSource
  void nullTopN(Integer topN) {
    if (topN == null) {
      throw new NullPointerException("TopN cannot be null");
    } else {
      Assertions.fail("Expected NullPointerException for null topN");
    }
  }
}
