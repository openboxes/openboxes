# Getting Started

## Authentication

First of all, we're going to need to authenticate. And although not entirely necessary 
it is recommended that you use cURL's `-c` argument in order to create a local cookies 
file (cookies.txt) so that you don't need to keep passing auth headers around on every
request.

``` 
curl -i -c cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"username":"jmiranda","password":"password","location":"1"}' \
https://openboxes.ngrok.io/openboxes/api/login
```


## Using Generic API
Once that's done, let's start with the views that require the read/list APIs like shipments and requisitions. For this we're going to use 
what I'm calling the [Generic API](http://docs.openboxes.com/en/latest/api-guide/generic/). You should be able to handle most (if not all) 
of the basic CRUD operations through the Generic API. 

In general, you have access to the following operations against the Generic API.

* List = GET https://openboxes.ngrok.io/openboxes/api/generic/**resource**
* Read = GET https://openboxes.ngrok.io/openboxes/api/generic/**resource**/:id
* Create = POST https://openboxes.ngrok.io/openboxes/api/generic/**resource**
* Update = PUT (or POST) https://openboxes.ngrok.io/openboxes/api/generic/**resource**/:id
* Delete = DELETE https://openboxes.ngrok.io/openboxes/api/generic/**resource**/:id

where <domain> is any of the domain classes in the system and <id> is the primary key.

Here are some example **resources**:

* Shipment = shipment
* Shipment Item = shipmentItem
* Requisition = requisition
* RequisitionItem = requisitionItem
* Product = product
* InventoryItem = inventoryItem
* Transaction = transaction
* TransactionEntry = transactionEntry


## Get shipments
So let's start off with Shipments by retrieving a list of max = 1 shipments.
NOTE: I've added a paging parameter (max=1) in the following request. You should review the 
[pagination documentation](http://docs.openboxes.com/en/latest/api-guide/#pagination) when you get a chance.
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" https://openboxes.ngrok.io/openboxes/api/generic/shipment?max=1 | jsonlint
{
  "data": [
    {
      "id": "ff808181646b260401646b61df3f0034",
      "name": "shipment to store 1",
      "status": "SHIPPED",
      "origin": {
        "id": "ff8081816430012d0164301b8eda0008",
        "name": "Depot 1",
        "type": "DEPOT"
      },
      "destination": {
        "id": "ff808181646b260401646b3f2ced0002",
        "name": "Store 1",
        "type": "DEPOT"
      },
      "expectedShippingDate": "07/05/2018 00:00",
      "actualShippingDate": "07/05/2018 12:03",
      "expectedDeliveryDate": "07/05/2018 00:00",
      "actualDeliveryDate": null,
      "shipmentItems": [
        {
          "id": "ff808181646b260401646b6256ed0036",
          "inventoryItem": {
            "id": "ff80818163f7308a0163f73d5bda0002",
            "product": {
              "id": "ff80818155df9de40155df9e31000001",
              "name": "Ibuprofen 200mg",
              "productCode": "AB12"
            },
            "lotNumber": "ABC123",
            "expirationDate": "01/01/2021"
          },
          "quantity": 150,
          "recipient": null,
          "shipment": {
            "id": "ff808181646b260401646b61df3f0034",
            "name": "shipment to store 1"
          },
          "container": {
            "id": "ff808181646b260401646b62d1c10037",
            "name": "Box 1\r",
            "type": "Box|fr:Boite"
          }
        }
      ],
      "containers": [
        {
          "id": "ff808181646b260401646b62d1c10037",
          "name": "Box 1\r",
          "type": "Box|fr:Boite",
          "shipmentItems": [
            {
              "id": "ff808181646b260401646b6256ed0036",
              "inventoryItem": {
                "id": "ff80818163f7308a0163f73d5bda0002",
                "product": {
                  "id": "ff80818155df9de40155df9e31000001",
                  "name": "Ibuprofen 200mg",
                  "productCode": "AB12"
                },
                "lotNumber": "ABC123",
                "expirationDate": "01/01/2021"
              },
              "quantity": 150,
              "recipient": null,
              "shipment": {
                "id": "ff808181646b260401646b61df3f0034",
                "name": "shipment to store 1"
              },
              "container": {
                "id": "ff808181646b260401646b62d1c10037",
                "name": "Box 1\r",
                "type": "Box|fr:Boite"
              }
            }
          ]
        }
      ]
    }
  ]
}
```

## Read shipment 

Now let's read the details for a specific shipment.
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/shipment/ff808181646b260401646b61df3f0034|jsonlint
{
  "data": {
    "id": "ff808181646b260401646b61df3f0034",
    "name": "shipment to store 1",
    "status": "SHIPPED",
    "origin": {
      "id": "ff8081816430012d0164301b8eda0008",
      "name": "Depot 1",
      "type": "DEPOT"
    },
    "destination": {
      "id": "ff808181646b260401646b3f2ced0002",
      "name": "Store 1",
      "type": "DEPOT"
    },
    "expectedShippingDate": "07/05/2018 00:00",
    "actualShippingDate": "07/05/2018 12:03",
    "expectedDeliveryDate": "07/05/2018 00:00",
    "actualDeliveryDate": null,
    "shipmentItems": [
      {
        "id": "ff808181646b260401646b6256ed0036",
        "inventoryItem": {
          "id": "ff80818163f7308a0163f73d5bda0002",
          "product": {
            "id": "ff80818155df9de40155df9e31000001",
            "name": "Ibuprofen 200mg",
            "productCode": "AB12"
          },
          "lotNumber": "ABC123",
          "expirationDate": "01/01/2021"
        },
        "quantity": 150,
        "recipient": null,
        "shipment": {
          "id": "ff808181646b260401646b61df3f0034",
          "name": "shipment to store 1"
        },
        "container": {
          "id": "ff808181646b260401646b62d1c10037",
          "name": "Box 1\r",
          "type": "Box|fr:Boite"
        }
      }
    ],
    "containers": [
      {
        "id": "ff808181646b260401646b62d1c10037",
        "name": "Box 1\r",
        "type": "Box|fr:Boite",
        "shipmentItems": [
          {
            "id": "ff808181646b260401646b6256ed0036",
            "inventoryItem": {
              "id": "ff80818163f7308a0163f73d5bda0002",
              "product": {
                "id": "ff80818155df9de40155df9e31000001",
                "name": "Ibuprofen 200mg",
                "productCode": "AB12"
              },
              "lotNumber": "ABC123",
              "expirationDate": "01/01/2021"
            },
            "quantity": 150,
            "recipient": null,
            "shipment": {
              "id": "ff808181646b260401646b61df3f0034",
              "name": "shipment to store 1"
            },
            "container": {
              "id": "ff808181646b260401646b62d1c10037",
              "name": "Box 1\r",
              "type": "Box|fr:Boite"
            }
          }
        ]
      }
    ]
  }
}
```

## Create a new shipment
Pass an empty JSON object to the create method and it'll tell you what fields are required.

* name
* origin
* destination
* expectedShippingDate
* shipmentType

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" -d '{}' \
https://openboxes.ngrok.io/openboxes/api/generic/shipment|jsonlint
{
  "errorCode": 400,
  "errorMessage": "Validation errors",
  "data": [
    {
      "arguments": [
        "destination",
        "Shipment"
      ],
      "bindingFailure": false,
      "class": "org.springframework.validation.FieldError",
      "code": "nullable",
      "codes": [
        "Shipment.destination.nullable.error.Shipment.destination",
        "Shipment.destination.nullable.error.destination",
        "Shipment.destination.nullable.error.Location",
        "Shipment.destination.nullable.error",
        "shipment.destination.nullable.error.Shipment.destination",
        "shipment.destination.nullable.error.destination",
        "shipment.destination.nullable.error.Location",
        "shipment.destination.nullable.error",
        "Shipment.destination.nullable.Shipment.destination",
        "Shipment.destination.nullable.destination",
        "Shipment.destination.nullable.Location",
        "Shipment.destination.nullable",
        "shipment.destination.nullable.Shipment.destination",
        "shipment.destination.nullable.destination",
        "shipment.destination.nullable.Location",
        "shipment.destination.nullable",
        "nullable.Shipment.destination",
        "nullable.destination",
        "nullable.Location",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "destination",
      "objectName": "Shipment",
      "rejectedValue": null
    },
    {
      "arguments": [
        "expectedShippingDate",
        "Shipment"
      ],
      "bindingFailure": false,
      "class": "org.springframework.validation.FieldError",
      "code": "nullable",
      "codes": [
        "Shipment.expectedShippingDate.nullable.error.Shipment.expectedShippingDate",
        "Shipment.expectedShippingDate.nullable.error.expectedShippingDate",
        "Shipment.expectedShippingDate.nullable.error.java.util.Date",
        "Shipment.expectedShippingDate.nullable.error",
        "shipment.expectedShippingDate.nullable.error.Shipment.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.error.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.error.java.util.Date",
        "shipment.expectedShippingDate.nullable.error",
        "Shipment.expectedShippingDate.nullable.Shipment.expectedShippingDate",
        "Shipment.expectedShippingDate.nullable.expectedShippingDate",
        "Shipment.expectedShippingDate.nullable.java.util.Date",
        "Shipment.expectedShippingDate.nullable",
        "shipment.expectedShippingDate.nullable.Shipment.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.java.util.Date",
        "shipment.expectedShippingDate.nullable",
        "nullable.Shipment.expectedShippingDate",
        "nullable.expectedShippingDate",
        "nullable.java.util.Date",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "expectedShippingDate",
      "objectName": "Shipment",
      "rejectedValue": null
    },
    {
      "arguments": [
        "name",
        "Shipment"
      ],
      "bindingFailure": false,
      "class": "org.springframework.validation.FieldError",
      "code": "nullable",
      "codes": [
        "Shipment.name.nullable.error.Shipment.name",
        "Shipment.name.nullable.error.name",
        "Shipment.name.nullable.error.java.lang.String",
        "Shipment.name.nullable.error",
        "shipment.name.nullable.error.Shipment.name",
        "shipment.name.nullable.error.name",
        "shipment.name.nullable.error.java.lang.String",
        "shipment.name.nullable.error",
        "Shipment.name.nullable.Shipment.name",
        "Shipment.name.nullable.name",
        "Shipment.name.nullable.java.lang.String",
        "Shipment.name.nullable",
        "shipment.name.nullable.Shipment.name",
        "shipment.name.nullable.name",
        "shipment.name.nullable.java.lang.String",
        "shipment.name.nullable",
        "nullable.Shipment.name",
        "nullable.name",
        "nullable.java.lang.String",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "name",
      "objectName": "Shipment",
      "rejectedValue": null
    },
    {
      "arguments": [
        "origin",
        "Shipment"
      ],
      "bindingFailure": false,
      "class": "org.springframework.validation.FieldError",
      "code": "nullable",
      "codes": [
        "Shipment.origin.nullable.error.Shipment.origin",
        "Shipment.origin.nullable.error.origin",
        "Shipment.origin.nullable.error.Location",
        "Shipment.origin.nullable.error",
        "shipment.origin.nullable.error.Shipment.origin",
        "shipment.origin.nullable.error.origin",
        "shipment.origin.nullable.error.Location",
        "shipment.origin.nullable.error",
        "Shipment.origin.nullable.Shipment.origin",
        "Shipment.origin.nullable.origin",
        "Shipment.origin.nullable.Location",
        "Shipment.origin.nullable",
        "shipment.origin.nullable.Shipment.origin",
        "shipment.origin.nullable.origin",
        "shipment.origin.nullable.Location",
        "shipment.origin.nullable",
        "nullable.Shipment.origin",
        "nullable.origin",
        "nullable.Location",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "origin",
      "objectName": "Shipment",
      "rejectedValue": null
    },
    {
      "arguments": [
        "shipmentType",
        "Shipment"
      ],
      "bindingFailure": false,
      "class": "org.springframework.validation.FieldError",
      "code": "nullable",
      "codes": [
        "Shipment.shipmentType.nullable.error.Shipment.shipmentType",
        "Shipment.shipmentType.nullable.error.shipmentType",
        "Shipment.shipmentType.nullable.error.ShipmentType",
        "Shipment.shipmentType.nullable.error",
        "shipment.shipmentType.nullable.error.Shipment.shipmentType",
        "shipment.shipmentType.nullable.error.shipmentType",
        "shipment.shipmentType.nullable.error.ShipmentType",
        "shipment.shipmentType.nullable.error",
        "Shipment.shipmentType.nullable.Shipment.shipmentType",
        "Shipment.shipmentType.nullable.shipmentType",
        "Shipment.shipmentType.nullable.ShipmentType",
        "Shipment.shipmentType.nullable",
        "shipment.shipmentType.nullable.Shipment.shipmentType",
        "shipment.shipmentType.nullable.shipmentType",
        "shipment.shipmentType.nullable.ShipmentType",
        "shipment.shipmentType.nullable",
        "nullable.Shipment.shipmentType",
        "nullable.shipmentType",
        "nullable.ShipmentType",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "shipmentType",
      "objectName": "Shipment",
      "rejectedValue": null
    }
  ]
}
```