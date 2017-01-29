#!/bin/bash

curl -X POST -d "queueName=CAPCollatorQueue&topic=AllFeeds"  http://localhost:8080/subscription/newRabbitQueue
