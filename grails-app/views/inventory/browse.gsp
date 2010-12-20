
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'inventory.label', default: 'Inventory')}" />
        <title><g:message code="default.browse.label" args="[entityName]" /></title>    
        
        <style>
        	.tabSelected { 
        		text-align: center;
	        	background-color: white; 
	        	border: 1px solid black;
	        	border-bottom: hidden; 
        	}
        	.tabDefault { 
        		text-align: center;
        		background-color: #f0f0f0; 
        		border: 1px solid black; 
        		border-bottom: 1px solid black; 
        		border-top: 1px solid black; 
        		border-right: 1px solid black; 
        		border-left: 1px solid black;
        	}
        	.tabSpacer { 
        		width: 3px;
        		border-top: 0px;
        		border-bottom: 1px solid black;
        	}
        	
        </style>
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
            
                        
            <table style="margin-top: 10px;">
	            <thead>
    				<tr>				
						<th class="tabSpacer"></th>
						<g:each var="productType" in="${productTypes}" status="i">
							<th class="${((String.valueOf(productType.id)==params?.productType?.id) || (!params?.productType && i == 0))?'tabSelected':'tabDefault'}">								
								<a href="${createLink(action:'browse',params:["productType.id":productType.id])}">${productType.name}</a>
							</th>
							<th class="tabSpacer"></th>
						</g:each>    				
    				</tr>        
            	</thead>
            </table>
            
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
							<g:each var="productInstance" in="${productMap.get(productType) }" status="i">
							 	<g:set var="itemInstanceList" value="${inventoryMap.get(productInstance)}"/>	
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
										${(itemInstanceList)?itemInstanceList*.quantity.sum():'Not Available' }
									</td>
									<td style="width: 15%; text-align: center">
										<g:link controller="inventoryItem" action="showStockCard" params="['product.id':productInstance?.id]">View Stock</g:link>
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</g:if>
			</div>
		</div>
    </body>
</html>
