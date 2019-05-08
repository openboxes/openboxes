

## Generic API
After creating a few API endpoints I got a little tired of writing the same boilerplate code. So I've added 
another endpoint to allow developers to access any of the domain objects via a more generic boilerplate-y API.

### List
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product | jsonlint
```
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
      "name": "Acetaminophen 325mg",
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
        "name": "Computer Equipment"
      }
    }
  ]
}
```

### Read 
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff80818155df9de40155df9e31000001
```
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

### Create
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"name":"product 0", "category.id":"ROOT"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```
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

### Update 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"description":"This is the penultimate product"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff8081816407132d0164071eec250001 | jsonlint
```
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


### Create multiple 
The REST API allows you to create multiple objects at once. 

If there are no errors, both objects should be created and returned.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1","category.id":"ROOT"},{"name":"product 2","category.id":"ROOT"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```
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

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1"},{"name":"product 2"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```
```
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
```
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

```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{"searchAttributes":[{"property":"name", "operator":"ilike", "value":"Ace%"}]}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/search?max=1
```
```
{
	"data": [{
		"id": "ff80818155df9de40155df9e321c0005",
		"productCode": "00002",
		"name": "Acetaminophen 325mg",
		"description": null,
		"category": {
			"id": "1",
			"name": "Medicines"
		}
	}]
}
```
 