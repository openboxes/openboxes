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
