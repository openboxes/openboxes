                                                                        
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Your Shopping Cart</title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
                 <div class="message">
                       ${flash.message}
                 </div>
            </g:if>    
            
            
			<h2><img src="${createLinkTo(dir: 'images/icons/silk/', file: 'cart.png') }"> &nbsp; Cart Items</h2>
			<g:form action="shoppingCart">
				<table>
					<thead>
						<tr>
							<g:sortableColumn property="name" title="Name" />
							<th></th>
						</tr>
					</thead>
					<tbody>
						<g:each var="product" in="${cartItems}" status="status">
							<tr class="${status%2==0?'odd':'even'}">
								<td>
									${product.name?.encodeAsHTML()}
								</td>
								<td class="actionButtons"></td>
							</tr>
						</g:each>
						<tr>
							<td></td>
							<td></td>
							<td></td>
							<td>Total: ${cartItems.size()}
							</td>
							<td></td>
						</tr>
					</tbody>
				</table>
				<g:submitButton name="continueShopping" value="Continue Shopping"></g:submitButton>	           
				<g:submitButton name="checkout" value="Checkout"></g:submitButton>
			</g:form>

        </div>
    </body>
</html>
