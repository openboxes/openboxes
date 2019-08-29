<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title><g:message code="inventory.createDefaultInventoryItems.label"/></title>    
    </head>    

	<body>
		<div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			
			<div>	
				
			</div>
			
			<div>
				<table>
					<tr>					
						<td>
							<label>
								<img src="${resource(dir:'images/icons/silk',file:'error.png')}" style="vertical-align: middle"/> 
								<warehouse:message code="inventory.productsWithoutDefaultInventoryItem.label"/>
							</label>
							<div style="padding: 10px;">
								<warehouse:message code="inventory.createDefaultInventoryItems.message"/>
								
								<g:link controller="inventory" action="createDefaultInventoryItems">
									<warehouse:message code="inventory.createDefaultInventoryItems.label"/>
								</g:link>
							</div>
							<div class="list box">
								<table>
				       	           	<tbody>			
										<g:each var="entry" in="${products.groupBy { it.category} }" status="i">    
											<tr>
												<th>
													${entry.key }
												</th>
											</tr>       
											<g:each var="product" in="${entry.value}" status="j">
												<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">            
													<td>
														<g:link controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]">
															${product }
														</g:link>
													</td>
												</tr>						
											</g:each>
										</g:each>
									</tbody>
								</table>				
							</div>
						</td>
					</tr>			
				</table>	
			</div>		
		</div>
		
	</body>

</html>
