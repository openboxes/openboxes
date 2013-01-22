<html>
<head>
	<meta name="layout" content="custom" />
	<g:layoutHead />
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript" ></script>    
    <script src="${createLinkTo(dir:'js/', file:'inventory.js')}" type="text/javascript" ></script>
    
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
		
		<g:render template="../product/summary" model="[productInstance:commandInstance?.productInstance,
			inventoryInstance:commandInstance?.inventoryInstance,
			inventoryLevelInstance: commandInstance?.inventoryLevelInstance,
			totalQuantity: commandInstance?.totalQuantity]"/>

		<div class="dialog">		
			<table>				
				<tr>
					<td style="width: 25%;">
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

		</div>
	</div>
	<script>
		$(document).ready(function() {
			$(".tabs").tabs({ cookie: { expires: 1 } });
		});	
	</script>		

</body>
</html>