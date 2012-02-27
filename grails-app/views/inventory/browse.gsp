<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="default.browse.label" args="[entityName]" /></title>    
        <style>
        	.tableScrollContainer { 
	        	
        	}
        </style>
        
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
		         				<g:render template="tabs" 
		         					model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>						
		         				<g:render template="filters" 
		         					model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>						
		         	<table>
						<tr class="prop">
			         		<td style="padding: 0; margin: 0; vertical-align: middle;">
					            <form id="inventoryActionForm" name="inventoryActionForm" action="createTransaction" method="POST">
					                <fieldset>
						                <table class="tableScroll"> 
					            			<g:if test="${commandInstance?.categoryToProductMap}">
												<thead> 
						           					<tr>
														<th class="center middle">
															<input type="checkbox" id="toggleCheckbox">	
														</th>
														<th class="middle">
															
														</th>
														<th class="middle">
															<warehouse:message code="default.description.label"/>
														</th>
														<th class="center middle" style="width: 10%;">
															<warehouse:message code="product.manufacturer.label"/>
														</th>
														<th class="center" style="width: 7%;">
															<warehouse:message code="inventory.qtyin.label"/>
														</th>
														<th class="center" style="width: 7%;">
															<warehouse:message code="inventory.qtyout.label"/>
														</th>
														<th class="center middle" style="width: 7%;">
															<warehouse:message code="default.qty.label"/>
														</th>
						           					</tr>
												</thead> 
												<tbody> 
													<g:set var="counter" value="${0 }"/>
													<g:each var="entry" in="${commandInstance?.categoryToProductMap}" status="i">
														<g:set var="totalQuantity" value="${0 }"/>
														<g:set var="categoryInventoryItems" value="${commandInstance?.categoryToProductMap[entry.key]}"/>
														<g:each var="inventoryItem" in="${categoryInventoryItems}" status="status">
															<g:set var="supported" value="${!inventoryItem?.inventoryLevel?.status || inventoryItem?.inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.SUPPORTED }"/>													
															<g:set var="quantity" value="${supported ? inventoryItem?.quantityOnHand : 0 }"/>
															<g:set var="totalQuantity" value="${totalQuantity + (quantity?:0) }"/>
															<g:set var="totalProducts" value="${totalProducts + 1}"/>
															
															<tr class="${counter++%2==0?'even':'odd' } prop">
																<td class="middle center">
																	<g:checkBox id="${inventoryItem?.product?.id }" name="product.id" 
																		class="checkbox" style="top:0em;" checked="${false }" 
																			value="${inventoryItem?.product?.id }" />
																</td>																
																<td class="checkable center middle">
																	<img src="${resource(dir: 'images/icons/inventoryStatus', file: inventoryItem?.inventoryLevel?.status?.name()?.toLowerCase() + '.png')}" 
																		alt="${inventoryItem?.inventoryLevel?.status?.name() }" title="${inventoryItem?.inventoryLevel?.status?.name() }" style="vertical-align: middle;"/>
																</td>
																<td class="checkable middle">
																	<span class="fade">
																		<format:category category="${inventoryItem?.product?.category}"/> 
																	</span>
																	<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
																		<g:if test="${inventoryItem?.product?.name?.trim()}">
																			<format:product product="${inventoryItem?.product}"/> 
																		</g:if>
																		<g:else>
																			<warehouse:message code="product.untitled.label"/>
																		</g:else>
																	</g:link> 
																</td>
																<td class="checkable middle center" style="width: 20%">
																	<span class="fade">${inventoryItem?.product?.manufacturer }</span>
																</td>
																<td class="checkable middle center" style="width: 7%; border-left: 1px solid lightgrey;">
																	<g:if test="${supported }">																
																		${inventoryItem?.quantityToReceive?:0}
																	</g:if>
																	<g:else>
																		<span class="fade"><warehouse:message code="default.na.label"/></span>																
																	</g:else>
																</td>
																<td class="checkable middle center" style="width: 7%; border-right: 1px solid lightgrey;">
																	<g:if test="${supported }">																
																		${inventoryItem?.quantityToShip?:0}
																	</g:if>
																	<g:else>
																		<span class="fade"><warehouse:message code="default.na.label"/></span>																
																	</g:else>
																</td>
																<td class="checkable middle center" style="width: 7%;">
																	<g:if test="${supported }">																
																		<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
																			${inventoryItem?.quantityOnHand?:0}
																		</g:link>
																	</g:if>
																	<g:else>
																		<span class="fade"><warehouse:message code="default.na.label"/></span>																
																	</g:else>
																</td>
															</tr>
														</g:each>
													</g:each>
												</tbody>
												<tfoot>
													<tr>
														<td colspan="1" class="center middle">
															<g:render template="./actions" model="[]"/>
														</td>			
														<td colspan="6" class="left middle">
															<warehouse:message code="inventory.showingProductsInCategories.label" args="[totalProducts,commandInstance?.categoryToProductMap?.keySet()?.size()]" />
															<%-- 
															(<g:each var="category" in="${commandInstance?.categoryToProductMap?.keySet()}">
																<g:link controller="inventory" action="browse" params="['categoryId':category.id]">
																	<format:metadata obj="${category}"/>&nbsp;
																</g:link>
															</g:each>)
															--%>
														</td>
													</tr>
												</tfoot>
											</g:if>	    
											<g:else>
												<tbody >
													<tr>
														<td class="middle center" style="height: 100px;">
															<warehouse:message code="inventory.searchNoMatch.message" args="[commandInstance?.searchTerms?:'',format.metadata(obj:commandInstance?.categoryInstance)]"/>
														</td>
													</tr>
												</tbody>
											</g:else>
										</table>
									</fieldset>	
								</form>									
			         		</td>
			         	</tr>
			        </table>
				</div>
			</div>
		</div>
		<script>
			$(document).ready(function() {
				$('.tableScroll').tableScroll({height: 250, width: '99%'});
				
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
