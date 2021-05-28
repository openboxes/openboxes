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
				<td style="width: 1%; white-space: nowrap;" class="middle fa-2x">
					<g:renderHandlingIcons product="${productInstance}" />
				</td>
				<td class="middle">
					<div id="product-header" style="float: left;">
			            <div id="product-title" class="title">
			            	<small>${productInstance?.productCode }</small>
			            	<g:link controller="inventoryItem" action="showStockCard" params="['product.id': productInstance?.id]">
			                	${productInstance?.name }
			                </g:link>
			            </div>
        			</div>
        		</td>
				<td class="right" width="1%">
					<div id="product-status" class="title">
						<g:if test="${productInstance?.active}">
							<g:productStatus product="${productInstance.id}"/>
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
<script>
    $(function() {
        $('.nailthumb-product img').hide();
        $('.nailthumb-product img').nailthumb({width : 40, height : 40});
    });
</script>
