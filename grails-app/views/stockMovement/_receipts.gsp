<div id="receipt" class="dialog box">
    <h2>
        <img src="${createLinkTo(dir:'images/icons',file:'handtruck.png')}" />
        <label><warehouse:message code="shipping.receipt.label"/></label>
    </h2>
    <table>
        <tr>
            <th><g:message code="receipt.receiptStatusCode.label"/></th>
            <th><g:message code="receipt.receiptNumber.label"/></th>
            <th><g:message code="shipping.shipmentNumber.label"/></th>
            <th><g:message code="transaction.transactionNumber.label"/></th>
            <th><g:message code="product.productCode.label"/></th>
            <th><g:message code="product.label"/></th>
            <th><g:message code="inventoryItem.lotNumber.label"/></th>
            <th><g:message code="inventoryItem.expirationDate.label"/></th>
            <th><g:message code="location.binLocation.label"/></th>
            <th><g:message code="receiptItem.quantityCanceled.label" default="Canceled"/></th>
            <th><g:message code="receiptItem.quantityPending.label" default="Pending"/></th>
            <th><g:message code="receiptItem.quantityReceived.label" default="Received"/></th>
        </tr>
        <g:each var="receiptItem" in="${receiptItems}" status="status">
            <tr class="prop ${status%2?'even':'odd'}">
                <td>
                    <span title="${receiptItem?.receipt.receiptStatusCode} on ${receiptItem?.receipt?.actualDeliveryDate}
                    ${g.message(code:'default.created.label')} ${receiptItem?.receipt?.dateCreated}">
                        <format:metadata obj="${receiptItem?.receipt.receiptStatusCode}"/>
                    </span>
                </td>
                <td>
                    ${receiptItem?.receipt.receiptNumber?:receiptItem?.receipt?.id}
                </td>
                <td>
                    ${receiptItem?.receipt?.shipment?.shipmentNumber}
                </td>
                <td>
                    <g:if test="${receiptItem?.receipt?.transaction}">
                        <g:link controller="inventory" action="showTransaction" id="${receiptItem?.receipt?.transaction?.id}">
                            ${receiptItem?.receipt?.transaction?.transactionNumber?:receiptItem?.receipt?.transaction?.id}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:message code="default.notAvailable.label"/>
                    </g:else>
                </td>
                <td>
                    <g:link controller="inventoryItem" action="showStockCard" id="${receiptItem?.product?.id}">
                        ${receiptItem?.product.productCode}
                    </g:link>

                </td>
                <td>
                    <g:link controller="inventoryItem" action="showStockCard" id="${receiptItem?.product?.id}">
                        <format:product product="${receiptItem?.product}"/>
                    </g:link>
                </td>
                <td>
                    ${receiptItem?.inventoryItem?.lotNumber?:"Default"}
                </td>
                <td>
                    <g:expirationDate date="${receiptItem?.inventoryItem?.expirationDate}"/>
                </td>
                <td>
                    ${receiptItem?.binLocation?.name}
                </td>
                <td>
                    ${receiptItem?.quantityCanceled?:0}
                </td>
                <td>
                    <g:if test="${receiptItem?.receipt.receiptStatusCode != org.pih.warehouse.receiving.ReceiptStatusCode.RECEIVED}">
                        ${receiptItem?.quantityReceived?:0}
                    </g:if>
                    <g:else>
                        0
                    </g:else>
                </td>
                <td>
                    <g:if test="${receiptItem?.receipt.receiptStatusCode == org.pih.warehouse.receiving.ReceiptStatusCode.RECEIVED}">
                        ${receiptItem?.quantityReceived?:0}
                    </g:if>
                    <g:else>
                        0
                    </g:else>
                </td>
            </tr>
        </g:each>

    </table>


    <g:unless test="${receiptItems}">
        <div class="empty fade center">
            <g:message code="shipment.noReceipt.message" default="Shipment has not been received yet"/>
        </div>
    </g:unless>

</div>
