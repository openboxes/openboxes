# OpenBoxes Docker
An overview of how Docker is used within OpenBoxes.

## Docker compose files
We provide the following compose files for running the application under different setups. You can switch between them with the `-f` commandline arg. If you don't specify which file to use, `docker-compose.yml` will be used.

- `docker-compose-base.yml` - Not to be run directly. Contains common configuration that is shared between all compose files.
- `docker-compose.yml` - For running the app with a bundled, containerized database.
- `docker-compose-hostdb.yml` - For running the app against a non-containerized database that is running directly on the host machine
- `docker-compose-remotedb.yml` - For running the app against a database that is externally managed on a separate host

## Running OpenBoxes via Docker (compose)
The following outlines how to run a containerized instance of OpenBoxes.

These compose files support running against officially released OpenBoxes images and locally built ones. See the community guide for [instructions on creating your own Docker image of the application](https://openboxes.gitbook.io/contributor-guide/release-planning/creating-a-new-docker-image).

### Prerequisites
1. Install [Docker](https://docs.docker.com/) and [Docker Compose](https://docs.docker.com/compose/)
2. [An openboxes database with a valid db user](https://openboxes.gitbook.io/contributor-guide/software-development/onboarding/running-openboxes-locally#id-2.-configure-the-database-instance)

### Steps
1. Start Docker
2. Navigate to the `/docker` directory
3. Optionally create a `.env` file containing any environment variable overrides that you need. See `.env.example` for a reference. If you do not provide an override for a variable, the default will be used. For example, given a line like `{DATASOURCE_USERNAME:-openboxes}` in the compose file, if `DATASOURCE_USERNAME` is not defined in your .env file, or it is blank, a value of `openboxes` will be used.
4. Run `docker compose up` to start your containers. You can select the compose file to use by specifying the `-f <config file>` argument. For example: `docker compose -f docker-compose-hostdb.yml up`
