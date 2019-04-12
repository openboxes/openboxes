
## Category API

### Create

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

### Exceptions

#### Validation Errors
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
