
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${message(code: 'inventory.label', default: 'inventory')}" />
<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>

<body>
<div class="body">

	<div class="nav">
		<g:render template="../inventory/nav"/>
	</div>
	<g:if test="${flash.message}">
		<div class="message">
			${flash.message}
		</div>
	</g:if> 
	<g:hasErrors bean="${itemInstance}">
		<div class="errors"><g:renderErrors bean="${itemInstance}" as="list" /></div>
	</g:hasErrors>
	<div class="dialog">		
		<table >
			<tr>
				<td style="width: 250px;">
					<style>
						span.name {   } 
						span.value { font-size: 1.0em; } 
						span.name:after { content: ": " }
						th { color: lightgrey; } 
						fieldset#productDetails { padding: 10px; margin: 0px;} 
						fieldset#productDetails table td { padding: 6px; } 
					</style>
					<fieldset id="productDetails">
						<legend><span class="fade">Product Details</span></legend>											
						<table>
							<tr class="">	
								<td style="text-align: left;">
									<span class="name">Description</span>
								</td>
								<td>
									<span class="value">${productInstance?.name }</span>
								</td>
							</tr>
							<tr class="">	
								<td style="text-align: left;">
									<span class="name">Product Code</span>
								</td>
								<td>
									<span class="value">${productInstance?.productCode?:'<span class="fade">none</span>' }</span>
								</td>
							</tr>
							
							<tr class="">	
								<td style="text-align: left;">
									<span class="name">Cold Chain</span>
								</td>
								<td>
									<span class="value">${productInstance?.coldChain?'Yes':'No' }</span>
								</td>
							</tr>
							<tr class="">	
								<td style="text-align: left;">
									<span class="name">Category</span>
								</td>
								<td>
									<g:if test="${productInstance?.category?.parentCategory }">
										<span class="value">${productInstance?.category?.parentCategory?.name } &rsaquo;</span>
									</g:if>
									<span class="value">${productInstance?.category?.name }
								</td>
							</tr>
							<g:each var="productAttribute" in="${productAttributes}">
							<tr>
								<td style="text-align: left;">
									<span class="name">${productAttribute?.name }</span>
								</td>
								<td>
									<span class="value">${productAttribute.value }</span>
								</td>
							</tr>													
							</g:each>
							<tr>
								<td colspan="3"><hr/></td>
							</tr>
							<tr class="">
								<td style="text-align: left;">
									<span class="name">Minimum Qty</span>
								</td>
								<td>
									<span class="value">${inventoryLevelInstance?.minQuantity?:'<span class="fade">Not Configured</span>' }</span>
								</td>
							</tr>
							<tr class="">
								<td style="text-align: left;">
									<span class="name">Reorder Qty</span>
								</td>
								<td>
									<span class="value">${inventoryLevelInstance?.reorderQuantity?:'<span class="fade">Not Configured</span>' }</span>
								</td>
							</tr>										
							<tr class="">
								<td></td>
								<td>
									<div style="text-align: left;">			
										<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
										<a href="#" id="configureWarningLevelsLink">
										Configure</a>
									</div>													
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
				<td>				
					<%-- 
					<div style="text-align: left; padding: 10px;">
						<img src="${resource(dir: 'images/icons/silk', file: 'table_refresh.png')}"/>
						<g:link controller="inventoryItem" 
							action="showStockCard" params="['product.id':productInstance?.id]">Back to Stock Card</g:link>
					</div>					
					--%>	
					<g:hasErrors bean="${inventoryItem}">
			            <div class="errors">
			                <g:renderErrors bean="${inventoryItem}" as="list" />
			            </div>
		            </g:hasErrors>	

										
					<fieldset style="min-height:300px;">	
						<legend></legend>
						<div id="inventoryForm">
							<g:form action="saveInventoryItems" autocomplete="off">
								<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
								<g:hiddenField name="productId" value="${productInstance?.id}"/>
								<g:hiddenField name="active" value="true"/>
								<g:hiddenField name="initialQuantity" value="0"/>							
								<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
									
								<div style="padding: 10px;">
									<label>Inventory date:</label>	
									<g:jqueryDatePicker 
										id="transactionDate" 
										name="transactionDate" 
										value="${new Date() }" 
										format="MM/dd/yyyy"
										showTrigger="false" />
								</div>
								<table>
									<thead>
										<tr>
											<th>ID</th>
											<th>Lot/Serial No</th>
											<th>Expires</th>
											<th>Description</th>
											<th>Qty</th>
										</tr>											
									</thead>
									<tbody>
										<g:each var="itemInstance" in="${inventoryItemList }" status="status">				
											<tr class="${(status%2==0)?'odd':'even' }">
												<td>
													${itemInstance?.id }
												</td>
												<td>
													${itemInstance?.lotNumber?:'<span class="fade">EMPTY</span>' }
												</td>
												<td>
													<g:if test="${itemInstance?.expirationDate}">
														<g:formatDate date="${itemInstance?.expirationDate }" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}" />
													</g:if>
													<g:else>
														<span class="fade">n/a</span>
													</g:else>
												</td>
												<td>
													${itemInstance?.product?.name }
												</td>
												<td style="text-align: center;">
													<g:textField name="inventoryItemList[${status }].quantity" size="3" value="${itemInstance?.quantity }"/>
												</td>	
											</tr>
										</g:each>
										<tr>
											<td>
												<span class="fade">new</span>
											</td>
											<td>
												<g:textField name="lotNumber" size="20" value="${inventoryItem?.lotNumber }"/>
											</td>
											<td nowrap>
												<g:jqueryDatePicker id="expirationDate" name="expirationDate" 
													value="${inventoryItem?.expirationDate}" format="MM/dd/yyyy" showTrigger="false" />
											</td>
											<td>

											</td>
											<td style="text-align: center">
												<g:textField name="quantity" size="3" value="${inventoryItem?.quantity}"/>
											</td>
										</tr>
									</tbody>										
								</table>
								<div style="margin: 35px; text-align: center;">
									<g:submitButton name="save" value="Save"/>
									&nbsp;
									<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">Cancel</g:link>
								</div>												
							</g:form>
						</div>									
					</fieldset>
				</td>
			</tr>
		</table>
	</div>			
</div>
</body>
</html>
