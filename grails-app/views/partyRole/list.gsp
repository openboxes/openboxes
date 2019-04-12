
<%@ page import="org.pih.warehouse.core.PartyRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'partyRole.label', default: 'PartyRole')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['partyRole']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['partyRole']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'partyRole.id.label', default: 'Id')}" />
                            
                                <th><warehouse:message code="partyRole.party.label" default="Party" /></th>
                            
                                <g:sortableColumn property="roleType" title="${warehouse.message(code: 'partyRole.roleType.label', default: 'Role Type')}" />
                            
                                <g:sortableColumn property="startDate" title="${warehouse.message(code: 'partyRole.startDate.label', default: 'Start Date')}" />
                            
                                <g:sortableColumn property="endDate" title="${warehouse.message(code: 'partyRole.endDate.label', default: 'End Date')}" />
                            
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${partyRoleInstanceList}" status="i" var="partyRoleInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td><g:link action="edit" id="${partyRoleInstance.id}">${fieldValue(bean: partyRoleInstance, field: "id")}</g:link></td>
                            
                                <td>${fieldValue(bean: partyRoleInstance, field: "party")}</td>
                            
                                <td>${fieldValue(bean: partyRoleInstance, field: "roleType")}</td>
                            
                                <td><format:date obj="${partyRoleInstance.startDate}" /></td>
                            
                                <td><format:date obj="${partyRoleInstance.endDate}" /></td>
                            
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${partyRoleInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
