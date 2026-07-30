## Product Availability API

Facility-scoped available items (stock on hand / available to promise by
bin, lot, and product).

### Available Items

NOTE: Rows with quantity on hand equal to zero are excluded. Pagination
follows the usual OpenBoxes list pattern: if max is omitted it defaults to 10,
and max cannot exceed 100. Clients that need the full facility picture should
page through this API (there is no unbounded dump endpoint).

```
$ curl -X GET -b cookies.txt \
-H "Content-Type: application/json" \
"https://openboxes.ngrok.io/openboxes/api/facilities/1/availableItems?max=1&offset=0" | jsonlint
{
  "data": [
    {
      "inventoryItem.id": "ff80818155df9de40155df9e3356000e",
      "product.name": "General Pain Reliever",
      "productCode": "00004",
      "lotNumber": "lot57",
      "expirationDate": "01/28/2017",
      "binLocation.id": "ff808181646d3ec101646d5e7d480001",
      "binLocation.name": "Bin 1",
      "zone": null,
      "quantityAvailable": 10000,
      "quantityOnHand": 10000,
      "location": {
        "id": "1",
        "name": "Boston Headquarters"
      }
    }
  ],
  "totalCount": 1
}
```
