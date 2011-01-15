
<%@ page import="org.pih.warehouse.inventory.StockCardItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventoryItem.label', default: 'Inventory Item')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
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


			
				<table>
					<tr>
						<td style="width:250px;">
							<g:render template="productDetails" model="[productInstance:itemInstance.product]"/>
						</td>
						<td>
						
							<div style="text-align: left; padding: 10px;">
								<img src="${resource(dir: 'images/icons/silk', file: 'table_refresh.png')}"/>
								<g:link controller="inventoryItem" 
									action="showStockCard" params="['product.id':itemInstance?.product?.id]">Show Stock Card</g:link>
							</div>					
						
						
							<g:form action="save">
								<g:hiddenField name="id" value="${itemInstance?.id}"/>
								<g:hiddenField name="inventory.id" value="${params?.inventory?.id}"/>
								<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
								<g:hiddenField name="product.id" value="${itemInstance?.product?.id }"/>
								<fieldset>	
									<table>
										<thead>
											<tr>
												<th>ID</th>
												<th>Description</th>
												<th>Lot/Serial Number</th>
												<th>Expires</th>
												<th style="text-align: center;">Qty</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<g:set var="counter" value="${0 }"/>
											<g:each var="inventoryItem" in="${inventoryItems}" status="status">
												<tr class="${(status % 2 ==0)?'odd':'even' }">
													<td>
														${inventoryItem?.id }
													</td>
													<td>
														${inventoryItem?.description }
													</td>
													<td>
														${inventoryItem?.lotNumber }
													</td>
													<td>
														<g:formatDate date="${inventoryItem?.inventoryLot?.expirationDate }" format="MM-yyyy"/>
													</td>
													<td style="text-align: center;">
														${inventoryItem?.quantity}
													</td>
													<td>
														<g:link controller="inventoryItem" action="deleteInventoryItem" id="${inventoryItem?.id }">
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png') }" alt="Delete" />
														</g:link>
													</td>
												</tr>
												<%-- 
												<tr class="${(status % 2 ==0)?'odd':'even' }">
													<td>
														${inventoryItem?.id }
														<g:hiddenField name="inventoryItems[${status }].id" value="${inventoryItem?.id }"/>
														<g:hiddenField name="inventoryItems[${status }].product.id" value="${inventoryItem?.product?.id }"/>
													</td>
													<td>
														<g:textField name="inventoryItems[${status }].description" 
															value="${inventoryItem?.description }" size="25"/>
													</td>
													<td>
														<g:textField name="inventoryItems[${status }].lotNumber" 
															value="${inventoryItem?.lotNumber }" size="10"/>
													</td>
													<td>
														<g:jqueryDatePicker id="inventoryItems-expirationDate-${status }" 
															name="inventoryItems[${status }].expirationDate" 
															value="${inventoryItem?.inventoryLot?.expirationDate }" 
															format="MM/dd/yyyy"/>
													</td>
												</tr>
												--%>
											</g:each>
											<tr class="${(counter % 2 ==0)?'odd':'even' }">
												<td>
													${inventoryItem?.id }
												</td>
												<td>
													<g:textField name="description" 
														value="${inventoryItem?.description }" size="25"/>
												</td>
												<td>
													<%-- 
													<g:textField name="lotNumber" 
														value="${inventoryItem?.lotNumber }" size="10"/>
													--%>
													<g:lotNumberComboBox name="lotNumber" />
												</td>
												<td>
													<g:jqueryDatePicker id="expirationDate-${status }" 
														name="expirationDate" 
														value="" 
														format="MM/dd/yyyy"/>
												</td>
												<td style="text-align: center;">
													<%--
													<g:textField name="initialQuantity" 
														value="${inventoryLot?.initialQuantity }" size="3"/>
													--%>
												</td>
												<td>
																									
												</td>
											</tr>
										</tbody>
									</table>
									<div style="text-align: center; padding: 10px; border-top: 1px solid lightgrey; ">
					                    <span class="buttons">
						                    <g:actionSubmit class="save" action="save" value="${message(code: 'default.button.save.label', default: 'Save')}" />
					                    </span>
					                    &nbsp;
					                    <g:link controller="inventoryItem" 
									action="showStockCard" params="['product.id':itemInstance?.product?.id]">Cancel</g:link>
				                    </div>
								</fieldset>
							</g:form>		
						</td>
					</tr>
				</table>	
			</div>
        </div>
    </body>
</html>
