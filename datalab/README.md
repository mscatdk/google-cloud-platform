# Google Datalab

## gsutil

````bash
# List zones
gcloud compute zones list

# Create Datalab instace
datalab create [instance name] --zone [zone]

# Do some work

# Stop the datalab instance
datalab stop [instance name]

# reconnect
datalab connect [instance name]
````