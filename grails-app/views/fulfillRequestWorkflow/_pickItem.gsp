<script type="text/javascript">
	$(document).ready(function(){			
		$("#pick-item-dialog").dialog({ autoOpen: true, modal: true, width: 800, height: 500 });				
	});
</script>	   
<div id="pick-item-dialog" title="Pick item" style="padding: 10px; display: none;" >
	<%-- 
	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.Container" form="fulfillItem"/>
	--%>
	
	<a href="top"></a>
	
	<g:if test="${message}">
		<div class="message">
			${message}
		</div>
	</g:if>	
	<%-- 
	<div class="head" >
		<table>
			<tr>
				<td class="left">
					<g:if test="${previousRequestItem }">
						<g:link action="fulfillRequest" event="showPickDialog" params="['requestItem.id':requestItem?.id, 'direction':'previous']">
							<img src="${resource(dir: 'images/icons/silk', file: 'arrow_left.png') }" style="vertical-align: middle;"/>
							<warehouse:message code="requestItem.previous.label"/>
						</g:link>
						<span class="fade">${previousRequestItem?.description }</span>
					</g:if>
				</td>
				<td class="right">
					<g:if test="${nextRequestItem }">
						<span class="fade">${nextRequestItem?.description }</span>
						<g:link action="fulfillRequest" event="showPickDialog" params="['requestItem.id':requestItem?.id, 'direction':'next']">						
							<warehouse:message code="requestItem.next.label"/>
							<img src="${resource(dir: 'images/icons/silk', file: 'arrow_right.png') }" style="vertical-align: middle"/>
						</g:link>
					</g:if>
				</td>			
			</tr>		
		</table>		
	</div>	
	--%>
	<g:form name="fulfillItem" action="fulfillRequest">
		<g:hiddenField name="fulfillment.request.id" value="${requestItem?.request?.id }"/>
		<div class="dialog">
			<div class="" style="background-color: #fff; padding: 10px">
				<table>
					<tr class="prop">
						<td class="name">
							<label>Requested</label>
						</td>
						<td class="value">			
							<g:if test="${requestItem?.product }">
								${requestItem?.product} 
								<span class="fade">${requestItem?.product?.category}</span>					
							</g:if>	
							<g:elseif test="${requestItem?.category }">
								${requestItem?.category} 
							</g:elseif>
							<g:else>
								${requestItem?.description} 
							</g:else>
							(${requestItem?.quantity} units)
						</td>
					</tr>					
					<tr class="prop">
						<td class="name">
							<label>Fulfilled</label>
						</td>
						<td class="value">	
						
							<g:if test="${requestItem?.product }">
								${requestItem?.product} 
								<span class="fade">${requestItem?.product?.category}</span>					
							</g:if>	
							<g:elseif test="${requestItem?.category }">
								${requestItem?.category} 
							</g:elseif>
							<g:else>
								${requestItem?.description} 
							</g:else>
							<span class="quantity">(${command?.quantityFulfilledByRequestItem(requestItem) } units)</span>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label>Available</label>
						</td>
						<td class="value">			
							
							
							<%-- 
							<div id="searchbox">
								Search: 
								<!--  oncomplete="showSpinner(false);" onloading="showSpinner(true);" -->
								<g:remoteField name="search" paramName="term" update="searchResults" url="[controller:'json', action:'searchProductByName']">
								</g:remoteField>
							</div>
							<div id="searchResults">
								Results:
							</div>
							--%>
							<div style="height: 200px; overflow: auto" id="fulfillProduct">
							
								<g:if test="${product }">
									${product?.name } 
									<span class="fade">${product?.category}</span>						
								</g:if>
								<g:else>
									${requestItem?.product?.name} 
									<span class="fade">${requestItem?.product?.category}</span>
								</g:else>
								
								<a href="javascript:void(0);" id="changeProductBtn">change</a>
							
								<table border="0">
									<tr class="odd">
										<td>Lot Number</td>
										<td>Expires</td>
										<td>On Hand Qty</td>
										<td>Fulfill Qty</td>
									</tr>
									<g:if test="${inventoryItems }">
										<g:each var="entry" in="${inventoryItems}" status="i">
											<g:set var="quantity" value="${entry.value }"/>
											<g:set var="inventoryItem" value="${entry.key }"/>
											
											<tr class="${i%2?'odd':'even' }">	
												<td>
													<g:hiddenField name="fulfillmentItems[${i }].requestItem.id" value="${requestItem?.id }"/>
													<g:hiddenField name="fulfillmentItems[${i }].inventoryItem.id" value="${inventoryItem?.id }"/>
													${inventoryItem?.lotNumber?:"none" }
												</td>
												<td>
													${inventoryItem?.expirationDate?:"never" }
												</td>
												<td>
													${quantity }
												</td>
												<td>
													<g:textField name="fulfillmentItems[${i }].quantity" size="5"/>
												</td>
											</tr>
										</g:each>
									</g:if>
									<g:else>
										<tr class="even">
											<td colspan="4" class="center">
												<b>${product?.name }</b> does not currently exist in the <b>${session?.warehouse?.name }</b> inventory.
											</td>
										</tr>
									</g:else>
								</table>
							</div>						
							<div class="buttons left" id="changeProduct" style="display: none;">
								<g:autoSuggest id="fulfillProduct" name="fulfillProduct" jsonUrl="/warehouse/json/findProductByName" width="200" />
								<g:submitButton name="changeProduct" value="${warehouse.message(code:'default.search.label')}"></g:submitButton>					
							</div>
							
						</td>
					</tr>
				</table>
			</div>	
			<div class="buttons">
				<g:submitButton name="saveAndContinue" value="${warehouse.message(code:'default.saveAndContinue.label')}"></g:submitButton>
				<g:submitButton name="saveAndClose" value="${warehouse.message(code:'default.close.label')}"></g:submitButton>
			</div>
		</div>
	</g:form>
</div>


<script>
$(document).ready(function() {
	$("#changeProductBtn").click(function(){ 
		$("#changeProduct").toggle();
		$("#fulfillProduct").toggle();
	});
});

</script>
