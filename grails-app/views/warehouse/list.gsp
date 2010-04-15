
<%@ page import="org.pih.warehouse.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'warehouse.id.label', default: 'Id')}" />
                        
                            <th><g:message code="warehouse.manager.label" default="Manager" /></th>
                   	    
                            <g:sortableColumn property="name" title="${message(code: 'warehouse.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="country" title="${message(code: 'warehouse.country.label', default: 'Country')}" />
                        
                            <g:sortableColumn property="city" title="${message(code: 'warehouse.city.label', default: 'City')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${warehouseInstanceList}" status="i" var="warehouseInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${warehouseInstance.id}">${fieldValue(bean: warehouseInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: warehouseInstance, field: "manager")}</td>
                        
                            <td>${fieldValue(bean: warehouseInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: warehouseInstance, field: "country")}</td>
                        
                            <td>${fieldValue(bean: warehouseInstance, field: "city")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${warehouseInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
