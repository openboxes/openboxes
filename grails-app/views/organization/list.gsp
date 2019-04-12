
<%@ page import="org.pih.warehouse.core.Organization" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'organization.label', default: 'Organization')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['organization']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['organization']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'organization.id.label', default: 'Id')}" />
                            
                                <th><warehouse:message code="organization.partyType.label" default="Party Type" /></th>
                            
                                <g:sortableColumn property="name" title="${warehouse.message(code: 'organization.name.label', default: 'Name')}" />
                            
                                <g:sortableColumn property="description" title="${warehouse.message(code: 'organization.description.label', default: 'Description')}" />
                            
                                <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'organization.dateCreated.label', default: 'Date Created')}" />
                            
                                <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'organization.lastUpdated.label', default: 'Last Updated')}" />
                            
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${organizationInstanceList}" status="i" var="organizationInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td><g:link action="edit" id="${organizationInstance.id}">${fieldValue(bean: organizationInstance, field: "id")}</g:link></td>
                            
                                <td>${fieldValue(bean: organizationInstance, field: "partyType")}</td>
                            
                                <td>${fieldValue(bean: organizationInstance, field: "name")}</td>
                            
                                <td>${fieldValue(bean: organizationInstance, field: "description")}</td>
                            
                                <td><format:date obj="${organizationInstance.dateCreated}" /></td>
                            
                                <td><format:date obj="${organizationInstance.lastUpdated}" /></td>
                            
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${organizationInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
