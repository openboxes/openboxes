<table>
    <tr>
        <td width="1%">
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <g:displayReportLogo/>
            </div>
        </td>
        <td>
            <div class="header">
                <div class="header-title">
                    ${goodsReceiptNote.shipmentNumber}<g:if test="${goodsReceiptNote.description}"> - ${goodsReceiptNote.description}</g:if>
                </div>
                <g:render template="/goodsReceiptNote/shipmentStatus" model="[goodsReceiptNote: goodsReceiptNote]"/>
                <g:if test="${goodsReceiptNote.shipmentNumber}">
                    <div class="barcode">
                        <img src="${createLink(controller: 'product', action: 'barcode', params: [data: goodsReceiptNote.shipmentNumber, width: 100, height: 30, format: 'CODE_128'])}"/>
                    </div>
                </g:if>
            </div>
        </td>
        <td width="33%">
            <table>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="shipping.origin.label"/>:</label>
                    </td>
                    <td>
                        ${goodsReceiptNote.originName}
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="shipping.destination.label"/>:</label>
                    </td>
                    <td>
                        ${goodsReceiptNote.destinationName}
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="shipping.dateShipped.label"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${goodsReceiptNote.dateShipped}" format="d MMMMM yyyy hh:mma"/>
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><g:message code="default.datePrinted.label" default="Date printed"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${new Date()}" format="d MMMMM yyyy hh:mma"/>
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="default.lastReceipt.label" default="Last receipt"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${goodsReceiptNote.lastReceiptDate}" format="d MMMMM yyyy hh:mma"/>
                    </td>
                </tr>

            </table>
        </td>
    </tr>
</table>
<hr/>
<br/>
<div class="clear"></div>
