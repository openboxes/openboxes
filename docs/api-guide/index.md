# REST API

## Overview 

### Unauthorized Access
If you try to access the API with no cookies (or an invalid/stale cookie) you'll receive the following error and will need to (re-)authenticate
```
$ curl -i -X POST -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/categories

HTTP/1.1 401 Unauthorized
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=905D991AE2661B0FDD7F6FB140EB97D8; Path=/openboxes
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Thu, 21 Jun 2018 04:21:29 GMT

{"errorCode":401,"errorMessage":"Unauthorized user: Request categoryApi:save requires authentication"}
```

### Authentication
In order to authenticate, you need a valid user account. In order to get the full benefits of the API your user should 
probably be in role Superuser. Once you have created a Superuser <cough> user, you can attempt to authenticate 
using your username and password.
```
$ curl -i -c cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"username":"jmiranda","password":"password"}' https://openboxes.ngrok.io/openboxes/api/login

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531; Path=/openboxes
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:21:10 GMT

Authentication was successful
```
NOTE: `-c cookies` option will create a cookies file (cookies.txt) and save your session information to be used in 
subsequent requests. This saves a huge amount of headache when you're testing the API.

### Cookie
Once you have authenticated, you have two options to 


1. use the JSESSIONID in the "Cookie" request header to make further authenticated requests 
to the API
    ```
    $ curl -i -X POST -H "Content-Type: application/json" \
    -H "Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531" \
    https://openboxes.ngrok.io/openboxes/api/categories
    
    ```
1. Or use `-b` to read from a cookies file and start making requests against the API.
    ```
    $ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
    https://openboxes.ngrok.io/openboxes/api/categories
    
    ```

### Logout

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


## Product API

### Create 
Create a new product.
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New product", "category.id":"ff80818163e2de8d0163eb93c5a00001"}' https://openboxes.ngrok.io/openboxes/api/products


HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:37:12 GMT

{"id":"ff80818163e2de8d0163eba1b1e90002","productCode":null,"name":"New product","category":{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"},"description":null,"dateCreated":"2018-06-10T21:37:12Z","lastUpdated":"2018-06-10T21:37:12Z"}
```

### List 
Return all products (results paginated using offset and max)
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{ "offset":0, "max":1 }' https://openboxes.ngrok.io/openboxes/api/products


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

### Search 
Return products with Name starting with 'New product' (results paginged using offset and max)
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{ "name":"Aspirin", "offset":0, "max":1 }' https://openboxes.ngrok.io/openboxes/api/products

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
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/products/ff80818163e2de8d0163eba1b1e90002

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

### Exceptions

#### Create - Validation Errors 
Returns validation error (Category is a required field of Product)
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New product", "category":{"id":"ff80818163e2de8d0163eb93c5a00001"}}' \
https://openboxes.ngrok.io/openboxes/api/products

HTTP/1.1 500 Internal Server Error
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:35:58 GMT

{"errorCode":500,"errorMessage":"Unable to save category due to errors:\n- Field error in object 'org.pih.warehouse.product.Product' on field 'category': rejected value [null]; codes [org.pih.warehouse.product.Product.category.nullable.error.org.pih.warehouse.product.Product.category,org.pih.warehouse.product.Product.category.nullable.error.category,org.pih.warehouse.product.Product.category.nullable.error.org.pih.warehouse.product.Category,org.pih.warehouse.product.Product.category.nullable.error,product.category.nullable.error.org.pih.warehouse.product.Product.category,product.category.nullable.error.category,product.category.nullable.error.org.pih.warehouse.product.Category,product.category.nullable.error,org.pih.warehouse.product.Product.category.nullable.org.pih.warehouse.product.Product.category,org.pih.warehouse.product.Product.category.nullable.category,org.pih.warehouse.product.Product.category.nullable.org.pih.warehouse.product.Category,org.pih.warehouse.product.Product.category.nullable,product.category.nullable.org.pih.warehouse.product.Product.category,product.category.nullable.category,product.category.nullable.org.pih.warehouse.product.Category,product.category.nullable,nullable.org.pih.warehouse.product.Product.category,nullable.category,nullable.org.pih.warehouse.product.Category,nullable]; arguments [category,Product]; default message [Property [{0}] of class [{1}] cannot be null]\n"}
```


### Read - Product not found
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/products/invalididentifier

HTTP/1.1 404 Not Found
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:43:37 GMT

{"errorCode":404,"errorMessage":"Resource not found"}
```

## Category API

### Create

```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New category"}' https://openboxes.ngrok.io/openboxes/api/categories


HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:22:00 GMT

{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"}

```

### Exceptions

#### Validation Errors
Returns validation errors (Name is a required field of Category)
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{}' https://openboxes.ngrok.io/openboxes/api/categories

HTTP/1.1 500 Internal Server Error
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sat, 09 Jun 2018 14:30:20 GMT

{"errorCode":500,"errorMessage":"Unable to save category due to errors:\n- Field error in object 'org.pih.warehouse.product.Category' on field 'name': rejected value [null]; codes [org.pih.warehouse.product.Category.name.nullable.error.org.pih.warehouse.product.Category.name,org.pih.warehouse.product.Category.name.nullable.error.name,org.pih.warehouse.product.Category.name.nullable.error.java.lang.String,org.pih.warehouse.product.Category.name.nullable.error,category.name.nullable.error.org.pih.warehouse.product.Category.name,category.name.nullable.error.name,category.name.nullable.error.java.lang.String,category.name.nullable.error,org.pih.warehouse.product.Category.name.nullable.org.pih.warehouse.product.Category.name,org.pih.warehouse.product.Category.name.nullable.name,org.pih.warehouse.product.Category.name.nullable.java.lang.String,org.pih.warehouse.product.Category.name.nullable,category.name.nullable.org.pih.warehouse.product.Category.name,category.name.nullable.name,category.name.nullable.java.lang.String,category.name.nullable,nullable.org.pih.warehouse.product.Category.name,nullable.name,nullable.java.lang.String,nullable]; arguments [name,Category]; default message [Property [{0}] of class [{1}] cannot be null]\n"}
```

## Identifier API
The identifier API only supports POST.
### Create 
Create a new alphanumeric identifier for a given `identifierType` or `identifierFormat`.

#### Identifier format
Create your own identifier format using the following codes.

* A = Alphanumeric
* L = Alphabetic characters only
* N = Numeric 
* D = Numeric
* Any other character (i.e. dashes, periods) will be included as-is.

```
[jmiranda@jmiranda-ThinkPad-W540 ~]$ curl -i -X POST -b cookies.txt \
-H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/identifiers?identifierFormat=AAANNN
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Thu, 21 Jun 2018 04:49:26 GMT

{"data":"VCU789"}
```

#### Identifier type
Allowed `identifierType` values: 
* `requisition`
* `product`
* `productSupplier`
* `transaction`
* `shipment`
* `order`

```
[jmiranda@jmiranda-ThinkPad-W540 ~]$ curl -i -X POST -b cookies.txt \
-H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/identifiers?identifierType=product
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Thu, 21 Jun 2018 04:49:40 GMT

{"data":"VR26"}

```
These formats can be configured in openboxes-config.properties
```
openboxes.identifier.transaction.format = AAA-AAA-AAA
openboxes.identifier.order.format = NNNLLL
openboxes.identifier.product.format = LLNN
openboxes.identifier.productSupplier.format = LLNN
openboxes.identifier.requisition.format = NNNLLL
openboxes.identifier.shipment.format = NNNLLL
```
You can also edit the available characters and digits available for `identifierFormat` mask. This allows you to remove characters that may be confused with others (i.e. I, 1, l or 0 and O). By default we keep all numeric digits and remove the conflicting alphabetic characters.
```
openboxes.identifier.numeric = 0123456789
openboxes.identifier.alphabetic = ABCDEFGHJKMNPQRSTUVXYZ
openboxes.identifier.alphanumeric = 0123456789ABCDEFGHJKMNPQRSTUVWXYZ
```

## Generic API
After creating a few API endpoints I got a little tired of writing the same boilerplate code. So I've added 
another endpoint to allow developers to access any of the domain objects via a more generic boilerplate-y API.

### List
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product | jsonlint

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

### Read 
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff80818155df9de40155df9e31000001

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

### Create
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"name":"product 0", "category.id":"ROOT"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product

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

### Update 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"description":"This is the penultimate product"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff8081816407132d0164071eec250001 | jsonlint

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


### Create multiple 
The REST API allows you to create multiple objects at once. 

If there are no errors, both objects should be created and returned.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1","category.id":"ROOT"},{"name":"product 2","category.id":"ROOT"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product

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

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1"},{"name":"product 2"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product

{
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

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"id":"ff8081816407132d0164071eec250001", "name":"product 1+","category.id":"ROOT"},\
{"id":"ff8081816407132d0164071eec2d0002", "name":"product 2.1","category.id":"ROOT"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product

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

```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{"searchAttributes":[{"property":"name", "operator":"ilike", "value":"Tyl%"}]}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/search?max=1

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
 