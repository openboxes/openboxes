<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<g:set var="inventoryLevel" value="${product?.getInventoryLevel(session.warehouse.id)}"/>
<g:set var="latestInventoryDate" value="${product?.latestInventoryDate(session.warehouse.id)}" />
<g:if test="${inventoryLevel?.status == InventoryStatus.SUPPORTED}">
    <g:if test="${totalQuantity <= 0}">
        <g:if test="${latestInventoryDate}">
            <span style="color: red"><warehouse:message code="product.noStock.label"/></span>
        </g:if>
        <g:else>
            <warehouse:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/>
        </g:else>
    </g:if>
    <g:elseif test="${totalQuantity >= inventoryLevel?.maxQuantity}">
        <span style="color: green"><warehouse:message code="product.overStock.label" default="Over stock"/></span>
    </g:elseif>
    <g:elseif test="${totalQuantity <= inventoryLevel?.minQuantity}">
        <span style="color: orange"><warehouse:message code="product.lowStock.label" default="Low stock"/></span>
    </g:elseif>
    <g:elseif test="${totalQuantity <= inventoryLevel?.reorderQuantity }">
        <span style="color: orange;"><warehouse:message code="product.reorder.label" default="Reorder"/></span>
    </g:elseif>
    <g:else>
        <span style="color: green;"><warehouse:message code="product.inStock.label" default="In stock"/></span>
    </g:else>
</g:if>
<g:elseif test="${inventoryLevel?.status == InventoryStatus.NOT_SUPPORTED}">
    <span style="color: grey"><warehouse:message code="enum.InventoryStatus.NOT_SUPPORTED"/></span>
</g:elseif>
<g:elseif test="${inventoryLevel?.status == InventoryStatus.SUPPORTED_NON_INVENTORY}">
    <span style="color: grey"><warehouse:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/></span>
</g:elseif>
<g:else>
    <span style="color: grey"><warehouse:message code="enum.InventoryStatus.SUPPORTED"/></span>
</g:else>