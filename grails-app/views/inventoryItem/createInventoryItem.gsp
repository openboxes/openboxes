
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

				<div style="text-align: left; margin-left: 15px;">
					<g:link controller="inventoryItem" 
						action="showStockCard" params="['product.id':itemInstance?.product?.id]">
						&lsaquo; Back to Stock Card
					</g:link>
				</div>	
				
				
				<table>
					<tr>
						<td style="width:250px;">
							<g:render template="productDetails" model="[productInstance:itemInstance.product, inventoryLevelInstance: inventoryLevelInstance]"/>
						</td>
						<td>
							<g:form action="saveInventoryItem">
								<g:hiddenField name="id" value="${itemInstance?.id}"/>
								<g:hiddenField name="inventory.id" value="${params?.inventory?.id}"/>
								<g:hiddenField name="product.id" value="${itemInstance?.product?.id }"/>
								<fieldset>	
									<table>
										<thead>
											<tr class="odd">
												<th>ID</th>
												<th>Lot/Serial Number</th>
												<th>Description</th>
												<th>Expires</th>
												<th style="text-align: center;">Initial Qty</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<g:set var="counter" value="${0 }"/>
											<g:each var="inventoryItem" in="${inventoryItems}" status="status">
												<tr class="${(status % 2)?'odd':'even' }">
													<td>
														${inventoryItem?.id }
													</td>
													<td>
														${inventoryItem?.lotNumber }
													</td>
													<td>
														${inventoryItem?.description }
													</td>
													<td>
														<g:if test="${inventoryItem?.expirationDate }">
															<g:formatDate date="${inventoryItem?.expirationDate }" format="MM-yyyy"/>
														</g:if>
														<g:else>
															<span class="fade">never</span>
														</g:else>
													</td>
													<td style="text-align: center;">
														n/a
													</td>
													<td>
														<g:link controller="inventoryItem" action="deleteInventoryItem" id="${inventoryItem?.id }">
															<img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png') }" alt="Delete" />
														</g:link>
													</td>
												</tr>
											</g:each>
											<tr >
												<td>
													${itemInstance?.id }
												</td>
												<td>
													<g:lotNumberComboBox id="lotNumberId" name="lotNumber" valueName="${params.lotNumber }"
														value="${params.lotNumber }"/>
												</td>
												<td>
													<g:textField name="description" 
														value="${itemInstance?.description }" size="25"/>
												</td>
												<td>
													<g:jqueryDatePicker id="expirationDate-${status }" 
														name="expirationDate" 
														value="${itemInstance.expirationDate}" 
														format="MM/dd/yyyy"/>
												</td>
												<td style="text-align: center;">
													<g:textField name="quantity" size="3" style="text-align: center;"
														value="${params.quantity }"/>
												</td>
												<td>
																									
												</td>
											</tr>
										</tbody>
									</table>
									<div style="text-align: center; padding: 10px; border-top: 1px solid lightgrey; ">
					                    <span class="buttons">
						                    <g:actionSubmit class="save" action="saveInventoryItem" value="${message(code: 'default.button.save.label', default: 'Save')}" />
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
