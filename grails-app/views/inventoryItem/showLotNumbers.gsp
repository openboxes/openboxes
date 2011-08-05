
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${warehouse.message(code: 'inventory.lotNumbers.label')}" />
		<title>
			<warehouse:message code="default.show.label" args="[entityName]" /> &nbsp;&rsaquo;&nbsp; 
			<format:product product="${commandInstance?.productInstance}"/>
		</title>
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

			<div class="dialog" style="min-height: 880px">
			
				<fieldset>
				
					<div>
						<table>
							<tbody>			
								<tr>
									<td style="width: 50px; vertical-align: middle;">
										<div class="fade" style="font-size: 0.9em;">
											<!-- Action menu -->
											<span class="action-menu">
												<button class="action-btn">
													<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
													<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
												</button>
												<div class="actions">
													<g:render template="showStockCardMenuItems" model="[commandInstance: commandInstance]"/>																				
												</div>
											</span>				
										</div>			
									</td>
									<td style="vertical-align: middle;">
										<h1><format:product product="${commandInstance?.productInstance}"/></h1>
									
									</td>
								</tr>
							</tbody>
						</table>
				
						<!-- Content -->
						<table>				
							<tr>
								<!--  Product Details -->
								<td style="width: 250px;" rowspan="3">
									<g:render template="productDetails" 
										model="[productInstance:commandInstance?.productInstance, inventoryInstance:commandInstance?.inventoryInstance, 
											inventoryLevelInstance: commandInstance?.inventoryLevelInstance, totalQuantity: commandInstance?.totalQuantity]"/>
								</td>
								
								<!--  Lot Numbers -->
								<td>
									<g:render template="showLotNumbers"/>
								</td>
							</tr>
							
						</table>
					</div>
				</fieldset>
			</div>
		</div>	
	</body>
</html>
