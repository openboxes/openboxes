
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
            <g:hasErrors bean="${inventoryInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${inventoryInstance}" as="list" />
	            </div>
            </g:hasErrors>    
            
			<table>
				<%--
				<tr>
					<td>				
						<g:render template="/common/searchCriteriaHorizontal"/>					
						<br/>
					</td>
				</tr>			
				 --%>
				<tr>
					<td style="width:100px;">             
						<g:render template="/common/searchCriteriaVertical" model="[productInstanceList:productList]"/>					
             
						<%--
						<!-- Initial implementation of the product category --> 
         				<h2>Browse by Category</h2>
           				<div>      
           					<ul>
								<g:if test="${categoryInstance.parentCategory}">
	           						<li>
	           							<g:if test="${categoryInstance?.parentCategory?.name != 'ROOT' }">
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
											<g:link controller="inventory" action="browse" params="[categoryId: categoryInstance.parentCategory?.id ]">
												${categoryInstance.parentCategory.name }
											</g:link>
										</g:if>
										<li>
											<g:render template="dynamicMenuTree" model="[category:categoryInstance?.parentCategory, level: 0, selectedCategory: categoryInstance]"/>
										</li>
									</li>
								</g:if>
								<g:else>
									<li>
										<g:render template="dynamicMenuTree" model="[category:categoryInstance, level: 0, selectedCategory: categoryInstance]"/>
									</li>
								</g:else>
							</ul>
						</div>
						--%>
						<%-- 						
         				<h2>Browse by Site</h2>
           				<div>      
							<ul>
								<g:each var="warehouse" in="${Warehouse.list() }">
									<li>
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
										<g:link controller="inventory" action="browse" params="[categoryId: categoryInstance?.id, warehouseId: warehouse.id ]">
											${warehouse?.name }
										</g:link>		
									</li>
											
								</g:each>
							</ul>
						</div>
						--%>
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
													<th width="10%">Code</th>
													<th>Description</th>
													<th width="5%">Qty</th>
												</tr>
											</thead>
										</table>
									</div>
				            		<g:if test="${productList }">
				            			<div style="overflow: auto; height: 400px;">
					            			<table>         		
												<tbody>
													<g:set var="totalQuantity" value="${0 }"/>
													<g:each var="productInstance" in="${productList }" status="i">
													 	<g:set var="itemInstanceList" value="${inventoryMap.get(productInstance)}"/>	
													 	<g:set var="inventoryLevel" value="${inventoryLevelMap?.get(productInstance)?.get(0)}"/>
													 	<g:set var="quantity" value="${(itemInstanceList)?itemInstanceList*.quantity.sum():0 }"/>
														<g:set var="totalQuantity" value="${totalQuantity+quantity }"/>
														<tr class="${varStatus++%2==0?'even':'odd' }">
															<%-- 
															<td style="width: 5%; text-align: center;">
																<g:if test="${itemInstanceList }">
																	<g:if test="${quantity < 0}">
																		<img src="${createLinkTo(dir: 'images/icons/silk', file: 'exclamation.png') }" alt="Out of Stock"/>
																	</g:if>
																	<g:elseif test="${inventoryLevel?.minQuantity && quantity <= inventoryLevel?.minQuantity}">
																		<img src="${createLinkTo(dir: 'images/icons/silk', file: 'exclamation.png') }" alt="Low Stock"/>
																	</g:elseif>
																	<g:elseif test="${inventoryLevel?.reorderQuantity && quantity <= inventoryLevel?.reorderQuantity}">
																		<img src="${createLinkTo(dir: 'images/icons/silk', file: 'error.png') }" alt="Reorder"/>
																	</g:elseif>										
																	<g:elseif test="${inventoryLevel?.maxQuantity && quantity >= inventoryLevel?.maxQuantity}" >
																		<img src="${createLinkTo(dir: 'images/icons/silk', file: 'error.png') }" alt="Overstock"/>
																	</g:elseif>	
																	<g:else>
																		<img src="${createLinkTo(dir: 'images/icons/silk', file: 'tick.png') }" alt="Ok"/>
																	</g:else>
																</g:if>								
															</td>
															--%>		
															<td width="5%">${productInstance?.id }</td>						
															<td width="10%">${productInstance?.productCode }</td>						
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
														<th colspan="3">
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
													<td colspan="4" style="padding: 10px; text-align: center;">
														There are no products matching the selected criteria.
														<%-- 
														<g:render template="../category/breadcrumb" model="[categoryInstance:categoryInstance]"/>
														--%>
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
