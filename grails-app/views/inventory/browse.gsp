
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
            
			<script>
				$(function() {
					$("#dialogButton").click(function () { 
						$("#dialog").dialog({ "modal": "true", "width": 600});
					});
				});
			</script>

        	<g:set var="varStatus" value="${0 }"/>	            			
         	<table>
				<tr>
					<td style="width:20%">
						<h3 style="background-color: #525D76; color: white; padding: 10px;">Filters</h3>
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
	            		<g:if test="${commandInstance?.productList }">
	            			<div style="overflow: auto; height: 500px;">									
								<g:set var="productMap" value="${commandInstance?.productList.groupBy {it.category} }"/>
								<g:each var="entry" in="${productMap}" status="i">	
									<g:set var="totalQuantity" value="${0 }"/>
									<div class="list">
										<h3 style="background-color: #525D76; color: white; padding: 10px;">
											<%-- 
											<g:if test="${entry?.key?.parentCategory }">
												${entry?.key?.parentCategory?.name } &rsaquo; 
											</g:if>
											${entry?.key?.name?:"Uncategorized" }
											--%>										
											<g:render template="../category/breadcrumb" model="[categoryInstance:entry.key]"/>		
										</h3>
				            			<table class="data-table">         		
				            			<%-- 
				            				<thead>
												<tr>
													<th width="50%">Description</th>
													<th width="20%">Category</th>
													<th width="5%" style="text-align: center">Qty</th>
												</tr>
											</thead>
										--%>
											<tbody>
												<g:each var="productInstance" in="${productMap[entry.key] }">
													<g:set var="quantity" value="${commandInstance?.quantityMap?.get(productInstance) }"/>
													<g:set var="totalQuantity" value="${totalQuantity + (quantity?:0) }"/>
													<tr class="${varStatus++%2==0?'odd':'even' } prop">
														<td>
															<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">
																<g:if test="${productInstance?.name?.trim()}">
																	${fieldValue(bean: productInstance, field: "name") } 
																</g:if>
																<g:else>
																	Untitled Product
																</g:else>
															</g:link> 
														</td>
														<td>
															<span class="fade">
																${productInstance?.category?.name?:"Uncategorized" }
															</span>
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
												<tr class="${varStatus%2==0?'odd':'even' } prop">
													<th style="text-align: left;">
														Total items
													</th>
													<th style="width:20%;">
														${entry?.key?:"Uncategorized" }
													</th>
													<th style="text-align: center; width: 10%">
														${totalQuantity }
													</th>
												</tr>
											</tfoot>										
										</table>	
									</div>
								</g:each>										
							</div>									
						</g:if>
						<g:else>
							<div class="center middle">
								<g:if test="${commandInstance?.categoryFilters || commandInstance?.searchTermFilters}">
									<span>
										Your search did not return any items.  Please try again.
									</span>
								</g:if>
							</div>
						</g:else>		    
	         		</td>
	         	</tr>
	        </table>
		</div>
		
		<script>
			$(document).ready(function() {
				/* Action Menu */
				function show() {
					$(this).children(".actions").show();
				}
				  
				function hide() { 
					$(this).children(".actions").hide();
				}
					 
				$(".action-menu").hoverIntent({
					sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
					interval: 50,   // number = milliseconds for onMouseOver polling interval
					over: show,     // function = onMouseOver callback (required)
					timeout: 200,   // number = milliseconds delay before onMouseOut
					out: hide       // function = onMouseOut callback (required)
				});

				$( ".actions" ).position({ my: "left top", at: "left bottom" });	


				$('.toggleSupportedImage').click(function() {	
					var image = $('.toggleSupportedImage');
					var currImageSrc = image.attr("src");
					var playImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'control_play.png' )}";							
					var stopImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'control_stop.png' )}";							
					var imageSrc = (currImageSrc == playImageSrc)?stopImageSrc:playImageSrc;							
					image.attr("src",imageSrc);								
				});

				
			});	


			
		</script>			
		
    </body>
</html>
