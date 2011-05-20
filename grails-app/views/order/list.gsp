
<%@ page import="org.pih.warehouse.order.Order" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'order.label', default: 'Order')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div>            	
	            	<span class="menuButton">
	            		<g:link class="new" action="create"><g:message code="default.add.label" args="['order']"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="orderNumber" title="${message(code: 'order.orderNumber.label', default: 'Order Number')}" />

                            <g:sortableColumn property="description" title="${message(code: 'order.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="dateCreated" title="${message(code: 'order.dateCreated.label', default: 'Date Created')}" />
                        
                            <g:sortableColumn property="lastUpdated" title="${message(code: 'order.lastUpdated.label', default: 'Last Updated')}" />
                        
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${orderInstanceList}" status="i" var="orderInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>
                            	
                            	<g:link action="show" id="${orderInstance.id}">
                            		<g:if test="${orderInstance?.orderNumber }">
	                            		${fieldValue(bean: orderInstance, field: "orderNumber")}
                            		</g:if>
                            		<g:else>
	                            		${fieldValue(bean: orderInstance, field: "id")}
                            		</g:else>
                            	</g:link>
                            </td>
                        
                            <td>${fieldValue(bean: orderInstance, field: "description")}</td>

                            <td><g:formatDate date="${orderInstance.dateCreated}" /></td>
                        
                            <td><g:formatDate date="${orderInstance.lastUpdated}" /></td>
                        
                        
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${orderInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
