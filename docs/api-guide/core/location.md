## Location API

### List
```
$ curl -X GET -b cookies.txt \
-H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/locations" | jsonlint
{
  "data": [
    {
      "id": "1",
      "name": "Boston Headquarters",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "2",
        "name": "Depot|fr:D",
        "description": "Depot",
        "locationTypeCode": "DEPOT"
      },
      "locationTypeCode": "DEPOT"
    },
    {
      "id": "2",
      "name": "Miami Warehouse",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "2",
        "name": "Depot|fr:D",
        "description": "Depot",
        "locationTypeCode": "DEPOT"
      },
      "locationTypeCode": "DEPOT"
    },
    {
      "id": "3",
      "name": "Tabarre Depot",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "2",
        "name": "Depot|fr:D",
        "description": "Depot",
        "locationTypeCode": "DEPOT"
      },
      "locationTypeCode": "DEPOT"
    },
    {
      "id": "4",
      "name": "ZZZ Supply Company",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "2",
        "name": "Depot|fr:D",
        "description": "Depot",
        "locationTypeCode": "DEPOT"
      },
      "locationTypeCode": "DEPOT"
    },
    {
      "id": "ff80818155dd68010155dd6bb9c00001",
      "name": "Test Supplier",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "4",
        "name": "Supplier|fr:Fournisseurs",
        "description": "Supplier",
        "locationTypeCode": "SUPPLIER"
      },
      "locationTypeCode": "SUPPLIER"
    },
    {
      "id": "ff8081816430012d0164301b8eda0008",
      "name": "new depot location",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "2",
        "name": "Depot|fr:D",
        "description": "Depot",
        "locationTypeCode": "DEPOT"
      },
      "locationTypeCode": "DEPOT"
    }
  ]
}
```


### Read
```
$ curl -X GET -b cookies.txt \
-H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/locations/1" | jsonlint 
{
  "data": {
    "id": "1",
    "name": "Boston Headquarters",
    "description": null,
    "locationNumber": null,
    "locationGroup": null,
    "parentLocation": null,
    "locationType": {
      "id": "2",
      "name": "Depot|fr:D",
      "description": "Depot",
      "locationTypeCode": "DEPOT"
    },
    "locationTypeCode": "DEPOT"
  }
}
```



### Create 
```
$ curl -X POST -b cookies.txt \
-H "Content-Type: application/json" \
-d '{"name":"new depot location","locationType.id":"2"}' \
"https://openboxes.ngrok.io/openboxes/api/locations" | jsonlint 
{
  "data": {
    "id": "ff8081816430012d01643016c6440006",
    "name": "new depot location",
    "description": null,
    "locationNumber": null,
    "locationGroup": null,
    "parentLocation": null,
    "locationType": {
      "id": "2",
      "name": "Depot|fr:D",
      "description": "Depot",
      "locationTypeCode": "DEPOT"
    },
    "locationTypeCode": "DEPOT"
  }
}
```

### Update 
```
$ curl -X POST -b cookies.txt \
-H "Content-Type: application/json" \
-d '{"description":"this is that new one"}' \
"https://openboxes.ngrok.io/openboxes/api/locations/ff8081816430012d01643016c6440006" | jsonlint 
  {
    "data": {
      "id": "ff8081816430012d01643016c6440006",
      "name": "new depot location",
      "description": "this is that new one",
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "2",
        "name": "Depot|fr:D",
        "description": "Depot",
        "locationTypeCode": "DEPOT"
      },
      "locationTypeCode": "DEPOT"
    }
  }

```

### Delete
```
$ curl -i -X DELETE -b cookies.txt \
-H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/locations/ff8081816430012d0164301b8eda0008" 

HTTP/1.1 204 No Content
Server: Apache-Coyote/1.1
Date: Sun, 24 Jun 2018 04:44:57 GMT
```


