<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="bootstrap" />
    <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
    <title><warehouse:message code="inventory.browse.label" default="Browse inventory"/></title>
</head>

<body>
<div class="container-fluid">
<h1>Dashboard > ${session.warehouse.name}</h1>

</div>

</body>
</html>
