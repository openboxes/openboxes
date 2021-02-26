<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="bootstrap"/>
    <g:set var="entityName"
           value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}"/>
    <title><warehouse:message code="inventory.browse.label" default="Browse inventory"/></title>
</head>

<body>
<div class="container-fluid">

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${userInstance}">
        <div class="errors">
            <g:renderErrors bean="${userInstance}" as="list" />
        </div>
    </g:hasErrors>
    <div class="row">
        <div class="col-sm-12">

            <g:each var="entry" in="${loginLocationsMap}">
                <li class="list-group-item list-group-item-primary">${entry.key}</li>
                <ul class="list-group">
                    <g:each var="location" in="${entry.value.sort { it.name }}">
                        <li class="list-group-item list-group-item-action">
                            <g:link controller="dashboard" action="chooseLocation" id="${location?.id}">
                            ${location?.name}
                            </g:link>
                        </li>
                    </g:each>
                </ul>
            </g:each>
        </div>
    </div>
</div>
</body>
</html>
