
<g:if test="${inventoryLevel && inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.SUPPORTED}">
    <g:if test="${totalQuantity <= 0}">
        <span class="tag tag-danger"><g:message code="product.noStock.label"/></span>
    </g:if>
    <g:elseif test="${totalQuantity <= inventoryLevel?.minQuantity}">
        <span class="tag tag-warning"><g:message code="product.lowStock.label"/></span>
    </g:elseif>
    <g:elseif test="${totalQuantity <= inventoryLevel?.reorderQuantity }">
        <span class="tag tag-warning"><g:message code="product.reorder.label"/></span>
    </g:elseif>
    <g:elseif test="${totalQuantity > inventoryLevel?.maxQuantity}">
        <span class="tag tag-success"><g:message code="product.overStock.label"/></span>
    </g:elseif>
    <g:else>
        <span class="tag tag-success"><g:message code="product.inStock.label"/></span>
    </g:else>
</g:if>
<g:else>
    <g:if test="${totalQuantity > 0}">
        <span class="tag tag-success"><g:message code="product.inStock.label"/></span>
    </g:if>
    <g:else>
        <span class="tag tag-danger"><g:message code="product.noStock.label"/></span>
    </g:else>
</g:else>
