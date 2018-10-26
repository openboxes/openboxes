<div class="page dialog" style="page-break-after: ${pageBreakAfter};">
    <table>
        <thead>
            <tr>
                <th></th>
                <th>${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th>${warehouse.message(code: 'shipmentItem.quantityShipped.label')}</th>
                <th>${warehouse.message(code: 'shipmentItem.quantityReceived.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${shipment.shipmentItems}">
                <tr>
                    <td colspan="8" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>
            </g:unless>
            <g:each in="${shipment.shipmentItems}" status="i" var="shipmentItem">

                <tr class="prop">
                    <td>
                        ${i+1}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.productCode}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.name}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.lotNumber}
                    </td>
                    <td>
                        <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="dd/MMM/yyyy"/>
                    </td>
                    <td>
                        ${shipmentItem?.quantity}
                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:"default.each.label")}
                    </td>
                    <td>
                        ${shipmentItem?.quantityReceived()}
                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:"default.each.label")}
                    </td>

                </tr>

            </g:each>
        </tbody>
    </table>
</div>

