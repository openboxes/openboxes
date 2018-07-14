
# Reason Codes
Returns a list of translated reason codes based on the locale of the current session.

## List 

**Supported Activity Codes**
* SUBSTITUTE_REQUISITION_ITEM
* MODIFY_REQUISITION_ITEM
* MODIFY_PICKLIST_ITEM

```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/reasonCodes?activityCode=SUBSTITUTE_REQUISITION_ITEM"|jsonlint
{
  "data": [
    {
      "id": "SUBSTITUTION",
      "name": "Pharmacist-approved substitution [5]",
      "sortOrder": 5
    },
    {
      "id": "SUBSTITUTION_WITHIN_PRODUCT_GROUP",
      "name": "Substituted with product group item [20]",
      "sortOrder": 20
    },
    {
      "id": "REPLACED_BY_FORMULARY_ITEM",
      "name": "Replaced by formulary/stock item [9]",
      "sortOrder": 9
    },
    {
      "id": "STOCKOUT",
      "name": "Stock-out [1]",
      "sortOrder": 1
    },
    {
      "id": "LOW_STOCK",
      "name": "Low stock [2]",
      "sortOrder": 2
    },
    {
      "id": "EXPIRED",
      "name": "Expired product [3]",
      "sortOrder": 3
    },
    {
      "id": "DAMAGED",
      "name": "Damaged product [4]",
      "sortOrder": 4
    },
    {
      "id": "AVAILABLE_STOCK_RESERVED",
      "name": "Available stock reserved [13]",
      "sortOrder": 13
    },
    {
      "id": "DATA_ENTRY_ERROR",
      "name": "Data entry error [16]",
      "sortOrder": 16
    },
    {
      "id": "SUPPLY_MAX_QUANTITY",
      "name": "Supply maximum quantity on stock list [17]",
      "sortOrder": 17
    },
    {
      "id": "NOT_ON_STOCK_LIST",
      "name": "Not an item on stock list [18]",
      "sortOrder": 18
    },
    {
      "id": "INSUFFICIENT_QUANTITY_RECONDITIONED",
      "name": "Insufficient quantity packed down [19]",
      "sortOrder": 19
    },
    {
      "id": "SUPPLIED_BY_GOVERNMENT",
      "name": "Supplied by government [21]",
      "sortOrder": 21
    },
    {
      "id": "OTHER",
      "name": "Other",
      "sortOrder": 100
    }
  ]
}
```

## Read
The `read` action will probably never be used. Because it's not useful. But here it is anyway. Enjoy.
```
$ curl -b cookies.txt -X GET -H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/reasonCodes/OTHER"|jsonlint
{
  "data": {
    "id": "OTHER",
    "name": "Other",
    "sortOrder": 100
  }
}
```
