
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Your Invoice</title>
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a href="${createLinkTo(dir:'')}">Home</a>
		</span>
	</div>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${orderInstance}">
			<div class="errors">
				<g:renderErrors bean="${orderInstance }" as="list" />
			</div>
		</g:hasErrors>
		<div class="dialog">
		
		
			<fieldset>
           		<g:render template="../order/header" model="[orderInstance:order]"/>
		
				<table>
					<tbody>
						<tr class='prop'>
							<td valign='top' class='name'><label for='id'>Order Number:</label></td>
							<td valign='top' class='value'>
								<g:link controller="order" action="show" id="${order.id}">${order?.orderNumber}</g:link>
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
							<td valign='top' class='name'><label for='orderItems'>Items:</label></td>
							<td valign='top'>
								<g:if test="${order?.orderItems }">
									<table>
										<thead>
											<tr class="odd">
												<th>Type</th>
												<th>Category</th>
												<th>Product</th>
												<th>Quantity</th>										
											</tr>
										</thead>									
										<tbody>
											<g:each var="orderItem" in="${order?.orderItems}" status="i">
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
														${orderItem?.category?.name}
													</td>
													<td>
														<g:if test="${orderItem?.product }">
															${orderItem?.product?.name}
														</g:if>
														<g:else>
															${orderItem?.description }
														</g:else>
													</td>
													<td>
														${orderItem?.quantity}
													</td>
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
				
			</fieldset>
		</div>
	</div>
</body>
</html>