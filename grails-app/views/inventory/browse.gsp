
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>    
        
		<style>
			.data-table td, .data-table th { vertical-align: middle; }
			.data-table th { border: 0px; } 
		</style>        
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${commandInstance?.inventoryInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${commandInstance?.inventoryInstance}" as="list" />
	            </div>
            </g:hasErrors>    
			<div class="dialog">

				<!-- Action menu -->
				<div>
					<span class="action-menu">
						<button class="action-btn">
							<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
							<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
						</button>
						<div class="actions">
							<g:render template="browseInventoryMenuItems" model="[commandInstance: commandInstance]"/>																				
						</div>
					</span>				
				</div>			


	        	<g:set var="varStatus" value="${0 }"/>	            			
	         	<table>
					<tr>
						<td style="width:20%">
							
							<g:render template="/common/searchInventory" model="[commandInstance:commandInstance]"/>					
							
							<!--  Disbled 'Actions' until we have time to getting them working as expected -->
							<%-- 
							<g:if test="${commandInstance?.productList }">
								<hr/>
								<div style="padding: 10px;">
									<h3>Actions</h3>
									<div style="padding: 10px;">
										<g:if test="${commandInstance?.productList }">
											<g:link class="new" controller="inventory" action="createTransaction">
												<button>
													Record Inventory
													<img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_right.png' )}" style="vertical-align:middle"/>
												</button>
											</g:link> 				
										</g:if>
									</div>
								</div>	
							</g:if>
							--%>					
	         			</td>
		         		<td>
	            			<div style="overflow: auto; height: 500px; padding: 10px; border: 1px solid lightgrey">		
			            		<g:if test="${commandInstance?.productList }">
		            				<g:set var="productList" value="${commandInstance?.productList?.sort { it.name } }"/>							
									<g:set var="productMap" value="${commandInstance?.productList.groupBy {it.category} }"/>
									<g:each var="entry" in="${productMap}" status="i">	
										<g:set var="totalQuantity" value="${0 }"/>
										<div class="list">
											<div style="font-weight: bold; padding: 10px;">
												<g:render template="../category/breadcrumb" model="[categoryInstance:entry.key]"/>
											</div>
					            			<table class="data-table" style="width: 100%;">         		
					            				<thead>
													<tr class="odd">
														<th width="50%">Description</th>
														<th width="20%">Manufacturer</th>
														<th width="20%">Product Code</th>
														<th width="5%" style="text-align: center">Qty</th>
													</tr>
												</thead>
												<tbody>
													<g:each var="productInstance" in="${productMap[entry.key] }" status="status">
														<g:set var="quantity" value="${commandInstance?.quantityMap?.get(productInstance) }"/>
														<g:set var="totalQuantity" value="${totalQuantity + (quantity?:0) }"/>
														<tr class="${status%2==0?'even':'odd' } prop">
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
																<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]" fragment="inventory">
																	<g:if test="${productInstance?.name?.trim()}">
																		${fieldValue(bean: productInstance, field: "name") } 
																	</g:if>
																	<g:else>
																		Untitled Product
																	</g:else>
																</g:link> 
															</td>
															<td>
																${productInstance?.manufacturer }
															</td>
															<td>
																${productInstance?.productCode }
															</td>
															<td style="text-align: center;">
																<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">
																	${quantity?:0}
																</g:link>
															</td>
														</tr>
													</g:each>
												</tbody>
												<tfoot>										
													<tr class="even prop">
														<th style="text-align: left;">
															
														</th>
														<th style="width: 20%">
															
														</th>
														<th style="width: 20%">
															
														</th>
														<th style="text-align: center; width: 5%">
															${totalQuantity }
														</th>
													</tr>
												</tfoot>										
											</table>	
												
										</div>
									</g:each>										
									
								</g:if>
								<g:else>
									<div class="center">
										<g:if test="${commandInstance?.categoryFilters || commandInstance?.searchTermFilters}">
											<span class="fade">
												Your search did not return any items.  Please try again.
											</span>
										</g:if>
									</div>
								</g:else>		    
							</div>									
		         		</td>
		         	</tr>
		        </table>
			</div>
		</div>
		<script>
			$(document).ready(function() {
				/* Action Menu */
				function show() {
					//$(this).children(".actions").show();
				}
				  
				function hide() { 
					$(this).children(".actions").hide();
				}

				$(".action-btn").click(function() {
					$(this).parent().children(".actions").toggle();
				});
					 
				$(".action-menu").hoverIntent({
					sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
					interval: 50,   // number = milliseconds for onMouseOver polling interval
					over: show,     // function = onMouseOver callback (required)
					timeout: 100,   // number = milliseconds delay before onMouseOut
					out: hide       // function = onMouseOut callback (required)
				});

				//$(".actions").position({ my: "left top", at: "left bottom", of: this });	

				$("#dialogButton").click(function () { 
					$("#dialog").dialog({ "modal": "true", "width": 600});
				});
			});	
		</script>	
    </body>
</html>
