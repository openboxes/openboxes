<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
    <h1>${location.name}</h1>
    <h2>Stock Status ${status} (${products.size()})</h2>
    <g:render template="/email/productTable" model="[products:products]"/>
</body>
</html>
