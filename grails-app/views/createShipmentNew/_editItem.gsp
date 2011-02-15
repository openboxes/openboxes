<script type="text/javascript">
	$(document).ready(function(){
		$("#btnEditItem-${itemInstance?.id}").click(function() { $("#dlgEditItem-${itemInstance?.id}").dialog('open'); });									
		$("#dlgEditItem-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
	
		$("#btnAddItem-${containerInstance?.id}").click(function() { $("#dlgAddItem-${containerInstance?.id}").dialog('open'); });									
		$("#dlgAddItem-${containerInstance?.id}").dialog({ autoOpen: ${addItem == containerInstance?.id ? 'true' : 'false'}, modal: true, width: '600px' });				
	});
</script>
<g:if test="${itemInstance}">	   
	<div id="dlgEditItem-${itemInstance?.id}" title="Edit an item" style="padding: 10px; display: none;" >
</g:if>
<g:else>
	<div id="dlgAddItem-${containerInstance?.id}" title="Add an item" style="padding: 10px; display: none;" >
</g:else>
	<g:form action="createShipment">
		<g:if test="${containerInstance}">
			<g:hiddenField name="container.id" value="${containerInstance?.id }"/>
		</g:if>
		<g:if test="${itemInstance}">
			<g:hiddenField name="item.id" value="${itemInstance?.id }"/>
		</g:if>
		<table>
			<tbody>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="Container" /></label></td>                            
					<td valign="top" class="value">						
						${containerInstance?.name}
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest id="product-${containerInstance?.id}" name="product" jsonUrl="/warehouse/json/findProductByName" 
							width="200" valueId="${itemInstance?.product?.id}" valueName="${itemInstance?.product?.name}"/>
						
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" value="${itemInstance?.quantity}" size="3" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot Number" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="lotNumber" name="lotNumber" value="${itemInstance?.lotNumber}" size="30" /> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest id="recipient-${containerInstance?.id}" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
							width="200" valueId="${itemInstance?.recipient?.id}" valueName="${itemInstance?.recipient?.name}"/>							
					</td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveItem" value="Save Item"></g:submitButton>
							<g:submitButton name="deleteItem" value="Delete Item"></g:submitButton>
							<g:submitButton name="cancelItem" value="Cancel"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addAnotherItem" value="Save Item and Add Another Item"></g:submitButton>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

