
<script type="text/javascript">
	$(document).ready(function(){
		$("#btnAddToShipment-${itemInstance?.id}").click(function() { $("#dlgAddToShipment-${itemInstance?.id}").dialog('open'); });									
		$("#dlgAddToShipment-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });				
	});
</script>	   
<div class="action-menu-item">
	<a id="btnAddToShipment-${itemInstance?.id}">
		<img src="${resource(dir: 'images/icons/silk', file: 'lorry_add.png')}"/>&nbsp;Add to Shipment</a>
</div>


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
									<td valign="top" class="name"><label>Inventory</label></td>                            
									<td valign="top" class="value">
										 	${commandInstance?.inventoryInstance?.warehouse?.name }
									</td>
								</tr>						
								<tr class="prop">
									<td valign="top" class="name"><label>Item</label></td>                            
									<td valign="top" class="value">
									 	${commandInstance?.productInstance.name }
										<g:if test="${itemInstance?.lotNumber }">&rsaquo; ${itemInstance?.lotNumber }</g:if>
									</td>
								</tr>						
								<tr class="prop">
									<td valign="top" class="name"><label>Add to <g:message code="shipmentItem.shipment.label" default="Shipment" /></label></td>                            
									<td valign="top" class="value">
										<select name="shipmentContainer">
											<option value="null"></option>
											<g:each var="shipmentInstance" in="${commandInstance?.pendingShipmentList }">
												<g:set var="expectedShippingDate" value="${prettyDateFormat(date: shipmentInstance?.expectedShippingDate)}"/> 
												<g:set var="label" value="${shipmentInstance?.name + ' to ' + shipmentInstance?.destination?.name + ', departing ' + expectedShippingDate}"/>
												<optgroup label="${label }">
													<option value="${shipmentInstance?.id }:0">
														<g:set var="looseItems" value="${shipmentInstance?.shipmentItems?.findAll { it.container == null }}"/>
														&nbsp; Loose Items &rsaquo; ${looseItems.size() } items
													</option>
													<g:each var="containerInstance" in="${shipmentInstance?.containers }">
														<g:set var="containerItems" value="${shipmentInstance?.shipmentItems?.findAll { it?.container?.id == containerInstance?.id }}"/>
														<option value="${shipmentInstance?.id }:${containerInstance?.id }">
															&nbsp; ${containerInstance?.name } &rsaquo; ${containerItems.size() } items
														</option>
													</g:each>
												</optgroup>
											</g:each>
										</select>
										<%--
										<g:autoSuggestEditable id="shipment-${itemInstance?.id}" name="shipment" jsonUrl="/warehouse/json/findShipmentByName" 
											width="200" valueId="" valueName=""/>							
										 --%>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label>Quantity </label></td>                            
									<td valign="top" class="value">
										 <g:textField id="quantity" name="quantity" size="5" value="" /> &nbsp;
											<span class="fade">Remaining: ${itemQuantity }</span> 									 
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
		     

