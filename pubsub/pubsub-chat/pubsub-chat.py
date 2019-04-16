"""
Very simple chat based on Google Cloud Platform pubsub

Credential:
1. Create service account
2. Set environment variable 'export GOOGLE_APPLICATION_CREDENTIALS=/vagrant/envs/service_account.json'

Install dependencies:
1. pip install --upgrade google-cloud-pubsub
"""
from google.cloud import pubsub
import uuid
import time
import atexit

PROJECT_ID="lucid-authority-228515"
TOPIC_NAME="pubsub-chat"
SUBSCRIPTION_ID="p" + str(uuid.uuid4())

username = input('Please input a username: ')

"""
    Retrieve messages from the topic
"""
subscriber = pubsub.SubscriberClient()
publisher = pubsub.PublisherClient()

try:
    topic_path = subscriber.topic_path(PROJECT_ID, TOPIC_NAME)
    subscription_path = subscriber.subscription_path(PROJECT_ID, SUBSCRIPTION_ID)

    subscription = subscriber.create_subscription(subscription_path, topic_path)

    def callback(message):
        print('->{}: {}'.format(message.attributes["username"], str(message.data)))
        message.ack()

    subscriber.subscribe(subscription_path, callback=callback)

    """
        Write messages to the topic
    """
    event_type = publisher.topic_path(PROJECT_ID, TOPIC_NAME)
    #publisher.create_topic(event_type)

    while True: 
        message = input('Enter your input: ')
        publisher.publish(event_type, message.encode(), username=username)

finally:
    subscriber.delete_subscription(subscription_path)