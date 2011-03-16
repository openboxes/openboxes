
<script type="text/javascript">
	$(document).ready(function(){
		$("#btnAddToShipment-${itemInstance?.id}").click(function() { $("#dlgAddToShipment-${itemInstance?.id}").dialog('open'); });									
		$("#dlgAddToShipment-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
	});
</script>	   
<button id="btnAddToShipment-${itemInstance?.id}" class="action-btn">
	<img src="${resource(dir: 'images/icons/silk', file: 'lorry_add.png')}"/> Add to shipment
</button>


<div id="dlgAddToShipment-${itemInstance?.id}" title="Add to Shipment" style="padding: 10px; display: none; vertical-align: middle;" >	

	<g:if test="${commandInstance?.pendingShipmentList }">
		<table>
			<tr>
				<td>
					<g:form controller="inventoryItem" action="addToShipment">
						<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id}"/>
						<g:hiddenField name="inventory.id" value="${commandInstance?.inventoryInstance?.id}"/>
						<g:hiddenField name="inventoryItem.id" value="${itemInstance?.id}"/>
						<table>
							<tbody>
								<tr class="prop">
									<td valign="top" class="name"><label>Item</label></td>                            
									<td valign="top" class="value">
										 	${commandInstance?.productInstance.name }
											<g:if test="${itemInstance?.description}">&rsaquo; ${itemInstance?.description }</g:if> 
											<g:if test="${itemInstance?.lotNumber }">&rsaquo; ${itemInstance?.lotNumber }</g:if>
										
									</td>
								</tr>						
								<tr class="prop">
									<td valign="top" class="name"><label>Quantity </label></td>                            
									<td valign="top" class="value">
										 <g:textField id="quantity" name="quantity" size="1" value="${itemQuantity>0?itemQuantity:1 }" /> 
											<span class="fade">Remaining: ${itemQuantity }</span> 									 
									</td>
								</tr>  	        
								
								<tr class="prop">
									<td valign="top" class="name"><label>to <g:message code="shipmentItem.shipment.label" default="Shipment" /></label></td>                            
									<td valign="top" class="value">
										<select name="shipment.id">
											<g:each var="shipmentInstance" in="${commandInstance?.pendingShipmentList }">
												<option value="${shipmentInstance?.id }">
													<img src="${resource(dir: 'images/icons/silk', file: 'lorry_add.png')}"/>
													${shipmentInstance?.name }
												</option>
											</g:each>
										</select>
										<%--
										<g:autoSuggestEditable id="shipment-${itemInstance?.id}" name="shipment" jsonUrl="/warehouse/json/findShipmentByName" 
											width="200" valueId="" valueName=""/>							
										 --%>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label><g:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
									<td valign="top" class="value">
										<g:autoSuggestEditable id="recipient-${itemInstance?.id}" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
											width="200" valueId="" valueName=""/>							
									</td>
								</tr>
								<tr>
									<td></td>
									<td style="text-align: left;">
										<button type="submit" name="addItem" class="right">
											<img src="${resource(dir: 'images/icons/silk', file: 'lorry_add.png')}"/> Add to shipment
										</button>
									</td>
								</tr>
							</tbody>
						</table>
					</g:form>				
				</td>
				
			</tr>
		</table>		
	</g:if>
	<g:else>
		There are no pending shipments available.
	</g:else>											
</div>		
		     

