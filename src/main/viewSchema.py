import pyarrow.parquet as pq

table = pq.read_table("/Users/swetharanga/Downloads/datset.parquet") 
print(table.schema)

