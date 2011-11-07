
<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${warehouse.message(code: 'stockCard.label', default: 'Stock Card').toLowerCase()}" />
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
													<g:render template="showStockCardMenuItems" model="[product: commandInstance?.productInstance, inventory: commandInstance?.inventoryInstance]"/>																				
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
								<td style="width: 250px;">
									<g:render template="productDetails" 
										model="[productInstance:commandInstance?.productInstance, inventoryInstance:commandInstance?.inventoryInstance, 
											inventoryLevelInstance: commandInstance?.inventoryLevelInstance, totalQuantity: commandInstance?.totalQuantity]"/>
								</td>
								
								<!--  Current Stock and Transaction Log -->
								<td>
									<g:if test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.SUPPORTED }">
													<h2 class="fade"><warehouse:message code="inventory.currentAndPendingStock.label"/></h2>
										<table>
											<tr>
												<td style="padding: 0px;">
													<div id="transactionLogTabs">												
														<ul>
															<li><a href="#tabs-1"><warehouse:message code="inventory.currentStock.label"/></a></li>
															<li><a href="#tabs-2"><warehouse:message code="request.pendingRequestLog.label"/></a></li>
															<li><a href="#tabs-3"><warehouse:message code="order.pendingOrderLog.label"/></a></li>
															<li><a href="#tabs-4"><warehouse:message code="shipment.pendingShipmentLog.label"/></a></li>
														</ul>		
														<div id="tabs-1" style="padding: 0px;">										
															<g:render template="showCurrentStock"/>
														</div>
														<div id="tabs-2" style="padding: 0px;">
															<g:render template="showPendingRequestLog"/>
														</div>
														<div id="tabs-3" style="padding: 0px;">
															<g:render template="showPendingOrderLog"/>
														</div>
														<div id="tabs-4" style="padding: 0px;">
															<g:render template="showPendingShipmentLog"/>
														</div>

													</div>
												</td>
											</tr>
										</table>
									</g:if>
									<g:elseif test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.NOT_SUPPORTED }">
										<div> 	
											<h2 class="fade"><warehouse:message code="inventory.currentAndPendingStock.label"/></h2>
											<div class="padded center box">
												<span class="fade"><g:message code="enum.InventoryStatus.NOT_SUPPORTED"/></span>
												<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': commandInstance?.productInstance?.id, 'inventory.id':commandInstance?.inventoryInstance?.id]">
													<warehouse:message code="default.change.label"/>
												</g:link>
											</div>
										</div>									
									</g:elseif>								
									<g:elseif test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.SUPPORTED_NON_INVENTORY }">
										<div> 	
											<h2 class="fade"><warehouse:message code="inventory.currentAndPendingStock.label"/></h2>
											<div class="padded center box">
												<span class="fade"><g:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/></span>
												<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': commandInstance?.productInstance?.id, 'inventory.id':commandInstance?.inventoryInstance?.id]">
													<warehouse:message code="default.change.label"/>
												</g:link>
											</div>
										</div>
									</g:elseif>									
									<div>
										<h2 class="fade"><warehouse:message code="transaction.transactionLog.label"/></h2>								
										<g:render template="showTransactionLog"/>
									</div>
								</td>
							</tr>
						</table>
					</div>
				</fieldset>
			</div>
			
			
		</div>
		
		<script>
			$(document).ready(function() {

				// Define dialog
				$("#transaction-details").dialog({ title: "Transaction Details", 
					modal: true, autoOpen: false, width: 800, height: 400, position: 'middle' });    //end dialog									    

				// Click event -> open dialog
				$('.show-details').click(
			        function(event) {
				        //$("#example").load("", [], function() { 
				        //    jQuery("#example").dialog("open");
				        //});
				        //return false
						var link = $(this);
						var dialog = jQuery('#transaction-details').load(link.attr('href')).dialog("open");										        
				        event.preventDefault();
			        }
			    );	
				$( "#transactionLogTabs" ).tabs();
			});	


			
		</script>		
	</body>
</html>
