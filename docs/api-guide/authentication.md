# Authentication
You need a valid user account before you can authenticate. To get the full benefits of the API, create a Superuser account, then authenticate using your username and password.

```
$ curl -i -c cookies.txt -X POST -H "Content-Type: application/json" \
-d '{"username":"jmiranda","password":"password"}' \
https://openboxes.ngrok.io/openboxes/api/login
```

Result:

```
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Set-Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531; Path=/openboxes
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Date: Sun, 10 Jun 2018 21:21:10 GMT

Authentication was successful
```

**NOTE**: The `-c cookies.txt` option creates a cookies file named `cookies.txt` and saves your session information for later requests. This prevents needing to pass authentication headers around in each request.

## Cookies
After you authenticate, you have two options to authorize requests: the cookies header, and the cookies file.

### Cookies Header 
Copy the JSESSIONID cookie from the response header to make authenticated requests.

```
$ curl -i -X POST -H "Content-Type: application/json" \
-H "Cookie: JSESSIONID=062F3CF6129FC12B6BDD4D02E15BA531" \
https://openboxes.ngrok.io/openboxes/api/categories
```

### Cookies File
Or use `-b cookies.txt` to read from a cookies file and make requests against the API. Use the `-c cookies.txt` on the login request to generate the proper cookies.

```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/categories
```

## Logout

`POST` a request to the logout endpoint to end your session.

```
$ curl -i -X POST -H "Content-Type: application/json" -b cookies.txt \
https://openboxes.ngrok.io/openboxes/api/logout
```

## Exceptions
### Unauthorized Access
If you try to access the API with no cookies, or an invalid or stale cookie, this error shows.

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

Re-authenticate to fix this error.

## Pagination
All endpoints return all objects unless you give pagination parameters. For example, `max=1` limits your results to one page.

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

## Formatting

While not necessary, if you pipe all cURL responses through JSONLint, you get a more readable response.
Install JSONlint with this command:

```
npm install jsonlint -g
```

Then add ` | jsonlint` at the end of your curl request.

```
$ curl -X POST -H "Content-Type: application/json" \
https://openboxes.ngrok.io/openboxes/api/products?max=1 | jsonlint
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

**CAUTION**: If you use the `-i` argument, then you get parsing errors.

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
