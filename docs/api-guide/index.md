# REST API

## Overview 

### Authentication
If you try to access the API with no cookies (or an invalid/stale cookie) you'll receive the following error and will need to (re-)authenticate
#### Request
```
$ curl -i -X POST -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/categories
```
#### Response
```
HTTP/1.1 302 Moved Temporarily
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=27C59970DF0F4E5DC7CEC2A695A2DCC5; Path=/openboxes
Location: http://openboxes.ngrok.io/openboxes/auth/login
Content-Length: 0
Date: Sun, 10 Jun 2018 21:20:45 GMT
```

Attempt to authenticate with a valid username and password.
#### Request 
```
$ curl -i -c cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"username":"jmiranda","password":"password"}' https://openboxes.ngrok.io/openboxes/api/login
```
#### Response
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531; Path=/openboxes
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:21:10 GMT

Authentication was successful
```
Once you have authenticated, use the JSESSIONID in the "Cookie" request header or use `-b` to read from a cookies 
file and start making requests against the API.
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/categories

```
If you want to end your session, you can `POST` a request to the logout endpoint.
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/logout

```

### Pagination
All API endpoints will return all objects if pagination parameters are not provided.
```
$ curl -X POST -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/products?offset=0&max=1 | jsonlint
[
  {
    "id": "ff80818155df9de40155df9e329b0009",
    "productCode": "00003",
    "name": "Aspirin 20mg",
    "category": {
      "id": "1",
      "name": "Medicines"
    },
    "description": null,
    "dateCreated": "2016-07-12T14:58:55Z",
    "lastUpdated": "2016-07-12T14:58:55Z"
  }
]

```

### JSON Lint
While testing the API, I'd recommend installing jsonlint ...
```css
npm install jsonlint -g
```

... and piping all curl responses through it to get a pretty response.
```
$ curl -X POST -H "Content-Type: application/json" https://openboxes.ngrok.io/openboxes/api/products?max=1 | jsonlint
[
  {
    "id": "ff80818155df9de40155df9e329b0009",
    "productCode": "00003",
    "name": "Aspirin 20mg",
    "category": {
      "id": "1",
      "name": "Medicines"
    },
    "description": null,
    "dateCreated": "2016-07-12T14:58:55Z",
    "lastUpdated": "2016-07-12T14:58:55Z"
  }
]

```
NOTE: You'll need to remove the `-i` argument from the following examples to prevent parsing errors:
```
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   221    0   221    0     0    455      0 --:--:-- --:--:-- --:--:--   455
Error: Parse error on line 1:
SerP/1.1 200 OK
^
Expecting 'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '[', got 'undefined'
    at Object.parseError (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/jsonlint.js:55:11)
    at Object.parse (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/jsonlint.js:132:22)
    at parse (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/cli.js:82:14)
    at Socket.<anonymous> (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/cli.js:149:41)
    at emitNone (events.js:91:20)
    at Socket.emit (events.js:185:7)
    at endReadableNT (_stream_readable.js:974:12)
    at _combinedTickCallback (internal/process/next_tick.js:74:11)
    at process._tickCallback (internal/process/next_tick.js:98:9) 
```


## Category API

### Create a new category - Exception (500)
Returns validation errors (Name is a required field of Category)
#### Request
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{}' https://openboxes.ngrok.io/openboxes/api/categories

```
#### Response
```
HTTP/1.1 500 Internal Server Error
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sat, 09 Jun 2018 14:30:20 GMT

{"errorCode":500,"errorMessage":"Unable to save category due to errors:\n- Field error in object 'org.pih.warehouse.product.Category' on field 'name': rejected value [null]; codes [org.pih.warehouse.product.Category.name.nullable.error.org.pih.warehouse.product.Category.name,org.pih.warehouse.product.Category.name.nullable.error.name,org.pih.warehouse.product.Category.name.nullable.error.java.lang.String,org.pih.warehouse.product.Category.name.nullable.error,category.name.nullable.error.org.pih.warehouse.product.Category.name,category.name.nullable.error.name,category.name.nullable.error.java.lang.String,category.name.nullable.error,org.pih.warehouse.product.Category.name.nullable.org.pih.warehouse.product.Category.name,org.pih.warehouse.product.Category.name.nullable.name,org.pih.warehouse.product.Category.name.nullable.java.lang.String,org.pih.warehouse.product.Category.name.nullable,category.name.nullable.org.pih.warehouse.product.Category.name,category.name.nullable.name,category.name.nullable.java.lang.String,category.name.nullable,nullable.org.pih.warehouse.product.Category.name,nullable.name,nullable.java.lang.String,nullable]; arguments [name,Category]; default message [Property [{0}] of class [{1}] cannot be null]\n"}
```

### Create a new category - Success (200)
#### Request
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New category"}' https://openboxes.ngrok.io/openboxes/api/categories

```
#### Response
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:22:00 GMT

{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"}

```

## Product API

### Create a new product 
Returns validation error (Category is a required field of Product)
#### Request
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New product", "category":{"id":"ff80818163e2de8d0163eb93c5a00001"}}' \
https://openboxes.ngrok.io/openboxes/api/products
```
#### Response
```
HTTP/1.1 500 Internal Server Error
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:35:58 GMT

{"errorCode":500,"errorMessage":"Unable to save category due to errors:\n- Field error in object 'org.pih.warehouse.product.Product' on field 'category': rejected value [null]; codes [org.pih.warehouse.product.Product.category.nullable.error.org.pih.warehouse.product.Product.category,org.pih.warehouse.product.Product.category.nullable.error.category,org.pih.warehouse.product.Product.category.nullable.error.org.pih.warehouse.product.Category,org.pih.warehouse.product.Product.category.nullable.error,product.category.nullable.error.org.pih.warehouse.product.Product.category,product.category.nullable.error.category,product.category.nullable.error.org.pih.warehouse.product.Category,product.category.nullable.error,org.pih.warehouse.product.Product.category.nullable.org.pih.warehouse.product.Product.category,org.pih.warehouse.product.Product.category.nullable.category,org.pih.warehouse.product.Product.category.nullable.org.pih.warehouse.product.Category,org.pih.warehouse.product.Product.category.nullable,product.category.nullable.org.pih.warehouse.product.Product.category,product.category.nullable.category,product.category.nullable.org.pih.warehouse.product.Category,product.category.nullable,nullable.org.pih.warehouse.product.Product.category,nullable.category,nullable.org.pih.warehouse.product.Category,nullable]; arguments [category,Product]; default message [Property [{0}] of class [{1}] cannot be null]\n"}
```

### Create a new product
#### Request
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New product", "category.id":"ff80818163e2de8d0163eb93c5a00001"}' https://openboxes.ngrok.io/openboxes/api/products

```
#### Response
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:37:12 GMT

{"id":"ff80818163e2de8d0163eba1b1e90002","productCode":null,"name":"New product","category":{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"},"description":null,"dateCreated":"2018-06-10T21:37:12Z","lastUpdated":"2018-06-10T21:37:12Z"}
```

### List all products (results paginated using offset and max)
#### Request
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{ "offset":0, "max":1 }' https://openboxes.ngrok.io/openboxes/api/products

```
#### Response
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 22:13:27 GMT
[{
	"id": "ff80818155df9de40155df9e329b0009",
	"productCode": "00003",
	"name": "Aspirin 20mg",
	"category": {
		"id": "1",
		"name": "Medicines"
	},
	"description": null,
	"dateCreated": "2016-07-12T14:58:55Z",
	"lastUpdated": "2016-07-12T14:58:55Z"
}]
```

### Search products with Name starting with 'New product' (results paginged using offset and max)
#### Request
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{ "name":"Aspirin", "offset":0, "max":1 }' https://openboxes.ngrok.io/openboxes/api/products
```
#### Response
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 22:15:08 GMT

[{
	"id": "ff80818163e2de8d0163eba1b1e90002",
	"productCode": "KX43",
	"name": "New product",
	"category": {
		"id": "ff80818163e2de8d0163eb93c5a00001",
		"name": "New category"
	},
	"description": null,
	"dateCreated": "2018-06-10T21:37:13Z",
	"lastUpdated": "2018-06-10T21:37:13Z"
}]
```

### Get an existing product 
#### Request
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/products/ff80818163e2de8d0163eba1b1e90002
```
#### Response
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:38:27 GMT

{
	"id": "ff80818163e2de8d0163eba1b1e90002",
	"productCode": "KX43",
	"name": "New product",
	"category": {
		"id": "ff80818163e2de8d0163eb93c5a00001",
		"name": "New category"
	},
	"description": null,
	"dateCreated": "2018-06-10T21:37:13Z",
	"lastUpdated": "2018-06-10T21:37:13Z"
}
```

### Product not found

#### Request
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/products/invalididentifier
```
#### Response
```
HTTP/1.1 404 Not Found
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:43:37 GMT

{"errorCode":404,"errorMessage":"Resource not found"}
```


## Generic API
After creating a few API endpoints I got a little tired of writing the same boilerplate code. So I've added 
another endpoint to allow developers to access any of the domain objects via a more generic boilerplate-y API.

### List objects
#### Request
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product | jsonlint
```
#### Response
```
{
  "data": [
    {
      "id": "ff80818155df9de40155df9e31000001",
      "productCode": "00001",
      "name": "Ibuprofen 200mg",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e321c0005",
      "productCode": "00002",
      "name": "Tylenol 325mg",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e329b0009",
      "productCode": "00003",
      "name": "Aspirin 20mg",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e3312000d",
      "productCode": "00004",
      "name": "General Pain Reliever",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e33930011",
      "productCode": "00005",
      "name": "Similac Advance low iron 400g",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e34080015",
      "productCode": "00006",
      "name": "Similac Advance + iron 365g",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e347e0019",
      "productCode": "00007",
      "name": "MacBook Pro 8G",
      "description": null,
      "category": {
        "id": "2",
        "name": "Supplies"
      }
    },
    {
      "id": "ff80818155df9de40155df9e34f1001d",
      "productCode": "00008",
      "name": "Print Paper A4",
      "description": null,
      "category": {
        "id": "2",
        "name": "Supplies"
      }
    }
  ]
}
```


### Read object
#### Request
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff80818155df9de40155df9e31000001
```
#### Response
```
{
	"data": {
		"id": "ff80818155df9de40155df9e31000001",
		"productCode": "00001",
		"name": "Ibuprofen 200mg",
		"description": null,
		"category": {
			"id": "1",
			"name": "Medicines"
		}
	}
}
```

### Create an object

#### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"name":"product 0", "category.id":"ROOT"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```
#### Response
```
{
	"data": {
		"id": "ff8081816407132d01640730bd150003",
		"productCode": null,
		"name": "product 0",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}
}
```

### Update an object
#### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"description":"This is the penultimate product"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff8081816407132d0164071eec250001 | jsonlint
```
#### Response
```
{
  "data": {
    "id": "ff8081816407132d0164071eec250001",
    "productCode": "BK71",
    "name": "product 1",
    "description": "This is the penultimate product",
    "category": {
      "id": "ROOT",
      "name": "ROOT"
    }
  }
}
```


### Create multiple objects 
The REST API allows you to create multiple objects at once. 

If there are no errors, both objects should be created and returned.
#### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1","category.id":"ROOT"},{"name":"product 2","category.id":"ROOT"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```
#### Response
```
{
	"data": [{
		"id": "ff8081816407132d0164071eec250001",
		"productCode": null,
		"name": "product 1",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}, {
		"id": "ff8081816407132d0164071eec2d0002",
		"productCode": null,
		"name": "product 2",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}]
}
```
In the following example, you'll notice that the POST request on multiple objects is an all-or-nothing transaction, 
so if there are any errors (i.e. validation errors) the entire request will fail. In this case, we throw an error
for the first object that failed - not for all objects that fail.
#### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1"},{"name":"product 2"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

#### Response
```{
	"errorCode": 400,
	"errorMessage": "Validation errors",
	"data": {
		"errors": [{
			"object": "org.pih.warehouse.product.Product",
			"field": "category",
			"rejected-value": null,
			"message": "Property [category] of class [Product] cannot be null"
		}]
	}
}
```


### Update multiple objects 
The REST API also allows you to update multiple objects at once. You'll POST to same endpoint used to create new 
objects, but you'll include the ID within each data object. 

#### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"id":"ff8081816407132d0164071eec250001", "name":"product 1+","category.id":"ROOT"},\
{"id":"ff8081816407132d0164071eec2d0002", "name":"product 2.1","category.id":"ROOT"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

#### Response
```
{
	"data": [{
		"id": "ff8081816407132d0164071eec250001",
		"productCode": "BK71",
		"name": "product 1+",
		"description": "This is the penultimate product",
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}, {
		"id": "ff8081816407132d0164071eec2d0002",
		"productCode": "DD67",
		"name": "product 2.1",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}]
}
```

### Search

The Search API currently supports the following operators on any string property of any object:

* eq (default: you can just specify 'property' and 'value' if you want an equality search)
* like
* ilike

Support for other property types (Integer, Date, BigDecimal) and operators (i.e. in, isNull, between, gt, ge, lt, le, 
etc) will be added at some point in the future. In addition, there's no way to perform nested searches (i.e. search for 
all shipment items that reference a particular product) at this time so please 

**NOTE**: If you don't see a search operator or property type supported, please raise an issue on GitHub
(https://github.com/openboxes/openboxes/issues).

#### Request

```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{"searchAttributes":[{"property":"name", "operator":"ilike", "value":"Tyl%"}]}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/search?max=1
```

#### Response
```
{
	"data": [{
		"id": "ff80818155df9de40155df9e321c0005",
		"productCode": "00002",
		"name": "Tylenol 325mg",
		"description": null,
		"category": {
			"id": "1",
			"name": "Medicines"
		}
	}]
}
```
 