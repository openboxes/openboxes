<%@ page defaultCodec="html" %>
<div class="page-content">
    <table class="w100">
        <thead>
        <tr>
            <th class="center" width="3%"><g:message code="report.number.label" default="No."/></th>
            <th><g:message code="product.productCode.label" default="Code"/></th>
            <th><g:message code="product.label" default="Product"/></th>
            <th><g:message code="inventoryItem.lotNumber.label" default="Lot"/></th>
            <th><g:message code="inventoryItem.expirationDate.label" default="Expiry"/></th>
            <th class="center"><g:message code="deliveryNote.quantityDelivered.label" default="Qty Delivered"/></th>
            <th class="center"><g:message code="deliveryNote.quantityReceived.label" default="Qty Received"/></th>
            <th><g:message code="default.comment.label" default="Comment"/></th>
        </tr>
        </thead>
        <tbody>
        <g:set var="sortedItems" value="${shipment.shipmentItems?.sort { it.product?.name }}"/>
        <g:each var="item" in="${sortedItems}" status="i">
            %{--
                We are able to receive different products than the ones on the shipment,
                and one shipment item can be split into multiple receipt items.
                If any receipt item differs from the shipment item, we show the original row with strikethrough
                and then one row per receipt item with the actual received values.
            --}%
            <g:set var="receiptItems"
                   value="${shipment.receipts?.collectMany { r -> r.receiptItems?.findAll { ri -> ri.shipmentItem?.id == item.id && ri.quantityReceived > 0 } ?: [] } ?: []}"/>
            <g:set var="productChanged" value="${receiptItems.any { ri -> ri.product?.id != item.product?.id }}"/>
            <g:set var="lotChanged"
                   value="${receiptItems.any { ri -> (ri.lotNumber ?: '') != (item.lotNumber ?: '') }}"/>
            <g:set var="expiryChanged" value="${receiptItems.any { ri -> ri.expirationDate != item.expirationDate }}"/>
            <g:set var="anyChanged" value="${productChanged || lotChanged || expiryChanged}"/>
            <g:if test="${anyChanged}">
                <%-- Original shipment item row - struck through --%>
                <tr>
                    <td class="center" rowspan="${receiptItems.size() + 1}">${i + 1}</td>
                    <td><div class="canceled">${item.product?.productCode}</div></td>
                    <td><div class="canceled">${item.product?.name}</div></td>
                    <td><div class="canceled">${item.lotNumber ?: ''}</div></td>
                    <td class="no-wrap"><div class="canceled"><g:formatDate date="${item.expirationDate}"
                                                                            format="MM/yyyy"/></div></td>
                    <td class="center"><div class="canceled">${item.quantity}</div></td>
                    <td></td>
                    <td></td>
                </tr>
                <%-- Actual received values --%>
                <g:each var="receiptItem" in="${receiptItems}">
                    <tr>
                        <td>${receiptItem.product?.productCode}</td>
                        <td>${receiptItem.product?.name}</td>
                        <td>${receiptItem.lotNumber ?: ''}</td>
                        <td class="no-wrap"><g:formatDate date="${receiptItem.expirationDate}" format="MM/yyyy"/></td>
                        <td></td>
                        <td class="center">${receiptItem.quantityReceived ?: ''}</td>
                        <td>${receiptItem.comment ?: ''}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <%-- Single row, without changes during receiving --%>
                <g:set var="quantityReceived" value="${receiptItems.sum { it.quantityReceived } ?: 0}"/>
                <g:set var="comments" value="${receiptItems.collect { it.comment }.findAll { it }?.join(', ')}"/>
                <tr>
                    <td class="center">${i + 1}</td>
                    <td>${item.product?.productCode}</td>
                    <td>${item.product?.name}</td>
                    <td>${item.lotNumber ?: ''}</td>
                    <td class="no-wrap"><g:formatDate date="${item.expirationDate}" format="MM/yyyy"/></td>
                    <td class="center">${item.quantity}</td>
                    <td class="center">${quantityReceived ?: ""}</td>
                    <td>${comments ?: ""}</td>
                </tr>
            </g:else>
        </g:each>
        </tbody>
    </table>
</div>

