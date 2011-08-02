<%-- Should probably be deleted.  This was used when we were loading the edit inventory item dialog using a remote ajax call --%>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="dialog" />
        <g:set var="entityName" value="${message(code: 'inventoryItem.label', default: 'Inventory Item')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>    
	</head>
	
	
		<div class="dialog" style="padding: 10px;" >	
		
						
			<jqvalui:renderValidationScript 
				for="org.pih.warehouse.inventory.InventoryItem" 
				form="editInventoryItem" 
				renderErrorsOnTop="true"/>					
		
			<g:form name="editInventoryItem" controller="inventoryItem" action="update">
				<g:hiddenField name="id" value="${itemInstance?.id}"/>
				<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
				<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
				
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message code="inventoryItem.product.label" default="Product" /></label></td>                            
							<td valign="top" class="value">
								<g:autoSuggestEditable id="editItem-product-${itemInstance?.id}" name="product" jsonUrl="/warehouse/json/findProductByName" 
									size="20" valueId="${itemInstance?.product?.id }" valueName="${itemInstance?.product?.name }"/>	
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message code="inventoryItem.lotNumber.label" default="Lot/Serial Number" /></label></td>                            
							<td valign="top" class="value">
								<g:autoSuggestEditable id="editItem-lotNumber-${itemInstance?.id}" name="lotNumber" jsonUrl="/warehouse/json/findLotsByName?productId=${itemInstance?.product?.id }" 
									size="20" valueId="${itemInstance?.lotNumber}" valueName="${itemInstance?.lotNumber}"/>							
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message code="inventoryItem.expirationDate.label" default="Expiration Date" /></label></td>                            
							<td valign="top" class="value">
								<g:datePicker name="expirationDate" value="" precision="month" default="none" noSelection="['':'']"/>
							</td>
						</tr>
						<tr>
							<td></td>
							<td style="text-align: left;">
								<div class="buttons">
									<g:submitButton name="addItem" value="Save"></g:submitButton>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</g:form>				

		</div>		
	</div>

