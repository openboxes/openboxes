## Product API

[TOC]

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

### Sub Resources

#### Available Items 

NOTE: I'm realizing it could be dangerous to use this endpoint because it leaves the bin location empty. 
This is misleading since you should not be able to make any changes to the quantity associated with the inventory item
without specifying a valid bin location. 
```
$ curl  -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/products/ff80818155df9de40155df9e3312000d/availableItems?location.id=1"|jsonlint
{
  "data": [
    {
      "inventoryItem.id": "ff80818155df9de40155df9e3356000e",
      "product.name": "General Pain Reliever",
      "productCode": "00004",
      "lotNumber": "lot57",
      "expirationDate": "2017-01-28T15:58:54Z",
      "binLocation.id": null,
      "binLocation.name": null,
      "quantityAvailable": 10000
    }
  ]
}

```

#### Available Bin Locations 
```
$ curl  -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/products/ff80818155df9de40155df9e3312000d/availableItems?location.id=1"|jsonlint
{
  "data": [
    {
      "inventoryItem.id": "ff8081816473166d0164731813990001",
      "product.name": "General Pain Reliever",
      "productCode": "00004",
      "lotNumber": "252151251",
      "expirationDate": "2025-01-01T06:00:00Z",
      "binLocation.id": null,
      "binLocation.name": null,
      "quantityAvailable": 0
    },
    {
      "inventoryItem.id": "ff80818155df9de40155df9e3356000e",
      "product.name": "General Pain Reliever",
      "productCode": "00004",
      "lotNumber": "lot57",
      "expirationDate": "2017-01-28T15:58:54Z",
      "binLocation.id": null,
      "binLocation.name": null,
      "quantityAvailable": 10000
    },
    {
      "inventoryItem.id": "ff8081816473166d0164731814310002",
      "product.name": "General Pain Reliever",
      "productCode": "00004",
      "lotNumber": "214214212",
      "expirationDate": "2020-01-01T06:00:00Z",
      "binLocation.id": null,
      "binLocation.name": null,
      "quantityAvailable": 0
    }
  ]
}
```


#### Product Associations
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/products/ff80818155df9de40155df9e31000001/associatedProducts?type=SUBSTITUTE&type=EQUIVALENT&location.id=1" \ |jsonlint
{
  "data": {
    "product": {
      "id": "ff80818155df9de40155df9e31000001",
      "productCode": "00001",
      "name": "Ibuprofen 200mg",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    "availableItems": [
      {
        "inventoryItem": {
          "id": "ff80818155df9de40155df9e31930002",
          "product": {
            "id": "ff80818155df9de40155df9e31000001",
            "name": "Ibuprofen 200mg",
            "productCode": "00001"
          },
          "lotNumber": "lot57",
          "expirationDate": "2016-07-15T14:58:53Z"
        },
        "quantity": 9990
      }
    ],
    "hasAssociations": true,
    "hasEarlierExpiringItems": false,
    "productAssociations": [
      {
        "id": "ff808181643c655f01643c6ed8870001",
        "type": "SUBSTITUTE",
        "product": {
          "id": "ff80818155df9de40155df9e321c0005",
          "productCode": "00002",
          "name": "Acetaminophen 325mg",
          "description": null,
          "category": {
            "id": "1",
            "name": "Medicines"
          }
        },
        "conversionFactor": 1,
        "comments": null,
        "minExpirationDate": "2016-08-01T14:58:54Z",
        "availableQuantity": 10244
      },
      {
        "id": "ff808181643f048401643f93b95b0005",
        "type": "SUBSTITUTE",
        "product": {
          "id": "ff80818155df9de40155df9e3312000d",
          "productCode": "00004",
          "name": "General Pain Reliever",
          "description": null,
          "category": {
            "id": "1",
            "name": "Medicines"
          }
        },
        "conversionFactor": 1,
        "comments": null,
        "minExpirationDate": "2017-01-28T15:58:54Z",
        "availableQuantity": 10000
      }
    ]
  }
}
```