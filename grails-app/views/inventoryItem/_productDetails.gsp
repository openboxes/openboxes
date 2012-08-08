
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-details" class="box-slim">
	<table>
		<tbody>
			<tr class="odd">
				<td colspan="2">
					<label>${warehouse.message(code: 'product.status.label') }</label>
				</td>
			</tr>
			<tr class="even">	
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
				
				
			<tr class="odd prop">
				<td colspan="2">
					<label>${warehouse.message(code: 'product.details.label') }</label>
				</td>
			</tr>
			<tr class="even">	
				<td class="left label">
					<span class="name"><warehouse:message code="category.label"/></span>
				</td>
				<td>
					<div class="value">
						<g:if test="${productInstance?.category?.name }">
							<g:link controller="inventory" action="browse" params="[subcategoryId:productInstance?.category?.id,showHiddenProducts:'on',showOutOfStockProducts:'on',searchPerformed:true]">
								<format:category category="${productInstance?.category}"/>
							</g:link>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</div>
					<g:each var="category" in="${productInstance?.categories }">						
						<div>
							<g:link controller="inventory" action="browse" params="[subcategoryId:category?.id,showHiddenProducts:true,showOutOfStockProducts:true,searchPerformed:true]">
								<format:category category="${category}"/>
							</g:link>
						</div>
					</g:each>
					
				</td>
			</tr>
			<g:if test="${productInstance?.productGroups }">
				<tr class="even">	
					<td class="label left">
						<span class="name"><warehouse:message code="productGroup.label"/></span>
					</td>
					<td>
						<g:each var="productGroup" in="${productInstance?.productGroups }">
							<g:link controller="productGroup" action="edit" id="${productGroup.id }">
							${productGroup?.description }
							</g:link>
						</g:each>			
					</td>
				</tr>
			</g:if>
			<tr class="even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.units.label"/></span>
				</td>
				<td colspan="2">
					<span class="value">
						<g:if test="${productInstance?.unitOfMeasure }">
							<format:metadata obj="${productInstance?.unitOfMeasure}"/>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="even">	
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
			<g:if test="${productInstance?.upc }">
				<tr class="even">	
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
			</g:if>			
			<g:if test="${productInstance?.ndc }">
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
			</g:if>			
			<tr class="even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.coldChain.label"/></span>
				</td>
				<td>
					<span class="value">${productInstance?.coldChain ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
				</td>
			</tr>
			<g:set var="status" value="${0 }"/>
			<g:each var="productAttribute" in="${productInstance?.attributes}">
				<tr class="even">
					<td class="label left">
						<span class="name"><format:metadata obj="${productAttribute?.attribute}"/></span>
					</td>
					<td>
						<span class="value">${productAttribute.value }</span>
					</td>
				</tr>													
			</g:each>
			
			<%-- 
			<tr class="odd">
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
			<tr class="even">
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
			<g:if test="${productInstance?.images}">
				<tr class="odd prop">
					<td class="label left" colspan="2">
						<span class="name">
							<label><warehouse:message code="product.images.label"></warehouse:message></label>
						</span>
					</td>
				</tr>
				
				<tr class="even">
					<td colspan="2" class="center middle">
						<g:each var="document" in="${productInstance?.images}" status="i">
							<a class="open-dialog" href="javascript:openDialog('#dialog-${document.id }', '#img-${document.id }');">
								<img src="${createLink(controller:'product', action:'viewThumbnail', id:document.id)}" 
									class="middle" style="padding: 2px; margin: 2px; border: 1px solid lightgrey;" />		
							</a>
							
							<div id="dialog-${document.id }" title="${document.filename }" style="display:none;" class="dialog center">
								<div>
									<img id="img-${document.id }" src="${createLink(controller:'product', action:'viewImage', id:document.id, params:['width':'300','height':'300'])}" 
			           							class="middle image" style="border: 1px solid lightgrey" />
								</div>
								<g:link controller="document" action="download" id="${document.id}">Download</g:link>
							</div>						
						</g:each>
					</td>			
				</tr>													
			</g:if>
		</tbody>		
	</table>
</div>
<script>
	function openDialog(dialogId, imgId) { 
		$(dialogId).dialog({autoOpen: true, modal: true, width: 500, height: 360});
	}
</script>


