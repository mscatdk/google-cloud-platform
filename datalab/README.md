# Google Datalab

## gsutil

````bash
# List zones
gcloud compute zones list

# Create Datalab instace
datalab create mydatalabvm --zone [zone]

# Do some work

# Stop the datalab instance
gcloud compute instances stop mydatalabvm

# reconnect
datalab connect mydatalabvm 
````