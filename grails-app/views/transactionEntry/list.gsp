
<%@ page import="org.pih.warehouse.inventory.TransactionEntry" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transactionEntry.label', default: 'TransactionEntry')}" />
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
	            	<span class="linkButton">
	            		<g:link class="new" action="create"><warehouse:message code="default.add.label" args="['transactionEntry']"/></g:link>
	            	</span>
            	</div>
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'transactionEntry.id.label', default: 'Id')}" />
                        
                            <th><warehouse:message code="transactionEntry.inventoryItem.label" default="Inventory Item" /></th>
                   	    
                            <g:sortableColumn property="quantity" title="${warehouse.message(code: 'transactionEntry.quantity.label', default: 'Quantity')}" />
                        
                            <g:sortableColumn property="comments" title="${warehouse.message(code: 'transactionEntry.comments.label', default: 'Comments')}" />
                        
                            <th><warehouse:message code="transactionEntry.transaction.label" default="Transaction" /></th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${transactionEntryInstanceList}" status="i" var="transactionEntryInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="edit" id="${transactionEntryInstance.id}">${fieldValue(bean: transactionEntryInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: transactionEntryInstance, field: "inventoryItem")}</td>
                        
                            <td>${fieldValue(bean: transactionEntryInstance, field: "quantity")}</td>
                        
                            <td>${fieldValue(bean: transactionEntryInstance, field: "comments")}</td>
                        
                            <td>${fieldValue(bean: transactionEntryInstance, field: "transaction")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${transactionEntryInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
