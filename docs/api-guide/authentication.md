# Authentication
In order to authenticate, you need a valid user account. In order to get the full benefits of the API your user should 
probably be in role Superuser. Once you have created a Superuser <cough> user, you can attempt to authenticate 
using your username and password.

Request
```shell
curl -i -c cookies.txt -X POST -H "Conent-Type: application/json" \
-d '{"username":"jmiranda","password":"password"}' \
https://openboxes.ngrok.io/openboxes/api/login
```
Response
```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531; Path=/openboxes
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:21:10 GMT

Authentication was successful
```
NOTE: `-c cookies` option will create a cookies file (cookies.txt) and save your session information to be used in 
subsequent requests. This saves a huge amount of headache when you're testing the API.

# Cookie
Once you have authenticated, you have two options to authorize requests.

## Cookie Header 
Copy the JSESSIONID Cookie from the response header to make subsequent authenticated requests 
to the API
```
$ curl -i -X POST -H "Content-Type: application/json" \
-H "Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531" \
https://openboxes.ngrok.io/openboxes/api/categories

```
## Cookie File
Or use `-b cookies.txt` to read from a cookies file and start making requests against the API. 
NOTE: You'll need to use the `-c cookies.txt` on the login request in order to generate the proper cookies.
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/categories
```

# Logout

If you want to end your session, you can `POST` a request to the logout endpoint.
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/logout

```
# Error Handling

## Unauthorized Access
If you try to access the API with no cookies (or an invalid/stale cookie) you'll receive the following error and will need to (re-)authenticate
```
$ curl -i -X POST -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/categories

HTTP/1.1 401 Unauthorized
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=905D991AE2661B0FDD7F6FB140EB97D8; Path=/openboxes
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Thu, 21 Jun 2018 04:21:29 GMT

{"errorCode":401,"errorMessage":"Unauthorized user: Request categoryApi:save requires authentication"}
```


