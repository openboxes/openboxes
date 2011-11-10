
<%@ page import="org.pih.warehouse.core.LocationType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationType.label', default: 'LocationType')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div>            	
	            	<span class="menuButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="['locationType']"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'locationType.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'locationType.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="code" title="${warehouse.message(code: 'locationType.code.label', default: 'Code')}" />
                        
                            <g:sortableColumn property="description" title="${warehouse.message(code: 'locationType.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="sortOrder" title="${warehouse.message(code: 'locationType.sortOrder.label', default: 'Sort Order')}" />
                        
                            <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'locationType.dateCreated.label', default: 'Date Created')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${locationTypeInstanceList}" status="i" var="locationTypeInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${locationTypeInstance.id}">${fieldValue(bean: locationTypeInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: locationTypeInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: locationTypeInstance, field: "code")}</td>
                        
                            <td>${fieldValue(bean: locationTypeInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: locationTypeInstance, field: "sortOrder")}</td>
                        
                            <td><format:date obj="${locationTypeInstance.dateCreated}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${locationTypeInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
