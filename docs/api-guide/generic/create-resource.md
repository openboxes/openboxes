Used to create a new resource.

## Create a Product
### Request
```
$ curl -b cookies.txt -i -X POST -H "Content-Type: application/json" \
-d '{"name":"New Product", "productType": {"id":"DEFAULT"}, "category": {"id": "ROOT"}}' 
https://openboxes.ngrok.io/openboxes/api/generic/product
```
### Response
```
HTTP/2 201 
content-type: application/json;charset=UTF-8
date: Sat, 20 Apr 2024 15:20:11 GMT
x-application-context: application:development

{
    "data": {
        "id": "ff8081818ef9ad0f018ef9b753f30000",
        "productCode": null,
        "name": "New Product",
        "description": null,
        "category": "ROOT",
        "unitOfMeasure": null,
        "pricePerUnit": null,
        "dateCreated": "2024-04-20T04:15:25Z",
        "lastUpdated": "2024-04-20T04:15:25Z",
        "updatedBy": "Justin Miranda",
        "color": null,
        "handlingIcons": [],
        "lotAndExpiryControl": false,
        "active": true,
        "displayNames": {
            "default": null
        }
    }
}
```
## Create a Category

### Request
```
$ curl -b cookies.txt -i -X POST -H "Content-Type: application/json" \
-d '{"name":"New Category", "parentCategory": {"id": "ROOT"}}' \
https://openboxes.ngrok.io/openboxes/api/generic/category
```
### Response
```
HTTP/2 201 
content-type: application/json;charset=UTF-8
date: Sat, 20 Apr 2024 15:20:11 GMT
x-application-context: application:development

{
    "data": {
        "id": "ff8081818ef9ad0f018efc17f2a80001",
        "name": "New Category",
        "parentCategory": {
            "id": "ROOT",
            "name": "ROOT",
            "parentCategory": null
        }
    }
}
```
