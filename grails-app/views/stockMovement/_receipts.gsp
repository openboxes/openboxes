<div id="receipt" class="box">
    <h2>
        <img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" />
        <label><warehouse:message code="shipping.receipt.label"/></label>
    </h2>
    <table>
        <tr>
            <th><g:message code="product.productCode.label"/></th>
            <th><g:message code="product.label"/></th>
            <th><g:message code="location.binLocation.label"/></th>
            <th><g:message code="inventoryItem.lotNumber.label"/></th>
            <th><g:message code="inventoryItem.expirationDate.label"/></th>
            <th><g:message code="receiptItem.quantityShipped.label" default="Shipped"/></th>
            <th><g:message code="receiptItem.quantityReceived.label" default="Received"/></th>
            <th><g:message code="receiptItem.quantityCanceled.label" default="Canceled"/></th>
        </tr>
        <g:each var="receipt" in="${receipts}">
            <tr>
                <th colspan="8">Receipt ${receipt?.id}</th>
            </tr>
            <g:each var="receiptItem" in="${receipt?.receiptItems}" status="status">
                <tr class="prop ${status%2?'even':'odd'}">
                    <td>
                        ${receiptItem?.product.productCode}
                    </td>
                    <td>
                        <format:product product="${receiptItem?.product}"/>
                    </td>
                    <td>
                        ${receiptItem?.binLocation?.name}
                    </td>
                    <td>
                        ${receiptItem?.inventoryItem?.lotNumber}
                    </td>
                    <td>
                        <g:expirationDate date="${receiptItem?.inventoryItem?.expirationDate}"/>
                    </td>
                    <td>
                        ${receiptItem?.quantityShipped?:0}
                    </td>
                    <td>
                        ${receiptItem?.quantityReceived?:0}
                    </td>
                    <td>
                        ${receiptItem?.quantityCanceled?:0}
                    </td>
                </tr>
            </g:each>
        </g:each>
    </table>
    <g:unless test="${receipts || shipmentInstance?.wasReceived()}">
        <div class="empty fade center">
            <g:message code="shipment.noReceipt.message" default="Shipment has not been received yet"/>
        </div>
    </g:unless>

</div>