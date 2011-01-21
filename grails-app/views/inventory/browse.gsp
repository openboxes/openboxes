
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>    
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
					<td style="width: 150px; border-right: 1px solid lightgrey;">             
						<g:render template="/common/searchCriteriaVertical" model="[productInstanceList: commandInstance?.productList, categoryFilters: commandInstance?.categoryFilters, rootCategory: commandInstance?.rootCategory]"/>					
             

					</td>
					<td>	
								            		
	            		<script>
							$(function() {
								$("#dialogButton").click(function () { 
									$("#dialog").dialog({ "modal": "true", "width": 600});
								});
							});
						</script>
						
	            		<g:set var="varStatus" value="${0 }"/>
	            			<div>
								<table class="dataTable">
									<thead>
										<tr class="odd">
											<th width="5%">ID</th>
											<th>Description</th>
											<th width="5%" style="text-align: center">Qty</th>
										</tr>
									</thead>
								</table>
							</div>
		            		<g:if test="${commandInstance?.productList }">
		            			<div style="overflow: auto; height: 400px;">
			            			<table>         		
										<tbody>
											<g:set var="totalQuantity" value="${0 }"/>
											<g:each var="productInstance" in="${commandInstance?.productList }" status="i">
											 	<g:set var="itemInstanceList" value="${commandInstance?.inventoryItemMap.get(productInstance)}"/>	
											 	<g:set var="quantity" value="${(itemInstanceList)?itemInstanceList*.quantity.sum():0 }"/>
												<g:set var="totalQuantity" value="${totalQuantity+quantity }"/>
												<tr class="${varStatus++%2==0?'even':'odd' }">
													<td width="5%">${productInstance?.id }</td>						
													<td style="">
														<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">
															${productInstance?.name }
														</g:link> &nbsp; <span class="fade">${productInstance?.category?.name }</span>
															
													</td>
													<td style="width: 5%; text-align: center;">
														<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">${(itemInstanceList)?itemInstanceList*.quantity.sum():'<span class="fade">N/A</span>' }</g:link>
													</td>
												</tr>
											</g:each>										
										</tbody>
										<tfoot>
										
											<tr class="${varStatus%2==0?'even':'odd' }">
												<th></th>
												<th style="text-align: right;">
													Total
												</th>
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
											<td colspan="3" style="padding: 10px; text-align: center;">
											
												<g:if test="${!commandInstance?.categoryFilters}">
													Please choose at least one category on the left.
												</g:if>
												<g:else>													
													There are no inventory items matching the selected criteria.
												</g:else>
											</td>
										</tr>
									</tbody>
								</table>
							</g:else>
						</table>
					</td>
				</tr>
			</table>
		</div>
    </body>
</html>
