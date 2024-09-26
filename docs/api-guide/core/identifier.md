
## Identifier API
The identifier API only supports POST.

### Create 
Create a new alphanumeric identifier for a given `identifierType` or `identifierFormat`.

#### Identifier format
Create your own identifier format using the following codes.

* A = Alphanumeric
* L = Alphabetic characters only
* N = Numeric 
* D = Numeric
* Any other character (i.e. dashes, periods) will be included as-is.

```
$ curl -i -X POST -b cookies.txt \
-H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/identifiers?identifierFormat=AAANNN

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Thu, 21 Jun 2018 04:49:26 GMT

{"data":"VCU789"}
```

#### Identifier type
Allowed `identifierType` values: 
* `requisition`
* `product`
* `productSupplier`
* `transaction`
* `shipment`
* `order`

```
$ curl -i -X POST -b cookies.txt \
-H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/identifiers?identifierType=product

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Thu, 21 Jun 2018 04:49:40 GMT

{"data":"VR26"}

```
These formats can be configured in openboxes-config.properties
```
openboxes.identifier.transaction.format = AAA-AAA-AAA
openboxes.identifier.order.format = NNNLLL
openboxes.identifier.product.format = LLNN
openboxes.identifier.productSupplier.format = LLNN
openboxes.identifier.requisition.format = NNNLLL
openboxes.identifier.shipment.format = NNNLLL
```
You can also edit the available characters and digits available for the `identifierFormat` 
mask. This allows you to remove characters that may be confused with others (i.e. I, 1, l or 0 and O). 
By default we keep all numeric digits and remove the conflicting alphabetic characters.
```
openboxes.identifier.numeric = 0123456789
openboxes.identifier.alphabetic = ABCDEFGHJKMNPQRSTUVXYZ
openboxes.identifier.alphanumeric = 0123456789ABCDEFGHJKMNPQRSTUVWXYZ
```