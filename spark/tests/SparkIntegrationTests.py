# test end - read from s3 + writes metadata to dynamodn + writes output to local file
#@ mock s3 file
# input file

from pathlib import Path
from pyspark.sql import SparkSession  # type: ignore
import os

import boto3

import pytest
import pyarrow as pa
import pyarrow.parquet as pq
import pandas as pd
from moto import mock_aws
from unittest.mock import patch
from spark.SparkJob import ProcessLogic
import pytest

@pytest.fixture
def sample_parquet(tmp_path):
    # Create a sample Parquet file with the expected schema
    df = pd.DataFrame({
        "messages": [[{"content": "foo"}, {"content": "bar"}]],
        "source": ["test_source"]
    })
    table = pa.Table.from_pandas(df)
    file_path = tmp_path / "input.parquet"
    pq.write_table(table, file_path)
    return str(file_path)

# read from a valid s3 path and is returning what's expected
@mock_aws
def test_process_logic(sample_parquet, tmp_path): 
    s3_resource = boto3.resource('s3', region_name='us-west-2')
    s3_client = boto3.client('s3', region_name='us-west-2')

    bucket = "reddit-test-bucket"
    key = "test/input.parquet"

    s3_resource.create_bucket(Bucket=bucket,
                                CreateBucketConfiguration={'LocationConstraint': 'us-west-2'})
    with open(sample_parquet, "rb") as f:
        s3_client.upload_fileobj(f, bucket, key)

    # Set up mock dynamo db

    dynamodb = boto3.resource("dynamodb", region_name="us-west-2")
    dynamodb.create_table(
        TableName="SparkJobMetadata",
        KeySchema=[{"AttributeName": "jobId", "KeyType": "HASH"}],
        AttributeDefinitions=[{"AttributeName": "jobId", "AttributeType": "S"}],
        BillingMode="PAY_PER_REQUEST"
    )

    dataset_path = f"s3://{bucket}/{key}"
    keyword = "test"
    job_type = "source"
    top_N = 1
    aws_access_key = "test"
    aws_secret_key = "test"
    region = "us-west-2"

    
    # Patch output_path in your module if needed, or pass as argument
   
    ProcessLogic(dataset_path, keyword, job_type, top_N, aws_access_key, aws_secret_key, region)

    # Assert output file exists

    output_path = "/Users/swetharanga/Desktop/output"
    output_files = list(Path(output_path).glob("*.parquet"))
    assert output_files, "No output parquet file created"

    # Optionally, read and check output content
    out_table = pq.read_table(output_files[0])
    out_df = out_table.to_pandas()
    assert not out_df.empty

""""
# read from invalid s3 path 
def invalidS3path():


# read and is obeying the topN 
def meetTopN():

# no match filter edge case
def noMatchrow():
"""




