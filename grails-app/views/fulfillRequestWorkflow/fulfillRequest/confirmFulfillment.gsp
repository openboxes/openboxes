
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="fulfillRequestWorkflow.markAsFulfilled.label" default="Mark as fulfilled"/></title>
<style>
</style>
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
		
		<g:form action="fulfillRequest" method="post">
			<div class="dialog">
				<fieldset>
					<g:render template="../request/summary" model="[requestInstance:command?.request]"/>
					<g:render template="progressBar" model="['state':'confirmFulfillment']"/>	
					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'>
									<label for='summary'>Summary</label>
								</td>
								<td valign='top'class='value'>
									<warehouse:message code="fulfillRequestWorkflow.markAsFulfilled.message"/>
									
								</td>
							</tr>
							<tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='requestItems'><warehouse:message code="request.items.label" default="Items" /></label></td>
	                            <td valign="top" class="table">

									<g:if test="${command?.fulfillment?.fulfillmentItems }">
										<table>
											<tr>
												<td style="padding: 0; margin: 0;">
													<div>
														<g:if test="${command?.fulfillment?.fulfillmentItems }">																
															<table id="shipmentItemsTable">
																<tr>
																	<th><warehouse:message code="product.label"/></th>
																	<th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
																	<th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
																	<th class="center"><warehouse:message code="fulfillmentItem.requested.label"/></th>
																	<th class="center"><warehouse:message code="fulfillmentItem.picked.label"/></th>
																	<th class="center"><warehouse:message code="fulfillmentItem.packed.label"/></th>
																</tr>
																<g:set var="counter" value="${0 }"/>
																<g:each var="packItem" in="${command?.fulfillment?.fulfillmentItems }" status="i">
																	<tr class="${counter++%2?'even':'odd'}">
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
																			${packItem?.requestItem?.quantity?:0 }
																		</td>
																		<td class="center">
																			${packItem?.quantity?:0 }
																		</td>
																		<td class="center">
																			${packItem?.quantityPacked()?:0 }
																		</td>
																	</tr>
																</g:each>
																<g:each var="requestItem" in="${command?.request?.requestItems }" status="i">
																	<g:set var="fulfillmentItems" value="${command.fulfillmentItems(requestItem) }"/>
																	<g:if test="${!fulfillmentItems }">
																		<tr class="${counter++%2?'even':'odd' }">
																			<td>
																				<format:metadata obj="${requestItem.displayName()}"/>
																			</td>							
																			<td>
																				
																			</td>										
																			<td>
																			
																			</td>
																			<td class="center">
																				${requestItem?.quantity}
																			</td>
																			<td class="center">
																				0
																			</td>
																			<td class="center">
																				0
																			</td>
																			<td>
																			</td>
																		</tr>		
																	</g:if>														
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
														<g:submitButton name="finish" value="${warehouse.message(code: 'default.button.finish.label')}"></g:submitButton>
														<g:link action="fulfillRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
													</div>
												</td>
											</tr>
										</table>										
									</g:if>
									<g:else>
										<span class="fade">No items</span>
									</g:else>	
	                            </td>
	                        </tr>
						</tbody>
					</table>
				</fieldset>
			</div>				
		</g:form>
	</div>
	
	<script>
		$(document).ready(function() {
			jQuery.fn.alternateRowColors = function() {
				$('tbody tr:odd', this).removeClass('odd').addClass('even');
				$('tbody tr:even', this).removeClass('even').addClass('odd');
				return this;
			};				

			$("#requestItemsTable").alternateRowColors();
	    	
    	});
    </script>	
</body>
</html>