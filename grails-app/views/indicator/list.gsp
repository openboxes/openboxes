
<%@ page import="org.pih.warehouse.reporting.Indicator" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'indicator.label', default: 'Indicator')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="button-bar">
                    <g:link class="button" action="list"><warehouse:message code="default.list.label" args="['indicator']"/></g:link>
                    <g:link class="button" action="create"><warehouse:message code="default.add.label" args="['indicator']"/></g:link>
	        	</div>

                <div class="box">
                    <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                    <table>
                        <thead>
                            <tr>
                                <g:sortableColumn property="name" title="${warehouse.message(code: 'indicator.name.label', default: 'Name')}" />

                                <g:sortableColumn property="description" title="${warehouse.message(code: 'indicator.description.label', default: 'Description')}" />
                            
                                <g:sortableColumn property="expression" title="${warehouse.message(code: 'indicator.expression.label', default: 'Expression')}" />

                                <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'indicator.dateCreated.label', default: 'Date Created')}" />

                                <g:sortableColumn property="lastUpdated" title="${warehouse.message(code: 'indicator.lastUpdated.label', default: 'Last Updated')}" />
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${indicatorInstanceList}" status="i" var="indicatorInstance">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                <td><g:link action="edit" id="${indicatorInstance.id}">${fieldValue(bean: indicatorInstance, field: "name")}</g:link></td>

                                <td>${fieldValue(bean: indicatorInstance, field: "description")}</td>
                            
                                <td>${fieldValue(bean: indicatorInstance, field: "expression")}</td>

                                <td><format:date obj="${indicatorInstance.dateCreated}" /></td>

                                <td><format:date obj="${indicatorInstance.lastUpdated}" /></td>
                            

                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${indicatorInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
