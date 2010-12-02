
<%@ page import="org.pih.warehouse.product.Product" %>
<%@ page import="org.pih.warehouse.product.DrugProduct" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>    
        <style>
        	optgroup { font-weight: bold; } 
        	#transactionEntryTable { border: 1px solid #ccc; } 
			#transactionEntryTable td { padding: 5px; text-align: center; }
			#transactionEntryTable th { text-align: center; } 
        	#prodSelectRow { padding: 10px; }  
        	#transactionEntryTable td.prodNameCell { text-align: left; } 
        	
        </style>
    </head>    

    <body>
        <div class="body">
        
			<div class="nav">
				<g:render template="nav"/>
			</div>
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    
            
            
			<div class="dialog">
			
				<g:form>
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${transactionInstance?.inventory?.id}"/>
				
					<fieldset>
						<legend>Transaction Details</legend>
						<table>
							<tr class="prop">
								<td class="name"><label>Transaction ID</label></td>
								<td class="value">
									<g:if test="${transactionInstance?.id }">
										${transactionInstance?.id }
									</g:if>
									<g:else><span class="fade">(new transaction)</span></g:else>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label>Transaction Date</label>
								</td>
								<td class="value">
									<g:formatDate date="${transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Transaction Type</label></td>
								<td class="value">
									${transactionInstance?.transactionType?.name }

								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Source</label></td>
								<td class="value">
									${transactionInstance?.source?.name }
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Destination</label></td>
								<td class="value">
									${transactionInstance?.destination?.name }
								</td>
							</tr>
						</table>
					</fieldset>
									
					<g:if test="${transactionInstance?.id }">
						
						<fieldset>
							<legend>Transaction Entries</legend>
							<table id="transactionEntryTable">
								<tr>
									<td colspan="2">
										<table id="prodEntryTable" border="1" style="border: 1px solid #ccc;">
											<tr>
												<th>ID</th>
												<th>Product</th>
												<th>Qty</th>
												<th>Lot Number</th>
												<th>Expiration Date</th>
												<th>&nbsp;</th>
											</tr>
											<g:each in="${transactionInstance?.transactionEntries.sort { it.product.name } }" var="transactionEntry" status="status">
												<tr class="${(status%2==0)?'even':'odd'}">
													<td>
														${transactionEntry?.id }
													</td>
													<td style="text-align: left;">
														${transactionEntry?.product?.name }
													</td>										
													<td>
														${transactionEntry?.quantity}
													</td>		
													<td>
														${transactionEntry?.inventoryItem?.lotNumber }
													</td>		
													<td>
														${transactionEntry?.inventoryItem?.inventoryLot?.expirationDate }
													</td>
												</tr>
											</g:each>
										</table>
									</td>
								</tr>
							</table>
						</g:if>
						
						<div class="buttonBar">
							<g:actionSubmit value="Save" action="saveTransaction" />
						</div>
						
						
					</fieldset>
				</g:form>			
			</div>
		</div>
	</body>
</html>
