<div class="dialog" >
    <g:render template="../product/summary" model="[productInstance:product]"/>

    <div class="tabs">
        <ul>
            <li><a href="${request.contextPath}/inventoryItem/showCurrentStock/${product?.id}"><warehouse:message code="inventory.currentStock.label" default="Current Stock"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showStockHistory/${product?.id}"><warehouse:message code="inventory.stockHistory.label"/></a></li>
        </ul>
    </div>
</div>


