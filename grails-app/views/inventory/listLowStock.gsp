<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title><warehouse:message code="inventory.lowStock.label"/></title>    
    </head>    

	<body>
		<div class="body">
       		
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
				
			<table>
				<tr>					
					<td>
						<div class="list box">
							<h2>
								<img src="${resource(dir:'images/icons/silk',file:'error.png')}" style="vertical-align: bottom"/> 
								<warehouse:message code="inventory.belowMinimumLevel.label"/>
							</h2>
							<table>
			                    <thead>
			                        <tr>   
			                        	<th><warehouse:message code="category.label"/>
										<th width="55%"><warehouse:message code="product.label"/></th>
										<th class="center" width="15%"><warehouse:message code="default.qty.label"/></th>
										<th class="center" width="15%"><warehouse:message code="inventoryLevel.minimumQuantity.label"/></th>
			                        </tr>
			                    </thead>
			       	           	<tbody>			
			       	     			<g:set var="counter" value="${0 }" />
									<g:each var="product" in="${minimumProductsQuantityMap?.keySet()}">           
										<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">            
											<td>
												<span class="fade"><format:category category="${product?.category}"/> </span>
											</td>
											<td width="55%">
												<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]">
													<format:product product="${product}"/> 
												</g:link>
											</td>
											<td class="center" width="15%">
												${inventoryLevelByProduct[product]?.minQuantity}
											</td>									
											<td class="center" width="15%">
												${minimumProductsQuantityMap[product]}
											</td>
										</tr>						
									</g:each>
									<g:unless test="${minimumProductsQuantityMap }">
										<tr>
											<td colspan="4" style="height: 60px;" class="center middle">
												
												<span class="fade">
													<warehouse:message code="inventory.noLowStockItems.label" default="No stocked out items"/>
												</span>
											
											</td>
										</tr>
									</g:unless>
								</tbody>
							</table>				
						</div>
					</td>
				</tr>			
			</table>
			

		</div>
		
	</body>

</html>
