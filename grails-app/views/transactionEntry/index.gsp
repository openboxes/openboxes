
<%@ page import="org.pih.warehouse.inventory.TransactionEntry" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="custom">
		<g:set var="entityName" value="${message(code: 'transactionEntry.label', default: 'TransactionEntry')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-transactionEntry" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-transactionEntry" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<th><g:message code="transactionEntry.inventoryItem.label" default="Inventory Item" /></th>
					
						<g:sortableColumn property="quantity" title="${message(code: 'transactionEntry.quantity.label', default: 'Quantity')}" />
					
						<g:sortableColumn property="comments" title="${message(code: 'transactionEntry.comments.label', default: 'Comments')}" />
					
						<th><g:message code="transactionEntry.transaction.label" default="Transaction" /></th>
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${transactionEntryInstanceList}" status="i" var="transactionEntryInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${transactionEntryInstance.id}">${fieldValue(bean: transactionEntryInstance, field: "inventoryItem")}</g:link></td>
					
						<td>${fieldValue(bean: transactionEntryInstance, field: "quantity")}</td>
					
						<td>${fieldValue(bean: transactionEntryInstance, field: "comments")}</td>
					
						<td>${fieldValue(bean: transactionEntryInstance, field: "transaction")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${transactionEntryInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
