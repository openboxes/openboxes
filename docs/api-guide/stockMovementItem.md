

# Stock Movement Items

## Create

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @addStockMovementItem.json \
https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001|jsonlint
```
### Post Body (addStockMovementItem.json)
```
{
	"id": "ff808181644d5e5b01644e5007500001",
	"name": "my new stock movement",
	"description": "",
	"identifier": "483ZSA",
	"origin.id": "2",
	"destination.id": "1",
	"dateRequested": "06/23/2018",
	"requestedBy.id": "1",
	"lineItems": [{
		"product.id": "ff8081816407132d0164071eec250001",
		"quantityRequested": "100",
		"sortOrder": 0,
		"recipient.id": "1"
	}]
}
```
### Response
```
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "",
    "identifier": "483ZSA",
    "origin": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "destination": {
      "id": "1",
      "name": "Boston Headquarters"
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
        "id": "ff808181644e51a401644e85891b0006",
        "productCode": "BK71",
        "product": {
          "id": "ff8081816407132d0164071eec250001",
          "productCode": "BK71",
          "name": "product 1+",
          "description": "This is the penultimate product",
          "category": {
            "id": "ROOT",
            "name": "ROOT"
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
        "sortOrder": null
      }
    ]
  }
}
```


## Update 
This is used to change the quantity or product associated with an item, as well as 
other changes like sort order. This is a direct change to the requisition item. 

If you're looking to record a new quantity and want to keep the originally requested 
information, then you'll likely want to use the Revise Stock Movement Item example below
This will allow you to keep the originally requested quantity and product information, 
record the new quantity, as well as a reason for the revision 
(see next section for more information)

```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @updateStockMovementItem.json \
https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001|jsonlint
```

### Request Body (updateStockMovementItem.json)
```
{
	"id": "ff808181644d5e5b01644e5007500001",
	"name": "my new stock movement",
	"description": "",
	"identifier": "483ZSA",
	"origin.id": "2",
	"destination.id": "1",
	"dateRequested": "06/23/2018",
	"requestedBy.id": "1",
	"lineItems": [{
		"id": "ff808181644e51a401644e85891b0006",
		"product.id": "ff80818155df9de40155df9e31000001",
		"quantityRequested": "500",
		"sortOrder": 0
	}]
}
```
### Response 
```
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "my new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "destination": {
      "id": "1",
      "name": "Boston Headquarters"
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
        "id": "ff808181644e51a401644e85891b0006",
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
        "quantityRequested": 500,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": 0,
        "quantityRevised": null,
        "reasonCode": null,
        "comments": null,
        "recipient": null,
        "sortOrder": null
      }
    ]
  }
}
```

## Revise 
This is used to record a revision to the quantity requested. This requires the user to 
choose a reason code (i.e. STOCKOUT) and optionally add comments that may help provide
more context for the revision.

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @reviseStockMovementItem.json \
https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001|jsonlint
```
### Post Body (reviseStockMovementItem.json)
```
{
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "",
    "identifier": "483ZSA",
    "origin.id": "2",
    "destination.id": "1",
    "dateRequested": "06/23/2018",
    "requestedBy.id": "1",
    "lineItems": [
      {
        "id":"ff808181644e51a401644e85891b0006",
        "quantityRevised":200,
        "reasonCode":"BECAUSE",
        "comments":"because i said so",
        
      }
    ]
  }
```
### Response
```
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "my new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "destination": {
      "id": "1",
      "name": "Boston Headquarters"
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
        "id": "ff808181644e51a401644e85891b0006",
        "productCode": "BK71",
        "product": {
          "id": "ff8081816407132d0164071eec250001",
          "productCode": "BK71",
          "name": "product 1+",
          "description": "This is the penultimate product",
          "category": {
            "id": "ROOT",
            "name": "ROOT"
          }
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "CHANGED",
        "quantityRequested": 100,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": 100,
        "quantityRevised": 200,
        "reasonCode": "BECAUSE",
        "comments": "because i said so",
        "recipient": null,
        "sortOrder": null
      }
    ]
  }
}
```
## Substitute
This is used to record a substitution for an stock movement item. This requires the user to 
enter a new product and quantity, as well as choose a reason code (i.e. STOCKOUT) and optionally add a comment that may 
help provide more context for the substitution.

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @substituteStockMovementItem.json \
https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001|jsonlint
```
### Post Body (substituteStockMovementItem.json)
```
{
	"id": "ff808181644d5e5b01644e5007500001",
	"name": "my new stock movement",
	"description": "",
	"identifier": "483ZSA",
	"origin.id": "2",
	"destination.id": "1",
	"dateRequested": "06/23/2018",
	"requestedBy.id": "1",
	"lineItems": [{
		"id": "ff808181644e51a401644e85891b0006",
		"substitute": "true",
		"newProduct.id":"ff80818155df9de40155df9e3312000d",
		"newQuantity":100,
		"reasonCode":"CLINICAL",
		"comments":"A clinical decision that is none of your business",
	}]
}  
```
NOTE: You can add multiple substitutions by adding another line item with the same ID along with 
the `substitute` instruction.

### Response
```
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "my new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "destination": {
      "id": "1",
      "name": "Boston Headquarters"
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
        "id": "ff808181644e51a401644e85891b0006",
        "productCode": "BK71",
        "product": {
          "id": "ff8081816407132d0164071eec250001",
          "productCode": "BK71",
          "name": "product 1+",
          "description": "This is the penultimate product",
          "category": {
            "id": "ROOT",
            "name": "ROOT"
          }
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "SUBSTITUTED",
        "quantityRequested": 100,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": 100,
        "quantityRevised": null,
        "substitutions": [
          {
            "id": "ff8081816458c881016458d22b5f0002",
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
            "statusCode": "APPROVED",
            "quantityRequested": 100,
            "quantityAllowed": null,
            "quantityAvailable": null,
            "quantityCanceled": null,
            "quantityRevised": null,
            "substitutions": [],
            "reasonCode": null,
            "comments": null,
            "recipient": null,
            "sortOrder": null
          }
        ],
        "reasonCode": "CLINICAL",
        "comments": "A clinical decision that is none of your business",
        "recipient": null,
        "sortOrder": null
      }
    ]
  }
}

```
**IMPORTANT**: If you want to make changes to an existing substitution item, the request body should include 
an instruction to revert the changes along with the modified substitution items. You can also modify 
ANY stock movement item by editing it's corresponding requisition item through the Requisition Item API endpoint
(currently `/api/generic/requisitionItem/:id`). Substitutions and modifications are just children requisition items
under the original requisition item.
```
{
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "",
    "identifier": "483ZSA",
    "origin.id": "2",
    "destination.id": "1",
    "dateRequested": "06/23/2018",
    "requestedBy.id": "1",
    "lineItems": [{
        "id": "ff8081816458c881016458ca819e0001",
        "revert": "true"
    },	
    {
        "id": "ff8081816458c881016458ca819e0001",
        "substitute": "true",
        "newProduct.id":"ff80818155df9de40155df9e33930011",
        "newQuantity":1000,
        "reasonCode":"CLINICAL",
        "comments":"A clinical decision that is none of your business", 
    },
    {
        "id": "ff8081816458c881016458ca819e0001",
        "substitute": "true",
        "newProduct.id":"ff80818155df9de40155df9e3312000d",
        "newQuantity":500,
        "reasonCode":"CLINICAL",
        "comments":"A clinical decision that is none of your business",	
    }]
}
```
### Exceptions
Cannot substitute a product that is not in the original product's list of available substitutions. 
See the Substitutions API.
```
{
  "errorCode": 500,
  "errorMessage": "Product 00004 General Pain Reliever is not a valid substitution of BK71 product 1+"
}
```
You also cannot substitute the product for itself.
```
{
  "errorCode": 500,
  "errorMessage": "Product BK71 product 1+ is not a valid substitution of BK71 product 1+"
}

```

## Cancel 
Similar to a revision, this operation allows you to cancel the stock movement item.

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @cancelStockMovementItem.json \
https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001|jsonlint
```
### Post Body (cancelStockMovementItem.json)
```
{
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "",
    "identifier": "483ZSA",
    "origin.id": "2",
    "destination.id": "1",
    "dateRequested": "06/23/2018",
    "requestedBy.id": "1",
    "lineItems": [
      {
        "id":"ff808181644e51a401644e85891b0006",
        "cancel":"true",
        "reasonCode":"BECAUSE",
        "comments":"more information since BECAUSE is not a good reason"
      }
    ]
}
```

### Response
```
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "my new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "destination": {
      "id": "1",
      "name": "Boston Headquarters"
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
        "id": "ff808181644e51a401644e85891b0006",
        "productCode": "BK71",
        "product": {
          "id": "ff8081816407132d0164071eec250001",
          "productCode": "BK71",
          "name": "product 1+",
          "description": "This is the penultimate product",
          "category": {
            "id": "ROOT",
            "name": "ROOT"
          }
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "CANCELED",
        "quantityRequested": 100,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": 100,
        "quantityRevised": null,
        "reasonCode": "BECAUSE",
        "comments": "more information since BECAUSE is not a good reason",
        "recipient": null,
        "sortOrder": null
      }
    ]
  }
}
```

## Revert 
This allows you to revert any changes made to the stock movement item (including revisions, cancellations, 
and substitutions). However it does not allow you to revert deletes and updates.

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @revertStockMovementItem.json \
https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001|jsonlint
```
### Post Body (revertStockMovementItem.json)
```
{
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "",
    "identifier": "483ZSA",
    "origin.id": "2",
    "destination.id": "1",
    "dateRequested": "06/23/2018",
    "requestedBy.id": "1",
    "lineItems": [
      {
        "id":"ff808181644e51a401644e85891b0006",
        "revert":"true"        
      }
    ]
}
```
### Response
```
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "my new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "destination": {
      "id": "1",
      "name": "Boston Headquarters"
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
        "id": "ff808181644e51a401644e85891b0006",
        "productCode": "BK71",
        "product": {
          "id": "ff8081816407132d0164071eec250001",
          "productCode": "BK71",
          "name": "product 1+",
          "description": "This is the penultimate product",
          "category": {
            "id": "ROOT",
            "name": "ROOT"
          }
        },
        "palletName": null,
        "boxName": null,
        "statusCode": "PENDING",
        "quantityRequested": 100,
        "quantityAllowed": null,
        "quantityAvailable": null,
        "quantityCanceled": 0,
        "quantityRevised": null,
        "reasonCode": null,
        "comments": null,
        "recipient": null,
        "sortOrder": null
      }
    ]
  }
}
```

## Delete 
This operation allows you to delete the stock movement item completely. 

### Request
```
$ curl -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @deleteStockMovementItem.json \
https://openboxes.ngrok.io/openboxes/api/stockMovements/ff808181644d5e5b01644e5007500001|jsonlint
```
### Post Body (deleteStockMovementItem.json)
```
{
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "",
    "identifier": "483ZSA",
    "origin.id": "2",
    "destination.id": "1",
    "dateRequested": "06/23/2018",
    "requestedBy.id": "1",
    "lineItems": [
      {
        "id":"ff808181644e51a401644e85891b0006",
        "delete":"true",
      }
    ]
}
```

### Response
```
{
  "data": {
    "id": "ff808181644d5e5b01644e5007500001",
    "name": "my new stock movement",
    "description": "my new stock movement",
    "identifier": "483ZSA",
    "origin": {
      "id": "2",
      "name": "Miami Warehouse"
    },
    "destination": {
      "id": "1",
      "name": "Boston Headquarters"
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

### Exceptions
This operation 
cannot be performed on a stock movement item that has been revised (due to a bug with 
foreign key constraints). 
```
{
  "errorCode": 500,
  "errorMessage": "Cannot delete or update a parent row: a foreign key constraint fails (`openboxes_integration`.`requisition_item`, CONSTRAINT `FK5358E4D6405AC22D` FOREIGN KEY (`modification_item_id`) REFERENCES `requisition_item` (`id`))"
}
```
Therefore you must revert all changes to the stock movement 
item before deleting.


## Picking

### Request
```
curl  -b cookies.txt -X POST -H "Content-Type: application/json" \
-d @pickStockMovementItems.json \
https://openboxes.ngrok.io/openboxes/api/stockMovementItems/ff808181646b260401646b5bf4cb002b
```
### Post Body (pickStockMovementItem.json)
```
{
	"picklistItems":[{
        "id": "ff80818164787ed10164788a0f190022",
	    "inventoryItem.id": "ff8081816473166d0164731d419f000c",
	    "binLocation.id": "ff808181646d3ec101646d5e8e3e0003",
	    "quantityPicked":"10"
	}]
}

```

NOTE: To create a pick list item, remove the ID field from the post body (value can also be empty string).
To edit an existing picklist item, specify the ID from the `picklistItems` array. To delete the picklist item
assign a value of 0 to the `quantityPicked` field.


### Exceptions
This operation might fail if the quantity picked of the selected item is greater than
the quantity on hand of that item in stock.