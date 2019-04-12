
<%@ page import="org.pih.warehouse.core.PartyType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'partyType.label', default: 'PartyType')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['partyType']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['partyType']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'partyType.id.label', default: 'Id')}" />

                                <g:sortableColumn property="code" title="${warehouse.message(code: 'partyType.code.label', default: 'Code')}" />

                                <g:sortableColumn property="name" title="${warehouse.message(code: 'partyType.name.label', default: 'Name')}" />
                            
                                <g:sortableColumn property="description" title="${warehouse.message(code: 'partyType.description.label', default: 'Description')}" />
                            
                                <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'partyType.dateCreated.label', default: 'Date Created')}" />
                            
                                <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'partyType.lastUpdated.label', default: 'Last Updated')}" />
                            
                                <g:sortableColumn property="partyTypeCode" title="${warehouse.message(code: 'partyType.partyTypeCode.label', default: 'Party Type Code')}" />
                            
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${partyTypeInstanceList}" status="i" var="partyTypeInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td><g:link action="edit" id="${partyTypeInstance.id}">${fieldValue(bean: partyTypeInstance, field: "id")}</g:link></td>

                                <td>${fieldValue(bean: partyTypeInstance, field: "code")}</td>

                                <td>${fieldValue(bean: partyTypeInstance, field: "name")}</td>
                            
                                <td>${fieldValue(bean: partyTypeInstance, field: "description")}</td>
                            
                                <td><format:date obj="${partyTypeInstance.dateCreated}" /></td>
                            
                                <td><format:date obj="${partyTypeInstance.lastUpdated}" /></td>
                            
                                <td>${fieldValue(bean: partyTypeInstance, field: "partyTypeCode")}</td>
                            
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${partyTypeInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
