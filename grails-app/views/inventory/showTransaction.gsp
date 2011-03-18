
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>    
        <style>
        	optgroup { font-weight: bold; } 
        	#transactionEntryTable { border: 1px solid #ccc; } 
			#transactionEntryTable td { padding: 5px; text-align: center; }
			#transactionEntryTable th { text-align: center; } 
        	#prodSelectRow { padding: 10px; }  
        	#transactionEntryTable td.prodNameCell { text-align: left; } 
			.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.header th { background-color: #525D76; color: white; }         	
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
				
						<ul>
							<li class="prop odd">
								<label>Transaction ID</label>
								<span class="value">
									<g:if test="${transactionInstance?.id }">
										${transactionInstance?.id }
									</g:if>
									<g:else><span class="fade">(new transaction)</span></g:else>
								</span>
							</li>
							<li class="prop even">
								<label>Transaction Date</label>
								<span class="value">
									<g:formatDate date="${transactionInstance?.transactionDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}"/>
								</span>
							</li>
							<li class="prop odd">
								<label>Transaction Type</label>
								<span class="value">
									${transactionInstance?.transactionType?.name }

								</span>
							</li>
							<li id="inventory-li" class="prop even">
								<label>Inventory</label>
								<span class="value">
									${warehouseInstance?.name }
								</span>								
							</li>
							<g:if test="${transactionInstance?.source }">
								<li class="prop odd">
									<label>From</label>
									<span class="value">
										${transactionInstance?.source?.name }
									</span>
								</li>
							</g:if>
							<g:if test="${transactionInstance?.destination }">
								<li class="prop odd">
									<label>To</label>
									<span class="value">
										${transactionInstance?.destination?.name }
									</span>
								</li>
							</g:if>
									
							<g:if test="${transactionInstance?.id }">
								<li class="prop even">
									<span class="value">
										<table id="prodEntryTable" border="1" style="border: 1px solid #ccc;">
											<tr>
												<th>Product</th>
												<th>Lot Number</th>
												<th>Expiration Date</th>
												<th>Qty</th>
												<th>&nbsp;</th>
											</tr>
											<g:if test="${transactionInstance?.transactionEntries }">
											
												<g:each in="${transactionInstance?.transactionEntries.sort { it.product.name } }" var="transactionEntry" status="status">
													<tr class="${(status%2==0)?'odd':'even'}">
														<td style="text-align: left;">
															${transactionEntry?.product?.name }
														</td>										
														<td>
															${transactionEntry?.inventoryItem?.lotNumber }
														</td>		
														<td>
															${transactionEntry?.inventoryItem?.expirationDate }
														</td>
														<td>
															${transactionEntry?.quantity}
														</td>		
														<td></td>
													</tr>
												</g:each>
											</g:if>
											<g:else>
												<tr>
													<td colspan="6">There are no entries</td>
												</tr>
											</g:else>
										</table>
									</td>
								</li>
							</g:if>
						</table>
						<div style="text-align: center; padding: 10px;">
							<button class="positive" name="_action_editTransaction" id="${transactionInstance?.id }">
								<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" />
							    ${message(code: 'default.button.edit.label', default: 'Edit')}        						
							</button>
							&nbsp;
							<button class="negative" name="_action_deleteTransaction" id="${transactionInstance?.id }" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
		    					<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" />
								${message(code: 'default.button.delete.label', default: 'Delete')}
							</button>							
						</div>
							
				</g:form>
			</div>
		</div>
    </body>
</html>
