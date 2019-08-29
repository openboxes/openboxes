
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
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

            <div class="buttonBar">
                <g:link action="list" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" default="List" args="[g.message(code: 'inventoryLevels.label')]"/>
                </g:link>
                <g:link action="create" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.add.label" default="Add" args="[g.message(code: 'inventoryLevel.label')]"/>
                </g:link>

            </div>

            <div class="box">
                <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>

                <g:render template="../inventoryLevel/form" model="[productInstance:inventoryLevelInstance.product,inventoryLevelInstance:inventoryLevelInstance]"/>

            </div>
        </div>
    </body>
</html>
