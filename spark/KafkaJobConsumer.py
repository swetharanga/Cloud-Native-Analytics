# Gets the job from Kafka
# import the kafkaproducerservice file 


from pyspark.sql import SparkSession # type: ignore
from pyspark.sql.functions import *
from pyspark.sql.types import *
from kafka import KafkaConsumer
import jsson
import subprocess

