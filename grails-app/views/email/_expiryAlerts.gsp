<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
    <h1>${location.name}</h1>
    <h2>Expired</h2>
    <p class="lead">
        The following items have expired.
    </p>
    <g:render template="/email/inventoryItemTable" model="[inventoryItems:expired]"/>

    <h2>Expiring Within ${daysUntilExpiry} Days</h2>
    <p class="lead">
        The following items will expire within ${daysUntilExpiry} days.
    </p>
    <g:render template="/email/inventoryItemTable" model="[inventoryItems:expiring]"/>
</body>
</html>
