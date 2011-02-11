<script type="text/javascript">
	$(document).ready(function(){
		$("#btnAddItem-${containerInstance?.id}").click(function() { $("#dlgAddItem-${containerInstance?.id}").dialog('open'); });									
		$("#dlgAddItem-${containerInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
	});
</script>	   
<div id="dlgAddItem-${containerInstance?.id}" title="Add an item" style="padding: 10px; display: none;" >
	<g:form action="createShipment">
		<g:hiddenField name="shipment.id" value="${shipmentInstance?.id }"/>
		<g:hiddenField name="container.id" value="${containerInstance?.id }"/>
		<table>
			<tbody>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="Container" /></label></td>                            
					<td valign="top" class="value">
						${containerInstance?.containerType?.name}						
						${containerInstance?.name}
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest id="product-${containerInstance?.id}" name="product" jsonUrl="/warehouse/json/findProductByName" 
							width="200" valueId="" valueName=""/>
						
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="quantity" name="quantity" size="3" /> 
					</td>
				</tr>  	        
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot Number" /></label></td>                            
					<td valign="top" class="value">
						<g:textField id="lotNumber" name="lotNumber" size="30" /> 
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><g:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
					<td valign="top" class="value">
						<g:autoSuggest id="recipient-${containerInstance?.id}" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
							width="200" valueId="" valueName=""/>							
					</td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="addItem" value="Add Item"></g:submitButton>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

