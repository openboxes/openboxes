## Stock Movement API

### Create 
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

### Read
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
