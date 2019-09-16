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
								<warehouse:message code="inventory.belowReorderLevel.label"/>
							</h2>
							<table>
			                    <thead>
			                        <tr>   
			                        	<th><warehouse:message code="category.label"/></th>
										<th width="55%"><warehouse:message code="product.label"/></th>
										<th class="center" width="15%"><warehouse:message code="inventory.reorderQuantity.label" default="Reorder quantity"/></th>
										<th class="center" width="15%"><warehouse:message code="inventory.remainingQuantity.label" default="Remaining quantity "/></th>
			                        </tr>
			                    </thead>
			       	           	<tbody>			
			       	     			<g:set var="counter" value="${0 }" />
									<g:each var="product" in="${reorderProductsQuantityMap?.keySet()}">           
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
												${inventoryLevelByProduct[product].reorderQuantity}
											</td>
											<td class="center" width="15%">
												${reorderProductsQuantityMap[product]}
											</td>
										</tr>						
									</g:each>
								</tbody>
							</table>				
						</div>
					</td>
				</tr>			
			</table>
				
		</div>
		
	</body>

</html>
