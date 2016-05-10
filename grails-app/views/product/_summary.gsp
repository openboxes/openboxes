<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-summary" productid="${productInstance?.id}" class="summary">
	<table id="product-summary-table" border="0">
		<tbody>
			<tr>						
				<td class="middle" style="width: 1%;">
                    <g:if test="${productInstance?.images }">
                        <div class="nailthumb-product">
                            <g:set var="image" value="${productInstance?.images?.sort()?.first()}"/>
                            <img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />
                        </div>
                    </g:if>
                    <g:else>
                    <div class="nailthumb-product">
                        <img src="${resource(dir: 'images', file: 'default-product.png')}" />
                    </div>
                    </g:else>
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
			            	<small>${productInstance?.productCode }</small>
			            	<g:link controller="inventoryItem" action="showStockCard" params="['product.id': productInstance?.id]">
			                	${productInstance?.name?:productInstance?.manufacturerName?:productInstance?.vendorName }		
			                </g:link>				
			            </div>
                        <div class="product-generic fade" style="text-transform:uppercase; line-height: 20px;">
                            <g:if test="${productInstance?.productGroups }">
                                ${productInstance?.productGroups?.sort().first()}
                            </g:if>
                            <g:else>
                                ${productInstance?.name }
                            </g:else>
                        </div>
                        <div id="product-tags">
                            <g:each var="tag" in="${productInstance?.tags }">
                                <g:link controller="inventory" action="browse" params="['tag':tag.tag,'max':params.max]">
                                    <span class="tag">${tag.tag }</span>
                                </g:link>
                            </g:each>
                        </div>

        			</div>
        		</td>
				<td class="right" width="1%">
        			<div id="product-status" class="title">

						<g:if test="${inventoryLevelInstance?.status == InventoryStatus.SUPPORTED}">
							<g:if test="${totalQuantity <= 0}">
								<span class="tag tag-danger"><warehouse:message code="product.noStock.label"/></span>
							</g:if>
							<g:elseif test="${totalQuantity <= inventoryLevelInstance?.minQuantity}">
								<span class="tag tag-danger"><warehouse:message code="product.lowStock.label"/></span>
							</g:elseif>
							<g:elseif test="${totalQuantity <= inventoryLevelInstance?.reorderQuantity }">
								<span class="tag tag-warning"><warehouse:message code="product.reorder.label"/></span>
							</g:elseif>
							<g:elseif test="${totalQuantity > inventoryLevelInstance?.maxQuantity}">
								<span class="tag tag-danger"><warehouse:message code="product.overStock.label"/></span>
							</g:elseif>
							<g:else>
								<span class="tag tag-success"><warehouse:message code="product.inStock.label"/></span>
							</g:else>
						</g:if>
						<g:elseif test="${inventoryLevelInstance?.status == InventoryStatus.NOT_SUPPORTED}">
							<warehouse:message code="enum.InventoryStatus.NOT_SUPPORTED"/>
						</g:elseif>
						<g:elseif test="${inventoryLevelInstance?.status == InventoryStatus.SUPPORTED_NON_INVENTORY}">
							<warehouse:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/>
						</g:elseif>
						<g:else>
							none
						</g:else>
        			</div>
			
				</td>
			</tr>
		</tbody>
	</table>
</div>
<div class="summary-actions">
    <table>
        <tr>
            <td width="1%">
                <g:render template="../product/actions" model="[productInstance:productInstance]" />
            </td>
            <td>
                <div class="button-container">
                    <g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="button">
						<img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}" />&nbsp;
						${warehouse.message(code: 'product.button.show.label', default: 'Show stock')}
                    </g:link>
                    <g:link controller='product' action='edit' id='${productInstance?.id }' class="button">
						<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
                        ${warehouse.message(code: 'product.button.edit.label', default: 'Edit product', args:['product'])}
                    </g:link>
                    <g:link controller='inventoryItem' action='showRecordInventory' params="['productInstance.id':productInstance?.id]" class="button">
						<img src="${resource(dir: 'images/icons/silk', file: 'calculator.png')}" />&nbsp;
						${warehouse.message(code: 'product.button.record.label', default: 'Record stock')}
                    </g:link>
                    <g:link controller='inventory' action='browse' class="button">
						<img src="${resource(dir: 'images/icons/silk', file: 'application_form_magnify.png')}" />&nbsp;
						${warehouse.message(code: 'inventory.button.browse.label', default: 'Browse inventory')}
                    </g:link>
                </div>
            </td>
        </tr>

    </table>
</div>
<script>
    $(function() {
        $('.nailthumb-product img').hide();
        $('.nailthumb-product img').nailthumb({width : 40, height : 40});
    });
</script>
