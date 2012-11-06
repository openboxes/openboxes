<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="fulfillRequestWorkflow.packItems.label" default="Pack items"/></title>

</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>	
	
		<g:hasErrors bean="${command}">
			<div class="errors">
				<g:renderErrors bean="${command}" as="list" />
			</div>
		</g:hasErrors>
				
		<g:form action="fulfillRequest" autocomplete="false">
			<div class="dialog">
				<fieldset>
					<g:render template="../request/summary" model="[requestInstance:command?.request]"/>				
					<g:render template="progressBar" model="['state':'packRequestItems']"/>		
					<table>
						<tr>
							<td style="padding: 0; margin: 0;">
								<div>
									<g:if test="${command?.fulfillment?.fulfillmentItems }">																
										<table id="shipmentItemsTable">
											<tr>
												<th><warehouse:message code="product.label"/></th>
												<th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
												<th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
												<th class="center"><warehouse:message code="requisitionItem.requested.label" default="Requested"/></th>
												<th class="center"><warehouse:message code="fulfillmentItem.picked.label" default="Picked"/></th>
												<th class="center"><warehouse:message code="fulfillmentItem.packed.label" default="Packed"/></th>
												<th class="left"><warehouse:message code="default.actions.label" default="Actions"/></th>
											</tr>
											<g:each var="packItem" in="${command?.fulfillment?.fulfillmentItems }" status="i">
												<tr class="${i%2?'even':'odd'}">
													<td>
														${packItem?.inventoryItem?.product?.name }
													</td>
													<td>
														${packItem?.inventoryItem?.lotNumber }
													</td>
													<td>
														${formatDate(date: packItem.inventoryItem?.expirationDate, format: org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT) }
													</td>
													<td class="center">
														${packItem?.requestItem?.quantity }
													</td>
													<td class="center">
														${packItem?.quantity }
													</td>
													<td class="center">
														${packItem?.quantityPacked() }
													</td>
													<td>	
														<g:link action="fulfillRequest" event="showPackDialog" params="['fulfillmentItem.id':packItem?.id, 'requestItem.id':packItem?.requestItem?.id]">
															<img src="${resource(dir: 'images/icons/silk', file: 'package.png') }"/>
															<warehouse:message code="fulfillRequestWorkflow.packItem.label"/>
														</g:link>
													</td>
												</tr>
											</g:each>
										</table>
									</g:if>
								</div>
							</td>		
						</tr>
						<tr class="prop">
							<td>
								<div class="buttons" style="border-top: 0px solid lightgrey;">
									<g:submitButton name="back" value="${warehouse.message(code: 'default.button.back.label')}"></g:submitButton>
									<g:submitButton name="next" value="${warehouse.message(code: 'default.button.next.label')}"></g:submitButton>
									<g:link action="fulfillRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
								</div>
							</td>
						</tr>
						
					</table>
				</fieldset>
			</div>
		</g:form>
		<g:if test="${showPackDialog}">
			<g:render template="packItem" model="['requestItem':requestItem]"/>
		</g:if>
	</div>	  
</body>
</html>