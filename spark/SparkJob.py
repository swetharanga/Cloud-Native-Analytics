import os
from pyspark.sql import SparkSession  # type: ignore
from pyspark.sql.functions import col, explode  # type: ignore
import sys
import boto3 
import pandas as pd
import pyarrow.parquet as pq 

from urllib.parse import urlparse

print("✅ Python script started")

# Initialize Spark session

def ProcessLogic(dataset_path, keyword, job_type, top_N, aws_access_key, aws_secret_key, region ):

    output_path = "/Users/swetharanga/Desktop/output"  # <--- add this line inside the function

    output_path_uri = "file://" + output_path
    
    spark = SparkSession.builder.appName("ReadS3Boto3").getOrCreate()

    spark.conf.set("spark.sql.parquet.enableVectorizedReader", "false")
    spark.conf.set("spark.sql.parquet.columnarReaderBatchSize", "1024")

    # Initialize boto3 S3 client
    s3 = boto3.client(
        's3',
        aws_access_key_id=aws_access_key,
        aws_secret_access_key=aws_secret_key,
        region_name=region
    )


    # Parse S3 URL to get bucket and key



    parsed = urlparse(dataset_path)
    if parsed.scheme not in ["s3", "s3a"]:
        raise ValueError("dataset_path must be an s3 or s3a URL")
    bucket = parsed.netloc
    key = parsed.path.lstrip("/")

    print(f"Downloading S3 object from bucket '{bucket}', key '{key}'...")

    temp_file = "/Users/swetharanga/Desktop/temp.parquet"

    with open(temp_file, "wb") as f:
        print(f"✅ Downloading to {temp_file}")
        s3.download_fileobj(bucket, key, f)
        f.flush()


    print(f"📊 Loading into Spark: {temp_file}")
    df = spark.read.parquet(temp_file)
    df.show(10)

    table = pq.read_table(temp_file)
    print(table.schema)
    print(table.to_pandas().head())

# Processing logic for the spark job 


    message = df.withColumn("message", explode("messages")) \
                .select(col("message.content").alias("content"), "source")

    dataframe = message.filter(col("source").contains(keyword) & col("source").endswith(job_type))

    dataframe = dataframe.limit(top_N).select("content")

    output_path_uri = "file://" + output_path

    dataframe.write.mode("overwrite").parquet(output_path_uri)

# Store metadata in DynamoDb

    dynamodb = boto3.resource(
        'dynamodb',
        aws_access_key_id=aws_access_key,
        aws_secret_access_key=aws_secret_key,
        region_name=region
    )
    table = dynamodb.Table('SparkJobMetadata')

    # Create a table if not exists
    existing_tables = [table.name for table in dynamodb.tables.all()]
    if 'SparkJobMetadata' not in existing_tables:

        dynamodb.create_table(
            KeySchema=[
                {'AttributeName': 'jobId', 'KeyType': 'HASH'},  # Partition key
            ],
            AttributeDefinitions=[
                {'AttributeName': 'jobId', 'AttributeType': 'S'},
            ],
            BillingMode='PAY_PER_REQUEST'
        )

    # STORE METADATA
    table.put_item(
    Item={
            'jobId': key.split('/')[-1].split('.')[0],
            'Keyword': keyword,
            'JobType': job_type,
            'TopN': top_N,
            'timestamp': str(pd.Timestamp.now()),
            'OutputPath': output_path_uri
        }
    )

    print("✅ Done.")

    os.remove(temp_file)
    spark.stop()

if  __name__ == "__main__":

    # AWS Credentials
    aws_access_key = ""
    aws_secret_key = ""
    region = "us-west-2"

    # Output path (local path)
    output_path = "/Users/swetharanga/Desktop/output"

    dataset_path = sys.argv[1]
    keyword = sys.argv[2]
    job_type = sys.argv[3]
    top_N = int(sys.argv[4])

    ProcessLogic(dataset_path, keyword, job_type, top_N, aws_access_key, aws_secret_key, region)



