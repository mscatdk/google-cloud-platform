REGISTER /usr/lib/pig/piggybank.jar;

DATA = 
   LOAD 'gs://lucid-authority-228515-dataproc/data.csv'
   using org.apache.pig.piggybank.storage.CSVExcelStorage(',', 'NO_MULTILINE', 'NOCHANGE', 'SKIP_INPUT_HEADER')
   AS (product:chararray,cost:int,price:int);

result = FOREACH DATA GENERATE *, price - cost AS earning:int;

store result into 'gs://lucid-authority-228515-dataproc/pigoutput/res' using PigStorage(',','-schema');





