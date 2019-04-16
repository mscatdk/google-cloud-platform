# Google Cloud Storage

## gsutil

````bash
export BUCKET=[bucket name]

# Create bucket
gsutil mb -c regional -l europe-north1 gs://${BUCKET}

# Copy data from one bucket to another
gsutil -m cp -r gs://automl-codelab-clouds/* gs://${BUCKET}
````