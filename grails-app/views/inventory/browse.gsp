
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>    
        
		<style>
			.data-table td, .data-table th { height: 3em; vertical-align: middle; }
		</style>        
    </head>    
    <body>
        <div class="body">
			<div class="nav">
				<g:render template="nav"/>
			</div>
        
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${commandInstance?.inventoryInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${commandInstance?.inventoryInstance}" as="list" />
	            </div>
            </g:hasErrors>    
            
			<table>
				<tr>
					<td width="15%" style="border: 1px solid lightgrey;">             
						<g:render template="/common/searchCriteriaVertical" model="[productInstanceList: commandInstance?.productList, categoryFilters: commandInstance?.categoryFilters, rootCategory: commandInstance?.rootCategory]"/>					
             

					</td>
					<td width="1%"></td>
					<td style="border: 1px solid lightgrey;">	
								            		
	            		<script>
							$(function() {
								$("#dialogButton").click(function () { 
									$("#dialog").dialog({ "modal": "true", "width": 600});
								});
							});
						</script>
	            			<g:set var="varStatus" value="${0 }"/>	            			
		            		<table>
		            			<tr class="odd">
			            			<td colspan="2">
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'product.png' )}" style="vertical-align:middle"/>
										<label>Inventory Items</label> 
					 				</td>
		            			</tr>
		            			<tr>
		            				<td>
										<div>	            		
											<table class="dataTable">
												<thead>
													<tr class="even prop">
														<th width="50%">Description</th>
														<th width="20%">Category</th>
														<th width="5%" style="text-align: center">Qty</th>
													</tr>
												</thead>
											</table>
						            		<g:if test="${commandInstance?.productList }">
						            			<div style="overflow: auto; ">
							            			<table class="data-table">         		
														<tbody>
															<g:set var="totalQuantity" value="${0 }"/>
															<g:each var="productInstance" in="${commandInstance?.productList }" status="i">											 	
																<g:set var="quantity" value="${commandInstance?.quantityMap?.get(productInstance) }"/>
																<g:set var="totalQuantity" value="${totalQuantity + (quantity?:0) }"/>
																<tr class="${varStatus++%2==0?'odd':'even' } prop">
																	<td width="50%">
																		<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">
																			${productInstance?.name }
																		</g:link> 
																			
																	</td>
																	<td width="20%">
																		<span class="fade">${productInstance?.category?.name }</span>
																	</td>
																	<td width="5%" style="text-align: center;">
																		<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">
																			${quantity}
																		</g:link>
																	</td>
																</tr>
															</g:each>										
														</tbody>
														<tfoot>
														
															<tr class="${varStatus%2==0?'odd':'even' } prop">
																<th style="text-align: left;">
																	
																</th>
																<th></th>
																<th style="text-align: center">
																	${totalQuantity }
																</th>
															</tr>
														</tfoot>										
													</table>	
												</div>									
											</g:if>
											<g:else>
												<table>									
													<tbody>
														<tr class="even">
															<td colspan="3" style="padding: 25px; text-align: left;">
																<g:if test="${!commandInstance?.categoryFilters}">
																	&laquo; Please choose at least one category on the left.
																</g:if>
																<g:else>													
																	There are no inventory items matching the selected criteria.
																</g:else>
															</td>
														</tr>
													</tbody>
												</table>
											</g:else>		    
										</div>        				
		            				</td>
		            			</tr>
		            		</table>
					</td>
				</tr>
			</table>
		</div>
    </body>
</html>
