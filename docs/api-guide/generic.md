# Using the generic API

**Table of contents**

- [List](#list)

- [Read](#read)

- [Create](#create)

	- [Create multiple products at once](#create-multiple-products-at-once)

- [Update](#update)

	- [Update multiple products at once](#update-multiple-products-at-once)

- [Get a partial list of shipments](#get-a-partial-list-of-shipments)

- [Read shipment details](#read-shipment-details)

- [Ask what fields a new shipment requires](#ask-what-fields-a-new-shipment-requires)

---

**NOTE**: *You must [authenticate](./authentication.md) before you can use the generic API*.

The generic API lets developers access any domain object without rewriting the boilerplate code. It has five operations:

- List
	- `GET https://openboxes.ngrok.io/openboxes/api/generic/**resource**`
- Read
	- `GET https://openboxes.ngrok.io/openboxes/api/generic/**resource**/:id`
- Create
	- `POST https://openboxes.ngrok.io/openboxes/api/generic/**resource**`
- Update
	- `POST https://openboxes.ngrok.io/openboxes/api/generic/**resource**/:id`
	- OR `PUT https://openboxes.ngrok.io/openboxes/api/generic/**resource**/:id`
- Delete
	- `DELETE https://openboxes.ngrok.io/openboxes/api/generic/**resource**/:id`

**NOTE**: `:id` is the primary key.

Here are some example **resource**s:

* Shipment = `shipment`
* Shipment Item = `shipmentItem`
* Requisition = `requisition`
* RequisitionItem = `requisitionItem`
* Product = `product`
* InventoryItem = `inventoryItem`
* Transaction = `transaction`
* TransactionEntry = `transactionEntry`

Let's go over each operation.

## List

Use this command to return a list of products.

```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product | jsonlint
```

Output:

```
{
  "data": [
    {
      "id": "ff80818155df9de40155df9e31000001",
      "productCode": "00001",
      "name": "Ibuprofen 200mg",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e321c0005",
      "productCode": "00002",
      "name": "Acetaminophen 325mg",
      "description": null,
      "category": {
        "id": "1",
        "name": "Medicines"
      }
    },
    {
      "id": "ff80818155df9de40155df9e347e0019",
      "productCode": "00007",
      "name": "MacBook Pro 8G",
      "description": null,
      "category": {
        "id": "2",
        "name": "Computer Equipment"
      }
	
	...

    }
  ]
}
```

## Read 

Use this command to access specific product information.

```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff80818155df9de40155df9e31000001
```

Output:

```
{
	"data": {
		"id": "ff80818155df9de40155df9e31000001",
		"productCode": "00001",
		"name": "Ibuprofen 200mg",
		"description": null,
		"category": {
			"id": "1",
			"name": "Medicines"
		}
	}
}
```

## Create

Use this command to create a new product.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"name":"product 0", "category.id":"ROOT"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

Output:

```
{
	"data": {
		"id": "ff8081816407132d01640730bd150003",
		"productCode": null,
		"name": "product 0",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}
}
```

### Create multiple products at once

`POST` to the same endpoint to create two or more products at once.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1","category.id":"ROOT"},{"name":"product 2","category.id":"ROOT"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

Output:

```
{
	"data": [{
		"id": "ff8081816407132d0164071eec250001",
		"productCode": null,
		"name": "product 1",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}, {
		"id": "ff8081816407132d0164071eec2d0002",
		"productCode": null,
		"name": "product 2",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}]
}
```

**NOTE**: the `POST` request on multiple objects fails if there is a single error. However, cURL throws an error for only the first object that fails.

This cURL command produces an error.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1"},{"name":"product 2"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

And this is the error it produces.

```
{
	"errorCode": 400,
	"errorMessage": "Validation errors",
	"data": {
		"errors": [{
			"object": "Product",
			"field": "category",
			"rejected-value": null,
			"message": "Property [category] of class [Product] cannot be null"
		}]
	}
}
```

## Update 

Use this command to update product information— here, the description.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"description":"This is the penultimate product"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/ff8081816407132d0164071eec250001 | jsonlint
```

Output:

```
{
  "data": {
    "id": "ff8081816407132d0164071eec250001",
    "productCode": "BK71",
    "name": "product 1",
    "description": "This is the penultimate product",
    "category": {
      "id": "ROOT",
      "name": "ROOT"
    }
  }
}
```

### Update multiple products at once

`POST` to the same endpoint, and include the ID within each product, to update multiple products at once.

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"id":"ff8081816407132d0164071eec250001", "name":"product 1+","category.id":"ROOT"},\
{"id":"ff8081816407132d0164071eec2d0002", "name":"product 2.1","category.id":"ROOT"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

Output:

```
{
	"data": [{
		"id": "ff8081816407132d0164071eec250001",
		"productCode": "BK71",
		"name": "product 1+",
		"description": "This is the penultimate product",
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}, {
		"id": "ff8081816407132d0164071eec2d0002",
		"productCode": "DD67",
		"name": "product 2.1",
		"description": null,
		"category": {
			"id": "ROOT",
			"name": "ROOT"
		}
	}]
}
```












<!-- problem area-->

## Search

The Search API supports these operators on any string property of any object:

* eq (This is the default. Specify `property` and `value` if you want an equality search.)
* like
* ilike

Support for other property types (Integer, Date, BigDecimal) and operators (i.e. in, isNull, between, gt, ge, lt, le, 
etc) will be added at some point in the future. In addition, there's no way to perform nested searches (i.e. search for 
all shipment items that reference a particular product) at this time so please 

**NOTE**: If you don't see a search operator or property type supported, please raise an issue on GitHub
(https://github.com/openboxes/openboxes/issues).

```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{"searchAttributes":[{"property":"name", "operator":"ilike", "value":"Ace%"}]}' \
https://openboxes.ngrok.io/openboxes/api/generic/product/search?max=1
```
```
{
	"data": [{
		"id": "ff80818155df9de40155df9e321c0005",
		"productCode": "00002",
		"name": "Acetaminophen 325mg",
		"description": null,
		"category": {
			"id": "1",
			"name": "Medicines"
		}
	}]
}
```

<!-- problem area over-->













In addition to these basic examples, below are more specific use cases for the generic API.

## Get a partial list of shipments

To prevent tall lists, add the paging parameter `max=1` to your request. This outputs only one page of products.

(*See [Pagination](./authentication.md/#pagination) for more information.*)

```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" https://openboxes.ngrok.io/openboxes/api/generic/shipment?max=1 | jsonlint
```

Output:

```
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

You can see how this output would get unwieldy without the paging parameter.

## Read shipment details

Now let's read the details for a specific shipment.

```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/shipment/ff808181646b260401646b61df3f0034|jsonlint
```

Output:

```
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

## Ask what fields a new shipment requires
Pass an empty JSON object to the Create method and it'll tell you what fields it requires.

* name
* origin
* destination
* expectedShippingDate
* shipmentType

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" -d '{}' \
https://openboxes.ngrok.io/openboxes/api/generic/shipment|jsonlint
```

Output:

```
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