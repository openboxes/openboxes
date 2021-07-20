## Docker-compose

Notes:
-  run default (`docker-compose.yml`) docker-compose configuration with `docker-compose up` inside _docker/_ directory
-  run other configurations by specifying `-f <config file>` argument. E.g:
 `docker-compose  -f docker-compose-hostdb.yml up`
-  every docker-compose run command takes env variables from `.env` file and injects them into the config file.
-  in each docker-compose configuration there are default values which are used in case of not providing env variables. E.g. `${DATASOURCE_USERNAME:-openboxes}`

Docker-compose config structure:
-  `docker-compose-generic.yml` - generic configuration which every other docker-compose config extends. It defines generic OpenBoxes `app` and `nginx` services.
-  `docker-compose.yml` - default config, which expects all env variables to be set in `.env` file or via command line. E.g. it requires proper definition of `DATASOURCE_URL` variable.
-  `docker-compose-containerdb.yml` - it extends generic configuration with `db` service - MySQL docker container.
-  `docker-compose-hostdb.yml` - it extends generic configuration with database connection to host's MySQL instance (via `host.docker.internal`).

## Docker - build and run image from local Dockerfile

### Prerequisities:
-  Docker 20.04+ is installed.
-  OpenBoxes database (and db user) is already created.

### Instructions:

**Note**: Run the below commands in **main** project directory.


1. Prepare docker build with gradle. It runs grails build to produce executable openboxes.war file. This WAR file contains embedded Tomcat. It also copies war and configuration files to *build/docker*.
`./gradlew prepareDocker -Dgrails.env=prod`
2. Build a docker image:
`docker build --tag="openboxes/openboxes:latest" build/docker/`
3. Once image is built you can spin up a container of the image in a few ways:
####	Just run it
Specify environment variables for database connection (*DATASOURCE_URL*, *DATASOURCE_USERNAME*, *DATASOURCE_PASSWORD*) in `docker/.env` or provide these variables with command-line (`--env` arguments).

`docker run --env-file=docker/.env -p 8080:8080 --name=openboxes openboxes/openboxes:latest`

Openboxes app will be reachable at `localhost:8080/openboxes` once database initalizes. You can also change the host port mapping in the docker's `-p` argument.
#### Persistable logs (journald)
If you'd like the application logs to be persistable (after removing the container) run the docker container with *journald* agent.

  `docker run --env-file=docker/.env -p 8080:8080 --name=openboxes --log-driver=journald openboxes/openboxes:latest`

The logs can be accessed with:

`journalctl -u docker CONTAINER_NAME=openboxes`
####	Local run with connection to the host's DB
You can connect OpenBoxes app inside container to the host's MySQL server by using `host.docker.internal` special DNS name (works with Docker 20.04+). It has to be provided as an environment variable in `.env` or in command-line.
You have to set bind.address=0.0.0.0 in MySQL configuration, and create an `openboxes` database user.

Run with command line environment variables:
`docker run -p 8080:8080 --name=openboxes --env DATASOURCE_URL='jdbc:mysql://host.docker.internal:3306/openboxes?useSSL=false' --env DATASOURCE_USERNAME='openboxes' --env=DATASOURCE_PASSWORD='openboxes' --add-host host.docker.internal:host-gateway openboxes/openboxes:latest`

Run with .env file:
`docker run --env-file=docker/.env -p 8080:8080 --name=openboxes --add-host host.docker.internal:host-gateway openboxes/openboxes:latest`
