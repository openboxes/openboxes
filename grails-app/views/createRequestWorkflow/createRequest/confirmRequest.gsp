
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Confirm Order</title>
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
							<tr class='prop'>
								<td valign='top' class='name'><label for='id'>Order Number:</label></td>
								<td valign='top' class='value'>
									<g:if test="${requestInstance?.requestNumber }">
										${requestInstance?.requestNumber }
									</g:if>
									<g:else>
										<span class="fade">New Order</span>
									</g:else>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='description'>Description:</label></td>
								<td valign='top' class='value'>
									${requestInstance?.description?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Ordered from:</label></td>
								<td valign='top' class='value'>
									${requestInstance?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Ordered for:</label></td>
								<td valign='top' class='value'>
									${requestInstance?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateRequested'>Requested date:</label></td>
								<td valign='top' class='value'>								
									<format:date obj="${requestInstance?.dateRequested }"/>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='requestedBy'>Ordered by:</label></td>
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
													<g:sortableColumn property="type" title="Type" />
													<g:sortableColumn property="name" title="Name" />
													<g:sortableColumn property="quantity" title="Quantity" />
													<th></th>
												</tr>
											</thead>
											<tbody>
												<g:each var="requestItem" in="${requestInstance.requestItems}" status="i">
													<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
														<td>
															<g:if test="${requestItem?.product }">
																Product
															</g:if>
															<g:elseif test="${requestItem?.category }">
																Category
															</g:elseif>							
															<g:else>
																Unclassified											
															</g:else>
														</td>
														<td>
															${requestItem?.description?.encodeAsHTML()}
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
										<span class="fade">No items</span>
									</g:else>
	                            </td>
	                        </tr>
						</tbody>
					</table>
					<div class="buttons">
						<span class="formButton"> 
							<g:submitButton name="back" value="Back"></g:submitButton> 
							<g:submitButton name="finish" value="Finish"></g:submitButton>
							<g:link action="createRequest" event="cancel">Cancel</g:link>
						</span>
					</div>
				</fieldset>
			</div>				
		</g:form>
	</div>
</body>
</html>