# Auto ML

## Prediction Request

````bash
# Set service account with role e.g. AutoML admin
export GOOGLE_APPLICATION_CREDENTIALS=`pwd`/service-account.json

# Generate Request
echo "{ \"payload\": { \"image\": { \"imageBytes\": \"`base64 -w 0 [file name]`\" } } }" > request.json

curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $(gcloud auth application-default print-access-token)"   https://automl.googleapis.com/v1beta1/projects/lucid-authority-228515/locations/us-central1/models/ICN5773436791249417699:predict -d @request.json
````