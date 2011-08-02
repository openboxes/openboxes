
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'stock.label', default: 'Stock')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>    
    </head>    

    <body>
        <div class="body">
			<div class="nav">
				<g:render template="nav"/>
			</div>
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    
			<div class="dialog">
			
				
				<g:form action="searchStock" method="GET">
					<g:hiddenField name="id" value="${transactionInstance?.id}"/>
					<g:hiddenField name="inventory.id" value="${warehouseInstance?.inventory?.id}"/>
					<fieldset>			
						<legend>Search Stock</legend>	
						<table>
							<tr>
								<td>
									<span>Search by barcode, lot number, serial number, product description</span>
								</td>
							</tr>
							<tr>
								<td align="center">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'magnifier.png') }"/>
									<input type="text" name="query" value="${params?.query}"/>
									<g:submitButton name="submit" value="Go"/>
								</td>
							</tr>
							<tr>
								<td>
									<div style="padding-left: 25px;">
										<table>
											<tr>
												<th>Product</th>
											</tr>
											

											<g:each var="productInstance" in="${productInstanceList}">
												<tr>
													<td>
														${productInstance?.name } &nbsp; (no lot number)
													</td>
													<td>
														0
													</td>
													<td>
														<g:link action="enterStock" params="['product.id':productInstance?.id]">Enter Stock Movement</g:link>														 	
													</td>
											 	</tr>
												<g:set var="inventoryItems" value="${inventoryItemMap?.get(productInstance)}"/>														
												<g:if test="${inventoryItems }">
													<g:each var="inventoryItem" in="${inventoryItems }">
														<tr>
															<td>
																${productInstance?.name } &nbsp; (Lot #${inventoryItem.lotNumber })	
															</td>
															<td>
																${inventoryItem.quantity }
															</td>
															<td>
																<g:link action="enterStock" params="['inventoryItem.id':inventoryItem.id]">Enter Stock Movement</g:link> 
															</td>
														</tr>
													</g:each>
												</g:if>
											</g:each>
										</table>
									</div>
								</td>
							</tr>
						</table>		
					</fieldset>					
				</g:form>
			</div>
		</div>
    </body>
</html>
