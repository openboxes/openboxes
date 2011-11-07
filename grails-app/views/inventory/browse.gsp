<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="default.browse.label" args="[entityName]" /></title>    
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${commandInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${commandInstance}" as="list" />
	            </div>
            </g:hasErrors>            
			<div>
				<!-- Inventory Browser -->
				<div>
		        	<g:set var="varStatus" value="${0}"/>
		        	<g:set var="totalProducts" value="${0}"/>      			
		         	<table style="border:none;">
		         		<tr>
		         			<td style="padding:0px; margin:0px;">
		         				<g:render template="filters" 
		         					model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>						
		         			</td>
		         		</tr>
						<tr class="prop" style="border:1px dotted lightgrey;">
			         		<td style="padding: 0; margin: 0; vertical-align: middle;">
			            		<g:if test="${commandInstance?.categoryToProductMap}">
						            <form id="inventoryActionForm" name="inventoryActionForm" method="POST">
						                <table class="tableScroll"> 
											<thead> 
					           					<tr class="odd">
													<th class="center">
														<input type="checkbox" id="toggleCheckbox">	
													</th>
													<th><warehouse:message code="default.description.label"/></th>
													<th><warehouse:message code="category.label"/></th>
													<th style="border-left: 1px solid #F7F7F7; border-right:1px solid lightgrey;"><warehouse:message code="product.manufacturer.label"/></th>
					           		 				<th colspan="2" class="center" style="border-left: 1px solid lightgrey; border-right: 1px solid lightgrey;"><warehouse:message code="default.pending.label"/></th>
													<th class="center"><warehouse:message code="default.qty.label"/></th>
					           					</tr>
												<tr class="odd">
													<th class="center"></th>
													<th></th>
													<th></th>
													<th></th>
													<th class="center" style="border-left: 1px solid lightgrey;"><warehouse:message code="inventory.qtyin.label"/></th>
													<th class="center" style="border-right: 1px solid lightgrey;"><warehouse:message code="inventory.qtyout.label"/></th>
													<th></th>
												</tr>
											</thead> 
											<tbody> 
												<g:each var="entry" in="${commandInstance?.categoryToProductMap}" status="i">
													<g:set var="totalQuantity" value="${0}"/>
													<g:set var="categoryInventoryItems" value="${commandInstance?.categoryToProductMap[entry.key]}"/>
													<g:each var="inventoryItem" in="${categoryInventoryItems}" status="status">
														<g:set var="quantity" value="${inventoryItem?.quantityOnHand }"/>
														<g:set var="totalQuantity" value="${totalQuantity + (quantity?:0) }"/>
														<g:set var="totalProducts" value="${totalProducts + 1}"/>
														
														<tr class="${status%2==0?'even':'odd' } prop">
															<td class="middle center" style="width: 1%">
																<g:checkBox id="${inventoryItem?.product?.id }" name="product.id" 
																	class="checkbox" style="top:0em;" checked="${false }" 
																		value="${inventoryItem?.product?.id }" />
															</td>																
															<td class="checkable middle" style="width: 25%">
																<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
																	<g:if test="${inventoryItem?.product?.name?.trim()}">
																		<format:product product="${inventoryItem?.product}"/> 
																	</g:if>
																	<g:else>
																		<warehouse:message code="product.untitled.label"/>
																	</g:else>
																</g:link> 
															</td>
															<td class="checkable middle" style="width: 10%">
																<span class="fade">
																	<format:category category="${inventoryItem?.product?.category}"/> 
																</span>
															</td>
															<td class="checkable middle" style="width: 10%">
																<span class="fade">${inventoryItem?.product?.manufacturer }</span>
															</td>
															
															<td class="checkable middle center" style="width: 5%; border-left: 1px solid lightgrey;">
																${inventoryItem?.quantityToReceive?:0}
															</td>
															<td class="checkable middle center" style="width: 5%; border-right: 1px solid lightgrey;">
																${inventoryItem?.quantityToShip?:0}
															</td>
															<td class="checkable middle center" style="width: 5%;">
																<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
																	${inventoryItem?.quantityOnHand?:0}
																</g:link>
															</td>
														</tr>
													</g:each>
													<tr>
														<th></th>
														<th></th>
														<th></th>
														<th></th>
														<th></th>
														<th></th>
														<th style="text-align: center;">${totalQuantity}</th>
													</tr>
												</g:each>
											</tbody> 
											<tfoot>
												<tr>
													<td class="left">
														<g:render template="./actions" model="[]"/>									
													</td>
													<td colspan="6">
													</td>
												</tr>
											</tfoot>
										</table>		
									</form>
								</g:if>	    
			         		</td>
			         	</tr>
			        </table>
				</div>
				<warehouse:message code="inventory.showingProductsInCategories.label" args="[totalProducts,commandInstance?.categoryToProductMap?.keySet()?.size()]" />
			</div>
		</div>
		<script>
			$(document).ready(function() {

				$('.tableScroll').tableScroll({height:350});
				
				$(".checkable a").click(function(event) {
					event.stopPropagation();
				});
				$('.checkable').toggle(
					function(event) {
						$(this).parent().find('input').click();
						//$(this).parent().addClass('checked');
						return false;
					},
					function(event) {
						$(this).parent().find('input').click();
						//$(this).parent().removeClass('checked');
						return false;
					}
				);
				
				$("#toggleCheckbox").click(function(event) {
					$(".checkbox").attr("checked", $(this).attr("checked"));
				});			
			});	
		</script>	
    </body>
</html>
