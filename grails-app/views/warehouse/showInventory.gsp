
<%@ page import="org.pih.warehouse.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <g:set var="pageTitle" value="${message(code: 'default.show.label' args="[entityName]")}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /> Inventory</content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>       
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">            
	            <h2>Show current inventory</h2>
                <table>
					<thead>
						<tr>
							<th>Product</th>
							<th>Quantity</th>
							<th>Reorder Level</th>
						</tr>
					</thead>
					<tbody>
						<g:each var="inventoryLineItem" in="${inventory}" status="i">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
								<td>${inventoryLineItem.key}</td>
								<td>${inventoryLineItem.value}</td>
								<td></td>
							</tr>
						</g:each>
		   <%--

			<td>${fieldValue(bean: inventoryLineItem, field: "product.name")}</td>
			<td>${fieldValue(bean: inventoryLineItem, field: "quantity")}</td>


		      <g:each in="${warehouseInstance.inventory.inventoryLineItems}" var="inventoryLineItem" status="i">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			  <td>${fieldValue(bean: inventoryLineItem, field: "id")}</td>
                          <td>${fieldValue(bean: inventoryLineItem, field: "product.name")}</td>
                          <td>${fieldValue(bean: inventoryLineItem, field: "quantity")}</td>
                          <td>${fieldValue(bean: inventoryLineItem, field: "reorderQuantity")}</td>
                        </tr>
		      </g:each>

			--%>
		  </tbody>
		</table>

	      <h2>Show all Transactions</h2>
                <table>
		  <thead>
		    <tr>
		      <th>Product</th>
		      <th>Quantity Change</th>
		    </tr>
		  </thead>
                  <tbody>


		      <g:each in="${warehouseInstance.transactions}" var="transaction" status="i">

			<tr>
			  <td colspan="2" align="right" valign="middle">
			    <g:link controller="transactionEntry" action="create" id="${transaction.id}">Add Entry on ${fieldValue(bean: transaction, field: "transactionDate")}</g:link>
			  </td>
			</tr>

			<g:each in="${transaction.transactionEntries}" var="transactionEntry" status="j">
			  <tr class="${(j % 2) == 0 ? 'odd' : 'even'}">			    
			    <td>${fieldValue(bean: transactionEntry, field: "product.name")}</td>
			    <td>${fieldValue(bean: transactionEntry, field: "quantityChange")}</td>
			  </tr>
			</g:each>
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
