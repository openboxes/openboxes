
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${message(code: 'stockCard.label', default: 'Stock Card')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="body">
	
			<div class="nav">
				<g:render template="../inventory/nav"/>
			</div>
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			<g:hasErrors bean="${itemInstance}">
				<div class="errors"><g:renderErrors bean="${itemInstance}" as="list" /></div>
			</g:hasErrors>
			
			
															
												
			<div class="dialog">		
				<table>
					<tr>
						<td style="width: 250px;">
							<g:render template="productDetails" model="[productInstance:productInstance]"/>											
						</td>
						<td>			
							<div class="stockOptions">
								<ul>
									<li>
										<img src="${resource(dir: 'images/icons/silk', file: 'table_refresh.png')}"/>
										<g:link controller="inventory" action="browse" >Back to Inventory</g:link>
									</li>
									<li>
										<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
										<g:link controller="inventoryItem" action="showRecordInventory" params="['product.id':productInstance?.id,'inventory.id':inventoryInstance?.id]">
											Record stock
										</g:link>
									</li>
									<li>
										<img src="${resource(dir: 'images/icons/silk', file: 'magnifier.png')}"/>
										<g:link controller="inventoryItem" action="showTransactions" params="['product.id':productInstance?.id,'inventory.id':inventoryInstance?.id]">
											Show changes
										</g:link>
									</li>
									<li>
										<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}"/>
										<g:link controller="inventoryItem" action="create" params="['product.id':productInstance?.id,'inventory.id':inventoryInstance?.id]">
											Add item
										</g:link>
									</li>
								</ul>
							</div>									
						
													
							<fieldset>	
								<legend class="fade">Current Stock</legend>																
								<div id="inventoryView" style="text-align: right;">										
									<table border="0" style="border:1px solid #f5f5f5" width="100%">
										<thead>
											<tr>
												<th>ID</th>
												<th>Description</th>
												<th>Lot Number</th>
												<th>Expires</th>
												<th style="text-align: center;" >Qty</th>
											</tr>											
										<tbody>
											<g:if test="${!inventoryItemList}">
												<tr class="odd">
													<td colspan="4" style="text-align: center">
														<span class="fade">No current stock </span>
													</td>
												</tr>
											</g:if>
										
											<g:each var="itemInstance" in="${inventoryItemList }" status="status">				
												<tr class="${(status%2==0)?'odd':'even' }">
													<td class="fade">${itemInstance?.id}</td>
													<td>${itemInstance?.description}</td>
													<td>${itemInstance?.lotNumber?:'<span class="fade">EMPTY</span>' }</td>
													<td>
														<g:if test="${itemInstance?.inventoryLot?.expirationDate}">
															<g:formatDate date="${itemInstance?.inventoryLot?.expirationDate }" format="dd/MMM/yy" />
														</g:if>
														<g:else>
															<span class="fade">n/a</span>
														</g:else>
													</td>
													<td style="text-align: center;">${itemInstance?.quantity }</td>												
												</tr>
											</g:each>
										</tbody>
										<tfoot>
											<tr>
												<th>Total</th>
												<th></th>
												<th></th>
												<th></th>
												<th style="text-align: center;">${inventoryItemList*.quantity.sum() }</th>
											</tr>
										</tfoot>
									</table>										
								</div>			
								
															
							</fieldset>
						</td>
					</tr>
				</table>
			</div>			
		</div>
	</body>
</html>
