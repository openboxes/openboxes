# Stock Movement Status
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

## Read
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" "https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
{
  "data": "CHECKING"
}
```

## Create
Not supported at this time.

## Delete 
The DELETE action allows you to rollback the current status for a stock movement. 
```
$ curl -b cookies.txt -X DELETE -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
```

## Update 

### Supported request parameters
* `stepNumber`: Allows you to customize the JSON response for the step number. In the case you are 
tranisitioning to the `PICKING` state, you may want to change the step number to 4 so that the JSON
response is customized for that page. If you don't provide the step number you'll get the default
JSON response. At the moment, only `stepNumber=4` returns a custom JSON response. But this will likely
change in a future version.

### Supported attributes
* `status`: The status you want to transition to.
* `statusOnly`: Allows you to change the status only (not apply business logic for the transition). 
Useful for testing and/or getting a stock movement back into a known state.
* `rollback`: Allows you to rollback the current status (see Delete).

### Supported attributes related to the PICKING state
* `clearPicklist`: Allows you to clear the picklist in case you want to start fresh.
* `createPicklist`: Allows you to auto-generate a picklist when entering the PICKING state.

### Created
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
          "name": "Acetaminophen 325mg",
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
### Editing
After creating the stock movement you are brought to the `EDITING` state (step 2) of the workflow which allows you
to edit the line items. Honestly, there's no requirement to move into this state from the current UI
so this state can probably be ignored. 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"EDITING"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status?stepNumber=2"|jsonlint
```
### Verifying
Once items have been added to the stock movement you'll move into the `VERIFYING` state (step 3) which 
allows you to revise quantity, substitute items, cancel items, and generally 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"VERIFYING"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status?stepNumber=3"|jsonlint
```
### Picking
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
### Picked
Once you have finished picking items for the stock movement, you'll move to the `PICKED` state (step 5). 
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"PICKED"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
```
### Issued
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
### Canceled
If at any point you'd like to cancel the stock movement you can transition to the `CANCELED` state.
If the stock movement is canceled after it has been moved to the `ISSUED` state then any transaction 
created as part of the stock movement will be reverted and the stock movement will be transitioned
to the `CANCELED` state.
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"status":"CANCELED"}' \
"https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status"|jsonlint
```


## Exceptions

### Cannot re-issue an ISSUED stock movement
If you really need to rollback the ISSUED state, you'll need to use the `Rollback Status` feature.
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" -d '{"status":"ISSUED"}' "https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181646b260401646b5bf4ca002a/status?stepNumber=5"|jsonlint
{
  "errorCode": 400,
  "exception": "grails.validation.ValidationException",
  "errorMessage": "Validation errors",
  "data": [
    {
      "arguments": null,
      "class": "org.springframework.validation.ObjectError",
      "code": "shipment.invalid.alreadyShipped",
      "codes": [
        "shipment.invalid.alreadyShipped.org.pih.warehouse.shipping.Shipment",
        "shipment.invalid.alreadyShipped"
      ],
      "defaultMessage": "Shipment has already shipped",
      "objectName": "org.pih.warehouse.shipping.Shipment"
    }
  ]
}
```
