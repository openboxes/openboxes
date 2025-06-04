## Docker-compose

### Overview

Docker Compose configurations are provided to simplify local development and deployment. Multiple configuration options are available to suit different needs.

### Quick Start

1. Navigate to the _docker/_ directory
2. Run `docker-compose up` for the default configuration
3. Access OpenBoxes at `localhost:8080/openboxes`

### Configuration Files

- `docker-compose-generic.yml` - Base configuration defining core OpenBoxes `app` and `nginx` services
- `docker-compose.yml` - Default configuration requiring environment variables in `.env`
- `docker-compose-containerdb.yml` - Includes MySQL container service
- `docker-compose-hostdb.yml` - Connects to host's MySQL instance

### Usage Notes

- Use `-f <config file>` to specify alternate configurations:
  ```bash
  docker-compose -f docker-compose-hostdb.yml up
  ```
- Environment variables are loaded from `.env` file
- Default fallback values are provided (e.g. `${DATASOURCE_USERNAME:-openboxes}`)

## Docker - build and run image from local Dockerfile

### Prerequisities:

- Docker 20.04+ is installed.
- OpenBoxes database (and db user) is already created.

### Instructions:

**Note**: Run the below commands in **main** project directory.

1. Prepare docker build with gradle. It runs grails build to produce executable openboxes.war file. This WAR file contains embedded Tomcat. It also copies war and configuration files to _build/docker_.
   `./gradlew prepareDocker -Dgrails.env=prod`
2. Build a docker image:
   `docker build --tag="openboxes/openboxes:latest" build/docker/`
3. Once image is built you can spin up a container of the image in a few ways:

#### Just run it

Specify environment variables for database connection (_DATASOURCE_URL_, _DATASOURCE_USERNAME_, _DATASOURCE_PASSWORD_) in `docker/.env` or provide these variables with command-line (`--env` arguments).

`docker run --env-file=docker/.env -p 8080:8080 --name=openboxes openboxes/openboxes:latest`

Openboxes app will be reachable at `localhost:8080/openboxes` once database initalizes. You can also change the host port mapping in the docker's `-p` argument.

#### Persistable logs (journald)

If you'd like the application logs to be persistable (after removing the container) run the docker container with _journald_ agent.

`docker run --env-file=docker/.env -p 8080:8080 --name=openboxes --log-driver=journald openboxes/openboxes:latest`

The logs can be accessed with:

`journalctl -u docker CONTAINER_NAME=openboxes`

#### Local run with connection to the host's DB

You can connect OpenBoxes app inside container to the host's MySQL server by using `host.docker.internal` special DNS name (works with Docker 20.04+). It has to be provided as an environment variable in `.env` or in command-line.
You have to set bind.address=0.0.0.0 in MySQL configuration, and create an `openboxes` database user.

Run with command line environment variables:
`docker run -p 8080:8080 --name=openboxes --env DATASOURCE_URL='jdbc:mysql://host.docker.internal:3306/openboxes?useSSL=false' --env DATASOURCE_USERNAME='openboxes' --env=DATASOURCE_PASSWORD='openboxes' --add-host host.docker.internal:host-gateway openboxes/openboxes:latest`

Run with .env file:
`docker run --env-file=docker/.env -p 8080:8080 --name=openboxes --add-host host.docker.internal:host-gateway openboxes/openboxes:latest`

### Development Setup

#### Method 1: Using openboxes-docker (Recommended)

1. Clone both repositories:
   ```bash
   git clone git@github.com:openboxes/openboxes.git
   git clone git@github.com:openboxes/openboxes-docker.git
   ```
2. Navigate to docker directory:
   ```bash
   cd openboxes-docker
   ```
3. Switch to a stable release tag (recommended):
   ```bash
   git checkout $(git describe --tags `git rev-list --tags --max-count=1`)
   ```
4. Start the application:
   ```bash
   docker-compose up --build
   ```

#### Common Build Issues

##### Grails Version Compatibility

The Docker setup needs to match the Grails version used in your OpenBoxes branch:

- **Main/Develop Branch**: Uses Grails 3.x

  ```yaml
  # docker-compose.yml
  services:
    grails:
      build:
        args:
          GRAILS_VERSION: 3.3.10
  ```

- **Legacy Branches**: Use Grails 1.3.9
  ```yaml
  # docker-compose.yml
  services:
    grails:
      build:
        args:
          GRAILS_VERSION: 1.3.9
  ```

To check your branch's Grails version:

1. Look in `application.properties` in the OpenBoxes repository
2. Match the Grails version in your Docker configuration accordingly
