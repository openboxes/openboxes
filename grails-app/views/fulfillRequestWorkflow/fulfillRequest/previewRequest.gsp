
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="fulfillRequestWorkflow.previewRequest.label" default="Preview request"/></title>
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
					<g:render template="progressBar" model="['state':'previewRequest']"/>		
					<table>
						<tbody>							
							<tr class="prop">
	                            <td valign="top" class="name">
	                            	<label for='requestItems'><warehouse:message code="request.items.label" default="Items" /></label>
	                            </td>
	                            <td valign="top" class="value" style="padding: 0px; margin: 0px;">
									<g:if test="${command?.request?.requestItems }">
										<table id="requestItemsTable">
											<thead>
												<tr class="odd">
													<th></td>
													<th><warehouse:message code="requestItem.type.label" default="Type"/></th>
													<th><warehouse:message code="requestItem.item.label" default="Item Requested"/></th>
													<th class="center"><warehouse:message code="requestItem.quantity.label" default="Qty"/></th>										
												</tr>
											</thead>									
											<tbody>
												<g:each var="requestItem" in="${command?.request?.requestItems }" status="i">
													<g:if test="${requestItem?.quantity > 0}">
														<tr class="${i%2?'odd':'even' }">
															<td>
																<a name="requestItems${i }"></a>																
															</td>
															<td>
																${requestItem?.type }
															</td>
															<td>
																${requestItem?.description }
															</td>
															<td class="center">
																${requestItem?.quantity}
															</td>
														</tr>
													</g:if>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade"><warehouse:message code="requestItem.item.label" default="No items requested"/></span>
									</g:else>	
	                            </td>
	                        </tr>
	                        <tr class="prop">
	                        	<td colspan="2">
									<div class="buttons">
										<span class="formButton"> 
											<g:submitButton name="next" value="${warehouse.message(code: 'default.button.next.label')}"></g:submitButton>
											<g:link action="fulfillRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
										</span>
									</div>	                        	
	                        	</td>
	                        </tr>
						</tbody>
					</table>
				</fieldset>
			</div>				
		</g:form>
	</div>
</body>
</html>