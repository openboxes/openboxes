
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
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
		
		<g:form action="purchaseOrder" method="post">
			<div class="dialog">
			
			
				<fieldset>
            		<g:render template="../order/header" model="[orderInstance:order]"/>
            				
					<table>
						<tbody>
							<tr class='prop'>
								<td valign='top' class='name'><label for='id'>Order Number:</label></td>
								<td valign='top' class='value'>
									<g:if test="${order?.orderNumber }">
										${order?.orderNumber }
									</g:if>
									<g:else>
										<span class="fade">New Order</span>
									</g:else>
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='description'>Description:</label></td>
								<td valign='top' class='value'>
									${order?.description?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='source'>Order from:</label></td>
								<td valign='top' class='value'>
									${order?.origin?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for="destination">Destination:</label></td>
								<td valign='top' class='value'>
									${order?.destination?.name?.encodeAsHTML()}
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='dateOrdered'>Order date:</label></td>
								<td valign='top' class='value'>								
									${order?.dateOrdered } 
								</td>
							</tr>
							<tr class='prop'>
								<td valign='top' class='name'><label for='orderedBy'>Ordered by:</label></td>
								<td valign='top'class='value'>
									${order?.orderedBy?.name }
								</td>
							</tr>
							<tr class="prop">
	                            <td valign="top" class="name"><g:message code="order.items.label" default="Items" /></td>
	                            <td valign="top" class="value">
									<g:if test="${order?.orderItems }">
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
												<g:each var="orderItem" in="${order.orderItems}" status="i">
													<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
														<td>
															<g:if test="${orderItem?.product }">
																Product
															</g:if>
															<g:elseif test="${orderItem?.category }">
																Category
															</g:elseif>							
															<g:else>
																Unclassified											
															</g:else>
														</td>
														<td>
															${orderItem?.description?.encodeAsHTML()}
														</td>
														<td>
															${orderItem?.quantity}
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
							<g:submitButton name="processOrder" value="Finish"></g:submitButton>
						</span>
					</div>
				</fieldset>
			</div>				
				
				
		</g:form>
	</div>
</body>
</html>