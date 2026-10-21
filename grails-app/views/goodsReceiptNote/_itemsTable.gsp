<%@ page import="org.pih.warehouse.core.Constants; org.pih.warehouse.receiving.GoodsReceiptNoteItemStatus" %>
<%-- Shared by the printable page and the PDF attached to the receipt email --%>
<g:set var="receipts" value="${goodsReceiptNote.receiptColumnsDisplayed ? goodsReceiptNote.receipts : []}"/>
<g:set var="defaultUnitOfMeasure" value="${g.message(code: 'default.each.label')}"/>
<table class="goods-receipt-note-items">
    <thead>
    <tr>
        <th>#</th>
        <th>${g.message(code: 'product.productCode.label')}</th>
        <th>${g.message(code: 'product.label')}</th>
        <th>${g.message(code: 'inventoryItem.lotNumber.label')}</th>
        <th>${g.message(code: 'inventoryItem.expirationDate.label')}</th>
        <th>${g.message(code: 'default.uom.label')}</th>
        <th>${g.message(code: 'shipmentItem.quantityShipped.label')}</th>
        <g:each in="${receipts}" var="receipt">
            <th>${g.message(code: 'shipping.receipt.label')} ${receipt.receiptNumber}</th>
        </g:each>
        <th>${g.message(code: 'shipping.totalReceived.label')}</th>
        <th class="center">${g.message(code: 'shipmentItem.discrepancy.label')}</th>
        <th>${g.message(code: 'default.comment.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${goodsReceiptNote.items}" var="item">
        <g:set var="productClass" value="${item.productCrossedOut ? 'canceled' : ''}"/>
        <g:set var="lotClass" value="${item.lotCrossedOut ? 'canceled' : ''}"/>
        <tr class="goods-receipt-note-original-item" style="border-top: 1px solid #ddd;">
            <td>${item.index}</td>
            <td class="${productClass}">${item.productCode}</td>
            <td class="${productClass} product-name">${item.productName}</td>
            <td class="${lotClass}">
                <g:if test="${item.lotNumber}">${item.lotNumber}</g:if>
                <g:elseif test="${item.lotCrossedOut}"><span class="canceled-blank"></span></g:elseif>
            </td>
            <td class="${lotClass}">
                <g:if test="${item.expirationDate}">
                    <g:formatDate date="${item.expirationDate}" format="${Constants.DEFAULT_DATE_FORMAT}"/>
                </g:if>
                <g:elseif test="${item.lotCrossedOut}"><span class="canceled-blank"></span></g:elseif>
            </td>
            <td>${item.unitOfMeasure ?: defaultUnitOfMeasure}</td>
            <td>${item.quantityShipped}</td>
            <g:each in="${receipts}" var="receipt">
                <td>${item.quantityReceivedByReceiptId[receipt.id]}</td>
            </g:each>
            <td>${item.totalQuantityReceived}</td>
            <td class="center" style="${item.status == GoodsReceiptNoteItemStatus.REMAINING ? 'color: #0066cc;' : ''}">
                <g:if test="${item.status != GoodsReceiptNoteItemStatus.COMPLETE}">
                    ${g.message(code: "goodsReceiptNote.status.${item.status.name()}.label",
                            args: [item.status == GoodsReceiptNoteItemStatus.CANCELED ? item.quantityCanceled : Math.abs(item.quantityRemaining)])}
                </g:if>
            </td>
            <td>
                <g:each in="${item.comments}" var="comment" status="i">
                    <g:if test="${i > 0}"><br/></g:if>${comment}
                </g:each>
            </td>
        </tr>
        <g:each in="${item.splitItems}" var="splitItem">
            <tr class="goods-receipt-note-split-item">
                <td></td>
                <td>${splitItem.productDisplayed ? splitItem.productCode : ''}</td>
                <td class="product-name">${splitItem.productDisplayed ? splitItem.productName : ''}</td>
                <td>${splitItem.lotNumber}</td>
                <td><g:formatDate date="${splitItem.expirationDate}" format="${Constants.DEFAULT_DATE_FORMAT}"/></td>
                <td>${splitItem.productDisplayed ? (splitItem.unitOfMeasure ?: defaultUnitOfMeasure) : ''}</td>
                <td></td>
                <g:each in="${receipts}" var="receipt">
                    <td>${splitItem.quantityReceivedByReceiptId[receipt.id]}</td>
                </g:each>
                <td>${splitItem.totalQuantityReceived}</td>
                <td></td>
                <td></td>
            </tr>
        </g:each>
    </g:each>
    </tbody>
</table>
