
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


			<h1>
				<span class="fade">${itemInstance?.product?.productType?.name }</span>			
				${itemInstance?.product?.name }
				<span class="fade">${itemInstance?.product?.dosageStrength} ${itemInstance?.product?.dosageUnit} ${itemInstance?.product?.dosageForm?.name}</span>
			</h1>

			<div class="dialog">
				<g:form action="save">
					<g:hiddenField name="id" value="${itemInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${params?.inventory?.id}"/>
					<g:hiddenField name="inventoryItemType" value="${org.pih.warehouse.inventory.InventoryItemType.NON_SERIALIZED}"/>
					<g:hiddenField name="onHandQuantity" value="0"/>
					<g:hiddenField name="product.id" value="${itemInstance?.product?.id }"/>
					<fieldset>	
					
						Create new lots for inventory			
						<table>
							<tr>
								<th>Lot Number</th>
								<th>Expiration Date</th>
								<th>Initial Quantity</th>
							</tr>
							<g:set var="varStatus" value="${0 }"/>
							<g:each var="inventoryLot" in="${inventoryLots}" status="status">
								<g:hiddenField name="inventoryLots[${status }].product.id" value="${inventoryLot?.product?.id }"/>
								<g:if test="${status==0}">
									<tr class="${(varStatus++ % 2==0)?'odd':'even' }">
										<td>
											(empty)
											<g:hiddenField name="inventoryLots[0].lotNumber" value="" size="10"/>
										</td>													
										<td>
											<g:jqueryDatePicker id="inventoryLot-expirationDate-0" 
												name="inventoryLots[0].expirationDate" 
												value="${inventoryLot?.expirationDate }" 
												format="MM/dd/yyyy"/>
										</td>
										<td>
											<g:textField name="inventoryLots[0].initialQuantity" value="${inventoryLot?.initialQuantity }" size="3"/>
										</td>
									</tr>
								</g:if>
								<g:else>
									<tr class="${(varStatus++ % 2 ==0)?'odd':'even' }">
										<td>
											<g:textField name="inventoryLots[${status }].lotNumber" 
												value="${inventoryLot?.lotNumber }" size="10"/>
										</td>
										<td>
											<g:jqueryDatePicker id="inventoryLot-expirationDate-${status }" 
												name="inventoryLots[${status }].expirationDate" 
												value="${inventoryLot?.expirationDate }" 
												format="MM/dd/yyyy"/>
										
											
										</td>
										<td>
											<g:textField name="inventoryLots[${status }].initialQuantity" 
												value="${inventoryLot?.initialQuantity }" size="3"/>
										</td>
									</tr>
								</g:else>
							</g:each>
						</table>
						<div class="buttons" style="text-align: right;">
		                    <g:actionSubmit class="save" action="save" value="${message(code: 'default.button.update.label', default: 'Update')}" />
	                    </div>
					</fieldset>
				</g:form>			
			</div>
        </div>
    </body>
</html>
