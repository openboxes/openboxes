<script type="text/javascript">
	$(document).ready(function(){			
		$("#pack-item-dialog").dialog({ autoOpen: true, modal: true, width: 800, height: 500 });				
	});
</script>	   
<div id="pack-item-dialog" title="${warehouse.message(code: 'fulfillRequestWorkflow.packItems.label') }" style="padding: 10px; display: none;" >
	<%-- 
	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.Container" form="fulfillItem"/>
	--%>
	
	<a href="top"></a>
	
	<g:if test="${message}">
		<div class="message">
			${message}
		</div>
	</g:if>	
	
	
	<g:form name="fulfillItem" action="fulfillRequest">
		<g:hiddenField name="requestItem.id" value="${requestItem?.id }"/>
		<g:hiddenField name="fulfillmentItem.id" value="${fulfillmentItem?.id }"/>
	
		<div class="dialog">
			<div class="" style="background-color: #fff; padding: 10px">
				<table>
					<tr class="prop">
						<td class="name">
							<label>${warehouse.message(code: 'fulfillmentItem.requested.label') }</label>
						</td>
						<td class="value">			
							<format:metadata obj="${requestItem.displayName()}"/>
							(${requestItem?.quantity} ${warehouse.message(code: 'default.units.label') })
						</td>
					</tr>					
					<tr class="prop">
						<td class="name">
							<label>${warehouse.message(code: 'fulfillmentItem.picked.label') }</label>
						</td>
						<td class="value">	
							<format:metadata obj="${requestItem.displayName()}"/>
							(${command?.quantityFulfilledMap()[requestItem] } ${warehouse.message(code: 'default.units.label') })
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label>${warehouse.message(code: 'shipmentItem.label') }</label>
						</td>
						<td class="value">	
							${fulfillmentItem?.inventoryItem?.product }
							${fulfillmentItem?.inventoryItem?.lotNumber }
							(${fulfillmentItem?.quantity } ${warehouse.message(code: 'default.units.label') })
							
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label>${warehouse.message(code: 'shipping.label') }</label>
						</td>
						<g:if test="${fulfillmentItem?.shipmentItems }">
							<td class="value" style="padding: 0px;">	
								<table>
									<tr>
										<th>${warehouse.message(code: 'shipment.label') }</th>
										<th>${warehouse.message(code: 'container.label') }</th>
										<th>${warehouse.message(code: 'product.label') }</th>
										<th>${warehouse.message(code: 'inventoryItem.lotNumber.label') }</th>
										<th>${warehouse.message(code: 'inventoryItem.expirationDate.label') }</th>
										<th>${warehouse.message(code: 'inventoryItem.quantity.label') }</th>
									</tr>
									<g:each in="${fulfillmentItem?.shipmentItems }" var="shipmentItem">
										<tr>
											<td>${shipmentItem?.shipment?.name }</td>
											<td>${shipmentItem?.container?.name }</td>
											<td>${shipmentItem?.product?.name }</td>
											<td>${shipmentItem?.lotNumber }</td>
											<td>${shipmentItem?.expirationDate }</td>
											<td>${shipmentItem?.quantity }</td>
										</tr>
									</g:each>
								</table>
							</td>
						</g:if>
						<g:else>
							<td class="value">
							<g:select optionKey="id" optionValue="${{it.name + ' to ' + it.origin.name + ' on ' + g.formatDate(date: it.expectedShippingDate, format: 'MMM dd')}}" 
									name="shipment.id" id="shipment" from="${shipments}" noSelection="['':warehouse.message(code:'shipment.selectOne.label')]"/>
							</td>
						</g:else>							
					</tr>
					<%-- 
					<tr class="prop">
						<td class="name">
							<label>Shipping</label>
						</td>
						<td class="value" style="padding: 0px;">		
							<table>
								<tr class="odd">
									<th>lotnumber</th>
									<th>expirationdate</th>
									<th>quantity</th>
									<th>shipment</th>
								</tr>
								<g:set var="counter" value="${0 }"/>
								<g:each var="fulfillmentItem" in="${command?.fulfillment?.fulfillmentItems }" status="i">
									<g:if test="${fulfillmentItem?.requestItem == requestItem }">
										<tr class="${counter++%2?'odd':'even' }">
											<td>
												${fulfillmentItem?.inventoryItem?.lotNumber }
											</td>
											<td>
												${fulfillmentItem?.inventoryItem?.expirationDate }
											</td>
											<td>
												${fulfillmentItem?.quantity }
											</td>
											<td>

												<g:select optionKey="id" optionValue="${{it.name + ' to ' + it.origin.name + ' on ' + g.formatDate(date: it.expectedShippingDate, format: 'MMM dd')}}" 
													name="shipment.id" id="shipment" from="${shipments}" noSelection="['':'-- Select a shipment --']"/>
							
											</td>
										</tr>				
									</g:if>		
								</g:each>
							</table>
						</td>
					</tr>
					--%>
					<%-- 
					<tr class="prop">
						<td class="name">
							<label>Shipment</label>
						</td>
						<td class="value">		

							<g:select optionKey="id" optionValue="${{it.name + ' to ' + it.origin.name + ' on ' + g.formatDate(date: it.expectedShippingDate, format: 'MMM dd')}}" 
								name="shipment.id" id="shipment" from="${shipments}" noSelection="['':'-- Select a shipment --']"/>
						    
							
						</td>
					</tr>
					--%>
					<%-- 
					<tr class="prop">
						<td class="name">
							<label>Container</label>
						</td>
						<td class="value">			
							
    						<g:select name="container" id="container"></g:select>
							
							
						</td>
					</tr>
					--%>
							<%--
							// This was an attempt at creating a cascading select, but I couldn't get the updateContainer() method to work.
							<g:select optionKey="id" optionValue="name" name="shipment" id="shipment" from="${org.pih.warehouse.shipping.Shipment.list()}"
						        onchange="${remoteFunction( controller:'json', 
						        							action:'getContainers', 
						        							params: '\'id=\' + escape(this.value)',
						        							onSuccess:'updateContainer(data)')}"></g:select>
							--%>
					
					
				</table>
			</div>	
			<div class="buttons">
				<g:submitButton name="saveAndContinuePackDialog" value="${warehouse.message(code:'default.button.save.label')}"></g:submitButton>
				<g:submitButton name="saveAndClosePackDialog" value="${warehouse.message(code:'default.button.close.label')}"></g:submitButton>
			</div>
		</div>
	</g:form>
</div>

<g:javascript>
function updateContainer(data) {
	
	//console.log(data);
	
	// evaluate JSON
	var containers = eval("(" + data + ")");
	//console.log(containers);

	// The response comes back as a bunch-o-JSON
	if (containers) {
		var rselect = document.getElementById('container');
		console.log(rselect);
		// Clear all previous options
		var length = rselect.length;
		while (length > 0) {
			length--;
			rselect.remove(length);
		}
		
		

		// Rebuild the select
		for (var i=0; i < containers.length; i++) {
			console.log(i);
			var container = containers[i];
			console.log(container);
			var opt = document.createElement('option');
			opt.text = container.name;
			opt.value = container.id;
	    	rselect.add(opt, null); // standards compliant; doesn't work in IE
		}
		console.log(rselect);
	}
}

	/*
	// This is called when the page loads to initialize city
	var zselect = document.getElementById('country.name')
	var zopt = zselect.options[zselect.selectedIndex]
	${remoteFunction(controller:"country", action:"ajaxGetCities", params:"'id=' + zopt.value", onComplete:"updateCity(e)")}
	*/
</g:javascript>
