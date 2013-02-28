<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-summary" productid="${productInstance?.id}">
	<table id="product-summary" border="0">
		<tbody>
			<tr>						
				<td class="top" style="width: 1%;">
					<g:render template="../product/actions" model="[productInstance:productInstance]" />
				</td>
				
				<g:if test="${productInstance?.coldChain }">
					<td style="width: 1%;" class="top">				
						<img src="${resource(dir: 'images/icons', file: 'coldchain.gif')}" 
							alt="" title="${warehouse.message(code:'product.coldChain.message') }" class="middle"/>
					</td>
				</g:if>									
				<td class="top">
								
					<div id="product-header" style="float: left;">
						<div>
							<g:if test="${productInstance?.manufacturer }">
								<span class="manufacturer">${productInstance?.manufacturer }</span> 
							</g:if>
							<g:if test="${productInstance?.manufacturerCode }">
								<span class="manufacturerCode">Mfr# ${productInstance?.manufacturerCode }</span>
							</g:if>
						</div>	
			            <div id="product-title" class="title">
			            	${productInstance?.productCode }
			            	<g:link controller="inventoryItem" action="showStockCard" params="['product.id': productInstance?.id]">
			                	${productInstance?.name?:productInstance?.manufacturerName?:productInstance?.vendorName }		
			                </g:link>				
			            </div>
		                <div>		
			                <span class="product-generic fade" style="text-transform:uppercase;">			
			                	<g:if test="${productInstance?.productGroups }">
		                    		${productInstance?.productGroups?.sort().first()}
		                    	</g:if>
		                    	<g:else>
		                    		${productInstance?.name }
		                    	</g:else>
		                    </span>							
	                    </div>
	                   
        			</div>
        			 <div id="product-tags" style="float: left; margin-left: 50px;">
	        				<g:each var="tag" in="${productInstance?.tags }">
	        					<g:link controller="inventory" action="browse" params="['tag':tag.tag,'max':params.max]">
		        					<span class="tag">${tag.tag }</span>
	        					</g:link>
	        				</g:each>
        			</div>
        			<div class="clear"></div>
        		</td>
				<td class="right">
        			<div id="product-status" class="title">        			
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
