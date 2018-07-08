[TOC]

# Stock Movements

## List 
Return stock movements 

Parameter | Description | Required
--- | --- | ---
**max** | Limits the number of records in response | Optional
**offset** | Indicate the first record to return | Optional
**exclude** | Indicate which fields you'd like to exclude from the response | Optional

```
$ curl -b cookies.txt -X GET \
-H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/stockMovements?max=1&offset=2&exclude=lineItems|jsonlint
{
  "data": [
    {
      "id": "ff808181644d5e5b01644e5007500001",
      "name": "new stock movement",
      "description": "new stock movement",
      "identifier": "483ZSA",
      "origin": {
        "id": "1",
        "name": "Boston Headquarters"
      },
      "destination": {
        "id": "2",
        "name": "Miami Warehouse"
      },
      "dateRequested": "06/23/2018",
      "requestedBy": {
        "id": "1",
        "name": "Mr Administrator",
        "firstName": "Mr",
        "lastName": "Administrator",
        "email": "admin@pih.org",
        "username": "admin"
      }
    }
  ]
}


```



## Read
Read an existing stock movement
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001"|jsonlint 
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "new stock movement",
    "description": "new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "1",
      "name": "Boston Headquarters"
    },
    "destination": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "dateRequested": "06/23/2018",
    "requestedBy": {
      "id": "1",
      "name": "Mr Administrator",
      "firstName": "Mr",
      "lastName": "Administrator",
      "email": "admin@pih.org",
      "username": "admin"
    },
    "lineItems": []
  }
}

```

## Create 

### Create a new stock movement
```
curl -X POST -b cookies.txt -H "Content-Type: application/json" \
-d '{"name":"my new stock movement", "description":"same as name", "origin.id":"1", "destination.id":"2","requestedBy.id":"1","dateRequested":"06/23/2018"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements" | jsonlint 
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "",
    "identifier": null,
    "origin": {
      "id": "1",
      "name": "Boston Headquarters"
    },
    "destination": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "dateRequested": "06/23/2018",
    "requestedBy": {
      "id": "1",
      "name": "Mr Administrator",
      "firstName": "Mr",
      "lastName": "Administrator",
      "email": "admin@pih.org",
      "username": "admin"
    },
    "lineItems": []
  }
}

```

### Create a new stock movement for inbound shipment
```
$ curl -X POST -b cookies.txt -H "Content-Type: application/json" \
-d '{"name":"new stock movement", "description":"same as name", "origin.id":"ff80818155dd68010155dd6bb9c00001", "destination.id":"2","requestedBy.id":"1","dateRequested":"06/23/2018"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements" | jsonlint 
{
  "data": {
    "id": "ff808181644e51a401644e5a916f0005",
    "name": "new stock movement",
    "description": "",
    "identifier": null,
    "origin": {
      "id": "ff80818155dd68010155dd6bb9c00001",
      "name": "Test Supplier"
    },
    "destination": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "dateRequested": "06/23/2018",
    "requestedBy": {
      "id": "1",
      "name": "Mr Administrator",
      "firstName": "Mr",
      "lastName": "Administrator",
      "email": "admin@pih.org",
      "username": "admin"
    },
    "lineItems": []
  }
}

```

### Create a new stock movement based on a stocklist
```
curl -X POST -b cookies.txt -H "Content-Type: application/json" \
-d '{"name":"stock movement based on stocklist", "description":"same as name", "origin.id":"1", "destination.id":"2","requestedBy.id":"1","dateRequested":"06/23/2018","stocklist.id":"ff808181641b2fd501641b39f4ef0001"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements" | jsonlint 
{
  "data": {
    "id": "ff808181644e51a401644e5838aa0001",
    "name": "stock movement based on stocklist",
    "description": "stock movement based on stocklist",
    "identifier": null,
    "origin": {
      "id": "1",
      "name": "Boston Headquarters"
    },
    "destination": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "dateRequested": "06/23/2018",
    "requestedBy": {
      "id": "1",
      "name": "Mr Administrator",
      "firstName": "Mr",
      "lastName": "Administrator",
      "email": "admin@pih.org",
      "username": "admin"
    },
    "lineItems": [
      {
        "id": "ff808181644e51a401644e5838ad0002",
        "productCode": "00005",
        "product": {
          "id": "ff80818155df9de40155df9e33930011",
          "productCode": "00005",
          "name": "Similac Advance low iron 400g",
          "description": null,
          "category": {
            "id": "1",
            "name": "Medicines"
          }
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "PENDING",
        "quantityRequested": 25,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": null,
        "quantityRevised": null,
        "reasonCode": null,
        "comments": null,
        "recipient": null,
        "sortOrder": 2
      },
      {
        "id": "ff808181644e51a401644e5838ad0003",
        "productCode": "00001",
        "product": {
          "id": "ff80818155df9de40155df9e31000001",
          "productCode": "00001",
          "name": "Ibuprofen 200mg",
          "description": null,
          "category": {
            "id": "1",
            "name": "Medicines"
          }
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "PENDING",
        "quantityRequested": 100,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": null,
        "quantityRevised": null,
        "reasonCode": null,
        "comments": null,
        "recipient": null,
        "sortOrder": 0
      },
      {
        "id": "ff808181644e51a401644e5838ad0004",
        "productCode": "00002",
        "product": {
          "id": "ff80818155df9de40155df9e321c0005",
          "productCode": "00002",
          "name": "Tylenol 325mg",
          "description": null,
          "category": {
            "id": "1",
            "name": "Medicines"
          }
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "PENDING",
        "quantityRequested": 50,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": null,
        "quantityRevised": null,
        "reasonCode": null,
        "comments": null,
        "recipient": null,
        "sortOrder": 1
      }
    ]
  }
}

```

## Update

### Update Stock Movement

```
curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"name":"new stock movement", "description":"new stock movement", "origin.id":"1", "destination.id":"2","requestedBy.id":"1","dateRequested":"06/23/2018"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181642fc9c101642fcccc420004" \
| jsonlint
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "new stock movement",
    "description": "new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "1",
      "name": "Boston Headquarters"
    },
    "destination": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "dateRequested": "06/23/2018",
    "requestedBy": {
      "id": "1",
      "name": "Mr Administrator",
      "firstName": "Mr",
      "lastName": "Administrator",
      "email": "admin@pih.org",
      "username": "admin"
    },
    "lineItems": []
  }
}
```

### Update Status
For stock movements, the Status API provides a way to transition the stock movement between states. 
The API is currently only configured to change the stock movement status and return the JSON required for the 
page associated with the next status. For example, moving from `REVIEWING` to `PICKING` would return the
customized JSON response required for the Picking stage of the stock movement workflow.

In future versions we'll add the ability to validate the stock movement between transitions as well as 
custom business logic related to the transition. For example in the transition to the `ISSUED` state, 
we'll want that to trigger a shipment to be sent if the stock movement has been fully picked.

The data returned by Status API is the same data returned by a GET request on the Stock Movement API.
Therefore, you can use the `stepNumber` parameter if you want to transform the response data for a 
specific step. At, the moment only `?stepNumber=4` transforms the data, but that might change in
a later version.

#### Created
The first state in the stock movement lifecycle is the `CREATED` state. You can also transition to `PENDING`
`OPEN` if that's more clear to your users. 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"CREATED"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
{
  "data": {
    "id": "ff808181646b260401646b5bf4ca002a",
    "name": "Stock - Store 1 - Mixed - Jul 05 2018",
    "description": null,
    "statusCode": "CREATED",
    "identifier": "916SUB",
    "origin": {
      "id": "ff808181646b260401646b3f2ced0002",
      "name": "Store 1"
    },
    "destination": {
      "id": "ff8081816430012d0164301b8eda0008",
      "name": "Depot 1"
    },
    "dateRequested": "07/05/2018",
    "requestedBy": {
      "id": "3",
      "name": "Justin Miranda",
      "firstName": "Justin",
      "lastName": "Miranda",
      "email": "jmiranda@pih.org",
      "username": "jmiranda"
    },
    "lineItems": [
      {
        "id": "ff808181646b260401646b5bf4cb002b",
        "productCode": "AB12",
        "product": {
          "id": "ff80818155df9de40155df9e31000001",
          "productCode": "AB12",
          "name": "Ibuprofen 200mg",
          "description": null,
          "category.id": "1",
          "category.name": "Medicines"
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "APPROVED",
        "quantityRequested": 25,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": null,
        "quantityRevised": null,
        "quantityPicked": 0,
        "reasonCode": null,
        "comments": null,
        "recipient": null,
        "substitutionItems": [],
        "sortOrder": 0
      },
      {
        "id": "ff808181646b260401646b5bf4cb002c",
        "productCode": "00002",
        "product": {
          "id": "ff80818155df9de40155df9e321c0005",
          "productCode": "00002",
          "name": "Tylenol 325mg",
          "description": null,
          "category.id": "1",
          "category.name": "Medicines"
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "CANCELED",
        "quantityRequested": 50,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": 50,
        "quantityRevised": null,
        "quantityPicked": 0,
        "reasonCode": "STOCKOUT",
        "comments": "temporary stock out",
        "recipient": null,
        "substitutionItems": [],
        "sortOrder": 1
      },
      {
        "id": "ff8081816472adba016472cc9b460001",
        "productCode": "00005",
        "product": {
          "id": "ff80818155df9de40155df9e33930011",
          "productCode": "00005",
          "name": "Similac Advance low iron 400g",
          "description": null,
          "category.id": "1",
          "category.name": "Medicines"
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "PENDING",
        "quantityRequested": 20,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": null,
        "quantityRevised": null,
        "quantityPicked": 0,
        "reasonCode": null,
        "comments": null,
        "recipient": null,
        "substitutionItems": [],
        "sortOrder": 2
      }
    ],
    "pickPage": null
  }

```
#### Editing
After creating the stock movement you are brought to the `EDITING` state (step 2) of the workflow which allows you
to edit the line items. Honestly, there's no requirement to move into this state from the current UI
so this state can probably be ignored. 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"EDITING"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status?stepNumber=2"|jsonlint
```
#### Verifying
Once items have been added to the stock movement you'll move into the `VERIFYING` state (step 3) which 
allows you to revise quantity, substitute items, cancel items, and generally 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"VERIFYING"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status?stepNumber=3"|jsonlint
```
#### Picking
The `PICKING` state (step 4) is the one state that currently has custom business logic associated with it. 
To trigger this business logic, you can choose to include optional attributes `"clearPicklist":"true"` or 
`"createPicklist":"true"` to your JSON body in order to, respectively: clear the current picklist of all 
items or automatically created a picklist and fill it with suggested items. 

NOTE: We use a first-expiry-first-out (FEFO) algorithm for stock picking. In the future, this might be 
configurable by product.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"PICKING", "createPicklist":"true"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status?stepNumber=4"|jsonlint
```
#### Picked
Once you have finished picking items for the stock movement, you'll move to the `PICKED` state (step 5). 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"PICKED"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
```
#### Issued
In the `PICKED` state you will be prompoted to enter information about the shipment used to send
stock to the destination. Once this information has been filled out and saved to the database, 
you can transitition to the `ISSUED` state which will attempt to send the stock movement to the 
`destination` as a shipment. As you can see in the response below, this state transition has not
been implemented yet.
```
curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"ISSUED"}' 
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status?stepNumber=6"
{
  "errorCode": 500,
  "errorMessage": "Cannot send stock movement 916SUB - method has not been implemented yet"
}
```
#### Canceled
If at any point you'd like to cancel the stock movement you can transition to the `CANCELED` state.
If the stock movement is canceled after it has been moved to the `ISSUED` state then any transaction 
created as part of the stock movement will be reverted and the stock movement will be transitioned
to the `CANCELED` state.
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"CANCELED"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
```
### Rollback Status
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"rollback":"true"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
```
