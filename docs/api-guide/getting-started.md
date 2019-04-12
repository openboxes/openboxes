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
        "org.pih.warehouse.shipping.Shipment.destination.nullable.error.org.pih.warehouse.shipping.Shipment.destination",
        "org.pih.warehouse.shipping.Shipment.destination.nullable.error.destination",
        "org.pih.warehouse.shipping.Shipment.destination.nullable.error.org.pih.warehouse.core.Location",
        "org.pih.warehouse.shipping.Shipment.destination.nullable.error",
        "shipment.destination.nullable.error.org.pih.warehouse.shipping.Shipment.destination",
        "shipment.destination.nullable.error.destination",
        "shipment.destination.nullable.error.org.pih.warehouse.core.Location",
        "shipment.destination.nullable.error",
        "org.pih.warehouse.shipping.Shipment.destination.nullable.org.pih.warehouse.shipping.Shipment.destination",
        "org.pih.warehouse.shipping.Shipment.destination.nullable.destination",
        "org.pih.warehouse.shipping.Shipment.destination.nullable.org.pih.warehouse.core.Location",
        "org.pih.warehouse.shipping.Shipment.destination.nullable",
        "shipment.destination.nullable.org.pih.warehouse.shipping.Shipment.destination",
        "shipment.destination.nullable.destination",
        "shipment.destination.nullable.org.pih.warehouse.core.Location",
        "shipment.destination.nullable",
        "nullable.org.pih.warehouse.shipping.Shipment.destination",
        "nullable.destination",
        "nullable.org.pih.warehouse.core.Location",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "destination",
      "objectName": "org.pih.warehouse.shipping.Shipment",
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
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable.error.org.pih.warehouse.shipping.Shipment.expectedShippingDate",
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable.error.expectedShippingDate",
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable.error.java.util.Date",
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable.error",
        "shipment.expectedShippingDate.nullable.error.org.pih.warehouse.shipping.Shipment.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.error.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.error.java.util.Date",
        "shipment.expectedShippingDate.nullable.error",
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable.org.pih.warehouse.shipping.Shipment.expectedShippingDate",
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable.expectedShippingDate",
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable.java.util.Date",
        "org.pih.warehouse.shipping.Shipment.expectedShippingDate.nullable",
        "shipment.expectedShippingDate.nullable.org.pih.warehouse.shipping.Shipment.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.expectedShippingDate",
        "shipment.expectedShippingDate.nullable.java.util.Date",
        "shipment.expectedShippingDate.nullable",
        "nullable.org.pih.warehouse.shipping.Shipment.expectedShippingDate",
        "nullable.expectedShippingDate",
        "nullable.java.util.Date",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "expectedShippingDate",
      "objectName": "org.pih.warehouse.shipping.Shipment",
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
        "org.pih.warehouse.shipping.Shipment.name.nullable.error.org.pih.warehouse.shipping.Shipment.name",
        "org.pih.warehouse.shipping.Shipment.name.nullable.error.name",
        "org.pih.warehouse.shipping.Shipment.name.nullable.error.java.lang.String",
        "org.pih.warehouse.shipping.Shipment.name.nullable.error",
        "shipment.name.nullable.error.org.pih.warehouse.shipping.Shipment.name",
        "shipment.name.nullable.error.name",
        "shipment.name.nullable.error.java.lang.String",
        "shipment.name.nullable.error",
        "org.pih.warehouse.shipping.Shipment.name.nullable.org.pih.warehouse.shipping.Shipment.name",
        "org.pih.warehouse.shipping.Shipment.name.nullable.name",
        "org.pih.warehouse.shipping.Shipment.name.nullable.java.lang.String",
        "org.pih.warehouse.shipping.Shipment.name.nullable",
        "shipment.name.nullable.org.pih.warehouse.shipping.Shipment.name",
        "shipment.name.nullable.name",
        "shipment.name.nullable.java.lang.String",
        "shipment.name.nullable",
        "nullable.org.pih.warehouse.shipping.Shipment.name",
        "nullable.name",
        "nullable.java.lang.String",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "name",
      "objectName": "org.pih.warehouse.shipping.Shipment",
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
        "org.pih.warehouse.shipping.Shipment.origin.nullable.error.org.pih.warehouse.shipping.Shipment.origin",
        "org.pih.warehouse.shipping.Shipment.origin.nullable.error.origin",
        "org.pih.warehouse.shipping.Shipment.origin.nullable.error.org.pih.warehouse.core.Location",
        "org.pih.warehouse.shipping.Shipment.origin.nullable.error",
        "shipment.origin.nullable.error.org.pih.warehouse.shipping.Shipment.origin",
        "shipment.origin.nullable.error.origin",
        "shipment.origin.nullable.error.org.pih.warehouse.core.Location",
        "shipment.origin.nullable.error",
        "org.pih.warehouse.shipping.Shipment.origin.nullable.org.pih.warehouse.shipping.Shipment.origin",
        "org.pih.warehouse.shipping.Shipment.origin.nullable.origin",
        "org.pih.warehouse.shipping.Shipment.origin.nullable.org.pih.warehouse.core.Location",
        "org.pih.warehouse.shipping.Shipment.origin.nullable",
        "shipment.origin.nullable.org.pih.warehouse.shipping.Shipment.origin",
        "shipment.origin.nullable.origin",
        "shipment.origin.nullable.org.pih.warehouse.core.Location",
        "shipment.origin.nullable",
        "nullable.org.pih.warehouse.shipping.Shipment.origin",
        "nullable.origin",
        "nullable.org.pih.warehouse.core.Location",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "origin",
      "objectName": "org.pih.warehouse.shipping.Shipment",
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
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable.error.org.pih.warehouse.shipping.Shipment.shipmentType",
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable.error.shipmentType",
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable.error.org.pih.warehouse.shipping.ShipmentType",
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable.error",
        "shipment.shipmentType.nullable.error.org.pih.warehouse.shipping.Shipment.shipmentType",
        "shipment.shipmentType.nullable.error.shipmentType",
        "shipment.shipmentType.nullable.error.org.pih.warehouse.shipping.ShipmentType",
        "shipment.shipmentType.nullable.error",
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable.org.pih.warehouse.shipping.Shipment.shipmentType",
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable.shipmentType",
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable.org.pih.warehouse.shipping.ShipmentType",
        "org.pih.warehouse.shipping.Shipment.shipmentType.nullable",
        "shipment.shipmentType.nullable.org.pih.warehouse.shipping.Shipment.shipmentType",
        "shipment.shipmentType.nullable.shipmentType",
        "shipment.shipmentType.nullable.org.pih.warehouse.shipping.ShipmentType",
        "shipment.shipmentType.nullable",
        "nullable.org.pih.warehouse.shipping.Shipment.shipmentType",
        "nullable.shipmentType",
        "nullable.org.pih.warehouse.shipping.ShipmentType",
        "nullable"
      ],
      "defaultMessage": "Property [{0}] of class [{1}] cannot be null",
      "field": "shipmentType",
      "objectName": "org.pih.warehouse.shipping.Shipment",
      "rejectedValue": null
    }
  ]
}
```