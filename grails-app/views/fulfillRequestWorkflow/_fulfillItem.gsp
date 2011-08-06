<script type="text/javascript">
	$(document).ready(function(){			
		$("#fulfill-item-dialog").dialog({ autoOpen: true, modal: true, width: 800, height: 500 });				
	});
</script>	   
<div id="fulfill-item-dialog" title="Fulfill item" style="padding: 10px; display: none;" >
	<%-- 
	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.Container" form="fulfillItem"/>
	--%>
	
	<a href="top"></a>
	
	<g:if test="${flash.message}">
		<div class="message">
			${flash.message}
		</div>
	</g:if>	
	
	<div class="head" >
		<table>
			<tr>
				<td class="left">
					<g:if test="${previousRequestItem }">
						<g:link action="fulfillRequest" event="showDialog" params="['requestItem.id':requestItem?.id, 'direction':'previous']">
							<img src="${resource(dir: 'images/icons/silk', file: 'arrow_left.png') }" style="vertical-align: middle;"/>
							<warehouse:message code="requestItem.previous.label"/>
						</g:link>
						<span class="fade">${previousRequestItem?.description }</span>
					</g:if>
				</td>
				<td class="right">
					<g:if test="${nextRequestItem }">
						<span class="fade">${nextRequestItem?.description }</span>
						<g:link action="fulfillRequest" event="showDialog" params="['requestItem.id':requestItem?.id, 'direction':'next']">						
							<warehouse:message code="requestItem.next.label"/>
							<img src="${resource(dir: 'images/icons/silk', file: 'arrow_right.png') }" style="vertical-align: middle"/>
						</g:link>
					</g:if>
				</td>			
			</tr>		
		</table>		
	</div>	
	<g:form name="fulfillItem" action="fulfillRequest">
		<div class="dialog">
			<div class="" style="background-color: #fff; padding: 10px">
				<table>
					<tr class="prop">
						<td class="name">
							<label>Product Requested</label>
						</td>
						<td class="value">				
							${requestItem.quantity} units of ${requestItem.product} <span class="fade">${requestItem.category}</span>
						</td>
					</tr>					
					<tr class="prop">
						<td class="name">
							<label>Product Fulfilled</label>
						</td>
						<td class="value">				
							<div class="buttons left">
								<g:if test="${product }">
									${product?.name } <span class="fade">${product.category}</span>						
								</g:if>
								<g:else>
									${requestItem.product?.name} <span class="fade">${requestItem.product.category}</span>
								</g:else>
								<br/>
								<g:autoSuggest id="product" name="product" jsonUrl="/warehouse/json/findProductByName" width="200" />
								<g:submitButton name="changeProduct" value="${warehouse.message(code:'default.change.label')}"></g:submitButton>					
							</div>
						</td>
					</tr>	
					<tr class="prop">
						<td class="name">
							<label>Available inventory</label>
						</td>
						<td class="value">					
						
							<div style="height: 125px; overflow: auto">
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
													${inventoryItem.lotNumber?:"none" }
												</td>
												<td>
													${inventoryItem.expirationDate?:"never" }
												</td>
												<td>
													${quantity }
												</td>
												<td>
													<g:textField name="quantity" size="5"/>
												</td>
											</tr>
										</g:each>
									</g:if>
									<g:else>
										<tr class="even">
											<td colspan="4" class="center">
												<b>${requestItem?.description }</b> does not currently exist in the <b>${session?.warehouse?.name }</b> inventory.
											</td>
										</tr>
									</g:else>
								</table>
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
	

			<%-- 		
			<g:formRemote url="[controller: 'request', action: 'fulfillItemPost']" name="fulfillmentForm" update="message">
			--%>
	
	
</div>
