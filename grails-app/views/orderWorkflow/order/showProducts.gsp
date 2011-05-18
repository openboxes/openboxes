
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Product List</title>
</head>
<body>
	<div class="body">
		<h1>Product List</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<div class="dialog">
			<g:form action="purchaseOrder" method="post" >
				<table>
					<tr>
						<td>Search</td>
						<td>
							<input type="text" name="searchTerms"/>
							<g:submitButton name="searchProducts" value="Search"></g:submitButton>								
						</td>
						<td>
							<span class="actionButton">
								<g:link action="purchaseOrder" event="showCart">Show Cart</g:link>
							</span>
						</td>
					</tr>
				</table>
			</g:form>
		</div>		
		<div class="list">
			<table>
				<thead>
					<tr class="odd">
						<g:sortableColumn property="id" title="Id" />
						<g:sortableColumn property="name" title="Name" />
						<th></th>
					</tr>
				</thead>
				<tbody>
					<g:each var="product" in="${productList}" status="i">
						<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">	
							<td>
								${product.id?.encodeAsHTML()}
							</td>
							<td>
								${product.name?.encodeAsHTML()}
							</td>
							<td class="actionButtons">
								<span class="actionButton">
									<g:form action="purchaseOrder" method="post" autocomplete="off" >
										<input type="hidden" name="id" value="${product?.id?.encodeAsHTML() }"/>									
										<input type="text" name="quantity" value="1" size="3"/>									
										<g:submitButton name="chooseProduct" value="Buy"></g:submitButton>		
									</g:form>
								</span>
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>
</body>
</html>