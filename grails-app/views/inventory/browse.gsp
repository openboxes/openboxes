<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="default.browse.label" args="[entityName]" /></title>    
        
		<style>
			.data td, .data th { 
				vertical-align: middle; 
			}
			.data th { 
				border: 0px; 
			} 
			.categoryBreadcrumb { 			
				font-weight: bold; 
				padding: 10px;
				text-align: left;
			}
			.clear-all { 
				padding: 5px; 
				margin-left: 20px;
			}
			.filterRow {
				font-size: 13px;
				padding: 5px; 
				margin: 0px;
				white-space:nowrap;
				background-color:#D8D8D8;	
			}
			.filter {
				border: 1px solid black;
				margin-left:10px;
			}
			.paddingRow {
				padding:2px;
				border-top:none; border-right:none; border-left:none;
				background-color:white;
			}
			.filterSelected {
				background-color:white;
				border-bottom:none;
			}
			.checked { 
				background-color: #FFCC66; 
			} 
			.checkbox { 
				width: 50px; 
				border: 1px solid black;
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
            
			<div class="">
				<!-- Inventory Browser -->
				<div>
		        	<g:set var="varStatus" value="${0}"/>
		        	<g:set var="totalProducts" value="${0}"/>        			
		         	<table style="border:none;">
		         		<tr>
		         			<td style="padding:0px; margin:0px;">
		         				<g:render template="filters" model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>						
		         			</td>
		         		</tr>
						<tr class="prop" style="border:1px dotted lightgrey;">
			         		<td style="padding: 0; margin: 0; vertical-align: middle;">
			            		<g:if test="${commandInstance?.categoryToProductMap}">
			            		
						            <g:form controller="shipment" action="addToShipment">
			            		
				            			<div style="overflow: auto; padding: 0px; height: 400px; ">		
											<g:each var="entry" in="${commandInstance?.categoryToProductMap}" status="i">	
												<g:set var="totalQuantity" value="${0}"/>
												<g:set var="categoryInventoryItems" value="${commandInstance?.categoryToProductMap[entry.key]}"/>
												<div class="list">
													<!-- Category Breadcrumb -->
													<div class="categoryBreadcrumb">
														<g:render template="../category/breadcrumb" model="[categoryInstance:entry.key]"/>
													</div>
							            			<table class="data" style="width: 100%;" border="0">         		
							            				<thead>
							            					<tr class="odd">
							            						<th></th>
							            						<th></th>
							            						<th></th>
							            						<th></th>
							            		 				<th colspan="2" class="center"style="text-align: center; border-left: 1px solid lightgrey; border-right: 1px solid lightgrey;"><warehouse:message code="default.pending.label"/></th>
							            						<th></th>
							            					</tr>
															<tr class="odd">
																<th style="width: 50px;"></th>
																<th style="width: 400px;"><warehouse:message code="default.description.label"/></th>
																<th style="width: 100px;"><warehouse:message code="product.manufacturer.label"/></th>
																<th style="width: 100px;"><warehouse:message code="product.code.label"/></th>
																<th style="text-align: center; border-left: 1px solid lightgrey; width: 50px;"><warehouse:message code="inventory.qtyin.label"/></th>
																<th style="text-align: center; border-right: 1px solid lightgrey; width: 50px;"><warehouse:message code="inventory.qtyout.label"/></th>
																<th style="text-align: center; width: 50px;"><warehouse:message code="default.qty.label"/></th>
															</tr>
														</thead>
														<tbody>
															<g:each var="inventoryItem" in="${categoryInventoryItems}" status="status">
																<g:set var="quantity" value="${inventoryItem?.quantityOnHand }"/>
																<g:set var="totalQuantity" value="${totalQuantity + (quantity?:0) }"/>
																<g:set var="cssClass" value="${quantity == 0 ? 'outofstock' : 'instock'  }"/>
																<g:set var="totalProducts" value="${totalProducts + 1}"/>
																
																<tr class="${status%2==0?'even':'odd' } prop ${cssClass}">
																	<td>
																		<g:checkBox id="${inventoryItem?.product?.id }" name="productId" 
																			class="checkbox" checked="${false }" 
																				value="${inventoryItem?.product?.id }" />
																	</td>																
																	<td class="checkable">
																		<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
																			<g:if test="${inventoryItem?.product?.name?.trim()}">
																				<format:product product="${inventoryItem?.product}"/> 
																			</g:if>
																			<g:else>
																				<warehouse:message code="product.untitled.label"/>
																			</g:else>
																		</g:link> 
																	</td>
																	<td class="checkable">
																		${inventoryItem?.product?.manufacturer }
																	</td>
																	<td class="checkable">
																		${inventoryItem?.product?.productCode }
																	</td>
																	<td class="checkable" style="text-align: center; border-left: 1px solid lightgrey;">
																		${inventoryItem?.quantityToReceive?:0}
																	</td>
																	<td class="checkable" style="text-align: center; border-right: 1px solid lightgrey;">
																		${inventoryItem?.quantityToShip?:0}
																	</td>
																	<td class="checkable" style="text-align: center;">
																		<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
																			${inventoryItem?.quantityOnHand?:0}
																		</g:link>
																	</td>
																</tr>
															</g:each>
														</tbody>
														<tfoot>										
															<tr class="even prop">
																<th style="text-align: left;" colspan="6">
																</th>
																<th style="text-align: center;">
																	${totalQuantity}
																</th>
															</tr>
														</tfoot>										
													</table>
												</div>
											</g:each>
										</div>			
										<div class="center" style="padding: 10px;">
											<button>
												<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" style="vertical-align: middle;"/><warehouse:message code="shipping.addToShipments.label"/>
											</button>
										</div>
									</g:form>
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
				$(".checkable a").click(function(e) {
					   e.stopPropagation();
				});
				$('.checkable').toggle(
					function(event) {
						$(this).parent().find('input').click();
						$(this).parent().addClass('checked');
						return false;
					},
					function(event) {
						$(this).parent().find('input').click();
						$(this).parent().removeClass('checked');
						return false;
					}
				);
			});	
		</script>	
    </body>
</html>
