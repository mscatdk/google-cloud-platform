# Identity and Access Management

## Service Account

````bash
# Create service account
gcloud iam service-accounts create [service account name]

# Generate key
gcloud iam service-accounts keys create key.json --iam-account [service account name]@[PROJECT ID].iam.gserviceaccount.com

# Activate service in e.g. cloud shell
gcloud auth activate-service-account --key-file key.json
````

## Token

````bash
# Generate token
gcloud auth print-access-token
````