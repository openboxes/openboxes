<html>
<head>

</head>
<body>
    <h1>Expired</h1>
    <p class="lead">
        The following items have expired.
    </p>
    <g:render template="/email/inventoryItemTable" model="[inventoryItems:expired]"/>

    <h1>Expiring Within ${daysUntilExpiry} Days</h1>
    <p class="lead">
        The following items will expire within ${daysUntilExpiry} days.
    </p>
    <g:render template="/email/inventoryItemTable" model="[inventoryItems:expiring]"/>

</body>
</html>
