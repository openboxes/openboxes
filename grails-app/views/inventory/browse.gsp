
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
         	<table style="height: 400px; ">
				<tr>
					<td style="border-right: 0px solid lightgrey; width: 200px; border-right: 1px solid lightgrey;">
						<div style="padding: 10px;">
							<h3>Filter products</h3>
							<g:render template="/common/searchCriteriaVertical" model="[productInstanceList: commandInstance?.productList, categoryFilters: commandInstance?.categoryFilters, rootCategory: commandInstance?.rootCategory]"/>					
						</div>
						
						<!--  Disbled 'Actions' until we have time to getting them working as expected -->
						<g:if test="${!true && commandInstance?.productList }">
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
         			</td>
	         		<td>
	            		<g:if test="${commandInstance?.productList }">
	            			<div style="overflow: auto; height: 400px;">									
								<g:set var="productMap" value="${commandInstance?.productList.groupBy {it.category} }"/>
								<g:each var="entry" in="${productMap}" status="i">	
									<g:set var="totalQuantity" value="${0 }"/>
									<h3 style="background-color: #525D76; color: white; padding: 10px;">${entry.key }</h3>
									<div class="list">
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
																${productInstance?.name }
															</g:link> 
																
														</td>
														<td>
															<span class="fade">${productInstance?.category?.name }</span>
														</td>
														<td style="text-align: center;">
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
														Total items
													</th>
													<th></th>
													<th style="text-align: center">
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
							<g:if test="${params.searchTerms || categoryFilters }">
								<span>
									Your search did not return any items.  Please try again.
								</span>
							</g:if>
						</g:else>		    
													
	         		</td>
	         	</tr>
	        </table>

		</div>
    </body>
</html>
