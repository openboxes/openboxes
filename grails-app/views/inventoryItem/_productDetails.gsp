
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-details" style="border: 1px solid lightgrey">
	<table>
		<thead>
			<tr class="even" style="background-color: #eee; border-bottom: 3px solid lightgrey; ">
				<td class="center" colspan="2">
					<label>${warehouse.message(code: 'product.details.label') }</label>
				</td>
			</tr>
		</thead>
		<tbody>
			<tr class="odd">	
				<td class="label left">
					<span class="name"><warehouse:message code="default.status.label"/></span>
				</td>
				<td>
					<span class="value">				
						<g:if test="${inventoryLevelInstance?.status == InventoryStatus.SUPPORTED}">
							<g:if test="${totalQuantity <= 0}">
								<span style="color: red"><warehouse:message code="product.noStock.label"/></span>
							</g:if>
							<g:elseif test="${totalQuantity <= inventoryLevelInstance?.minQuantity}">
								<span style="color: orange"><warehouse:message code="product.lowStock.label"/></span>
							</g:elseif>
							<g:elseif test="${totalQuantity <= inventoryLevelInstance?.reorderQuantity }">
								<span style="color: orange;"><warehouse:message code="product.reorder.label"/></span>
							</g:elseif>
							<g:else>
								<span style="color: green;"><warehouse:message code="product.inStock.label"/></span>
							</g:else>
						</g:if>			
						<g:elseif test="${inventoryLevelInstance?.status == InventoryStatus.NOT_SUPPORTED}">
							<warehouse:message code="enum.InventoryStatus.NOT_SUPPORTED"/>
						</g:elseif>
						<g:elseif test="${inventoryLevelInstance?.status == InventoryStatus.SUPPORTED_NON_INVENTORY}">
							<warehouse:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/>
						</g:elseif>
						<g:else>
							<warehouse:message code="enum.InventoryStatus.SUPPORTED"/>
						</g:else>
					
					</span>
				</td>
			</tr>				
			<tr class="even">	
				<td class="label left">
					<span class="name"><warehouse:message code="product.onHandQuantity.label"/></span>
				</td>
				<td>
					<span class="value">
						<b>${totalQuantity }</b></span>
				</td>
			</tr>			
				
			<tr class="odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.units.label"/></span>
				</td>
				<td colspan="2">
					<span class="value"><format:metadata obj="${productInstance?.unitOfMeasure}"/></span>
				</td>
			</tr>
			<tr class="even">	
				<td class="left label">
					<span class="name"><warehouse:message code="category.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.category?.name }">
							<format:category category="${productInstance?.category}"/>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.manufacturer.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.manufacturer }">
							${productInstance?.manufacturer }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.manufacturerCode.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.manufacturerCode }">
							${productInstance?.manufacturerCode }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.upc.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.upc }">
							${productInstance?.upc }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.ndc.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.ndc }">
							${productInstance?.ndc }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.coldChain.label"/></span>
				</td>
				<td>
					<span class="value">${productInstance?.coldChain ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
				</td>
			</tr>
			<g:each var="productAttribute" in="${productInstance?.attributes}" status="status">
				<tr class="${status%2==0?'even':'odd' }">
					<td class="label left">
						<span class="name"><format:metadata obj="${productAttribute?.attribute}"/></span>
					</td>
					<td>
						<span class="value">${productAttribute.value }</span>
					</td>
				</tr>													
			</g:each>
			
			<%-- 
			<tr class="even">
				<td class="label left">
					<span class="name"><warehouse:message code="product.minLevel.label"/></span>
				</td>
				<td>
					<span id="minQuantityTextValue" class="value" >
						<span id="minQuantityValue">
							<g:if test="${inventoryLevelInstance?.minQuantity}">
								${inventoryLevelInstance?.minQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.na.label"/></span>
							</g:else>
						</span>
						&nbsp;
					</span>
				</td>				
			</tr>
			<tr class="odd">
				<td class="label left">
					<span class="name"><warehouse:message code="product.reorderLevel.label"/></span>
				</td>
				<td class="value left">
					<span id="reorderQuantityTextValue" class="value">
						<span id="reorderQuantityValue">
							<g:if test="${inventoryLevelInstance?.reorderQuantity}">
								${inventoryLevelInstance?.reorderQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.na.label"/></span>
							</g:else>
						</span>
					</span>
				</td>
			</tr>		
			--%>
		</tbody>		
	</table>
</div>


