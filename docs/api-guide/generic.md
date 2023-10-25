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

The generic API lets developers access any domain object without rewriting boilerplate code. It has five operations:

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

Use this command to show specific product information.

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

**NOTE**: the `POST` request on multiple objects fails if a single error throws. However, cURL throws an error for only the first object that fails. For example, this command produces an error:

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '[{"name":"product 1"},{"name":"product 2"}]' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

Output:

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

`POST` to the same endpoint and list each product ID to update multiple products at once.

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
		"description": "This is the next-to-last product",
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

## Delete

Use this command to delete a product.

```
$ curl -b cookies.txt -X DELETE \
https://openboxes.ngrok.io/openboxes/api/generic/product/<id#>
```

In addition to the basic `GET`, `READ`, `POST`, `UPDATE`, and `DELETE` commands, the following are some more specific uses for the generic API.

## Get a partial list of shipments

Add the paging parameter `max=1` to your request to prevent too-tall lists of products. This outputs only one page of products.

(*See [Pagination](./authentication.md/#pagination) for more information.*)

```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/shipment?max=1 | jsonlint
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

You can see how the output can get overwhelming without the paging parameter.

## Read shipment details

Use this command to read the details for a specific shipment:

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

## Create a new shipment
All of these fields are required for each shipment. If even one is missing, cURL throws the error `"Property [{0}] of class [{1}] cannot be null"`.

* name
* origin
* destination
* expectedShippingDate
* shipmentType

Add the `name` with this command:

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"name":"product 0", "category.id":"ROOT"}' \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

**NOTE**: This is the same as the Create command above.

Add the `origin`— composed of ID, name, and type— with this command:

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d `{"origin.id":"1234567890", "origin.name":"Depot 1", "origin.type": "DEPOT"}` \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

Add the `destination`— composed of ID, name, and type— with this command:

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d `{"destination.id":"0987654321", "destination.name":"Ward 2", "destination.type": "WARD"}` \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

Add the `expectedShippingDate` with this command:

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d `{"expectedShippingDate":"07/05/2018 00:00", "category.id":"ROOT"}` \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

Add the `shipmentType` with this command:

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d `{"shipmentType":"AIR", "category.id":"ROOT"}` \
https://openboxes.ngrok.io/openboxes/api/generic/product
```

**NOTE**: `shipmentType` is an enum with three keys: AIR, LAND, and SEA.

You can also specify all five fields in a single command:

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d `{"name":"product 0", "category.id":"ROOT", \
"origin.id":"1234567890", "origin.name":"Depot 1", "origin.type": "DEPOT", \
"destination.id":"0987654321", "destination.name":"Ward 2", "destination.type": "WARD", \
"expectedShippingDate":"07/05/2018 00:00", "category.id":"ROOT", \
"shipmentType":"AIR", "category.id":"ROOT"}` \
https://openboxes.ngrok.io/openboxes/api/generic/product
```