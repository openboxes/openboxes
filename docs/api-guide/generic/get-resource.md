Get the resource instance identified by the given ID. 
200 (OK) 
404 (Not Found)

# Request
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff8081818ef9ad0f018ef9b753f30000
```
# Response
```
{
    "data": {
        "id": "ff8081818ef9ad0f018ef9b753f30000",
        "productCode": "026851",
        "name": "product 0",
        "description": null,
        "category": "ROOT",
        "unitOfMeasure": null,
        "pricePerUnit": null,
        "dateCreated": "2024-04-20T04:15:25Z",
        "lastUpdated": "2024-04-20T04:16:00Z",
        "updatedBy": null,
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
