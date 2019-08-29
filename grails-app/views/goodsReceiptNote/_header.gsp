<table border="0">
    <tr>
        <td width="1%">
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <g:displayReportLogo/>
            </div>
        </td>
        <td>
            <div class="header">
                <h1>${title}</h1>
                <h3>${shipment?.shipmentNumber} - ${shipment?.name }
                <g:if test="${shipment.shipmentNumber}">
                    <div class="barcode">
                        <img src="${createLink(controller: 'product', action: 'barcode', params: [data: shipment?.shipmentNumber, width: 100, height: 30, format: 'CODE_128'])}"/>
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
                        ${shipment?.origin?.name}
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="shipping.destination.label"/>:</label>
                    </td>
                    <td>
                        ${shipment?.destination?.name}
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="shipping.dateShipped.label"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${shipment?.actualShippingDate}" format="d MMMMM yyyy hh:mma"/>
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="default.datePrinted.label" default="Date printed"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${new Date()}" format="d MMMMM yyyy hh:mma"/>
                    </td>
                </tr>

            </table>
        </td>

    </tr>
</table>
<hr/>
<br/>
<div class="clear"></div>