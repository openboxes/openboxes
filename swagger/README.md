# Making API documentation and client libraries with Swagger

If you've found this file, you're almost there!

We manage our build using Gradle; here are two handy targets:

`grails gradle swaggerDocs`
: build swagger documentation (see `build/swagger/docs`)\

`grails gradle swaggerClients`
: build client libraries in Java and Python (see `build/swagger/clients`)

## API Reference

We publish API documentation for the REST interface [on SwaggerHub](https://app.swaggerhub.com/apis-docs/openboxes/api/). Documentation for Java and Python clients can be found, after building them, in `build/swagger/clients/*/docs/`. Note that the client documentation is auto-generated from documentation that is itself auto-generated and may be confusing; the examples below may be a better place to start.

## Installation

### Java

TBD.

### Python

The Python bindings should work against Python 2.7 and 3.4+ (we develop against 3.8).

```sh
$ pip install openboxes-client
$ python
```

### Building bindings locally

Clone the project and check out the Swagger branch, if you haven't already.

```sh
$ git clone git@github.com:openboxes/openboxes.git
$ cd openboxes
$ git checkout OBDS-74-swagger-client-library
```

Next, build client libraries as follows.

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

OpenBoxes uses cookie-based authentication, which is a little fiddly:

```python
>>> import openboxes
>>> login_body = openboxes.LoginRequest("username", "password")
>>> session = openboxes.AuthenticationApi()
>>> auth_header = session.login_with_http_info(login_body)
>>> session.api_client.cookie = auth_header[-1]["Set-Cookie"]
```

Once you have authenticated, the interface is more straightforward. Below, we find out how many doses of ibuprofen are currently stored at a particular storage depot:

```python
>>> locations = openboxes.LocationApi(session.api_client).list_locations()
>>> depot = [loc for loc in locations.data if loc.name == "Cange Depot"][0]
>>> openboxes.ConfigurationApi(session.api_client).choose_location(id=depot.id)
"b'User ******** is now logged into Cange Depot'"
>>> products = openboxes.ProductApi(session.api_client).list_products()
>>> treatments = [p for p in products.data if "buprofen" in p.name]
>>> stock_records = [
...     openboxes.ProductApi(session.api_client).product_availability(t.id)
...     for t in treatments
... ]
>>> sum([bin_loc.quantity_on_hand for record in stock_records for bin_loc in record.data])
80580  # as of this writing -- the exact number will vary
```

## License

This software, like all of OpenBoxes' source code, is made available under the [EPL-1.0](https://opensource.org/licenses/eclipse-1.0.php) license.

Copyright (c) 2022 Partners In Health.

## Terms of Use

See Partners In Health's [Terms of Use](https://www.pih.org/pages/terms).

## Feedback

As this is a project in the early stages of development, we are especially interested in feedback. Please reach out to us at `openboxes@pih.org` with any questions, comments or suggestions for improvement.