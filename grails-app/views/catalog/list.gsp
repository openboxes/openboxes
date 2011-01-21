
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'catalog.label', default: 'Catalog')}" />
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
				<tr>
					<td width="15%;" style="padding-top: 2px;">
						<g:if test="${productTypes }">
							<table>
								<tr>
									<th>by Type</th>
								</tr>
								<tbody>
									<g:each var="productType" in="${productTypes}" status="i">
										<tr>
											<td class="${((String.valueOf(productType.id)==params?.productType?.id) || (!params?.productType && i == 0))?'verticalTabSelected':'verticalTabDefault'}">								
												<a href="${createLink(action:'list',params:["productType.id":productType.id])}">
													${productType.name} 
												</a> &nbsp;(${productMap?.get(productType)?.size() })
											</td>
										</tr>
									</g:each>    				
								</tbody>
							</table>
						</g:if>
					</td>
					<td>
			            <div style="overflow: auto; height: 600px; padding: 10px;"> 
			            	<g:if test="${productType}">
			            	
			            		<g:set var="varStatus" value="${0 }"/>
								<table border="1" style="border: 1px solid #ccc">
									<thead>
										<tr class="even">
											<th>Status</th>
											<th>Product</th>
											<th>Quantity</th>
											<th>Action</th>
										</tr>
									</thead>
									<tbody>
										<g:each var="productInstance" in="${productMap.get(productType)}" status="i">
										 	<g:set var="itemInstanceList" value="${inventoryItempMap.get(productInstance)}"/>	
										 	<g:set var="inventoryLevel" value="${inventoryLevelMap?.get(productInstance)?.get(0)}"/>
										 	<g:set var="quantity" value="${(itemInstanceList)?itemInstanceList*.quantity.sum():0 }"/>
											<tr class="${varStatus++%2==0?'odd':'even' }">
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
												<td style="width: 30%;">
													<b>${productInstance?.name }</b>			
													<%-- 		
													${productInstance?.dosageStrength } ${productInstance?.dosageUnit } ${productInstance?.dosageForm?.name }
													--%>
												</td>
												<td style="width: 5%; text-align: center;">
													${(itemInstanceList)?itemInstanceList*.quantity.sum():'Not Available'}
													
												</td>
												<td style="width: 7%; text-align: center;">
													<g:if test="${session?.cart?.items?.get(productInstance?.id) }">
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'basket_remove.png') }" alt="Ok"/>
														<g:link controller="shoppingCart" action="removeFromCart" params="['product.id':productInstance?.id]">Remove</g:link>
													</g:if>
													<g:else>
														<img src="${createLinkTo(dir: 'images/icons/silk', file: 'basket_add.png') }" alt="Ok"/>
														<g:link controller="shoppingCart" action="addToCart" params="['product.id':productInstance?.id]">Add</g:link>
													</g:else>
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>
							</g:if>
						</div>					
					</td>
   				</tr>        
            </table>
            

		</div>
    </body>
</html>
