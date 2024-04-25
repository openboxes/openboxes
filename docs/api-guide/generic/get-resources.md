Return a list of resource instances.

## Request
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product | jsonlint
```

## Response

``` py
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
