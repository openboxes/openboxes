<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditItem-${itemInstance?.id}").click(function() { $("#dlgEditItem-${itemInstance?.id}").dialog('open'); });									
		$("#dlgEditItem-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });			
		$("#btnEditClose-${itemInstance?.id}").click( function() { $("#dlgEditItem-${itemInstance?.id}").dialog('close'); });	
	});
</script>	   
<div id="dlgEditItem-${itemInstance?.id}" title="${warehouse.message(code: 'inventory.editItem.label')}" style="padding: 10px; display: none;" >	
	
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
						<td valign="top" class="name"><label><warehouse:message code="product.label" /></label></td>                            
						<td valign="top" class="value">
							<%-- 
							<g:autoSuggestEditable id="editItem-product-${itemInstance?.id}" name="product" jsonUrl="${request.contextPath }/json/findProductByName" 
								size="20" valueId="${itemInstance?.product?.id }" valueName="${itemInstance?.product?.name }"/>	
							--%>
							
							<format:product product="${itemInstance?.product}"/>
						</td>
						<td>
					</tr>
					
					
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="product.lotNumber.label"/></label></td>                            
						<td valign="top" class="value">
							<g:textField name="lotNumber" value="${itemInstance?.lotNumber}"/>
							<%-- 
							<g:autoSuggestEditable id="editItem-lotNumber-${itemInstance?.id}" name="lotNumber" jsonUrl="${request.contextPath }/json/findLotsByName?productId=${itemInstance?.product?.id }" 
								size="20" valueId="${itemInstance?.lotNumber}" valueName="${itemInstance?.lotNumber}"/>							
							--%>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="product.expirationDate.label"/></label></td>                            
						<td valign="top" class="">
							<g:datePicker name="expirationDate" value="" precision="month" default="none" value="${itemInstance?.expirationDate }" noSelection="['':'']"/>
						</td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2" class="center">
							<button type="submit" name="addItem">
								<img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/> <warehouse:message code="default.button.save.label"/>
							</button>
							&nbsp;
							<a href="javascript:void();" id="btnEditClose-${itemInstance?.id }">
								<warehouse:message code="default.button.cancel.label"/>
							</a>
						</td>
					</tr>	
				</tfoot>			
			</table>
		</g:form>				

	</div>		
</div>

