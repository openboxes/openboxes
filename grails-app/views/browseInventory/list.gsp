<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>Browse inventory</title>
        <%-- 
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.popover',file:'jquery.popover.css')}" type="text/css" media="screen, projection" />    
		<script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
    	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.nailthumb',file:'jquery.nailthumb.1.1.css')}" type="text/css" media="all" />
    	--%>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <%-- 
            <g:hasErrors bean="${command}">
	            <div class="errors">
	                <g:renderErrors bean="${command}" as="list" />
	            </div>
            </g:hasErrors>   
            --%>
            
			<div>
        	
	            <div class="yui-gf">
					<div class="yui-u first">
						<div class="box">
							<h2>Locations</h2>
							<table>
								<g:each var="location" in="${locations }" status="status">
									<tr class="prop ${status%2?'odd':'even' }">
										<td>
											<g:if test="${location == selectedLocation }">
												${location.name }
											</g:if>
											<g:else>
												<g:link controller="browseInventory" action="list" params="['location.id':location.id]">
													${location.name }
												</g:link>
												
											</g:else>
										</td>
									</tr>
								</g:each>							
							</table>
							<h2>Categories</h2>
							<table>
								<g:each var="category" in="${categories }" status="status">
									<tr class="prop ${status%2?'odd':'even' }">
										<td>
											<g:link controller="browseInventory" action="list" params="['category.id':category.id]">
												${category.name }
											</g:link>
										</td>
									</tr>
								</g:each>							
							</table>
						</div>
	       				<%--
	       				<g:render template="filters" model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>						
						 --%>
					</div>
					<div class="yui-u">
						<div>
						Showing ${inventorySnapshots.size() } records
						</div>
						<div class="box">
							<g:form controller="browseInventory" action="list">
								<label>Search by name</label>
								<g:textField name="q" value="${params.q }" class="text" size="60"/>
								<g:hiddenField name="location.id" value="${selectedLocation?.id }"/>
								<g:hiddenField name="category.id" value="${selectedCategory?.id }"/>
							</g:form>
						</div>
					
						<div class="box">
						
							<table>
								<thead>
									<tr>
										<th>Product</th>
										<th>Category</th>
										<th>Unit of measure</th>
										<th>Inbound quantity</th>
										<th>Outbound quantity</th>
										<th>On hand quantity</th>
										<th>Available to promise</th>
										<th>Last updated</th>
										
									</tr>
								</thead>
								<g:each var="inventorySnapshot" in="${inventorySnapshots }">
									<tr>
										<td>
											<g:link controller="inventoryItem" action="showStockCard" id="${inventorySnapshot?.product?.id }">
											${inventorySnapshot.product?.name }
											</g:link>
										</td>
										<td>
											<g:link controller="browseInventory" action="list" 
												params="['category.id':inventorySnapshot?.product?.category?.id]">
												${inventorySnapshot.product?.category?.name }
											</g:link>
										</td>
										<td>
											${inventorySnapshot.product.unitOfMeasure }
										</td>
										<td>
											${inventorySnapshot.quantityInbound }
										</td>
										<td>
											${inventorySnapshot.quantityOutbound }
										</td>
										<td>
											${inventorySnapshot.quantityOnHand }
										</td>
										<td>
										<%-- 
											${inventorySnapshot.quantityAvailableToPromise }
										--%>
										</td>
										<td>
											<g:prettyDateFormat date="${inventorySnapshot.lastUpdated }" showTime="${true}"/>
											<g:formatDate date="${inventorySnapshot.lastUpdated }" format="hh:mm:ss a"/>
										</td>
									</tr>							
								</g:each>
							</table>
						</div>
					</div>
					
				</div>
			</div>
		</div>
	</body>
</html>