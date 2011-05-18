<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Add Order Items</title>
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
		
		<table>
			<tr>
				<td>
					<g:form action="purchaseOrder">
						<table>
							<thead>
								<tr class="odd">
									<g:sortableColumn property="type" title="Type" />
									<g:sortableColumn property="category" title="Category" />
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
											${orderItem?.category?.name}
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
					</g:form>
				</td>
				<td style="border-left: 1px solid lightgrey; height: 400px; width: 25%;">
					<h3>Add Item</h3>
					<g:form action="purchaseOrder" method="post">
						<div class="dialog">
							<table>
								<tbody>
									<tr class='prop'>
										<td valign='top' class='name'><label for='product.id'>Product:</label>
										</td>
										<td valign='top' class='value'>
											<div class="ui-widget">
												<g:select class="combobox" name="product.id" from="${org.pih.warehouse.product.Product.list().sort{it.name}}" optionKey="id" value="" noSelection="['':'']" />
											</div>
										</td>
									</tr>
									<tr class='prop'>
										<td valign='top' colspan="2" class="center">
											<label for='or'>- OR - </label>
										</td>
									</tr>
									<tr class='prop'>
										<td valign='top' class='name'><label for='source'>Category:</label>
										</td>
										<td valign='top' class='value'>
											<g:jqueryComboBox />
											<div class="ui-widget"> 
												<g:select class="combobox" name="category.id" from="${org.pih.warehouse.product.Category.list().sort()}" optionKey="id" value="" noSelection="['':'']" />
											</div>
										</td>
									</tr>
									<tr class='prop'>
										<td valign='top' class='name'><label for='description'>Description:</label>
										</td>
										<td valign='top' class='value'>
											<input type="text" name='description' value="" size="30" />
										</td>
									</tr>
									<tr>
										<td valign="top" class="name">
										
										</td>
										<td valign="top" class="value">
										
											<div class="buttons">
												<span class="formButton"> 
													<g:submitButton name="addItem" value="Add Item"></g:submitButton> 
												</span>
											</div>
										
										</td>
									</tr>
									
								</tbody>
							</table>
						</div>
					</g:form>				
				</td>			
			</tr>
		</table>

		<g:form action="purchaseOrder" method="post">
			<div class="buttons">
				<g:submitButton name="back" value="Back"></g:submitButton>
				<g:submitButton name="confirmOrder" value="Next"></g:submitButton>
			</div>
		</g:form>


	</div>
</body>
</html>