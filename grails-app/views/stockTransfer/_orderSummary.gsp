<div class="box">
    <h2>
        <warehouse:message code="default.summary.label"/>
    </h2>
    <g:if test="${orderInstance?.orderItems}">
        <g:set var="status" value="${0}"/>

        <table class="order-items">
            <thead>
            <tr>
                <th class="bottom">
                    <warehouse:message code="product.productCode.label"/>
                </th>
                <th class="bottom">
                    <warehouse:message code="product.name.label"/>
                </th>
                <th class="bottom">
                    <warehouse:message code="inventoryItem.lot.label"/>
                </th>
                <th class="bottom">
                    <warehouse:message code="inventoryItem.expirationDate.label"/>
                </th>
                <th class="bottom">
                    <warehouse:message code="inventory.stockTransfers.qtyTransferred" default="Qty transferred"/>
                </th>
                <th class="bottom">
                    <warehouse:message code="inventory.stockTransfers.transferredFrom" default="Transferred From"/>
                </th>
                <th class="bottom">
                    <warehouse:message code="inventory.stockTransfers.transferredTo" default="Transferred To"/>
                </th>
            </tr>
            </thead>

            <tbody>

            <g:each var="orderItem" in="${orderInstance?.orderItems?.findAll { !it.orderItems }?.sort { a,b -> a.dateCreated <=> b.dateCreated ?: a.orderIndex <=> b.orderIndex }}" status="i">
                <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'}" style="${isItemCanceled ? 'background-color: #ffcccb;' : ''}">
                    <td style="color: ${orderItem?.product?.color}">
                        ${orderItem?.product?.productCode}
                    </td>
                    <td>
                        <g:link controller="inventoryItem" action="showStockCard"
                                style="color: ${orderItem?.product?.color}"  params="['product.id':orderItem?.product?.id]">
                            <format:product product="${orderItem?.product}"/>
                        </g:link>
                    </td>
                    <td>
                        ${orderItem?.inventoryItem?.lotNumber}
                    </td>
                    <td>
                        <format:date obj="${orderItem?.inventoryItem?.expirationDate}"/>
                    </td>
                    <td>
                        ${orderItem?.quantity}
                    </td>
                    <td>
                        ${orderItem?.originBinLocation?.name}
                    </td>
                    <td>
                        ${orderItem?.destinationBinLocation?.name}
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>
