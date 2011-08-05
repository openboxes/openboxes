
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        <title><warehouse:message code="default.view.label" args="[entityName.toLowerCase()]" /></title>    
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
     
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    
            
            <div style="padding: 10px; ">
            	<g:if test="${params?.product?.id}">
		            <g:link class="bullet" controller="inventoryItem" action="showStockCard" params="['product.id': params?.product?.id]"><button>&lsaquo; Back to Stock Card</button></g:link> 
	            </g:if>
	            <g:else>
		            <g:link class="bullet" controller="inventory" action="listAllTransactions"><button>&lsaquo; <warehouse:message code="transaction.back.label"/></button></g:link> 
		    	</g:else>
			</div>
			<div class="dialog">
			
				<g:form>
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${transactionInstance?.inventory?.id}"/>
						<fieldset>
							<table>
								<tr class="prop odd">
									<td>
										<label><warehouse:message code="transaction.transactionId.label"/></label>
									</td>
									<td>
										<span class="value">
											<g:if test="${transactionInstance?.id }">
												${transactionInstance?.id }
											</g:if>
											<g:else><span class="fade"><warehouse:message code="transaction.new.label"/></span></g:else>
										</span>
									</td>
								</tr>
								<tr class="prop even">
									<td>
										<label><warehouse:message code="transaction.date.label"/></label>
									</td>
									<td>
										<span class="value">
											<format:date obj="${transactionInstance?.transactionDate}"/>
										</span>
									</td>										
								</tr>
								<tr class="prop odd">
									<td>
										<label><warehouse:message code="transaction.type.label"/></label>
									</td>
									<td>
										<span class="value ${transactionInstance?.transactionType?.transactionCode?.name()?.toLowerCase()}">
											<format:metadata obj="${transactionInstance?.transactionType}"/>
										</span>
									</td>										
								</tr>
								<tr id="inventory-li" class="prop even">
									<td>
										<label><warehouse:message code="inventory.label"/></label>
									</td>
									<td>
										<span class="value">
											${warehouseInstance?.name }
										</span>								
									</td>										
								</tr>
								<tr class="prop odd">
									<td>
										<label><warehouse:message code="transaction.numEntries.label"/></label>
									</td>
									<td>
										<span class="value">
											${transactionInstance?.transactionEntries?.size() }
										</span>
									</td>
								</tr>								
								<g:if test="${transactionInstance?.source }">
									<tr class="prop even">
										<td>
											<label><warehouse:message code="default.from.label"/></label>
										</td>
										<td>
											<span class="value">
												${transactionInstance?.source?.name }
											</span>
										</td>										
									</tr>
								</g:if>
								<g:if test="${transactionInstance?.destination }">
									<tr class="prop even">
										<td>
											<label><warehouse:message code="default.to.label"/></label>
										</td>
										<td>
											<span class="value">
												${transactionInstance?.destination?.name }
											</span>
										</td>										
									</tr>
								</g:if>
								
								
							</table>
						</fieldset>
						<fieldset>
							<table>
								<g:if test="${transactionInstance?.id }">
									<tr class="prop even">
										<td colspan="2">
										
											<table id="prodEntryTable" border="0" style="border: 0px solid #ccc;">
												<tr>
													<th><warehouse:message code="product.label"/></th>
													<th><warehouse:message code="product.lotNumber.label"/></th>
													<th><warehouse:message code="product.expirationDate.label"/></th>
													<th><warehouse:message code="default.qty.label"/></th>
													<th>&nbsp;</th>
												</tr>
												<g:if test="${transactionInstance?.transactionEntries }">
													<g:each in="${transactionInstance?.transactionEntries.sort { it?.inventoryItem?.product?.name } }" var="transactionEntry" status="status">
														<tr class="${(status%2==0)?'odd':'even'}">
															<td style="text-align: left;">
																<g:link controller="inventoryItem" action="showStockCard" params="['product.id':transactionEntry?.inventoryItem?.product?.id]">
																	<format:product product="${transactionEntry?.inventoryItem?.product}"/>
																</g:link>
															</td>										
															<td>
																${transactionEntry?.inventoryItem?.lotNumber }
															</td>		
															<td>
																<format:expirationDate obj="${transactionEntry?.inventoryItem?.expirationDate}"/>															
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
														<td colspan="6"><warehouse:message code="transaction.noEntries.message"/></td>
													</tr>
												</g:else>
											</table>	
										</td>
									</tr>
								</g:if>
							</table>
						</fieldset>	
						
						<fieldset>
							<div style="text-align: center; padding: 10px;" class="odd">
								<button class="positive" name="_action_editTransaction" id="${transactionInstance?.id }">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" />
								    &nbsp;${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}&nbsp;        						
								</button>
								&nbsp;
								<button class="negative" name="_action_deleteTransaction" id="${transactionInstance?.id }" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
			    					<img src="${createLinkTo(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" />
									&nbsp;${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}&nbsp;
								</button>							
							</div>
						</fieldset>
					
				</g:form>
			</div>
		</div>
    </body>
</html>
