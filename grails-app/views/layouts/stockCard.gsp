<html>
<head>
	<meta name="layout" content="custom" />
	<g:layoutHead />
	<title><g:pageProperty name="page.title"/></title>	

</head>
<body>
	<div class="body">	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if> 
		
		<g:hasErrors bean="${commandInstance}">
			<div class="errors">
				<g:renderErrors bean="${commandInstance}" as="list" />
			</div>
		</g:hasErrors>

		<g:hasErrors bean="${flash.errors}">
			<div class="errors">
				<g:renderErrors bean="${flash.errors}" as="list" />
			</div>
		</g:hasErrors>

		<div class="dialog">		
			<fieldset>
				<table>
					<tbody>			
						<tr>
							<td style="width: 50px; text-align:center;">
								<g:render template="../inventoryItem/actions" 
									model="[product:commandInstance?.productInstance,
										inventory:commandInstance?.inventoryInstance]"/>
							</td>
							<td class="middle">
								<h1>
									<g:pageProperty name="page.heading"/>						
								</h1>
							</td>
						</tr>
					</tbody>
				</table>
				
				<!-- Content -->
				<table>				
					<tr>
					
						<!--  Product Details -->
						<td style="width: 250px;">
							<g:render template="productDetails" 
								model="[productInstance:commandInstance?.productInstance, 
									inventoryInstance:commandInstance?.inventoryInstance, 
									inventoryLevelInstance: commandInstance?.inventoryLevelInstance, 
									totalQuantity: commandInstance?.totalQuantity]"/>
						</td>
						<td>
							<g:pageProperty name="page.content"/>	
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
	<script>
		$(document).ready(function() {
			$(".tabs").tabs({ cookie: { expires: 1 } });
		});	
	</script>		

</body>
</html>