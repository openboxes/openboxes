# Running Integration Tests

Integration tests differ from unit tests in that they run against a full application context and database. We have
multiple types of tests (API tests, domain slice tests, smoke tests) that (by our definition) all fit under the 
integration tests umbrella.

## Setup Instructions
1. [Install Docker](https://docs.docker.com/get-docker/). Our integration tests utilize [Testcontainers](https://testcontainers.com/)
to ensure that they run against a clean database, which requires Docker to be installed.
2. Make sure docker is running on your machine (you can verify by running any Docker command such as `docker ps`).
3. Make sure to enable the docker.sock file so that you can run tests via IDE
    * In Docker Desktop: Settings > Advanced > Enable default Docker socket
4. Make sure the active user has permissions to call into the docker daemon via the socket.
    * On Linux: `chmod 777 /var/run/docker.sock`

## Running Tests

> [!WARNING]  
> If you have a ~/.grails/openboxes-config.properties or ~/.grails/openboxes.yml file, make sure you remove or comment
> out any lines relating to dataSource before running tests to avoid running them against a non-testcontainer database.

To run all integration tests, use the following command: `grails test-app -integration`

To run only a single test, use: `grails test-app AuthApiSpec -integration`

To run all tests of a certain type/phase, use: `grails test-app org.pih.warehouse.api.** -integration`

If you want to run tests against a specific database, use: `TEST_DATABASE=mysql:8.0.36 grails test-app -integration`

## Debugging Test Containers

Testcontainers runs the database container on a random port so that we don't run into port conflicts when running tests
on CI servers. However, this makes it difficult when testing locally, where you may want to connect to the database for
debugging purposes.

Testcontainers Desktop provides the ability to run the testcontainers on a fixed port, and to keep them alive after the
tests complete.

1. [Install Testcontainers Desktop](https://testcontainers.com/desktop/)
2. [Follow the instructions on the testcontainers site](https://testcontainers.com/guides/simple-local-development-with-testcontainers-desktop/)
to enable a toml file that sets up a fixed port for the database server that you want to test against.

   For example, if you want a MySQL database running on port 4444, add the following to a `mysql.toml` file:
    ```
    ports = [
      {local-port = 4444, container-port = 3306},
    ]
   
    selector.image-names = ["mysql"]
    ```

3. Run the test(s) and keep the test container alive via one of the following methods:
    * Add a breakpoint somewhere in the test
    * Use testcontainers desktop to "freeze container shutdown"
4. Connect to the database as you normally would via connection string: `test:test@localhost:4444`