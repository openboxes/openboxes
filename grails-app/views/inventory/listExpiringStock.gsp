<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title>Expiring/expired stock ${session.warehouse.name}</title>    
    </head>    

	<body>
       <div class="body">
       
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
						
			<div class="list">
				
				<h3>Expired Stock </h3>
				<table>
                    <thead>
                        <tr>                           	
	                        <%--<th width="2%">Actions</th> --%>
							<th class="center" width="5%">Qty</th>
							<th width="20%">Item</th>
							<th width="10%">Expires</th>
                        </tr>
                    </thead>
       	           	<tbody>			
	       	           	<g:set var="counter" value="${0 }" />	
						<g:each var="inventoryItem" in="${expiredStock}" status="i">     
						      
							<g:set var="quantity" value="${quantityMap[inventoryItem] }"/>							
							<g:if test="${quantity > 0 }">
								<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">            
									<%--
									<td align="center">									
										<span class="action-menu">
											<button class="action-btn">
												<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
												<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
											</button>
											<div class="actions">
												<div class="action-menu-item">
													<g:link action="showTransaction" id="${inventoryItem?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" style="vertical-align: middle;"/>&nbsp;View transaction details
													</g:link>
												</div>
												
												<div class="action-menu-item">
													<g:link action="editTransaction" id="${inventoryItem?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: middle;"/>&nbsp;Edit transaction details
													</g:link>
													
												</div>
											</div>
										</span>				
									</td>
									 --%>
									<td class="center">
										${quantityMap[inventoryItem] }
									</td>
									<td>
										<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
											${inventoryItem.product.name } ${inventoryItem.lotNumber }
										</g:link>
									</td>
									<td>
										${prettyDateFormat(date: inventoryItem.expirationDate)}
									</td>
								</tr>
							</g:if>
						</g:each>
					</tbody>
				</table>
			</div>
			
			<div class="list">
				<h3>Expiring Stock within the next 6 months </h3>
				<table>
                    <thead>
                        <tr>   
	                        <%--<th width="2%">Actions</th> --%>
							<th class="center" width="5%">Qty</th>
							<th width="20%">Item</th>
							<th width="10%">Expires</th>
                        </tr>
                    </thead>
       	           	<tbody>			
       	     			<g:set var="counter" value="${0 }" />
						<g:each var="inventoryItem" in="${expiringStock}" status="i">           
							<g:set var="quantity" value="${quantityMap[inventoryItem] }"/>
							
							<g:if test="${quantity > 0 }">
								<tr class="${(counter++ % 2) == 0 ? 'odd' : 'even'}">            
									<%--
									<td align="center">
										<span class="action-menu">
											<button class="action-btn">
												<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
												<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
											</button>
											<div class="actions">
												<div class="action-menu-item">
													<g:link action="showTransaction" id="${inventoryItem?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" style="vertical-align: middle;"/>&nbsp;View transaction details
													</g:link>
												</div>
												
												<div class="action-menu-item">
													<g:link action="editTransaction" id="${inventoryItem?.id }">
														<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: middle;"/>&nbsp;Edit transaction details
													</g:link>
													
												</div>
											</div>
										</span>				
									</td>
									 --%>								
									<td class="center">
										${quantity }
									</td>									
									<td>
										<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
											${inventoryItem.product.name } ${inventoryItem.lotNumber }
										</g:link>
									</td>
									<td>
										${prettyDateFormat(date: inventoryItem.expirationDate)}
									</td>
								</tr>
							</g:if>
						</g:each>
					</tbody>
				</table>				
				
			</div>
		</div>
		
	</body>

</html>
