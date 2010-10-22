                                                                        
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Product List</title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
                 <div class="message">
	                 ${flash.message}
                 </div>
            </g:if>
			<table>
				<tbody>
					<g:each var="product" in="${bookList}" status="status">
						<tr class="prop ${status%2==0?'odd':'even'}">
							<td style="width: 25%; height:50px;">
								<img
									src="${createLinkTo(dir: 'images/icons/silk/', file: 'pill.png') }">
								<b>${product.name?.encodeAsHTML()}</b><br />
								${product.description?.encodeAsHTML()}
							</td>
							<td>
								<span class="actionButton"><g:link
									action="shoppingCart" id="${product.id}" event="chooseBook">Buy</g:link></span>
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
        </div>
    </body>
</html>
