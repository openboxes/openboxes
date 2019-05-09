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
          "name": "Acetaminophen 325mg",
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

