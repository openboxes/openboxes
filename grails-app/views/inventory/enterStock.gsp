
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'transaction.label', default: 'Transaction')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>    
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


				<g:form action="searchStock" method="GET">
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${warehouseInstance?.inventory?.id}"/>
					<fieldset>			
						<legend>Search Stock</legend>	
						<table>
							<tr>
								<td>
									<span>Search by barcode, lot number, serial number, product description</span>
								</td>
							</tr>
							<tr>
								<td align="center">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'magnifier.png') }"/>
									<input type="text" name="query" value="${params?.query}"/>
									<g:submitButton name="submit" value="Go"/>
								</td>
							</tr>
						</table>
					</fieldset>
				</g:form>				

			
				<g:form>
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${warehouseInstance?.inventory?.id}"/>
				
				
				
					<fieldset>
						<legend>Enter Stock</legend>
						<table>
							<tr class="prop">
								<td class="name">
									<label>Item</label>
								</td>
								<td class="value">
									${productInstance?.name } 
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label>Lot/SN</label>
								</td>
								<td class="value">
									<g:textField name="lotNumber" size="10" value="${inventoryItem?.lotNumber }"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label>Quantity</label>
								</td>
								<td class="value">
									<g:textField name="quantity" size="3"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label>Date</label>
								</td>
								<td class="value">
									<g:jqueryDatePicker id="transactionDate" name="transactionDate"
											value="${transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>	
								</td>
							</tr>
							<tr class="prop">
								<td class="name">
									<label>Direction</label>
								</td>
								<td class="value">
									<g:radio name="direction" value="shipping"/> Shipping to
									<div style="padding-left: 16px;">							
										<g:select name="destination.id" 
													from="${org.pih.warehouse.inventory.Warehouse.list()}" 
													optionKey="id" 
													optionValue="name"
													value="${transactionInstance?.destination}" 
													noSelection="['':'']"
													style="width: 180px" />
									</div>
									<br/>
									<g:radio name="direction" value="receving"/> Receiving from	
									<div style="padding-left: 16px;">							
										<g:select name="destination.id" 
													from="${org.pih.warehouse.inventory.Warehouse.list()}" 
													optionKey="id" 
													optionValue="name"
													value="${transactionInstance?.source}" 
													noSelection="['':'']"
													style="width: 180px" />
									</div>
								</td>
							</tr>
							
							
							<tr class="prop">
								<td colspan="2" style="text-align: center">
									<button name="_action_saveTransaction">
				    					<img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" />
					                    ${message(code: 'default.button.save.label', default: 'Save')}						
									</button>
									&nbsp;
									
									<g:link action="showTransaction" id="${transactionInstance?.id }">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="Cancel" />
					                    ${message(code: 'default.button.cancel.label', default: 'Cancel')}						
									</g:link>			
											
								</td>
															
							
							</tr>
						
							<%-- 
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
									<g:jqueryDatePicker id="transactionDate" name="transactionDate"
											value="${transactionInstance?.transactionDate}" format="MM/dd/yyyy"/>
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Transaction Type</label></td>
								<td class="value">
									<g:select name="transactionType.id" from="${transactionTypeList}" 
			                       		optionKey="id" optionValue="name" value="${transactionInstance.transactionType?.id}" noSelection="['null': '']" />
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Source</label></td>
								<td class="value">
									<g:select name="source.id" from="${warehouseInstanceList}" 
			                       		optionKey="id" optionValue="name" value="${transactionInstance?.source?.id}" noSelection="['null': '']" />
								</td>
							</tr>
							<tr class="prop">
								<td class="name"><label>Destination</label></td>
								<td class="value">
									${warehouseInstance?.name }
									<g:hiddenField name="destination.id" value="${warehouseInstance?.id }"/>
								</td>
							</tr>
							--%>
							
							<%--
							<g:if test="${transactionInstance?.id }">
								<tr class="prop">
									<td class="name"><label>Confirmed</label></td>
									<td class="value">
										<g:checkBox name="confirmed" value="${transactionInstance?.confirmed }"/>
				                	</td>
				                </tr>
								<tr class="prop">
									<td class="name"><label>Confirmed by</label></td>
									<td class="value">
										<g:select name="confirmedBy.id" from="${org.pih.warehouse.core.User.list()}" 
				                       		optionKey="id" optionValue="name" value="${transactionInstance?.confirmedBy?.id}" noSelection="['null': '']" />									
									</td>
								</tr>							
								<tr class="prop">
									<td class="name"><label>Confirmed on</label></td>
									<td class="value">
										<g:jqueryDatePicker id="dateConfirmed" name="dateConfirmed"
												value="${transactionInstance?.dateConfirmed}" format="MM/dd/yyyy"/>
										
									</td>
								</tr>							
								<tr class="prop">
									<td class="name">
										<label>Transaction Entries</label>
									</td>
									<td class="value">
										
										<table id="prodEntryTable" border="1" style="border: 1px solid #ccc;">
											<tr>
												<th>ID</th>
												<th>Product</th>
												<th>Qty</th>
												<th>Lot Number</th>
												<th>Expiration Date</th>
												<th>&nbsp;</th>
											</tr>
											<g:if test="${transactionInstance?.transactionEntries }">
												<g:each in="${transactionInstance?.transactionEntries.sort { it.inventoryItem?.product.name } }" var="transactionEntry" status="status">
													<tr class="${(status%2==0)?'odd':'even'}">
														<td>
															${transactionEntry?.id }
														</td>
														<td style="text-align: left;">
															${transactionEntry?.inventoryItem?.product?.name }
														</td>										
														<td>
															${transactionEntry?.quantity}
														</td>		
														<td>
															${transactionEntry?.inventoryItem?.lotNumber }
														</td>		
														<td>
															${transactionEntry?.inventoryItem?.expirationDate }
														</td>
														<td></td>
													</tr>
												</g:each>
											</g:if>
																					 
											<tr id="prodSelectRow" >
												<td colspan="5" style="text-align: center; padding: 10px;">
													<select id="productSelect">
														<option value="">Choose a product to add</option>
														<g:each var="key" in="${productInstanceMap.keySet() }">
															<g:set var="productInstanceList" value="${productInstanceMap.get(key) }"/>
															<optgroup label="${key?.name?:'None'}"></optgroup>
															<g:each var="productInstance" in="${productInstanceList}">
																<option value="${productInstance?.id }">${productInstance?.name }</option>
															</g:each>
														</g:each>
													</select>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</g:if>
							 --%>
						</table>
						
						
					</fieldset>
				</g:form>
				
			</div>
		</div>

    </body>
</html>
