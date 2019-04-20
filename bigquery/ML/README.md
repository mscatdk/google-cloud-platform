# Google Big Query ML

## Linear Regression Example on generated data

Load the CSV files training.csv, verification.csv, and test.csv into the dataset ML. Train the model using:

````sql
CREATE MODEL `ML.demo_model`
OPTIONS(model_type='linear_reg') AS
SELECT
  y AS label,
  x1 AS foot,
  x2 AS hand
FROM
  `ML.training`
````

Evaluate the model using the following SQL:

````sql
SELECT
  *
FROM
  ML.EVALUATE(MODEL `ML.demo_model`, (
SELECT
  y AS label,
  x1 AS foot,
  x2 AS hand
FROM
  `ML.verification`
 ));
 ````

Predictions can be made using:

````sql
SELECT
  *
FROM
  ML.PREDICT(MODEL `ML.demo_model`, (
SELECT
  x1 AS foot,
  x2 AS hand
FROM
  `ML.test`
  ));
````