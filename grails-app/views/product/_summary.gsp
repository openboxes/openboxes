<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<div id="product-summary" productid="${productInstance?.id}" class="summary">
	<table id="product-summary-table" border="0">
		<tbody>
			<tr>
				<td class="middle" style="width: 1%;">
                    <g:if test="${productInstance?.images }">
                        <div class="nailthumb-product">
                            <g:set var="image" value="${productInstance?.thumbnail}" />
                            <img src="${createLink(controller:'product', action:'viewThumbnail', id:image.id)}" style="display:none" />
                        </div>
                    </g:if>
                    <g:else>
                    <div class="nailthumb-product">
                        <img src="${resource(dir: 'images', file: 'default-product.png')}" />
                    </div>
                    </g:else>
                </td>

				<td style="width: 1%; white-space: nowrap;" class="middle fa-2x">
					<g:renderHandlingIcons product="${productInstance}" />
				</td>
				<td class="middle">

					<div id="product-header" style="float: left;">
						<g:if test="${productInstance?.productFamily}">
							<span class="fade">${productInstance.productFamily}</span>
						</g:if>
			            <div id="product-title" class="title">
			            	<small class="font-weight-bold">${productInstance?.productCode }</small>
							<format:displayName product="${productInstance}" showTooltip="${true}" />
			            </div>
                        <div id="product-catalogs">
                            <g:each var="productCatalog" in="${productInstance?.productCatalogs }">
								<g:link controller="inventory" action="browse" params="['catalogs':productCatalog.id]">
                                	<span class="tag tag-info" title="${g.message(code: 'productCatalog.label')}">${productCatalog.name }</span>
								</g:link>
                            </g:each>
                        </div>
                        <div id="product-tags">
                            <g:each var="tag" in="${productInstance?.tags }">
                                <g:link controller="inventory" action="browse" params="['tags':tag.id]">
                                    <span class="tag tag-success" title="${g.message(code: 'tag.label', default: 'Tag')}">${tag.tag }</span>
                                </g:link>
                            </g:each>
							<g:if test="${productInstance?.productEvents?.productMergeEvents}">
								<g:link controller="inventoryItem" action="showStockCard" params="['id': productInstance?.productEvents?.otherProductId]">
									<span class="tag tag-danger" title="${productInstance?.productEvents?.productMergeSummary}">
										<g:message code="product.mergeProducts.merged.label"/>
									</span>
								</g:link>
							</g:if>
                        </div>

        			</div>
        		</td>
				<td class="right" width="1%">
					<div id="product-status" class="title">
						<g:if test="${productInstance?.active}">
							<g:productStatus product="${productInstance}"/>
						</g:if>
						<g:else>
							<span class="tag tag-danger"><g:message code="default.inactive.label"/></span>
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
                <g:render template="/product/actions" model="[productInstance:productInstance]" />
            </td>
            <td>
                <div class="button-container">
					<div class="button-group">
						<g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }' class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}" />&nbsp;
							${warehouse.message(code: 'inventory.showStockCard.label', default: 'Show stock')}
						</g:link>
						<g:link controller='inventoryItem' action='showRecordInventory' params="['product.id':productInstance?.id]" class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'calculator.png')}" />&nbsp;
							${warehouse.message(code: 'inventory.record.label', default: 'Record Stock')}
						</g:link>
						<g:link controller='product' action='edit' id='${productInstance?.id }' class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
							${warehouse.message(code: 'default.edit.label', args:[g.message(code: 'product.label', 'Product')])}
						</g:link>
						<g:isUserManager>
							<g:link controller="inventoryItem" action="showLotNumbers" params="['product.id': productInstance?.id]" class="button">
								<img src="${resource(dir: 'images/icons', file: 'barcode.png')}"/>&nbsp;
								<warehouse:message code="inventory.showLotNumbers.label"/>
							</g:link>
						</g:isUserManager>
						<g:link controller="stocklistManagement" action="index" id="${productInstance?.id}" class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}"/>&nbsp;
							${warehouse.message(code: 'default.manage.label', default: 'Manage stock lists', args:[warehouse.message(code:'requisitionTemplates.label')])}
						</g:link>
					</div>

					<g:isSuperuser>
						<div class="button-group">
							<g:link controller="product" action="createProductSnapshot" id="${productInstance?.id}" class="button"
									onclick="return confirm('${warehouse.message(code: 'default.button.confirm.message', default: 'Are you sure?')}');">
								<img src="${resource(dir: 'images/icons/silk', file: 'camera.png')}"/>&nbsp;
								<warehouse:message code="default.create.label" default="Create Snapshot" args="[g.message(code: 'snapshot.label', default:' Snapshot')]"/>
							</g:link>
							<g:link controller="migration" action="migrateProduct" id="${productInstance?.id}" class="button"
									onclick="return confirm('${warehouse.message(code: 'default.button.confirm.message', default: 'Are you sure?')}');">
								<img src="${resource(dir: 'images/icons/silk', file: 'arrow_switch_bluegreen.png')}"/>&nbsp;
								<warehouse:message code="product.migrateInventoryTransactions.label" default="Migrate Inventory Transactions"/>
							</g:link>
							<g:link controller="migration" action="nextInventoryTransaction" params="[max:1]" class="button">
								<img src="${resource(dir: 'images/icons/silk', file: 'resultset_next.png')}"/>&nbsp;
								<g:message code="default.button.next.label" default="Next"/>
							</g:link>
							<g:if test="${grailsApplication.config.openboxes.products.merge.enabled}">
								<button class="btn-show-dialog button" data-title="${g.message(code:'product.mergeProducts.label')}"
								   data-url="${request.contextPath}/product/showMergeProductDialog?primaryProduct=${productInstance?.id}&template=mergeProducts">
									<img src="${resource(dir: 'images/icons/silk', file: 'share.png')}"/>&nbsp;
									<g:message code="product.mergeProducts.label" default="Merge Products"/>
								</button>
							</g:if>
						</div>
					</g:isSuperuser>
					<div class="button-group right">
						<g:link controller="product" action="addDocument" id="${productInstance?.id}" class="button">
							<img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
							<warehouse:message code="product.addDocument.label" default="Add document"/>
						</g:link>
					</div>
                </div>
            </td>
        </tr>

    </table>
</div>
<script>
	$(function () {
		$('.nailthumb-product img').hide();
		$('.nailthumb-product img').nailthumb({ width: 40, height: 40, replaceAnimation: null });
	});
</script>
