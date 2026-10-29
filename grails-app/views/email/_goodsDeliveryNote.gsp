<%@ page import="org.pih.warehouse.core.Constants" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <style>
    body {
        font-size: 10px;
        font-family: 'Inter', sans-serif;
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
        border-top: 1px solid #ccc;
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

    .header-title {
        font-size: 16px;
        font-weight: bold;
        line-height: 1.2;
        margin-top: 6px;
    }

    .header-table td.right {
        white-space: nowrap;
    }

    .canceled-blank {
        display: inline-block;
        width: 50px;
        border-top: 1px solid #666;
        vertical-align: middle;
    }
    .goods-receipt-note-original-item td {
        color: #222;
        font-weight: 600;
    }

    .goods-receipt-note-original-item td.canceled {
        color: #333;
    }

    .goods-receipt-note-split-item td {
        color: #888;
    }
    </style>
</head>

<body>
<table class="header-table">
    <tr>
        <td width="1%">
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <g:displayReportLogo/>
            </div>
        </td>
        <td width="66%">
            <h1>${g.message(code: 'goodsReceiptNote.label')}</h1>
            <div class="header-title">${goodsReceiptNote.shipmentNumber}<g:if test="${goodsReceiptNote.description}"> - ${goodsReceiptNote.description}</g:if></div>
            <g:render template="/goodsReceiptNote/shipmentStatus" model="[goodsReceiptNote: goodsReceiptNote]"/>
            <g:if test="${barcodeFileUri}">
                <div class="barcode">
                    <img src="${barcodeFileUri}" alt="barcode" style="width: 100px; height: 30px;"/>
                </div>
            </g:if>
        </td>
        <td width="34%">
            <table class="header-table">
                <tr><td class="right"><strong><g:message code="shipping.origin.label"/>:</strong>
                </td><td>${goodsReceiptNote.originName}</td></tr>
                <tr><td class="right"><strong><g:message code="shipping.destination.label"/>:</strong>
                </td><td>${goodsReceiptNote.destinationName}</td></tr>
                <tr><td class="right"><strong><g:message code="shipping.dateShipped.label"/>:</strong>
                </td><td><g:formatDate date="${goodsReceiptNote.dateShipped}" format="${Constants.DEFAULT_DATE_FORMAT}"/></td></tr>
                <tr><td class="right"><strong><g:message code="default.datePrinted.label"/>:</strong>
                </td><td><g:formatDate date="${new Date()}" format="${Constants.DEFAULT_DATE_TIME_FORMAT}"/></td></tr>
                <tr><td class="right"><strong><g:message code="default.lastReceipt.label"/>:</strong>
                </td><td><g:formatDate date="${goodsReceiptNote.lastReceiptDate}" format="${Constants.DEFAULT_DATE_FORMAT}"/></td></tr>
            </table>
        </td>
    </tr>
</table>

<hr/>

<g:render template="/goodsReceiptNote/itemsTable" model="[goodsReceiptNote: goodsReceiptNote]"/>

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
