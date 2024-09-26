# Receiving API

## Get Partial Receipt Candidates

### Request
```
curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/partialReceiving/ff808181646b260401646b61df3f0034"|jsonlint
```
### Response
```
{
  "data": {
    "receipt.id": null,
    "receiptStatus": "PENDING",
    "shipment.id": "ff808181646b260401646b61df3f0034",
    "shipment.name": "shipment to store 1",
    "shipment.shipmentNumber": "110VZU",
    "shipmentStatus": "SHIPPED",
    "origin.id": "ff8081816430012d0164301b8eda0008",
    "origin.name": "Depot 1",
    "destination.id": "ff808181646b260401646b3f2ced0002",
    "destination.name": "Store 1",
    "dateShipped": "2018-07-05T17:03:00Z",
    "dateDelivered": null,
    "containers": [
      {
        "container.id": "ff808181646b260401646b62d1c10037",
        "container.name": "Box 1\r",
        "container.type": "Box|fr:Boite",
        "shipmentItems": [
          {
            "shipmentItem.id": "ff808181646b260401646b6256ed0036",
            "container.id": "ff808181646b260401646b62d1c10037",
            "container.name": "Box 1\r",
            "product.id": "ff80818155df9de40155df9e31000001",
            "product.productCode": "AB12",
            "product.name": "Ibuprofen 200mg",
            "inventoryItem.id": "ff80818163f7308a0163f73d5bda0002",
            "inventoryItem.lotNumber": "ABC123",
            "inventoryItem.expirationDate": "01/01/2021",
            "binLocation.id": null,
            "binLocation.name": null,
            "recipient.id": null,
            "recipient.name": null,
            "quantityShipped": 150,
            "quantityReceived": 0,
            "quantityReceiving": 0,
            "quantityRemaining": 150,
            "cancelRemaining": false
          }
        ]
      }
    ]
  }
}

```

## Create Partial Receipt 
This endpoint does not save to the database unless the `receiptStatus` is set to `COMPLETE`. 
That way you can save a draft before checking the receipt and completing it

### Request
```
curl -b cookies.txt -X POST -H "Content-Type: application/json" -d @partialReceiving.json \
"https://openboxes.ngrok.io/openboxes/api/partialReceiving/ff808181646b260401646b61df3f0034"|jsonlint
```
### Data
```
{
    "shipment.id":"ff808181646b260401646b61df3f0034",
    "receiptStatus":"PENDING",
    "dateShipped": "07/22/2018",
    "dateDelivered": "07/24/2018",
    "containers":[{
        "container.id": "ff808181646b260401646b62d1c10037",
        "container.name": "Box 1\r",
        "container.type": "Box|fr:Boite",
        "shipmentItems":[{
            "shipmentItem.id": "ff808181646b260401646b6256ed0036",
            "quantityReceiving": 50,
            "binLocation.id": "ff808181646d3ec101646d5e7d480001",
            "recipient.id": "1",
            "cancelRemaining":false
        }]
    }]  
}


```
### Response
```
{
  "data": {
    "receipt.id": null,
    "receiptStatus": "PENDING",
    "shipment.id": "ff808181646b260401646b61df3f0034",
    "shipment.name": "shipment to store 1",
    "shipment.shipmentNumber": "110VZU",
    "shipmentStatus": "SHIPPED",
    "origin.id": "ff8081816430012d0164301b8eda0008",
    "origin.name": "Depot 1",
    "destination.id": "ff808181646b260401646b3f2ced0002",
    "destination.name": "Store 1",
    "dateShipped": "2018-07-05T17:03:00Z",
    "dateDelivered": "2018-07-24T05:00:00Z",
    "containers": [
      {
        "container.id": "ff808181646b260401646b62d1c10037",
        "container.name": "Box 1\r",
        "container.type": "Box|fr:Boite",
        "shipmentItems": [
          {
            "shipmentItem.id": "ff808181646b260401646b6256ed0036",
            "container.id": "ff808181646b260401646b62d1c10037",
            "container.name": "Box 1\r",
            "product.id": "ff80818155df9de40155df9e31000001",
            "product.productCode": "AB12",
            "product.name": "Ibuprofen 200mg",
            "inventoryItem.id": "ff80818163f7308a0163f73d5bda0002",
            "inventoryItem.lotNumber": "ABC123",
            "inventoryItem.expirationDate": "01/01/2021",
            "binLocation.id": "ff808181646d3ec101646d5e7d480001",
            "binLocation.name": "Bin 1",
            "recipient.id": "1",
            "recipient.name": "Mr Administrator",
            "quantityShipped": 150,
            "quantityReceived": 0,
            "quantityReceiving": 50,
            "quantityRemaining": 100,
            "cancelRemaining": false
          }
        ]
      }
    ]
  }
}


```

## Complete Partial Receipt
Saves the partial receipt to the database and creates an inbound transaction for the receipt. 
NOTE: The response is just a `GET /api/partialReceiving` for the remaining items. The items
won't remember previous quantities and bin locations selected.

### Request
```
curl -b cookies.txt -X POST -H "Content-Type: application/json" -d @partialReceiving.json \
"https://openboxes.ngrok.io/openboxes/api/partialReceiving/ff808181646b260401646b61df3f0034"|jsonlint
```

### Data (partialReceiving.json)
```
{
    "shipment.id":"ff808181646b260401646b61df3f0034",
    "receiptStatus":"COMPLETE",
    "dateShipped": "07/22/2018",
    "dateDelivered": "07/24/2018",
    "containers":[{
        "container.id": "ff808181646b260401646b62d1c10037",
        "container.name": "Box 1\r",
        "container.type": "Box|fr:Boite",
        "shipmentItems":[{
            "shipmentItem.id": "ff808181646b260401646b6256ed0036",
            "quantityReceiving": 50,
            "binLocation.id": "ff808181646d3ec101646d5e7d480001",
            "recipient.id": "1",
            "cancelRemaining":false
        }]
    }]  
}

```

### Response
```
{
  "data": {
    "receipt.id": null,
    "receiptStatus": "PENDING",
    "shipment.id": "ff808181646b260401646b61df3f0034",
    "shipment.name": "shipment to store 1",
    "shipment.shipmentNumber": "110VZU",
    "shipmentStatus": "SHIPPED",
    "origin.id": "ff8081816430012d0164301b8eda0008",
    "origin.name": "Depot 1",
    "destination.id": "ff808181646b260401646b3f2ced0002",
    "destination.name": "Store 1",
    "dateShipped": "2018-07-05T17:03:00Z",
    "dateDelivered": null,
    "containers": [
      {
        "container.id": "ff808181646b260401646b62d1c10037",
        "container.name": "Box 1\r",
        "container.type": "Box|fr:Boite",
        "shipmentItems": [
          {
            "shipmentItem.id": "ff808181646b260401646b6256ed0036",
            "container.id": "ff808181646b260401646b62d1c10037",
            "container.name": "Box 1\r",
            "product.id": "ff80818155df9de40155df9e31000001",
            "product.productCode": "AB12",
            "product.name": "Ibuprofen 200mg",
            "inventoryItem.id": "ff80818163f7308a0163f73d5bda0002",
            "inventoryItem.lotNumber": "ABC123",
            "inventoryItem.expirationDate": "01/01/2021",
            "binLocation.id": null,
            "binLocation.name": null,
            "recipient.id": null,
            "recipient.name": null,
            "quantityShipped": 150,
            "quantityReceived": 50,
            "quantityReceiving": 0,
            "quantityRemaining": 100,
            "cancelRemaining": false
          }
        ]
      }
    ]
  }
}

```

## Rollback Partial Receipts 

### Request
```
curl -b cookies.txt -X POST -H "Content-Type: application/json" -d @partialReceivingRollback.json \
"https://openboxes.ngrok.io/openboxes/api/partialReceiving/ff808181646b260401646b61df3f0034"|jsonlint
```

### Data (partialReceivingRollback.json)
```
{
    "receiptStatus":"ROLLBACK"
}
```


### Response
```
{
  "data": {
    "receipt.id": null,
    "receiptStatus": "PENDING",
    "shipment.id": "ff808181646b260401646b61df3f0034",
    "shipment.name": "shipment to store 1",
    "shipment.shipmentNumber": "110VZU",
    "shipmentStatus": "SHIPPED",
    "origin.id": "ff8081816430012d0164301b8eda0008",
    "origin.name": "Depot 1",
    "destination.id": "ff808181646b260401646b3f2ced0002",
    "destination.name": "Store 1",
    "dateShipped": "2018-07-05T17:03:00Z",
    "dateDelivered": null,
    "containers": [
      {
        "container.id": "ff808181646b260401646b62d1c10037",
        "container.name": "Box 1\r",
        "container.type": "Box|fr:Boite",
        "shipmentItems": [
          {
            "shipmentItem.id": "ff808181646b260401646b6256ed0036",
            "container.id": "ff808181646b260401646b62d1c10037",
            "container.name": "Box 1\r",
            "product.id": "ff80818155df9de40155df9e31000001",
            "product.productCode": "AB12",
            "product.name": "Ibuprofen 200mg",
            "inventoryItem.id": "ff80818163f7308a0163f73d5bda0002",
            "inventoryItem.lotNumber": "ABC123",
            "inventoryItem.expirationDate": "01/01/2021",
            "binLocation.id": null,
            "binLocation.name": null,
            "recipient.id": null,
            "recipient.name": null,
            "quantityShipped": 150,
            "quantityReceived": 0,
            "quantityReceiving": 0,
            "quantityRemaining": 150,
            "cancelRemaining": false
          }
        ]
      }
    ]
  }
}

```

