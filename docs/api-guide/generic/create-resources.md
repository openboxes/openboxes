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
			"object": "Product",
			"field": "category",
			"rejected-value": null,
			"message": "Property [category] of class [Product] cannot be null"
		}]
	}
}
```
