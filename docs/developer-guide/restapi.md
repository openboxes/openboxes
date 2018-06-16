# REST API

## Overview 

### Authentication
If you try to access the API with no cookies (or an invalid/stale cookie) you'll receive the following error and will need to (re-)authenticate
```
$ curl -i -X POST -H "Content-Type: application/json" https://openboxes.ngrok.io/openboxes/api/categories

HTTP/1.1 302 Moved Temporarily
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=27C59970DF0F4E5DC7CEC2A695A2DCC5; Path=/openboxes
Location: http://openboxes.ngrok.io/openboxes/auth/login
Content-Length: 0
Date: Sun, 10 Jun 2018 21:20:45 GMT
```

Attempt to authenticate with a valid username and password.
```
$ curl -i -c cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"username":"jmiranda","password":"password"}' https://openboxes.ngrok.io/openboxes/api/login

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531; Path=/openboxes
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:21:10 GMT

Authentication was successful
```
Once you have authenticated, use the JSESSIONID in the "Cookie" request header and start making 
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/categories

```
If you want to end a session, you can 
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/logout

```

### Pagination
All API endpoints will return all objects if pagination parameters are not provided.
```
$ curl -X POST -H "Content-Type: application/json" https://openboxes.ngrok.io/openboxes/api/products?offset=0&max=1 | jsonlint
[
  {
    "id": "ff80818155df9de40155df9e329b0009",
    "productCode": "00003",
    "name": "Aspirin 20mg",
    "category": {
      "id": "1",
      "name": "Medicines"
    },
    "description": null,
    "dateCreated": "2016-07-12T14:58:55Z",
    "lastUpdated": "2016-07-12T14:58:55Z"
  }
]

```

### JSON Lint
While testing the API, I'd recommend installing jsonlint ...
```css
npm install jsonlint -g
```

... and piping all curl responses through it to get a pretty response.
```
$ curl -X POST -H "Content-Type: application/json" https://openboxes.ngrok.io/openboxes/api/products?max=1 | jsonlint
[
  {
    "id": "ff80818155df9de40155df9e329b0009",
    "productCode": "00003",
    "name": "Aspirin 20mg",
    "category": {
      "id": "1",
      "name": "Medicines"
    },
    "description": null,
    "dateCreated": "2016-07-12T14:58:55Z",
    "lastUpdated": "2016-07-12T14:58:55Z"
  }
]

```
NOTE: You'll need to remove the `-i` argument from the following examples to prevent parsing errors:
```
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   221    0   221    0     0    455      0 --:--:-- --:--:-- --:--:--   455
Error: Parse error on line 1:
SerP/1.1 200 OK
^
Expecting 'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '[', got 'undefined'
    at Object.parseError (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/jsonlint.js:55:11)
    at Object.parse (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/jsonlint.js:132:22)
    at parse (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/cli.js:82:14)
    at Socket.<anonymous> (/home/jmiranda/.nvm/versions/node/v6.6.0/lib/node_modules/jsonlint/lib/cli.js:149:41)
    at emitNone (events.js:91:20)
    at Socket.emit (events.js:185:7)
    at endReadableNT (_stream_readable.js:974:12)
    at _combinedTickCallback (internal/process/next_tick.js:74:11)
    at process._tickCallback (internal/process/next_tick.js:98:9) 
```


## Category API

### Create a new category - Exception (500)
Returns validation errors (Name is a required field of Category)
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{}' https://openboxes.ngrok.io/openboxes/api/categories

HTTP/1.1 500 Internal Server Error
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sat, 09 Jun 2018 14:30:20 GMT

{"errorCode":500,"errorMessage":"Unable to save category due to errors:\n- Field error in object 'org.pih.warehouse.product.Category' on field 'name': rejected value [null]; codes [org.pih.warehouse.product.Category.name.nullable.error.org.pih.warehouse.product.Category.name,org.pih.warehouse.product.Category.name.nullable.error.name,org.pih.warehouse.product.Category.name.nullable.error.java.lang.String,org.pih.warehouse.product.Category.name.nullable.error,category.name.nullable.error.org.pih.warehouse.product.Category.name,category.name.nullable.error.name,category.name.nullable.error.java.lang.String,category.name.nullable.error,org.pih.warehouse.product.Category.name.nullable.org.pih.warehouse.product.Category.name,org.pih.warehouse.product.Category.name.nullable.name,org.pih.warehouse.product.Category.name.nullable.java.lang.String,org.pih.warehouse.product.Category.name.nullable,category.name.nullable.org.pih.warehouse.product.Category.name,category.name.nullable.name,category.name.nullable.java.lang.String,category.name.nullable,nullable.org.pih.warehouse.product.Category.name,nullable.name,nullable.java.lang.String,nullable]; arguments [name,Category]; default message [Property [{0}] of class [{1}] cannot be null]\n"}
```

### Create a new category - Success (200)
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New category"}' https://openboxes.ngrok.io/openboxes/api/categories

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:22:00 GMT

{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"}

```

## Product API

### Create a new product 
Returns validation error (Category is a required field of Product)
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New product", "category":{"id":"ff80818163e2de8d0163eb93c5a00001"}}' \
https://openboxes.ngrok.io/openboxes/api/products

HTTP/1.1 500 Internal Server Error
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:35:58 GMT

{"errorCode":500,"errorMessage":"Unable to save category due to errors:\n- Field error in object 'org.pih.warehouse.product.Product' on field 'category': rejected value [null]; codes [org.pih.warehouse.product.Product.category.nullable.error.org.pih.warehouse.product.Product.category,org.pih.warehouse.product.Product.category.nullable.error.category,org.pih.warehouse.product.Product.category.nullable.error.org.pih.warehouse.product.Category,org.pih.warehouse.product.Product.category.nullable.error,product.category.nullable.error.org.pih.warehouse.product.Product.category,product.category.nullable.error.category,product.category.nullable.error.org.pih.warehouse.product.Category,product.category.nullable.error,org.pih.warehouse.product.Product.category.nullable.org.pih.warehouse.product.Product.category,org.pih.warehouse.product.Product.category.nullable.category,org.pih.warehouse.product.Product.category.nullable.org.pih.warehouse.product.Category,org.pih.warehouse.product.Product.category.nullable,product.category.nullable.org.pih.warehouse.product.Product.category,product.category.nullable.category,product.category.nullable.org.pih.warehouse.product.Category,product.category.nullable,nullable.org.pih.warehouse.product.Product.category,nullable.category,nullable.org.pih.warehouse.product.Category,nullable]; arguments [category,Product]; default message [Property [{0}] of class [{1}] cannot be null]\n"}
```

### Create a new product
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
-d '{"name":"New product", "category.id":"ff80818163e2de8d0163eb93c5a00001"}' https://openboxes.ngrok.io/openboxes/api/products

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:37:12 GMT

{"id":"ff80818163e2de8d0163eba1b1e90002","productCode":null,"name":"New product","category":{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"},"description":null,"dateCreated":"2018-06-10T21:37:12Z","lastUpdated":"2018-06-10T21:37:12Z"}
```

### List all products (results paginated using offset and max)
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{ "offset":0, "max":1 }' https://openboxes.ngrok.io/openboxes/api/products

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 22:13:27 GMT

{data:[{"id":"cab2b4f35c33cdec015c53e129ce1dea","productCode":"HS45","name":"\tExtension Set, 7\" Smllbore, Microclave, Clamp, Rotating Luer","category":{"id":"752e945e67d511e5a90eaa0009a30cce","name":"MedEquip&Supplies_IV_Supplies"},"description":null,"dateCreated":"2017-05-29T16:04:44Z","lastUpdated":"2017-05-29T16:04:44Z"}]


HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 22:08:21 GMT

[{"id":"cab2b4f35c33cdec015c53e129ce1dea","productCode":"HS45","name":"\tExtension Set, 7\" Smllbore, Microclave, Clamp, Rotating Luer","category":{"id":"752e945e67d511e5a90eaa0009a30cce","name":"MedEquip&Supplies_IV_Supplies"},"description":null,"dateCreated":"2017-05-29T16:04:44Z","lastUpdated":"2017-05-29T16:04:44Z"},{"id":"c879370c36cd8e710136cda1e4d50025","productCode":"AD65","name":" Blade for surgical knives size 15","category":{"id":"cab2b4f34d7c909b014e030ceba04968","name":"MedEquip&Supplies_Surgical"},"description":null,"dateCreated":"2012-04-20T07:43:20Z","lastUpdated":"2016-10-21T02:28:06Z"},{"id":"cab2b4f36153318d0161d31945635eee","productCode":"QZ00","name":" Quetiapine fumarate, 100mg, Tablet","category":{"id":"cab2b4f34fdce3cc014ff7ebc74717bf","name":"Drugs_Psychiatric_&_Antiepileptic"},"description":null,"dateCreated":"2018-02-26T23:11:44Z","lastUpdated":"2018-02-26T23:11:44Z"},{"id":"cab2b4f35915dee301591802f8690495","productCode":"DM86","name":" Suture, Chromic gut, 4-0, C-13, Reverse-cutting, Braided, Absorbable, 30in","category":{"id":"cab2b4f34d7c909b014e030ceba04968","name":"MedEquip&Supplies_Surgical"},"description":null,"dateCreated":"2016-12-19T22:56:04Z","lastUpdated":"2016-12-19T22:59:00Z"},{"id":"cab2b4f35fbe36a5016022cfda3d257e","productCode":"ZP59","name":" Walker, Side Style, One Arm","category":{"id":"cab2b4f34fdce3cc014ff65d4b4b1734","name":"MedEquip&Supplies_Rehabilitation"},"description":"Lightweight design allows the hemi sidestepper to be maneuvered easily even with just one arm.\r\nHelps provide more stability and more weight-bearing ability than a quad cane.\r\nHeight-adjustable in 1\" (2.5 cm) increments.\r\nBi-level hand grip offer extra assistance for users getting in and out of chairs.\r\n250-lb. (113 kg) Weight capacity; Height adjustment, 32\"-36\" (81 cm-91 cm); Folded width, 3\" (8 cm).","dateCreated":"2017-12-05T00:35:35Z","lastUpdated":"2017-12-05T00:35:35Z"},{"id":"c879370c40a9cf710140df0d02243693","productCode":"KB27","name":"#2 blue filter, A/C split systems","category":{"id":"c879370c3bac412e013c2105979d0857","name":"Z One-off products"},"description":null,"dateCreated":"2013-09-02T19:21:46Z","lastUpdated":"2016-08-26T19:56:18Z"},{"id":"c879370c40a9cf710140df0d02503695","productCode":"YE24","name":"#3 blue filter, A/C split systems","category":{"id":"c879370c3bac412e013c2105979d0857","name":"Z One-off products"},"description":null,"dateCreated":"2013-09-02T19:21:46Z","lastUpdated":"2016-08-26T19:56:18Z"},{"id":"c879370c40a9cf710140df0d01fa3691","productCode":"MG54","name":"#4 blue filter, A/C split systems","category":{"id":"c879370c3bac412e013c2105979d0857","name":"Z One-off products"},"description":null,"dateCreated":"2013-09-02T19:21:46Z","lastUpdated":"2016-08-26T19:56:18Z"},{"id":"c879370c40a9cf710140df0d02a23697","productCode":"CE59","name":"#unknown blue filter, A/C split systems","category":{"id":"c879370c3bac412e013c2105979d0857","name":"Z One-off products"},"description":null,"dateCreated":"2013-09-02T19:21:46Z","lastUpdated":"2016-08-26T19:56:18Z"},{"id":"c879370c4564d5e50145b2ce88a02f63","productCode":"RA62","name":".33 HP VM Classic Motor Kit","category":{"id":"727efb7a67d511e5a90eaa0009a30cce","name":"Facilities_Mechanical"},"description":null,"dateCreated":"2014-04-30T18:24:11Z","lastUpdated":"2017-03-15T00:55:35Z"}]
```

### Search products with Name starting with 'New product' (results paginged using offset and max)
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
-d '{ "name":"New product", "offset":0, "max":1 }' https://openboxes.ngrok.io/openboxes/api/products

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 22:15:08 GMT

[{"id":"ff80818163e2de8d0163eba1b1e90002","productCode":"KX43","name":"New product","category":{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"},"description":null,"dateCreated":"2018-06-10T21:37:13Z","lastUpdated":"2018-06-10T21:37:13Z"}]
```

### Get an existing product 
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/products/ff80818163e2de8d0163eba1b1e90002

HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:38:27 GMT

{"id":"ff80818163e2de8d0163eba1b1e90002","productCode":"KX43","name":"New product","category":{"id":"ff80818163e2de8d0163eb93c5a00001","name":"New category"},"description":null,"dateCreated":"2018-06-10T21:37:13Z","lastUpdated":"2018-06-10T21:37:13Z"}
```

### Product not found
```
$ curl -i -X GET -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/products/invalididentifier

HTTP/1.1 404 Not Found
Server: Apache-Coyote/1.1
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:43:37 GMT

{"errorCode":404,"errorMessage":"Resource not found"}
```
