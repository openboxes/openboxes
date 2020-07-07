<div class="box">
    <h2><warehouse:message code="shipments.label"/></h2>
    <g:if test="${orderInstance?.orderItems?.shipmentItems }">
        <table>
            <thead>
            <tr class="odd">
                <th><warehouse:message code="order.orderItem.label"/></th>
                <th width="25%"><warehouse:message code="product.label"/></th>
                <th><warehouse:message code="shipment.label"/></th>
                <th><warehouse:message code="default.type.label"/></th>
                <th><warehouse:message code="default.status.label"/></th>
                <th><warehouse:message code="shipmentItem.packLevel.label" default="Pack Level"/></th>
                <th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
                <th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
                <th class="right"><warehouse:message code="default.quantity.label"/></th>
                <th class="center"><warehouse:message code="product.unitOfMeasure.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="orderItem" in="${orderInstance?.orderItems}" status="i">
                <g:each var="shipmentItem" in="${orderItem.shipmentItems}" status="j">
                    <tr class="${i%2?'even':'odd'}">
                        <td>
                            <g:if test="${!j}">
                                ${i+1}
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${!j}">
                                ${shipmentItem?.product?.productCode}
                                <format:product product="${shipmentItem?.product}"/>
                            </g:if>
                        </td>
                        <td>
                            <g:link controller="stockMovement" action="show" id="${shipmentItem?.shipment?.id }">${shipmentItem?.shipment?.shipmentNumber} ${shipmentItem?.shipment?.name }</g:link>
                        </td>
                        <td>
                            <format:metadata obj="${shipmentItem?.shipment?.shipmentType}"/>
                        </td>
                        <td>
                            <format:metadata obj="${shipmentItem?.shipment?.currentStatus}"/>
                        </td>
                        <td class="center middle">
                            <g:if test="${shipmentItem?.container?.parentContainer}">
                                ${shipmentItem?.container?.parentContainer?.name} &rsaquo;
                            </g:if>
                            ${shipmentItem?.container?.name}
                        </td>
                        <td>
                            ${shipmentItem?.inventoryItem?.lotNumber}
                        </td>
                        <td>
                            <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
                        </td>
                        <td class="right">
                            ${shipmentItem?.quantity}
                        </td>
                        <td class="center">
                            ${shipmentItem?.product?.unitOfMeasure}
                        </td>
                    </tr>
                </g:each>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="order.noShipments.label"/></div>
    </g:else>
</div>
