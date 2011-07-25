
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>    
        
		<style>
			.data-table td, .data-table th { 
				vertical-align: middle; 
			}
			.data-table th { 
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
			.filter { 
				background-color: #f7f7f7;
				font-size: 13px;
				padding: 5px; 
				margin: 5px;
				border: 1px solid lightgrey; 
			}
			.checked { 
				background-color: lightyellow; 
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
            
			<div class="dialog">
				<fieldset>
					<!-- Inventory Browser -->
					<div>
			        	<g:set var="varStatus" value="${0 }"/>	            			
			         	<table>
			         		<tr class="">
			         			<td>
			         				<g:render template="summary" model="[commandInstance:commandInstance]"/>						
			         			</td>
			         		</tr>
			         		<tr class="prop">
			         			<td>
			         				<g:render template="filters" model="[commandInstance:commandInstance]"/>						
			         			</td>
			         		</tr>
							<tr class="prop">
				         		<td style="padding: 0; margin: 0; vertical-align: middle; text-align: center">
				            		<g:if test="${commandInstance?.inventoryItems}">
				            		
							            <g:form controller="shipment" action="addToShipment">
				            		
					            			<div style="overflow: auto; padding: 0px; height: 400px; ">		
												<g:each var="entry" in="${commandInstance?.inventoryItems}" status="i">	
													<g:set var="totalQuantity" value="${0}"/>
													<g:set var="categoryInventoryItems" value="${commandInstance?.inventoryItems[entry.key]}"/>
													<div class="list">
														<!-- Category Breadcrumb -->
														<div class="categoryBreadcrumb">
															<g:render template="../category/breadcrumb" model="[categoryInstance:entry.key]"/>
														</div>
								            			<table class="data-table" style="width: 100%;">         		
								            				<thead>
																<tr class="odd">
																	<th></th>
																	<th width="50%">Description</th>
																	<th width="20%">Manufacturer</th>
																	<th width="20%">Product Code</th>
																	<th width="5%" style="text-align: center">Qty In</th>
																	<th width="5%" style="text-align: center">Qty Out</th>
																	<th width="5%" style="text-align: center">Qty</th>
																</tr>
															</thead>
															<tbody>
																<g:each var="inventoryItem" in="${categoryInventoryItems}" status="status">
																	<g:set var="quantity" value="${inventoryItem?.quantityOnHand }"/>
																	<g:set var="totalQuantity" value="${totalQuantity + (quantity?:0) }"/>
																	<g:set var="cssClass" value="${quantity == 0 ? 'outofstock' : 'instock'  }"/>
																	<tr class="${status%2==0?'even':'odd' } prop checkable ${cssClass}">
																	<%-- 
																		<td nowrap="true">
																			<div class="action-menu">
																				<button class="action-btn">
																					<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
																					<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
																				</button>
																				<div class="actions" style="position: absolute; display: none;">
																					<div class="action-menu-item">				
																						<g:link controller="inventory" action="browse">
																							<img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh.png')}" style="vertical-align: middle;"/>&nbsp;Refresh
																						</g:link>
																					</div>																			
																				</div>
																			</div>	
																		</td>
																	--%>
																		<td>
																			<g:checkBox id="${inventoryItem?.product?.id }" name="productId" 
																				class="checkbox" checked="${false }" 
																					value="${inventoryItem?.product?.id }" />
																		</td>																
																		<td>
																			<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
																				<g:if test="${inventoryItem?.product?.name?.trim()}">
																					${fieldValue(bean: inventoryItem?.product, field: "name") } 
																				</g:if>
																				<g:else>
																					Untitled Product
																				</g:else>
																			</g:link> 
																		</td>
																		<td>
																			${inventoryItem?.product?.manufacturer }
																		</td>
																		<td>
																			${inventoryItem?.product?.productCode }
																		</td>
																		<td style="text-align: center; border-left: 1px solid lightgrey;">
																			${inventoryItem?.quantityToReceive?:0}
																		</td>
																		<td style="text-align: center; border-right: 1px solid lightgrey;">
																			${inventoryItem?.quantityToShip?:0}
																		</td>
																		<td style="text-align: center;">
																			<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
																				${inventoryItem?.quantityOnHand?:0}
																			</g:link>
																		</td>
																	</tr>
																</g:each>
															</tbody>
															<tfoot>										
																<tr class="even prop">
																	<th>
																	</th>
																	<th style="text-align: left;">
																		
																	</th>
																	<th style="width: 20%">
																		
																	</th>
																	<th style="width: 20%">
																		
																	</th>
																	<th>
																	
																	</th>
																	<th>
																	
																	</th>
																	<th style="text-align: center; width: 5%">
																		${totalQuantity }
																	</th>
																</tr>
															</tfoot>										
														</table>	
															
													</div>
												</g:each>										
											</div>			
											<div class="center" style="padding: 10px;">
												<button>
													<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" style="vertical-align: middle;"/>Add to shipment(s)
												</button>
											</div>
										</g:form>
									</g:if>
									<g:else>
										<span >
											<g:if test="${commandInstance?.categoryFilters || commandInstance?.searchTermFilters}">
												Your search did not return any items.  Please try again.
											</g:if>
										</span>
									</g:else>		    
				         		</td>
				         	</tr>
				        </table>
					</div>
				</fieldset>
			</div>
		</div>
		<script>
			$(document).ready(function() {
				$(".checkable a").click(function(e) {
					   e.stopPropagation();
				});
				$('.checkable').toggle(
					function(event) {
						$(this).find('input').attr('checked', true);
						$(this).addClass('checked');
						return false;
					},
					function(event) {
						$(this).find('input').attr('checked', false);
						$(this).removeClass('checked');
						return false;
					}
				);

				
				$(".toggle-outofstock").click(function() { 
					$(".outofstock").toggle();					
				});
				
				
				
			});	
		</script>	
    </body>
</html>
