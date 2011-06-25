<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditItem-${itemInstance?.id}").click(function() { $("#dlgEditItem-${itemInstance?.id}").dialog('open'); });									
		$("#dlgEditItem-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });			
	});
</script>	   
<div class="action-menu-item">
	<a id="btnEditItem-${itemInstance?.id}">
		<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;Edit Item
	</a>
</div>
<div id="dlgEditItem-${itemInstance?.id}" title="Edit Item" style="padding: 10px; display: none;" >	
	
	<div class="dialog" style="padding: 10px;" >	
		
		<%-- 
		<jqvalui:renderValidationScript 
			renderErrorsOnTop="true"
			form="editInventoryItem" 
			for="org.pih.warehouse.inventory.InventoryItem" 
			not="exprirationDate"
		/>		
		--%>
		

		<g:form name="editInventoryItem" controller="inventoryItem" action="update">
			<g:hiddenField name="id" value="${itemInstance?.id}"/>
			<g:hiddenField name="inventory.id" value="${inventoryInstance?.id}"/>
			<g:hiddenField name="product.id" value="${itemInstance?.product?.id}"/>
			<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
			
			<table>
				<tbody>
				
				
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="inventoryItem.product.label" default="Product" /></label></td>                            
						<td valign="top" class="value">
							<%-- 
							<g:autoSuggestEditable id="editItem-product-${itemInstance?.id}" name="product" jsonUrl="/warehouse/json/findProductByName" 
								size="20" valueId="${itemInstance?.product?.id }" valueName="${itemInstance?.product?.name }"/>	
							--%>
							
							${itemInstance?.product?.name }
						</td>
						<td>
					</tr>
					
					
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="inventoryItem.lotNumber.label" default="Lot/Serial Number" /></label></td>                            
						<td valign="top" class="value">
							<g:autoSuggestEditable id="editItem-lotNumber-${itemInstance?.id}" name="lotNumber" jsonUrl="/warehouse/json/findLotsByName?productId=${itemInstance?.product?.id }" 
								size="20" valueId="${itemInstance?.lotNumber}" valueName="${itemInstance?.lotNumber}"/>							
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="inventoryItem.expirationDate.label" default="Expiration Date" /></label></td>                            
						<td valign="top" class="">
							<g:datePicker name="expirationDate" value="" precision="month" default="none" value="${itemInstance?.expirationDate }" noSelection="['':'']"/>
						</td>
					</tr>
					<tr>
						<td></td>
						<td style="text-align: left;">								
							<button type="submit" name="addItem" class="right">
								<img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}"/> Save
							</button>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>				

	</div>		
</div>

