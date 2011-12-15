
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.confirmOrder.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestInstance}">
			<div class="errors">
				<g:renderErrors bean="${requestInstance}" as="list" />
			</div>
		</g:hasErrors>
		
		<g:form action="createRequest" method="post">
			<div class="dialog">
			
			
				<fieldset>
            		<g:render template="../request/summary" model="[requestInstance:requestInstance]"/>
            				
					<table>
						<tbody>
							<%-- 
							<tr class='prop'>
								<td valign='top' class='name'><label for='id'><warehouse:message code="request.orderNumber.label"/>:</label></td>
								<td valign='top' class='value'>
									<g:if test="${requestInstance?.requestNumber }">
										${requestInstance?.requestNumber }
									</g:if>
									<g:else>
										<span class="fade">New Order</span>
									</g:else>
								</td>
							</tr>
							--%>
							<tr class='prop'>
								<td valign='top' class='name'><label for='description'><warehouse:message code="default.description.label"/>:</label></td>
								<td valign='top' class='value'>
									${requestInstance?.description?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'><warehouse:message code="request.orderedFrom.label"/>:</label></td>
								<td valign='top' class='value'>
									${requestInstance?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination"><warehouse:message code="request.orderedFor.label"/>:</label></td>
								<td valign='top' class='value'>
									${requestInstance?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateRequested'><warehouse:message code="request.date.label"/>:</label></td>
								<td valign='top' class='value'>								
									<format:date obj="${requestInstance?.dateRequested }"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='requestedBy'><warehouse:message code="request.orderedBy.label"/>:</label></td>
								<td valign='top'class='value'>
									${requestInstance?.requestedBy?.name }
								</td>
							</tr>
							<tr class="prop">
	                            <td valign="top" class="name"><warehouse:message code="request.items.label" default="Items" /></td>
	                            <td valign="top" class="value">
									<g:if test="${requestInstance?.requestItems }">
										<table>
											<thead>
												<tr class="odd">
													<g:sortableColumn property="type" title="${warehouse.message(code:'request.type.label')}" />
													<g:sortableColumn property="name" title="${warehouse.message(code:'default.name.label')}" />
													<g:sortableColumn property="quantity" title="${warehouse.message(code:'default.quantity.label')}" />
													<th></th>
												</tr>
											</thead>
											<tbody>
												<g:each var="requestItem" in="${requestInstance.requestItems}" status="i">
													<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
														<td>
															<g:if test="${requestItem?.product }">
																<warehouse:message code="product.label"/>
															</g:if>
															<g:elseif test="${requestItem?.category }">
																<warehouse:message code="product.category"/>
															</g:elseif>							
															<g:else>
																<warehouse:message code="default.unclassified.label"/>											
															</g:else>
														</td>
														<td>
															<format:metadata obj="${requestItem.displayName()}"/>
														</td>
														<td>
															${requestItem?.quantity}
														</td>
														<td class="actionButtons"></td>
													</tr>
												</g:each>
											</tbody>
										</table>
									</g:if>
									<g:else>
										<span class="fade"><warehouse:message code="default.noItems.label"/></span>
									</g:else>
	                            </td>
	                        </tr>
						</tbody>
					</table>
					<div class="buttons">
						<span class="formButton"> 
							<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton> 
							<g:submitButton name="finish" value="${warehouse.message(code:'default.button.finish.label')}"></g:submitButton>
							<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
						</span>
					</div>
				</fieldset>
			</div>				
		</g:form>
	</div>
</body>
</html>