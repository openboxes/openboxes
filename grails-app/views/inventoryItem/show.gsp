
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'stockCard.label', default: 'Stock Card')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>    
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
				<fieldset>
										
					<h1>${itemInstance?.product?.name }
					
							<span class="fade">			
							${itemInstance?.product?.dosageStrength } ${itemInstance?.product?.dosageUnit }
							 ${itemInstance?.product?.dosageForm?.name }
							</span>
							<span style="font-size: 1em;">					
								<a href="${createLink(action: 'edit', id: itemInstance?.id, params: ['inventory.id':itemInstance?.inventory?.id]) }">edit</a>
							</span>					
					
					</h1> 
				</fieldset>
				
				<fieldset>
					<%-- 
					<table>
						<tr>
							<td>								
								<ul>
									<li>Min Quantity&nbsp;<label>${itemInstance?.minQuantity }</label> </li>
									<li>Reorder Quantity&nbsp;<label>${itemInstance?.reorderQuantity }</label></li>
									<li>Max Quantity&nbsp;<label>${itemInstance?.maxQuantity }</label></li>
								
								</ul>
							</td>														
							<td>
								<g:if test="${itemInstance?.warnings }">
									<div class="error">	
										<ul>										
											<g:each var="warning" in="${itemInstance?.warnings }">
												<li>
													<g:if test="${warning?.contains('error') || warning?.contains('alert')}">
														<img src="${resource(dir: 'images/icons/silk', file: 'exclamation.png') }" />
													</g:if>
													<g:elseif test="${warning?.contains('warning')}">
														<img src="${resource(dir: 'images/icons/silk', file: 'error.png') }" />
													</g:elseif>
													<g:elseif test="${warning?.contains('info')}">
														<img src="${resource(dir: 'images/icons/silk', file: 'information.png') }" />
													</g:elseif>														
													&nbsp;${message(code: warning)}								
												</li>							
											</g:each>
										</ul>						
									</div>
								</g:if>
								<g:else>
									No Alerts
								</g:else>									
							
							</td>
							<td style="text-align: right;">
								<!--<g:barcode4j fmt="png" type="code128" msg="${itemInstance?.lotNumber }" height="6" mw="0.2"/>-->
							</td>
						</tr>
						--%>
					</table>
					<h2>Transactions</h2>
					<table border="1" style="border:1px solid #f5f5f5">
	                    <thead>
	                        <tr>    	
	                            <g:sortableColumn property="transactionDate" title="${message(code: 'transaction.transactionDate.label', default: 'Transaction Date')}" />								
	                            <g:sortableColumn property="transactionType" title="${message(code: 'transaction.transactionType.label', default: 'Transaction Type')}" />								
	                            <g:sortableColumn property="source" title="${message(code: 'transaction.source.label', default: 'Origin')}" />								
	                            <g:sortableColumn property="destination" title="${message(code: 'inventory.destination.label', default: 'Destination')}" />								
	                            <g:sortableColumn property="quantity" title="${message(code: 'inventory.quantity.label', default: 'Qty')}" />								
								<th>Actions</th>
	                        </tr>
	                    </thead>
	       	           	<tbody>
		                    <g:if test="${transactionEntryList}">
		       	           		<g:each var="transactionEntry" in="${transactionEntryList}" status="i">	       	           		
									<tr class="${(i%2==0)?'odd':'even' }">
										<td>
											<g:formatDate date="${transactionEntry?.transaction?.transactionDate}" format="MMM dd"/></td>
										<td>${transactionEntry?.transaction?.transactionType?.name }</td>
										<td>${transactionEntry?.transaction?.source?.name }</td>
										<td>${transactionEntry?.transaction?.destination?.name }</td>
										<td style="text-align: right;">${transactionEntry?.quantity}</td>
										<td>
											<g:link controller="inventoryItem" action="deleteTransactionEntry" id="${transactionEntry?.id }" params="['inventoryItem.id':itemInstance?.id]">Remove</g:link>	
										</td>
									</tr>			
								</g:each>
							</g:if>			
							<g:else>
								<tr>
									<td colspan="6" style="text-align: center">
										<div class="fade" style="padding:50px;">enter a transaction below</div>
									</td>
								</tr>
							</g:else>				
						</tbody>
						<tfoot>
							<tr style="height: 40px;">
								<th colspan="5" style="text-align: right; vertical-align: middle; font-size: 1.5em;">
									<img src="${resource(dir: 'images/icons/silk', file: 'sum.png') }"/>&nbsp;Total Quantity
									 &nbsp; = &nbsp;
									${itemInstance?.quantity }
								</th>
								<th></th>
							</tr>
							
						</tfoot>
					</table>
				</fieldset>
			</div>
			<div class="dialog">
				<g:form action="addTransactionEntry">
					<g:hiddenField name="inventory.id" value="${itemInstance?.inventory?.id}"/>
					<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
					<fieldset>				
						<legend>
							<h1>Enter a new transaction</h1>
						</legend>
						<table bgcolor="#efdfb7" border=0 cellspacing=0 cellpadding=2>
							<tr>
								<th>Date</th>
								<th>Type</th>
								<th>Lot Number</th>
								<th>Quantity <super>*</super></th>	
								<th>Origin</th>
								<th>Destination</th>
								<th></th>
							</tr>
							<tr>
								<td>
									<g:jqueryDatePicker id="transactionDate" name="transactionDate" value="" format="MM/dd/yyyy"/>
								</td>
								<td>
									<g:select name="transactionType.id" from="${org.pih.warehouse.inventory.TransactionType.list()}" 
										optionKey="id" optionValue="name" value=""  />
								</td>
								<td align=center>
									${itemInstance?.lotNumber }
								</td>
								<td align=center>
									<g:textField name="quantity" size="5"/>
								</td>							
								<td>
									<g:select name="source.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value=""  />							
								</td>
								<td>
									<g:select name="destination.id" from="${org.pih.warehouse.inventory.Warehouse.list()}" optionKey="id" value=""  />
								</td>
								<td align=center>
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
