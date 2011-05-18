
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Your Shopping Cart</title>
</head>
<body>
	<div class="body">
		<h1>Your Shopping Cart</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<h2>Cart Items</h2>
		<g:form action="purchaseOrder">
			<table>
				<thead>
					<tr class="odd">
						<g:sortableColumn property="id" title="Id" />
						<g:sortableColumn property="name" title="Name" />
						<th></th>
					</tr>
				</thead>
				<tbody>
					<g:each var="item" in="${cartItems}" status="i">
						<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

							<td>
								${item.id?.encodeAsHTML()}
							</td>
							<td>
								${item.name?.encodeAsHTML()}
							</td>
							<td class="actionButtons"></td>
						</tr>
					</g:each>
				</tbody>
			</table>
			<g:submitButton name="continueShopping" value="Continue Shopping"></g:submitButton>
			<g:submitButton name="checkout" value="Checkout"></g:submitButton>
		</g:form>

	</div>
</body>
</html>