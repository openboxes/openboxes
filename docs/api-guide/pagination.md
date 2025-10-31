# Pagination
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

