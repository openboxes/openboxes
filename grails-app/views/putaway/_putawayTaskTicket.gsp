<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <style>
        body {
            font: 12px "lucida grande", verdana, arial, helvetica, sans-serif;
            margin: 20px;
        }
        * {
            padding: 0;
            margin: 0;
        }
        h1 {
            font-size: 18px;
            margin-bottom: 15px;
            border-bottom: 2px solid #333;
            padding-bottom: 5px;
        }
        table.ticket-details {
            border-collapse: collapse;
            border-spacing: 0;
            width: 100%;
            margin-bottom: 20px;
        }
        table.ticket-details th,
        table.ticket-details td {
            padding: 6px 10px;
            border: 1px solid #ccc;
            text-align: left;
            vertical-align: top;
        }
        table.ticket-details th {
            background-color: #f0f0f0;
            font-weight: bold;
            width: 150px;
            white-space: nowrap;
        }
        .barcode-section {
            margin-top: 20px;
            page-break-inside: avoid;
        }
        .barcode-section h2 {
            font-size: 14px;
            margin-bottom: 10px;
            border-bottom: 1px solid #999;
            padding-bottom: 3px;
        }
        .barcode-row {
            display: inline-block;
            text-align: center;
            margin-right: 30px;
            margin-bottom: 15px;
            vertical-align: top;
        }
        .barcode-row img {
            display: block;
            margin: 0 auto 5px auto;
        }
        .barcode-label {
            font-size: 10px;
            font-family: monospace;
            color: #333;
        }
        .barcode-field-label {
            font-size: 10px;
            font-weight: bold;
            margin-bottom: 3px;
            color: #666;
        }
        .na {
            color: #999;
        }
        @page {
            size: letter;
            margin: 15mm;
        }
        @media print {
            body { margin: 0; }
            .no-print { display: none; }
        }
        .print-actions {
            margin-bottom: 15px;
        }
        .print-actions button {
            padding: 5px 15px;
            font-size: 13px;
            cursor: pointer;
        }
    </style>
    <title>Putaway Task - ${task.identifier ?: task.id}</title>
</head>

<body>
    <g:unless test="${pdfMode}">
        <div class="print-actions no-print">
            <button onclick="window.print();">Print</button>
        </div>
    </g:unless>

    <h1>
        <warehouse:message code="putawayTask.ticket.label" default="Putaway Task Ticket"/>
    </h1>

    <table class="ticket-details">
        <tr>
            <th><warehouse:message code="putawayTask.identifier.label" default="Identifier"/></th>
            <td>${task.identifier ?: '\u2014'}</td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.status.label" default="Status"/></th>
            <td>${task.status}</td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.type.label" default="Type"/></th>
            <td><format:metadata obj="${task.putawayTypeCode}"/></td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.product.label" default="Product"/></th>
            <td>${task.product?.productCode} - ${task.product?.name}</td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.currentLocation.label" default="Current Location"/></th>
            <td>${task.location?.name ?: '\u2014'}</td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.quantity.label" default="Quantity"/></th>
            <td>${task.quantity}</td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.destination.label" default="Destination"/></th>
            <td>${task.destination?.name ?: '\u2014'}</td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.container.label" default="Putaway Container"/></th>
            <td>${task.container?.name ?: '\u2014'}</td>
        </tr>
        <tr>
            <th><warehouse:message code="putawayTask.assignee.label" default="Assignee"/></th>
            <td>${task.assignee?.name ?: warehouse.message(code: 'putawayTask.unassigned.label', default: 'Unassigned')}</td>
        </tr>
        <tr>
            <th><warehouse:message code="receipt.receiptNumber.label" default="Receipt Number"/></th>
            <td>
                <g:if test="${task.putawayOrderItem?.receipt}">
                    <g:if test="${!pdfMode}">
                        <a href="${createLink(controller: 'shipment', action: 'showDetails', id: task.putawayOrderItem.receipt.shipment?.id)}">
                            ${task.putawayOrderItem.receipt.receiptNumber}
                        </a>
                    </g:if>
                    <g:else>
                        ${task.putawayOrderItem.receipt.receiptNumber}
                    </g:else>
                </g:if>
                <g:else>&#x2014;</g:else>
            </td>
        </tr>
    </table>

    <div class="barcode-section">
        <h2>Barcodes</h2>

        <%-- Product Code Barcode --%>
        <div class="barcode-row">
            <div class="barcode-field-label">
                <warehouse:message code="putawayTask.product.label" default="Product"/>
            </div>
            <g:if test="${task.product?.productCode}">
                <g:if test="${pdfMode}">
                    <rendering:inlinePng bytes="${productCodeBytes}" />
                </g:if>
                <g:else>
                    <img src="${createLink(controller: 'product', action: 'barcode',
                         params: [data: task.product.productCode, format: 'CODE_128', width: 200, height: 50])}" />
                </g:else>
                <div class="barcode-label">${task.product.productCode}</div>
            </g:if>
            <g:else>
                <div class="na">&mdash;</div>
            </g:else>
        </div>

        <%-- Destination Barcode --%>
        <div class="barcode-row">
            <div class="barcode-field-label">
                <warehouse:message code="putawayTask.destination.label" default="Destination"/>
            </div>
            <g:if test="${task.destination?.locationNumber ?: task.destination?.name}">
                <g:if test="${pdfMode}">
                    <rendering:inlinePng bytes="${destinationBytes}" />
                </g:if>
                <g:else>
                    <img src="${createLink(controller: 'product', action: 'barcode',
                         params: [data: task.destination.locationNumber ?: task.destination.name, format: 'CODE_128', width: 200, height: 50])}" />
                </g:else>
                <div class="barcode-label">${task.destination.locationNumber ?: task.destination.name}</div>
            </g:if>
            <g:else>
                <div class="na">&mdash;</div>
            </g:else>
        </div>

        <%-- Container Barcode --%>
        <div class="barcode-row">
            <div class="barcode-field-label">
                <warehouse:message code="putawayTask.container.label" default="Putaway Container"/>
            </div>
            <g:if test="${task.container?.locationNumber ?: task.container?.name}">
                <g:if test="${pdfMode}">
                    <rendering:inlinePng bytes="${containerBytes}" />
                </g:if>
                <g:else>
                    <img src="${createLink(controller: 'product', action: 'barcode',
                         params: [data: task.container.locationNumber ?: task.container.name, format: 'CODE_128', width: 200, height: 50])}" />
                </g:else>
                <div class="barcode-label">${task.container.locationNumber ?: task.container.name}</div>
            </g:if>
            <g:else>
                <div class="na">&mdash;</div>
            </g:else>
        </div>
    </div>
</body>
</html>
