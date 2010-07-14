<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /> Transactions</content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>       
		<content tag="breadcrumb"><g:render template="breadcrumb" model="[warehouse:warehouseInstance]"/></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">

				<h2>Transactions</h2>
				<table>
					<thead>
						<tr>
							<th>ID</th>
							<th>Transaction Date</th>
							<th>Details</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${transactions}" var="transaction" status="i">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
								<td>
								${fieldValue(bean: transaction, field: "id")}
								</td>
								<td>
								${fieldValue(bean: transaction, field: "transactionDate")}
								</td>
								<td><g:link controller="transaction" action="show"
									id="${transaction.id}">Show Details</g:link></td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>


	  <!--

	  Should allow user to reorder

            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${warehouseInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
	  -->
        </div>
    </body>
</html>
