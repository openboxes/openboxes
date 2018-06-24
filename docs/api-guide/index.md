## Overview 

### Authentication
In order to authenticate, you need a valid user account. In order to get the full benefits of the API your user should 
probably be in role Superuser. Once you have created a Superuser <cough> user, you can attempt to authenticate 
using your username and password.
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
NOTE: `-c cookies` option will create a cookies file (cookies.txt) and save your session information to be used in 
subsequent requests. This saves a huge amount of headache when you're testing the API.

### Cookie
Once you have authenticated, you have two options to 


#### Cookie Header 
Copy the JSESSIONID Cookie from the response header to make subsequent authenticated requests 
to the API
```
$ curl -i -X POST -H "Content-Type: application/json" \
-H "Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531" \
https://openboxes.ngrok.io/openboxes/api/categories

```
#### Cookie File
Or use `-b cookies.txt` to read from a cookies file and start making requests against the API. 
NOTE: You'll need to use the `-c cookies.txt` on the login request in order to generate the proper cookies.
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/categories
```

### Logout

If you want to end your session, you can `POST` a request to the logout endpoint.
```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/logout

```
### Exceptions

#### Unauthorized Access
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


### Pagination
All API endpoints will return all objects if pagination parameters are not provided.
```
$ curl -X POST -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/products?offset=0&max=1 | jsonlint
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


