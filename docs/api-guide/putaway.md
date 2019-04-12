# Putaway API

## Get Putaway Candidates
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/putaways?location.id=ff808181646b260401646b3f2ced0002"|jsonlint
{
  "data": [
    {
      "putawayStatus": TODO,
      "currentFacility.id": "ff808181646b260401646b3f2ced0002",
      "currentFacility.name": "Store 1",
      "currentLocation.id": "ff808181648c6e2401648c95140f0001",
      "currentLocation.name": "Receiving",
      "container": null,
      "product.id": "ff80818155df9de40155df9e31000001",
      "productCode": "AB12",
      "product.name": "Ibuprofen 200mg",
      "inventoryItem.id": "ff808181646b260401646b495487000d",
      "lotNumber": "ABC789",
      "expirationDate": "2020-01-01T06:00:00Z",
      "recipient.id": null,
      "recipient.name": null,
      "availableBins": [
        "Bin 2",
        "Bin 3",
        "Receiving",
        "Bin 1"
      ],
      "putawayFacility.id": null,
      "putawayFacility.name": null,
      "putawayLocation.id": null,
      "putawayLocation.name": null,
      "quantity": 5
    }
  ]
}
```

## Create Putaway 

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @createPutawayItem.json \
"https://openboxes.ngrok.io/openboxes/api/putaways"|jsonlint
```
### Data
```
{
    "putawayNumber":"",
    "putawayAssignee.id":"",
    "putawayStatus":"",
    "putawayDate":"",
    "putawayItems":[{
        "putawayStatus":"TODO",
        "currentFacility.id":"ff808181646b260401646b3f2ced0002",
        "currentLocation.id":"ff808181648c6e2401648c95140f0001",
        "product.id":"ff80818155df9de40155df9e31000001",
        "inventoryItem.id":"ff808181646b260401646b495487000d",
        "recipient.id":"1",
        "putawayFacility.id":"",
        "putawayLocation.id":"",
        "quantity": 5
    }]
}
```
### Response
```
{
  "data": {
    "putawayNumber": "ABC123",
    "putawayStatus": "TODO",
    "putawayDate": "2019-01-01T06:00:00Z",
    "putawayAssignee": {
      "id": "1",
      "name": "Mr Administrator",
      "firstName": "Mr",
      "lastName": "Administrator",
      "email": "admin@pih.org",
      "username": "admin"
    },
    "putawayItems": [
      {
        "putawayStatus": "TODO",
        "currentFacility.id": "ff808181646b260401646b3f2ced0002",
        "currentFacility.name": "Store 1",
        "currentLocation.id": "ff808181648c6e2401648c95140f0001",
        "currentLocation.name": "Receiving",
        "container": null,
        "product.id": "ff80818155df9de40155df9e31000001",
        "productCode": "AB12",
        "product.name": "Ibuprofen 200mg",
        "inventoryItem.id": "ff808181646b260401646b495487000d",
        "lotNumber": "ABC789",
        "expirationDate": "2020-01-01T06:00:00Z",
        "recipient.id": "1",
        "recipient.name": "Mr Administrator",
        "availableBins": [
          "Bin 2",
          "Bin 3",
          "Receiving",
          "Bin 1"
        ],
        "putawayFacility.id": null,
        "putawayFacility.name": null,
        "putawayLocation.id": null,
        "putawayLocation.name": null,
        "quantity": 5
      }
    ]
  }
}
```

## Update Putaway 

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @updatePutaway.json \
"https://openboxes.ngrok.io/openboxes/api/putaways"|jsonlint
```

### Data
```
{
    "putawayNumber":"ABC123",
    "putawayAssignee.id":"1",
    "putawayStatus":"TODO",
    "putawayDate":"07/13/2018",
    "putawayItems":[{
        "putawayStatus":"TODO",
        "currentFacility.id":"ff808181646b260401646b3f2ced0002",
        "currentLocation.id":"ff808181648c6e2401648c95140f0001",
        "product.id":"ff80818155df9de40155df9e31000001",
        "inventoryItem.id":"ff808181646b260401646b495487000d",
        "recipient.id":"1",
        "putawayFacility.id":"ff808181646b260401646b3f2ced0002",
        "putawayLocation.id":"ff808181646d3ec101646d5e7d480001",
        "quantity": 5
    }]
}
```

## Split Putaway Item

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @splitPutawayItems.json \
"https://openboxes.ngrok.io/openboxes/api/putaways"|jsonlint
```
### Data
```
{
    "putawayNumber":"",
    "putawayAssignee.id":"1",
    "putawayStatus":"TODO",
    "putawayDate":"01/01/2019",
    "putawayItems":[{
        "putawayStatus":"TODO",
        "currentFacility.id":"ff808181646b260401646b3f2ced0002",
        "currentLocation.id":"ff808181648c6e2401648c95140f0001",
        "product.id":"ff80818155df9de40155df9e31000001",
        "inventoryItem.id":"ff808181646b260401646b495487000d",
        "recipient.id":"1",
        "putawayFacility.id":"ff808181646b260401646b3f2ced0002",
        "putawayLocation.id":"ff808181646d3ec101646d5e7d480001",
        "quantity": 5,
        "splitItems":[{
                "putawayFacility.id":"ff808181646b260401646b3f2ced0002",
                "putawayLocation.id":"ff808181646d3ec101646d5e7d480001",
                "quantity":"2",
            },
            {
                "putawayFacility.id":"ff808181646b260401646b3f2ced0002",
                "putawayLocation.id":"ff808181646d3ec101646d5e7d480001",
                "quantity":"3",
            }
        ]
    }]
}
```
### Response
```
{
  "data": {
    "id": null,
    "putawayNumber": "824VDY",
    "putawayStatus": "TODO",
    "putawayDate": "2019-01-01T06:00:00Z",
    "putawayAssignee": {
      "id": "1",
      "name": "Mr Administrator",
      "firstName": "Mr",
      "lastName": "Administrator",
      "email": "admin@pih.org",
      "username": "admin"
    },
    "putawayItems": [
      {
        "id": null,
        "stockMovement.id": "Receiving",
        "stockMovement.name": "Receiving",
        "putawayStatus": "TODO",
        "transactionNumber": null,
        "currentFacility.id": "ff808181646b260401646b3f2ced0002",
        "currentFacility.name": "Store 1",
        "currentLocation.id": "ff808181648c6e2401648c95140f0001",
        "currentLocation.name": "Receiving",
        "container": null,
        "product.id": "ff80818155df9de40155df9e31000001",
        "product.productCode": "AB12",
        "product.name": "Ibuprofen 200mg",
        "inventoryItem.id": "ff808181646b260401646b495487000d",
        "inventoryItem.lotNumber": "ABC789",
        "inventoryItem.expirationDate": "2020-01-01T06:00:00Z",
        "recipient.id": "1",
        "recipient.name": "Mr Administrator",
        "currentBins": "Bin 1,Bin 2,Bin 3,Receiving",
        "currentBinsAbbreviated": "Bin 1,Bin 2,Bin 3,Rece...",
        "putawayFacility.id": "ff808181646b260401646b3f2ced0002",
        "putawayFacility.name": "Store 1",
        "putawayLocation.id": "ff808181646d3ec101646d5e7d480001",
        "putawayLocation.name": "Bin 1",
        "quantity": 2
      },
      {
        "id": null,
        "stockMovement.id": "Receiving",
        "stockMovement.name": "Receiving",
        "putawayStatus": "TODO",
        "transactionNumber": null,
        "currentFacility.id": "ff808181646b260401646b3f2ced0002",
        "currentFacility.name": "Store 1",
        "currentLocation.id": "ff808181648c6e2401648c95140f0001",
        "currentLocation.name": "Receiving",
        "container": null,
        "product.id": "ff80818155df9de40155df9e31000001",
        "product.productCode": "AB12",
        "product.name": "Ibuprofen 200mg",
        "inventoryItem.id": "ff808181646b260401646b495487000d",
        "inventoryItem.lotNumber": "ABC789",
        "inventoryItem.expirationDate": "2020-01-01T06:00:00Z",
        "recipient.id": "1",
        "recipient.name": "Mr Administrator",
        "currentBins": "Bin 1,Bin 2,Bin 3,Receiving",
        "currentBinsAbbreviated": "Bin 1,Bin 2,Bin 3,Rece...",
        "putawayFacility.id": "ff808181646b260401646b3f2ced0002",
        "putawayFacility.name": "Store 1",
        "putawayLocation.id": "ff808181646d3ec101646d5e7d480001",
        "putawayLocation.name": "Bin 1",
        "quantity": 3
      }
    ]
  }
}
```

## Complete Putaway 
Change the `putawayStatus` of the root object to `COMPLETE` in order to actually complete the 
putaway process. 

NOTE: If you see a transactionNumber associated with each putaway item you will know that it worked.
Do not send this request more than once or else you will create multiple putaways.

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @updatePutaway.json \
"https://openboxes.ngrok.io/openboxes/api/putaways"|jsonlint
```

### Data
```
{
    "putawayNumber":"ABC123",
    "putawayAssignee.id":"1",
    "putawayStatus":"COMPLETE",
    "putawayDate":"07/13/2018",
    "putawayItems":[{
        "putawayStatus":"TODO",
        "currentFacility.id":"ff808181646b260401646b3f2ced0002",
        "currentLocation.id":"ff808181648c6e2401648c95140f0001",
        "product.id":"ff80818155df9de40155df9e31000001",
        "inventoryItem.id":"ff808181646b260401646b495487000d",
        "recipient.id":"1",
        "putawayFacility.id":"ff808181646b260401646b3f2ced0002",
        "putawayLocation.id":"ff808181646d3ec101646d5e7d480001",
        "quantity": 5
    }]
}
```