
<%@ page import="org.pih.warehouse.core.Party" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'party.label', default: 'Party')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['party']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['party']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                            
                                <g:sortableColumn property="id" title="${warehouse.message(code: 'party.id.label', default: 'Id')}" />
                            
                                <th><warehouse:message code="party.partyType.label" default="Party Type" /></th>
                            
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${partyInstanceList}" status="i" var="partyInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            
                                <td><g:link action="edit" id="${partyInstance.id}">${fieldValue(bean: partyInstance, field: "id")}</g:link></td>
                            
                                <td>${fieldValue(bean: partyInstance, field: "partyType")}</td>
                            
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${partyInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
