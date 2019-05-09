
# Search APIs
These are APIs that support lookup requests like listing and read operations (GET). That means that most of these APIs
don't currently support create (POST), update (PUT) or delete (DELETE) operations.

However, all of the domain objects in OpenBoxes will respond to the Generic API (e.g. `/api/generic/product`), but the following 
API endpoints will handle more advanced search criteria.


## Bin Locations

Supports filtering by `parentLocation.id`
```
$ curl -X GET -b cookies.txt -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/binLocations?parentLocation.id=1" | jsonlint 
{
  "data": [
    {
      "id": "ff808181641b3e5901641b4693c00003",
      "name": "Bin 2",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": {
        "id": "1",
        "name": "Boston Headquarters",
        "description": null,
        "locationNumber": null,
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "2",
          "name": "Depot|fr:D",
          "description": "Depot",
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "locationType": {
        "id": "cab2b4f35ba2d867015ba2e17e390001",
        "name": "Bin Location",
        "description": "Default bin location type",
        "locationTypeCode": "BIN_LOCATION"
      },
      "locationTypeCode": "BIN_LOCATION"
    },
    {
      "id": "ff808181641b3e5901641b46aa820005",
      "name": "Bin 3",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": {
        "id": "1",
        "name": "Boston Headquarters",
        "description": null,
        "locationNumber": null,
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "2",
          "name": "Depot|fr:D",
          "description": "Depot",
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "locationType": {
        "id": "cab2b4f35ba2d867015ba2e17e390001",
        "name": "Bin Location",
        "description": "Default bin location type",
        "locationTypeCode": "BIN_LOCATION"
      },
      "locationTypeCode": "BIN_LOCATION"
    },
    {
      "id": "ff808181641b3e5901641b467af30001",
      "name": "Bin 1",
      "description": null,
      "locationNumber": null,
      "locationGroup": null,
      "parentLocation": {
        "id": "1",
        "name": "Boston Headquarters",
        "description": null,
        "locationNumber": null,
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "2",
          "name": "Depot|fr:D",
          "description": "Depot",
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "locationType": {
        "id": "cab2b4f35ba2d867015ba2e17e390001",
        "name": "Bin Location",
        "description": "Default bin location type",
        "locationTypeCode": "BIN_LOCATION"
      },
      "locationTypeCode": "BIN_LOCATION"
    }
  ]
}
```


## Stocklists
Supports filtering by `name`, `requisitionNumber`, `origin.id`, `destination.id` as well as other attributes to be documented at a later time.
```
$ curl -X GET -b cookies.txt \
-H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/stocklists" | jsonlint
{
  "data": [
    {
      "id": "ff808181641b2fd501641b39f4ef0001",
      "name": "New Stocklist",
      "requisitionNumber": "739BJY",
      "description": null,
      "isTemplate": true,
      "type": "STOCK",
      "status": null,
      "commodityClass": null,
      "dateRequested": "2018-06-20T03:25:40Z",
      "dateReviewed": null,
      "dateVerified": null,
      "dateChecked": null,
      "dateDelivered": null,
      "dateIssued": null,
      "dateReceived": null,
      "origin": {
        "id": "2",
        "name": "Miami Warehouse",
        "description": null,
        "locationNumber": null,
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "2",
          "name": "Depot|fr:D",
          "description": "Depot",
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "destination": {
        "id": "1",
        "name": "Boston Headquarters",
        "description": null,
        "locationNumber": null,
        "locationGroup": null,
        "parentLocation": null,
        "locationType": {
          "id": "2",
          "name": "Depot|fr:D",
          "description": "Depot",
          "locationTypeCode": "DEPOT"
        },
        "locationTypeCode": "DEPOT"
      },
      "requestedBy": {
        "id": "3",
        "username": "jmiranda",
        "firstName": "Justin",
        "lastName": "Miranda",
        "displayName": "Justin Miranda"
      },
      "reviewedBy": null,
      "verifiedBy": null,
      "checkedBy": null,
      "deliveredBy": null,
      "issuedBy": null,
      "receivedBy": null,
      "recipient": null,
      "requisitionItems": [
        {
          "id": "ff808181641b2fd501641b3a3b0c0002",
          "status": "PENDING",
          "requisition.id": "ff808181641b2fd501641b39f4ef0001",
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
          "inventoryItem": null,
          "quantity": 100,
          "quantityApproved": null,
          "quantityCanceled": null,
          "cancelReasonCode": null,
          "cancelComments": null,
          "orderIndex": 0,
          "changes": [],
          "modification": null,
          "substitution": null,
          "picklistItems": []
        },
        {
          "id": "ff808181641b2fd501641b3a61200003",
          "status": "PENDING",
          "requisition.id": "ff808181641b2fd501641b39f4ef0001",
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
          "inventoryItem": null,
          "quantity": 50,
          "quantityApproved": null,
          "quantityCanceled": null,
          "cancelReasonCode": null,
          "cancelComments": null,
          "orderIndex": 1,
          "changes": [],
          "modification": null,
          "substitution": null,
          "picklistItems": []
        },
        {
          "id": "ff808181641b2fd501641b3aab850004",
          "status": "PENDING",
          "requisition.id": "ff808181641b2fd501641b39f4ef0001",
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
          "inventoryItem": null,
          "quantity": 25,
          "quantityApproved": null,
          "quantityCanceled": null,
          "cancelReasonCode": null,
          "cancelComments": null,
          "orderIndex": 2,
          "changes": [],
          "modification": null,
          "substitution": null,
          "picklistItems": []
        }
      ]
    }
  ]
}
```