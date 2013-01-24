<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-details">
	<g:if test="${productInstance?.images}">	
		<table class="box">
			<tbody>			
			
				<tr class="odd">
					<td class="odd" colspan="2">
						<label><warehouse:message code="product.images.label"/></label>
					</td>
				</tr>				
				<tr class="prop">
					<td colspan="2" class="center middle">
						<g:each var="document" in="${productInstance?.images}" status="i">
							<div style="float: left;">
								<a class="open-dialog" href="javascript:openDialog('#dialog-${document.id }', '#img-${document.id }');">
									<img src="${createLink(controller:'product', action:'viewThumbnail', id:document.id)}" 
										class="middle" style="padding: 2px; margin: 2px; border: 1px solid lightgrey;" />		
	          							
								</a>
								<br/>
								${document.name }
							</div>	
							
							<div id="dialog-${document.id }" title="${document.filename }" style="display:none;" class="dialog center">
								<div >
									<img id="img-${document.id }" src="${createLink(controller:'product', action:'viewImage', id:document.id, params:['width':'300','height':'300'])}" 
			           							class="middle image" style="border: 1px solid lightgrey" />
								</div>
								<br/>
								<g:link controller="document" action="download" class="button icon arrowdown" id="${document.id}">Download</g:link>
							</div>					
						</g:each>
					</td>			
				</tr>													
			</tbody>		
		</table>
	</g:if>

	<g:if test="${productInstance?.description }">
		<table class="box">
			<tbody>
				<tr class="odd">
					<td colspan="2">
						<label>${warehouse.message(code: 'product.description.label') }</label>
					</td>
				</tr>
				<tr class="prop">	
					<td class="value" colspan="2" style="text-align: justify">
						${productInstance?.description }
					</td>
				</tr>
			</tbody>		
		</table>
	</g:if>
	<g:if test="${productInstance?.tags }">
		<table class="box">
			<tbody>
				<tr class="odd">
					<td colspan="2">
						<label>${warehouse.message(code: 'product.tags.label') }</label>
					</td>
				</tr>
				<tr class="prop">	
					<td class="value" colspan="2" style="text-align: justify">
						<div class="tags">
						<g:each var="tag" in="${productInstance?.tags}">
							<span class="tag">
								<g:link controller="inventory" action="browse" params="['tag':tag.tag]">
									${tag.tag }
								</g:link>
							</span>
						</g:each>
					</div>	
					</td>
				</tr>
			</tbody>		
		</table>
	</g:if>
	<g:set var="latestInventoryDate"
		value="${productInstance?.latestInventoryDate(session.warehouse.id)}" />
	<table class="box">
		<tbody>
			<tr class="odd">
				<td colspan="2">
					<label>${warehouse.message(code: 'product.status.label') }</label>
				</td>
			</tr>
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="default.status.label"/></label>
				</td>
				<td class="value">
					<span class="">	
						<g:if test="${inventoryLevelInstance?.status == InventoryStatus.SUPPORTED}">
							<g:if test="${totalQuantity <= 0}">
								<g:if test="${latestInventoryDate}">
									<span style="color: red"><warehouse:message code="product.noStock.label"/></span>
								</g:if>
								<g:else>
									<warehouse:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/>
								</g:else>
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
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.latestInventoryDate.label"/></label>
				</td>
				<td class="value">
					<span class=""> 						
						<g:if test="${latestInventoryDate}">
							${g.formatDate(date: latestInventoryDate, format: 'dd-MMM-yyyy') }<br/>
							<span class="fade">${g.formatDate(date: latestInventoryDate, format: 'hh:mm:ss a') }</span>
						</g:if> 
						<g:else>
							<span class="fade"><warehouse:message code="default.never.label" /></span>
						</g:else>
				</span></td>
			</tr>					
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.onHandQuantity.label"/></label>
				</td>
				<td class="value">
					<g:if test="${!productInstance?.packages }">
						<div class="">
							${g.formatNumber(number: totalQuantity, format: '###,###,###') }
							<span class="">
								<g:if test="${productInstance?.unitOfMeasure }">
									<format:metadata obj="${productInstance?.unitOfMeasure}"/>
								</g:if>
								<g:else>
									${warehouse.message(code:'default.each.label') }
								</g:else>
							</span>
						</div>
					</g:if>
					<g:else>
						<table>
							<g:each var="productPackage" in="${productInstance?.packages?.sort { it?.uom?.sortOrder} }">
								<tr>
									<td>
										<g:if test="${productPackage?.uom?.code != 'EA' }">~</g:if>
									</td>
										
									<td class="right">
										<span class="">${productPackage?.uom?.code }/${productPackage.quantity }</span>
									</td>
									<td class="right">
										<g:set var="quantityPerPackage" value="${totalQuantity / productPackage?.quantity }"/>
										${g.formatNumber(number: quantityPerPackage, format: '###,###,###.#') }
									</td>
								</tr>
							</g:each>
						</table>
					</g:else>
				</td>
			</tr>	
			
			<tr class="prop">
				<td class="label">
					<label><warehouse:message code="inventoryLevel.minQuantity.label"/></label>
				</td>
				<td class="value">
					<g:if test="${inventoryLevelInstance?.minQuantity}">
						${inventoryLevelInstance?.minQuantity?:'' }
						<span class="">
							<g:if test="${productInstance?.unitOfMeasure }">
								<format:metadata obj="${productInstance?.unitOfMeasure}"/>
							</g:if>
							<g:else>
								${warehouse.message(code:'default.each.label') }
							</g:else>
						</span>
					</g:if>
					<g:else>
						<span class="fade"><warehouse:message code="default.na.label"/></span>
					</g:else>
				</td>				
			</tr>
			<tr class="prop">
				<td class="label">
					<label><warehouse:message code="inventoryLevel.binLocation.label"/></label>
				</td>
				<td class="value">
					<g:if test="${inventoryLevelInstance?.binLocation}">
						${inventoryLevelInstance?.binLocation?:'' }
					</g:if>
					<g:else>
						<span class="fade"><warehouse:message code="default.na.label"/></span>
					</g:else>
				</td>				
			</tr>			
			
			<tr class="prop">
				<td class="label">
					<label><warehouse:message code="inventoryLevel.maxQuantity.label"/></label>
				</td>
				<td class="value">
					<g:if test="${inventoryLevelInstance?.maxQuantity}">
						${inventoryLevelInstance?.maxQuantity?:'' }
						<span class="">
							<g:if test="${productInstance?.unitOfMeasure }">
								<format:metadata obj="${productInstance?.unitOfMeasure}"/>
							</g:if>
							<g:else>
								${warehouse.message(code:'default.each.label') }
							</g:else>
						</span>
					</g:if>
					<g:else>
						<span class="fade"><warehouse:message code="default.na.label"/></span>
					</g:else>
				</td>				
			</tr>
			<tr class="prop">
				<td class="label">
					<label><warehouse:message code="inventoryLevel.reorderQuantity.label"/></label>
				</td>
				<td class="value">
					
					<g:if test="${inventoryLevelInstance?.reorderQuantity}">
						${inventoryLevelInstance?.reorderQuantity?:'' }
						<span class="">
							<g:if test="${productInstance?.unitOfMeasure }">
								<format:metadata obj="${productInstance?.unitOfMeasure}"/>
							</g:if>
							<g:else>
								${warehouse.message(code:'default.each.label') }
							</g:else>
						</span>
					</g:if>
					<g:else>
						<span class="fade"><warehouse:message code="default.na.label"/></span>
					</g:else>
				</td>
			</tr>				
			
		</tbody>
	</table>			
	<table class="box">
		<tbody>				
			<tr class="odd">
				<td colspan="2">
					<label>${warehouse.message(code: 'product.details.label') }</label>
				</td>
			</tr>
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="category.label"/></label>
				</td>
				<td class="value" id="productCategory">
					<span class="">
						<g:if test="${productInstance?.category?.name }">
							<g:link controller="inventory" action="browse" params="[subcategoryId:productInstance?.category?.id,showHiddenProducts:'on',showOutOfStockProducts:'on',searchPerformed:true]">
								<format:category category="${productInstance?.category}"/>
							</g:link>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
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
				<tr class="prop">	
					<td class="label left">
						<label><warehouse:message code="productGroup.label"/></label>
					</td>
					<td class="value">
						<g:each var="productGroup" in="${productInstance?.productGroups }">
							<g:link controller="productGroup" action="edit" id="${productGroup.id }">
								${productGroup?.description }
							</g:link>
						</g:each>			
					</td>
				</tr>
			</g:if>
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.units.label"/></label>
				</td>
				<td class="value" id="unitOfMeasure">
					<span class="">
						<g:if test="${productInstance?.unitOfMeasure }">
							<format:metadata obj="${productInstance?.unitOfMeasure}"/>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.manufacturer.label"/></label>
				</td>
				<td class="value" id="manufacturer">
					<span class="">
						<g:if test="${productInstance?.manufacturer }">
							${productInstance?.manufacturer }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.brandName.label"/></label>
				</td>
				<td class="value" id="brandName">
					<span class="">
						<g:if test="${productInstance?.brandName }">
							${productInstance?.brandName }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>			
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.modelNumber.label"/></label>
				</td>
				<td class="value" id="modelNumber">
					<span class="">
						<g:if test="${productInstance?.modelNumber }">
							${productInstance?.modelNumber }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>			
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.manufacturerCode.label"/></label>
				</td>
				<td class="value" id="manufacturerCode">
					<span class="">
						<g:if test="${productInstance?.manufacturerCode }">
							${productInstance?.manufacturerCode }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.vendor.label"/></label>
				</td>
				<td class="value">
					<span class="">
						<g:if test="${productInstance?.vendor }">
							${productInstance?.vendor }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.vendorCode.label"/></label>
				</td>
				<td class="value">
					<span class="">
						<g:if test="${productInstance?.vendorCode }">
							${productInstance?.vendorCode }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			<g:if test="${productInstance?.modelNumber }">
				<tr class="prop">	
					<td class="label">
						<label><warehouse:message code="product.modelNumber.label"/></label>
					</td>
					<td class="value">
						<span class="">
							<g:if test="${productInstance?.modelNumber }">
								${productInstance?.modelNumber }
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.none.label"/></span>
							</g:else>
						</span>
					</td>
				</tr>
			</g:if>			
			<g:if test="${productInstance?.upc }">
				<tr class="prop">	
					<td class="label">
						<label><warehouse:message code="product.upc.label"/></label>
					</td>
					<td class="value">
						<span class="">
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
				<tr class="prop">	
					<td class="label">
						<label><warehouse:message code="product.ndc.label"/></label>
					</td>
					<td class="value">
						<span class="">
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
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.coldChain.label"/></label>
				</td>
				<td class="value">
					<span class="">${productInstance?.coldChain ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
				</td>
			</tr>
			
			<g:set var="status" value="${0 }"/>
			<g:each var="productAttribute" in="${productInstance?.attributes}">
				<tr class="prop">
					<td class="label left">
						<label><format:metadata obj="${productAttribute?.attribute}"/></label>
					</td>
					<td>
						<span class="">${productAttribute.value }</span>
					</td>
				</tr>													
			</g:each>
		</tbody>
	</table>
	<g:if test="${productInstance?.packages }">		
		<table class="box">
			<tbody>				
				<tr class="odd">
					<td colspan="2">
						<label>${warehouse.message(code: 'product.packaging.label') }</label>
					</td>
				</tr>			
				<g:each var="productPackage" in="${productInstance?.packages?.sort { it.quantity }}">
					<tr class="prop">	
						<td class="label">
							<label>${productPackage?.uom }</label>
						</td>
						<td class="value">
							<span class="">
								${productPackage?.uom?.code }/${productPackage?.quantity }
							</span>
						</td>
					</tr>			
				</g:each>	
			</tbody>
		</table>
	</g:if>
	<table class="box">
		<tbody>
			<tr class="odd">
				<td colspan="2">
					<label>${warehouse.message(code: 'default.auditing.label') }</label>
				</td>
			</tr>
			
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.createdBy.label"/></label>
				</td>
				<td class="value">
					<span class="">${productInstance?.createdBy?.name?:warehouse.message(code: 'default.unknown.label') }</span> <br/>
				</td>
			</tr>	
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.modifiedBy.label"/></label>
				</td>
				<td class="value">
					<span class="">${productInstance?.updatedBy?.name?:warehouse.message(code: 'default.unknown.label') }</span> <br/>
				</td>
			</tr>				
			<tr class="prop">	
				<td class="label">
					<label><warehouse:message code="product.createdOn.label"/></label>
				</td>
				<td class="value">
					${g.formatDate(date: productInstance?.dateCreated, format: 'dd-MMM-yyyy')}
					<br/>
					<span class="fade">${g.formatDate(date: productInstance?.dateCreated, format: 'hh:mm:ss a')}</span>
					 
				</td>
			</tr>			

			
				
			<tr class="prop">	
				<td class="label"  >
					<label><warehouse:message code="product.modifiedOn.label"/></label>
				</td>
				<td class="value">
					
					${g.formatDate(date: productInstance?.lastUpdated, format: 'dd-MMM-yyyy')}
					<br/>
					<span class="fade">${g.formatDate(date: productInstance?.lastUpdated, format: 'hh:mm:ss a')}</span>
				</td>
			</tr>			
			
		</tbody>		
	</table>
	<br/>	
	
		
</div>
<script>
	function openDialog(dialogId, imgId) { 
		$(dialogId).dialog({autoOpen: true, modal: true, width: 600, height: 400});
	}
</script>


