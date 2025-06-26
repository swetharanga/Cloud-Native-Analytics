import unittest
from pyspark.sql import SparkSession  # type: ignore
import os
from pyspark.sql import DataFrame
from pyspark.sql.functions import col, explode



class SparkUnitTests(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.spark = SparkSession.builder \
            .appName("Spark Unit Tests") \
            .master("local[*]") \
            .getOrCreate()

    @classmethod
    def tearDownClass(cls):
        cls.spark.stop()

    def process_job(self, df: DataFrame, keyword: str, job_type: str, top_N: int, output_path: str):

        if keyword is None  or keyword == "":
            raise ValueError("Null keyword")
        
        if top_N is None:
            raise(ValueError)
        
                
        if job_type is None or job_type == "":
            raise(ValueError)
        
        if top_N is not int :
            raise TypeError("Must be int")

        
        if keyword is not str or job_type is not str:
            rasie (TypeError)
        


        message = df.withColumn("message", explode("messages")) \
                    .select(col("message.content").alias("content"), "source")

        dataframe = message.filter(
            col("source").contains(keyword) & col("source").endswith(job_type)
        )

        dataframe = dataframe.limit(top_N).select("content")

        #output_path_uri = "file://" + output_path
        dataframe.write.mode("overwrite").parquet(output_path)

        return dataframe

    
    def setUp(self):
        self.spark = SparkSession.builder \
            .appName("Simple Test Case") \
            .master("local[*]") \
            .getOrCreate()
    
    def testNullkeyword(self):
       df = self.spark.createDataFrame([
            {"source": "open-r1/codeforces-cots", "messages": [{"content": "msg1"}]}
        ])

       with self.assertRaises(ValueError):
            self.process_job(df, None, "code-force", 2, "/Users/swetharanga/Desktop/output")
    
    def testNulljobType(self):
        df = self.spark.createDataFrame([
            {"source": "open-r1/codeforces-cots", "messages": [{"content": "msg1"}]}
        ])

        with self.assertRaises(ValueError):
            self.process_job(df, "open-r1", None, 2, "/Users/swetharanga/Desktop/output")

    def testNullTopN(self):
        df = self.spark.createDataFrame([
            {"source": "open-r1/codeforces-cots", "messages": [{"content": "msg1"}]}
        ])

        with self.assertRaises(ValueError):
            self.process_job(df, "open-r1", "code-force", None, "/Users/swetharanga/Desktop/output")
    
    
    def invalidTopN(self):
        df =  self.spark.createDataFrame([
    { "messages": [{"content": "msg"}], "num_tokens" : 12, "source": "api/search"}
])
        with self.assertRaises(TypeError):
            self.process_job(df, None, "code-force", "hi",  "/Users/swetharanga/Desktop/output")
    
    
    def invalidKeyword(self):
        df = self.spark.createDataFrame([
            {"source": "open-r1/codeforces-cots", "messages": [{"content": "msg1"}]}
        ])

        with self.assertRaises(TypeError):
            self.process_job(df, 3, "code-force", 2, "/Users/swetharanga/Desktop/output")

    def invalidJobType(self):
        df = self.spark.createDataFrame([
            {"source": "open-r1/codeforces-cots", "messages": [{"content": "msg1"}]}
        ])

        with self.assertRaises(TypeError):
            self.process_job(df, 3, "code-force", 2, "/Users/swetharanga/Desktop/output")


if __name__ == '__main__':
    unittest.main()
