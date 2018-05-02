# Edge Service
[![Build Status](https://travis-ci.org/AITestingOrg/edge-service.svg?branch=master)](https://travis-ci.org/AITestingOrg/edge-service)

The edge service is one of the backing services that serves as the front door for the API. The edge-service uses Zuul to route the different request to the corresponding mircroservice for it to handle.

# Features
* Reroutes request to the appropiate microservice based on the request url prefix.
    * /trip/cmd/ -> tripmanagementcmd
    * /trip/query/ -> tripmanagementquery

# Running the service:
* This service needs all the other microservices to be up and running.
* After making sure the project assembled properly run:  ```docker-compose up --build```

## Running with locally build images.
Build the images locally but tag them with version ```:local``` instead of default, for example instead of just building gmaps-adapter as aista/gmaps-adapter tag it aista/gmaps-adapter:local
Run ```docker-compose -f docker-compose-local.yml up```
remove the :local from the docker-compose-local.yml file for images that wont be changed locally.

# Performance Metrics API
Once edge service is UP, you can access the metrics at http://localhost:8080/actuator
