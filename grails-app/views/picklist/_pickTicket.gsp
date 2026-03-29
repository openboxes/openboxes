<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <style>
        * { padding: 0; margin: 0; }
        @page {
            size: letter;
            margin: 0.5in;
            background: white;
        }
        body { font: 11px "lucida grande", verdana, arial, helvetica, sans-serif; }
        table { border-collapse: collapse; border-spacing: 0; width: 100%; }
        table td, table th { padding: 4px 6px; border: 1px solid #333; vertical-align: top; }
        th { background-color: #e0e0e0; font-weight: bold; font-size: 10px; }
        h2 { text-align: center; font-size: 16px; margin: 0 0 8px 0; }
        .header-table td { border: 0; padding: 2px 4px; }
        .barcode { text-align: center; padding: 4px 0; }
        .barcode-label { font-size: 10px; font-family: monospace; margin-top: 2px; text-align: center; }
        .detail-table { margin-top: 8px; }
        .detail-table th { width: 20%; white-space: nowrap; }
        .pick-table { margin-top: 8px; }
        .pick-table th, .pick-table td { text-align: center; vertical-align: middle; }
        .signature-table { margin-top: 16px; }
        .signature-table td { height: 30px; }
        .signature-table th { width: 15%; white-space: nowrap; }
    </style>
</head>
<body>

    <h2><warehouse:message code="pickTicket.label" default="Pick Ticket"/></h2>

    <%-- Header: request number barcode --%>
    <table class="header-table">
        <tr>
            <td style="text-align: center;">
                <div class="barcode-label">${requisition?.requestNumber}</div>
            </td>
        </tr>
    </table>

    <%-- Requisition details --%>
    <table class="detail-table">
        <tr>
            <th><warehouse:message code="requisition.origin.label" default="Origin"/></th>
            <td>${requisition?.origin?.name}</td>
            <th><warehouse:message code="requisition.destination.label" default="Destination"/></th>
            <td>${requisition?.destination?.name}</td>
        </tr>
        <tr>
            <th><warehouse:message code="requisition.requisitionNumber.label" default="Request Number"/></th>
            <td>${requisition?.requestNumber}</td>
            <th><warehouse:message code="default.date.label" default="Date"/></th>
            <td><g:formatDate date="${new Date()}" format="MMM d, yyyy hh:mma"/></td>
        </tr>
    </table>

    <%-- Product --%>
    <table class="detail-table">
        <tr>
            <th><warehouse:message code="product.productCode.label" default="Product Code"/></th>
            <td width="30%" style="text-align: center;" class="barcode">
                <rendering:inlinePng bytes="${barcodes.productCode}" />
                <div class="barcode-label">${picklistItem.requisitionItem?.product?.productCode}</div>
            </td>
            <th><warehouse:message code="product.label" default="Product"/></th>
            <td>${picklistItem.requisitionItem?.product?.displayNameOrDefaultName}</td>
        </tr>
        <tr>
            <th><warehouse:message code="product.uom.label" default="UOM"/></th>
            <td colspan="3">${picklistItem.requisitionItem?.product?.unitOfMeasure ?: "EA"}</td>
        </tr>
    </table>

    <%-- Pick details --%>
    <table class="pick-table">
        <thead>
            <tr>
                <th style="width: 25%;"><warehouse:message code="inventoryLevel.binLocation.label" default="Bin Location"/></th>
                <th style="width: 25%;"><warehouse:message code="inventoryItem.lotNumber.label" default="Lot Number"/></th>
                <th style="width: 15%;"><warehouse:message code="inventoryItem.expirationDate.label" default="Expiry"/></th>
                <th style="width: 12%;"><warehouse:message code="pickTicket.quantityToPick.label" default="Qty to Pick"/></th>
                <th style="width: 12%;"><warehouse:message code="pickTicket.quantityPicked.label" default="Qty Picked"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    ${picklistItem.binLocation?.name ?: ''}
                </td>
                <td>
                    ${picklistItem.inventoryItem?.lotNumber ?: ''}
                </td>
                <td>
                    <g:if test="${picklistItem.inventoryItem?.expirationDate}">
                        <g:formatDate date="${picklistItem.inventoryItem?.expirationDate}" format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}" />
                    </g:if>
                    <g:else><warehouse:message code="default.never.label" default="Never"/></g:else>
                </td>
                <td>
                    ${picklistItem.quantity ?: 0}
                    ${picklistItem.requisitionItem?.product?.unitOfMeasure ?: "EA"}
                </td>
                <td>
                </td>
            </tr>
        </tbody>
    </table>

    <%-- Signature --%>
    <table class="signature-table">
        <tr>
            <th><warehouse:message code="requisition.pickedBy.label" default="Picked by"/></th>
            <td></td>
            <th><warehouse:message code="default.date.label" default="Date"/></th>
            <td></td>
        </tr>
        <tr>
            <th><warehouse:message code="default.signature.label" default="Signature"/></th>
            <td colspan="3"></td>
        </tr>
    </table>
</body>
</html>
