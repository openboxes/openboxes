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
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="listLowStock" method="get">
			            	<table >
			            		<tr>
			            			<th><warehouse:message code="category.label"/></th>
			            			<th><warehouse:message code="inventory.showUnsupportedProducts.label"/></th>
			            			<th>&nbsp;</th>
			            		</tr>
			            		<tr>
						           	<td class="filter-list-item">
						           		<g:select name="category"
														from="${categories}"
														optionKey="id" optionValue="${{format.category(category:it)}}" value="${categorySelected?.id}" 
														noSelection="['':'--All--']" />   
									</td>			
									 <td>	
						           		<g:checkBox name="showUnsupportedProducts" value="${showUnsupportedProducts }" } />
						           	</td>	           	
									<td class="filter-list-item" style="height: 100%; width: 70%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;<warehouse:message code="default.button.filter.label"/> </button>
									</td>							           	
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
			</table>
			<br/>
			
				
			<table>
				<tr>					
					<td>
						<label>
							<img src="${resource(dir:'images/icons/silk',file:'error.png')}" style="vertical-align: middle"/> 
							<warehouse:message code="inventory.belowMinimumLevel.label"/>
						</label>
						<div class="list box">
							<table>
			                    <thead>
			                        <tr>   
										<th><warehouse:message code="item.label"/></th>
										<th class="center"><warehouse:message code="default.qty.label"/></th>
			                        </tr>
			                    </thead>
			       	           	<tbody>			
			       	     			<g:set var="counter" value="${0 }" />
									<g:each var="product" in="${minimumProductsQuantityMap?.keySet()}">           
										<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">            
											<td>
												<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]">
													<format:product product="${product}"/> 
													<span class="fade"><format:category category="${product?.category}"/> </span>
												</g:link>
												
											</td>
											<td class="center">
												${minimumProductsQuantityMap[product]}
											</td>									
										</tr>						
									</g:each>
								</tbody>
							</table>				
						</div>
					</td>
				</tr>			
			</table>
			
			<br/><br/>
			
			<table>
				<tr>					
					<td>
						<label>
							<img src="${resource(dir:'images/icons/silk',file:'error.png')}" style="vertical-align: middle"/> 
							<warehouse:message code="inventory.belowReorderLevel.label"/>
						</label>
						<div class="list box">
							<table>
			                    <thead>
			                        <tr>   
										<th><warehouse:message code="item.label"/></th>
										<th class="center"><warehouse:message code="default.qty.label"/></th>
			                        </tr>
			                    </thead>
			       	           	<tbody>			
			       	     			<g:set var="counter" value="${0 }" />
									<g:each var="product" in="${reorderProductsQuantityMap?.keySet()}">           
										<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">            
											<td>
												<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]">
													<format:product product="${product}"/> 
													<span class="fade"><format:category category="${product?.category}"/> </span>
												</g:link>
												
											</td>
											<td class="center">
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
