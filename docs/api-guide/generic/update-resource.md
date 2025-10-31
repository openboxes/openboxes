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
