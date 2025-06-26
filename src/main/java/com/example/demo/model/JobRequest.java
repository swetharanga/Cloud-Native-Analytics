package com.example.demo.model;

/* Querry that the user submits */
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class JobRequest {

  public JobRequest() {
    // Default constructor
    super();
  }

  @NotBlank(message = "DataSet path cannot be null")
  private String dataSetPath;

  @NotBlank(message = "Job type cannot be null")
  private String jobType;

  @NotBlank(message = "Keyword cannot be null")
  private String keyword;

  private int topN;

  public JobRequest(
    String dataSetPath,
    String jobType,
    String keyword,
    int topN
  ) {
    this.dataSetPath = dataSetPath;
    this.jobType = jobType;
    this.keyword = keyword;
    this.topN = topN;
  }

  public String getDataSetPath() {
    return dataSetPath;
  }

  public String getJobType() {
    return jobType;
  }

  public String getKeyword() {
    return keyword;
  }

  public int getTopN() {
    return topN;
  }

  public void setDataSetPath(String dataSetPath) {
    this.dataSetPath = dataSetPath;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public void setTopN(int topN) {
    this.topN = topN;
  }

  @Override
  public String toString() {
    return (
      "JobRequest{" +
      "dataSetPath='" +
      dataSetPath +
      '\'' +
      ", jobType='" +
      jobType +
      '\'' +
      ", keyword='" +
      keyword +
      '\'' +
      ", topN=" +
      topN +
      '}'
    );
  }
}
