# Making API documentation and client libraries with Swagger

If you've found this file, you're almost there!

## API Reference

We publish API documentation for the REST interface
[on SwaggerHub](https://app.swaggerhub.com/apis-docs/openboxes/api/).
Documentation for Java and Python clients can be found, after building
them, in `build/swagger/clients/*/docs/`. Note that the client API
documentation is auto-generated from documentation that is itself
auto-generated and may be confusing; the examples below may be a better
place to start.

## Installation

### Java

TBD.

### Python

The Python bindings should work against Python 2.7 and 3.4+ (we develop
against 3.8).

```sh
$ pip install openboxes-client
$ python
```

### Building bindings locally

Clone the main OpenBoxes project and check out the Swagger branch, if you
haven't already.

```sh
$ git clone git@github.com:openboxes/openboxes.git
$ cd openboxes
$ git checkout OBDS-74-swagger-client-library
```

Next, build client libraries as follows. (You can build swagger declarations in,
json/yaml, with no client code, by running `grails gradle swaggerDocs` instead.)

```sh
$ npm ci
$ grails gradle swaggerClients
```

Finally, install required Python packages.

```sh
$ cd build/swagger/clients/python/
$ python setup.py install --user
```

## Usage/Examples

Click this button to go to an interactive playground.

[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/mdpearson/openboxes-python-client.git/HEAD?labpath=demo.ipynb)

## License

This software, like all of OpenBoxes' source code, is made available
under the [EPL-1.0](https://opensource.org/licenses/eclipse-1.0.php)
license.

Copyright (c) 2022 Partners In Health.

## Terms of Use

See Partners In Health's [Terms of Use](https://www.pih.org/pages/terms).

## Feedback

As this is a project in the early stages of development, we are
especially interested in feedback. Please reach out to us at
`openboxes@pih.org` with any questions, comments or suggestions for
improvement.
