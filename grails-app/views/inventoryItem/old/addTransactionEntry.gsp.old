<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'stockCard.label', default: 'Stock Card')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>    
    </head>    

	<body>
       <div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>

            <g:hasErrors bean="${itemInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${itemInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="dialog">				
				<g:form action="postTransactionEntry" method="POST" autocomplete="off">
					<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
					<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
					<g:hiddenField name="product.id" value="${productInstance?.id}"/>
					
					<fieldset>				
						<legend>Enter a new transaction</legend>
						<table bgcolor="#efdfb7" border=0 cellspacing=0 cellpadding=0>
							<tr>
								<th>Date</th>
								<th>Type</th>
							</tr>
							<tr>
								<td>
									<g:jqueryDatePicker id="transactionDate" name="transactionDate" value="" format="MM/dd/yyyy"/>
								</td>
								<td>
									<g:select name="transactionType.id" from="${org.pih.warehouse.inventory.TransactionType.list()}" 
										optionKey="id" optionValue="name" value=""  />
								</td>
							</tr>
							<tr>
								<th>Lot Number</th>
								<th>Qty <super>*</super></th>	
							</tr>
							<tr>
								<td align=center>
									<g:textField name="lotNumber" size="10"/>
									
								</td>
								<td align=center>
									<g:textField name="quantity" size="3"/>
								</td>							
							</tr>
							<tr>
								<th>Source</th>
								<th>Destination</th>
							</tr>
							<tr>
								<td>
									<g:select name="source.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
										optionKey="id" optionValue="name" value="" noSelection="['0': '']" />							
								</td>
								<td>
									<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" 
										optionKey="id" optionValue="name" value="" noSelection="['0': '']" />
								</td>
							</tr>
							<tr>
								<td colspan="2" align=center>
									<input type=submit value="Submit">
								</td>
							</tr>
						</table>
						<p class="fade">
							<super>*</super> You must enter a minus sign (-) for negative stock movements (e.g. -100). 
						</p>
					</fieldset>
				</g:form>		
			</div>
		</div>
	</body>
</html>		