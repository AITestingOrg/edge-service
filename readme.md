# Edge Service
The edge service is one of the backing services that serves as the front door for the API. The services utilizes Zuul.

# Running the service:

After making sure the project assembled properly run:  ```docker-compose up --build```

## Running with locally build images.

Build the images locally but tag them with version ```:local``` instead of default, for example instead of just building gmaps-adapter as aista/gmaps-adapter tag it aista/gmaps-adapter:local 

Run ```docker-compose -f docker-compose-local.yml up```

remove the :local from the docker-compose-local.yml file for images that wont be changed locally.
