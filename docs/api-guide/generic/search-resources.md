The Search API currently supports the following operators on any string property of any object:

* eq (default: you can just specify 'property' and 'value' if you want an equality search)
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
