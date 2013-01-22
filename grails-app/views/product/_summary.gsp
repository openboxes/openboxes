<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-summary" productid="${productInstance?.id}">
	<table id="product-summary">
		<tbody>
			<tr>						
				<td class="middle" style="width: 50px">
					<g:render template="../product/actions" model="[productInstance:productInstance]" />
				</td>
				<g:if test="${productInstance?.coldChain }">
					<td style="width: 1%;" class="middle">				
						<img src="${resource(dir: 'images/icons', file: 'coldchain.gif')}" 
							alt="" title="${warehouse.message(code:'product.coldChain.message') }" class="middle"/>
						
					</td>
				</g:if>					
				<td class="middle">
					<div>
						<g:if test="${productInstance?.manufacturer }">
							<span class="manufacturer">${productInstance?.manufacturer }</span> 
						</g:if>
						<g:if test="${productInstance?.manufacturerCode }">
							<span class="manufacturerCode">#${productInstance?.manufacturerCode }</span>
						</g:if>
					</div>
					<div class="title">
											
						${productInstance?.name} 
						<%-- 
						<span class="product-uom">(${productInstance?.unitOfMeasure })</span>
						<span class="product-category">
							${productInstance?.category?.name }
						</span>
						--%>
					</div>
				</td>
				<td class="right">
					<div class="product-status title">
						
						<g:if test="${inventoryLevelInstance?.status == InventoryStatus.SUPPORTED}">
							<g:if test="${totalQuantity <= 0}">
								<g:if test="${latestInventoryDate}">
									<span style="color: red"><warehouse:message code="product.noStock.label"/></span>
								</g:if>								
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

					
					</div>
				</td>
			</tr>
		</tbody>
	</table>
</div>
