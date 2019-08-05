
<%@ page import="org.pih.warehouse.core.LocationRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationRole.label', default: 'LocationRole')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['locationRole']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['locationRole']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'locationRole.id.label', default: 'Id')}" />
                            
                                <th><warehouse:message code="locationRole.user.label" default="User" /></th>
                            
                                <th><warehouse:message code="locationRole.location.label" default="Location" /></th>
                            
                                <th><warehouse:message code="locationRole.role.label" default="Role" /></th>
                            
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${locationRoleInstanceList}" status="i" var="locationRoleInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td><g:link action="edit" id="${locationRoleInstance.id}">${fieldValue(bean: locationRoleInstance, field: "id")}</g:link></td>
                            
                                <td>${fieldValue(bean: locationRoleInstance, field: "user")}</td>
                            
                                <td>${fieldValue(bean: locationRoleInstance, field: "location")}</td>
                            
                                <td>${fieldValue(bean: locationRoleInstance, field: "role")}</td>
                            
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${locationRoleInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
