<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <style>
    body {
        font-family: "DejaVu Sans", sans-serif;
        font-size: 10px;
    }

    table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 10px;
    }

    th {
        background-color: #f2f2f2;
        font-weight: bold;
        border: 1px solid #ccc;
        padding: 5px;
        text-align: left;
    }

    td {
        border: 1px solid #ccc;
        padding: 5px;
        vertical-align: top;
    }

    .header-table td {
        border: none;
    }

    .signature-table {
        margin-top: 30px;
    }

    .signature-table td {
        border: none;
        border-bottom: 1px solid #000;
        height: 40px;
    }

    .center {
        text-align: center;
    }

    .right {
        text-align: right;
    }

    .canceled {
        text-decoration: line-through;
        color: #666;
    }

    h1, h3 {
        margin: 0;
    }

    hr {
        border: 0;
        border-top: 1px solid #ccc;
        margin: 10px 0;
    }

    .document-title > h1 {
        width: 100%;
        margin-bottom: 10px;
        border-bottom: 1px solid #ccc;
    }
    </style>
</head>

<body>
<div class="document-title">
    <h1>${g.message(code: 'goodsReceiptNote.label')}</h1>
</div>
<table class="header-table">
    <tr>
        <td width="1%">
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <g:displayReportLogo/>
            </div>
        </td>
        <td width="66%">
            <div style="font-size: 18px; font-weight: bold;">
                <g:message code="enum.ShipmentStatusCode.${shipment?.status?.code?.name}"
                           default="${shipment?.status?.code?.name}"/>
            </div>

            <h3>${shipment?.shipmentNumber} - ${shipment?.name}</h3>
            <g:if test="${barcodeFileUri}">
                <div class="barcode">
                    <img src="${barcodeFileUri}" alt="barcode" style="width: 100px; height: 30px;"/>
                </div>
            </g:if>
        </td>
        <td width="34%">
            <table class="header-table">
                <tr><td class="right"><strong><g:message code="shipping.origin.label"/>:</strong>
                </td><td>${shipment?.origin?.name}</td></tr>
                <tr><td class="right"><strong><g:message code="shipping.destination.label"/>:</strong>
                </td><td>${shipment?.destination?.name}</td></tr>
                <tr><td class="right"><strong><g:message code="shipping.dateShipped.label"/>:</strong>
                </td><td><g:formatDate date="${shipment?.actualShippingDate}" format="dd/MMM/yyyy"/></td></tr>
                <tr><td class="right"><strong><g:message code="default.datePrinted.label"/>:</strong>
                </td><td><g:formatDate date="${new Date()}" format="dd/MMM/yyyy HH:mm"/></td></tr>
            </table>
        </td>
    </tr>
</table>

<hr/>

<table>
    <thead>
    <tr>
        <th>#</th>
        <th>${g.message(code: 'product.productCode.label')}</th>
        <th>${g.message(code: 'product.label')}</th>
        <th>${g.message(code: 'inventoryItem.lotNumber.label')}</th>
        <th>${g.message(code: 'inventoryItem.expirationDate.label')}</th>
        <th>${g.message(code: 'default.uom.label')}</th>
        <th>${g.message(code: 'shipmentItem.quantityShipped.label')}</th>
        <g:each in="${shipment?.receipts}" var="receipt">
            <th>${g.message(code: 'shipping.receipt.label')} ${receipt?.receiptNumber}</th>
        </g:each>
        <th class="center">${g.message(code: 'shipmentItem.discrepancy.label')}</th>
        <th>${g.message(code: 'default.comment.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${shipment?.sortShipmentItemsBySortOrder()?.findAll { it.receiptItems }}" status="i" var="shipmentItem">
        <g:set var="receiptItems" value="${shipmentItem.receiptItems.sort { !it.isSplitItem }}"/>
        <g:set var="hasSplit" value="${receiptItems.any { it.isSplitItem }}"/>

        <g:if test="${hasSplit}">
            <tr style="background-color: #f9f9f9;">
                <td>${i + 1}</td>
                <td class="canceled">${shipmentItem?.product?.productCode}</td>
                <td class="canceled">${shipmentItem?.product?.displayNameOrDefaultName}</td>
                <td class="canceled">${shipmentItem?.inventoryItem?.lotNumber}</td>
                <td class="canceled"><g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}"
                                                   format="dd/MMM/yyyy"/></td>
                <td class="canceled">${shipmentItem?.inventoryItem?.product?.unitOfMeasure ?: 'EA'}</td>
                <td class="canceled">${shipmentItem?.quantity}</td>
                <g:each in="${shipment.receipts}" var="receipt"><td></td></g:each>
                <td></td><td></td>
            </tr>
        </g:if>

        <g:each in="${receiptItems}" status="j" var="receiptItem">
            <tr>
                <td><g:if test="${!hasSplit && j == 0}">${i + 1}</g:if></td>
                <td>${receiptItem?.product?.productCode}</td>
                <td>${receiptItem?.product?.displayNameOrDefaultName}</td>
                <td>${receiptItem?.inventoryItem?.lotNumber}</td>
                <td><g:formatDate date="${receiptItem?.inventoryItem?.expirationDate}" format="dd/MMM/yyyy"/></td>
                <td>${j == 0 ? (shipmentItem?.inventoryItem?.product?.unitOfMeasure ?: 'EA') : ''}</td>
                <td>${receiptItem?.quantityShipped}</td>
                <g:each in="${shipment.receipts}" var="receipt">
                    <td>${receiptItem.receipt == receipt ? receiptItem.quantityReceived : 0}</td>
                </g:each>
                <td class="center">${receiptItem.quantityShipped - receiptItem.quantityReceived}</td>
                <td>${receiptItem?.comment}</td>
            </tr>
        </g:each>
    </g:each>
    </tbody>
</table>

<table class="signature-table">
    <g:each in="['deliveryNote.deliveredBy.label', 'deliveryNote.receivedBy.label', 'deliveryNote.checkedBy.label']"
            var="label">
        <tr>
            <td width="40%"><g:message code="${label}"/></td>
            <td width="30%"><g:message code="deliveryReceipt.signature.label"/></td>
            <td width="30%" class="right"><g:message code="deliveryReceipt.date.label"/></td>
        </tr>
    </g:each>
</table>

</body>
</html>
