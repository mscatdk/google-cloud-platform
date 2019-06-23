# Google Big Query

## External table

````bash
# Create definition
bq mkdef --source_format=CSV \
    gs://campaign-funding/indiv*.txt \
"CMTE_ID, AMNDT_IND, RPT_TP, TRANSACTION_PGI, IMAGE_NUM, TRANSACTION_TP, ENTITY_TP, NAME, CITY, STATE, ZIP_CODE, EMPLOYER, OCCUPATION, TRANSACTION_DT, TRANSACTION_AMT:FLOAT, OTHER_ID, TRAN_ID, FILE_NUM, MEMO_CD, MEMO_TEXT, SUB_ID" \
> indiv_def.json

# Correct demimiter and quote symbok
sed -i 's/"fieldDelimiter": ","/"fieldDelimiter": "|"/g; s/"quote": "\\""/"quote":""/g' indiv_def.json

# Create table
bq mk --external_table_definition=indiv_def.json -t ${DATASET}.transactions 
````

## bq command

````bash
# Create dataset
bq mk [dataset name]

# Create table
bq mk \
--time_partitioning_field timestamp \
--schema name:string,age:integer,test:float,timestamp:timestamp -t [dataset name].[table name]
````

## UDF

````bash
CREATE TEMPORARY FUNCTION translate(data STRING)
  RETURNS STRING LANGUAGE js AS """
var names = data.toLowerCase().split(" ");

names = names.map(x => x.replace(/^\\w/, c => c.toUpperCase()));

return names.join(" ").split(",").reverse().join(" ").trim();
""";
SELECT CAND_NAME, translate(CAND_NAME) FROM `campaign_funding.candidates` LIMIT 1;
````

## Snapshots
````SQL
# Data from one hour ago
SELECT count(*) FROM `test.finance` FOR SYSTEM_TIME AS OF TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 HOUR);
````