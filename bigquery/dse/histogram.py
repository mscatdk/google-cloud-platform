"""
Load data
export PROJECT_ID=
bq mk --dataset ${PROJECT_ID}:DSE_challenge
bq load --autodetect --source_format=CSV "${PROJECT_ID}:DSE_challenge.personal_info" ./personal_information.csv

Install dependencies
- pip install --upgrade google-cloud-bigquery[pandas]
- pip install --upgrade matplotlib
"""
from google.cloud import bigquery
import matplotlib.pyplot as plt

client = bigquery.Client()

df = client.query("SELECT days_old FROM `DSE_challenge.personal_info`").to_dataframe()

hist = df.hist()

plt.show()