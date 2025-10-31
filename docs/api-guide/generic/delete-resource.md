Attempt to delete a resource.

## Delete a category
### Request
```
$ curl -b cookies.txt -i -X DELETE -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/generic/category/ff8081818ef9ad0f018efc17f2a80001
```
### Response
```
HTTP/2 204 
date: Sat, 20 Apr 2024 15:26:24 GMT
x-application-context: application:development
```
