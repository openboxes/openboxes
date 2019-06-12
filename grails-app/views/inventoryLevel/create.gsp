
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${inventoryLevelInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${inventoryLevelInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
            	<div class="box">
            	    <h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>

                    <g:render template="../inventoryLevel/form" model="[productInstance:inventoryLevelInstance.product,inventoryLevelInstance:inventoryLevelInstance]"/>
                </div>
            </g:form>
        </div>

    </body>
</html>
