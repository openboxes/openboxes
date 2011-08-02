<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'locations.label', default: 'Locations')}" />
        <g:set var="pageTitle" value="${message(code: 'default.show.label' args="[entityName]")}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>        
        <div class="body">
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
              
            <div class="dialog">
				<span class="menuButton">
           			<g:link action="edit" class="new">Add location</g:link>
	           	</span>
           	</div>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        	<g:sortableColumn property="id" title="${message(code: 'eventType.id.label', default: 'Id')}" />
                            <g:sortableColumn property="name" title="${message(code: 'location.name.label', default: 'Name')}" />
                            <th><warehouse:message code="location.type.label" default="Type" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${locationInstanceList}" status="i" var="locationInstance">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
								<g:link action="edit" id="${locationInstance.id}">${fieldValue(bean: locationInstance, field: "name")}</g:link>
							</td>                           
                            <td>${fieldValue(bean: locationInstance, field: "locationType")}</td>                                                  
							<td class="center tenth"><g:link class="edit" action="edit" id="${locationInstance?.id}" >${message(code: 'default.button.edit.label', default: 'Edit')}</g:link></td>
						</tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
