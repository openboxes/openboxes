
<script type="text/javascript">
	$(document).ready(function(){
		$("#btnAdjustStock-${itemInstance?.id}").click(function() { $("#dlgAdjustStock-${itemInstance?.id}").dialog('open'); });									
		$("#dlgAdjustStock-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '400px' });				
	});
</script>	

<button id="btnAdjustStock-${itemInstance?.id}" class="action-btn">
	<img src="${resource(dir: 'images/icons/silk', file: 'book_open.png')}"/>
	&nbsp;Adjust stock
</button>
<g:link controller="inventoryItem" action="showRecordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id, 'inventoryItem.id':itemInstance?.id]">
</g:link>

<div id="dlgAdjustStock-${itemInstance?.id}" title="Adjust Stock" style="padding: 10px; display: none;" >	
	<table>
		<tr>
			<td>
				<g:form controller="inventoryItem" action="adjustStock">
					<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
					<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
					
					<table>
						<tbody>
							<tr class="propOff">
								<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
								<td valign="top" class="value">
									${commandInstance?.productInstance } 
									<g:if test="${itemInstance?.description}">&rsaquo; ${itemInstance?.description }</g:if> 
									<g:if test="${itemInstance?.lotNumber }">&rsaquo; ${itemInstance?.lotNumber }</g:if>
									
								</td>
							</tr>
							<tr class="propOff">
								<td valign="top" class="name"><label><g:message code="shipmentItem.shipment.label" default="Shipment" /></label></td>                            
								<td valign="top" class="value">
									<g:autoSuggestEditable id="shipment-${itemInstance?.id}" name="shipment" jsonUrl="/warehouse/json/findShipmentByName" 
										size="20" valueId="" valueName=""/>							
								</td>
							</tr>
							<tr class="propOff">
								<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
								<td valign="top" class="value">
									<g:textField id="quantity" name="quantity" size="3" /> out of ${commandInstance?.totalQuantity }
								</td>
							</tr>  	        
							<tr class="propOff">
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
										<g:submitButton name="addItem" value="Add to cart"></g:submitButton>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</g:form>				
			</td>
			
		</tr>
	</table>													
</div>		
		     

