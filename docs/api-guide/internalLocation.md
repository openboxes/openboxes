

# Internal Locations

## List 
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/internalLocations?location.id=ff808181646b260401646b3f2ced0002"|jsonlint
{
  "data": [
    {
      "id": "ff808181646d3ec101646d5e7d480001",
      "name": "Bin 1",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": {
        "id": "ff808181646b260401646b3f2ced0002",
        "name": "Store 1",
        "description": null,
        "locationNumber": "STORE1",
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "ff808181646b260401646b3ed20f0001",
          "name": "Store",
          "description": null,
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "locationType": {
        "id": "cab2b4f35ba2d867015ba2e17e390001",
        "name": "Bin Location",
        "description": "Default bin location type",
        "locationTypeCode": "BIN_LOCATION"
      },
      "locationTypeCode": "BIN_LOCATION"
    },
    {
      "id": "ff808181646d3ec101646d5e8e3e0003",
      "name": "Bin 2",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": {
        "id": "ff808181646b260401646b3f2ced0002",
        "name": "Store 1",
        "description": null,
        "locationNumber": "STORE1",
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "ff808181646b260401646b3ed20f0001",
          "name": "Store",
          "description": null,
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "locationType": {
        "id": "cab2b4f35ba2d867015ba2e17e390001",
        "name": "Bin Location",
        "description": "Default bin location type",
        "locationTypeCode": "BIN_LOCATION"
      },
      "locationTypeCode": "BIN_LOCATION"
    },
    {
      "id": "ff808181646d3ec101646d5ea1f20005",
      "name": "Bin 3",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": {
        "id": "ff808181646b260401646b3f2ced0002",
        "name": "Store 1",
        "description": null,
        "locationNumber": "STORE1",
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "ff808181646b260401646b3ed20f0001",
          "name": "Store",
          "description": null,
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "locationType": {
        "id": "cab2b4f35ba2d867015ba2e17e390001",
        "name": "Bin Location",
        "description": "Default bin location type",
        "locationTypeCode": "BIN_LOCATION"
      },
      "locationTypeCode": "BIN_LOCATION"
    },
    {
      "id": "ff808181648c6e2401648c95140f0001",
      "name": "Receiving",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": {
        "id": "ff808181646b260401646b3f2ced0002",
        "name": "Store 1",
        "description": null,
        "locationNumber": "STORE1",
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "ff808181646b260401646b3ed20f0001",
          "name": "Store",
          "description": null,
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "locationType": {
        "id": "ff8081816482352b01648249e8cc0001",
        "name": "Receiving",
        "description": null,
        "locationTypeCode": "BIN_LOCATION"
      },
      "locationTypeCode": "BIN_LOCATION"
    }
  ]
}
```

## Read 
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/internalLocations/ff808181648c6e2401648c95140f0001"|jsonlint
{
  "data": {
    "id": "ff808181648c6e2401648c95140f0001",
    "name": "Receiving",
    "description": null,
    "locationNumber": null,
    "locationGroup": null,
    "parentLocation": {
      "id": "ff808181646b260401646b3f2ced0002",
      "name": "Store 1",
      "description": null,
      "locationNumber": "STORE1",
      "locationGroup": null,
      "parentLocation": null,
      "locationType": {
        "id": "ff808181646b260401646b3ed20f0001",
        "name": "Store",
        "description": null,
        "locationTypeCode": "DEPOT"
      },
      "locationTypeCode": "DEPOT"
    },
    "locationType": {
      "id": "ff8081816482352b01648249e8cc0001",
      "name": "Receiving",
      "description": null,
      "locationTypeCode": "BIN_LOCATION"
    },
    "locationTypeCode": "BIN_LOCATION"
  }
}
```