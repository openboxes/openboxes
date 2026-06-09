<%@ page defaultCodec="html" %>
<%-- Parameters: shipment --%>
<table class="w100 fixed-layout b-0" style="margin-top: 20px;">
    <tr>
        <td class="b-0"><h2><warehouse:message code="deliveryNote.notes.label" default="Notes"/></h2></td>
    </tr>
    <tr>
        <td class="b-0">
            <label><warehouse:message code="deliveryNote.trackingNumber.label" default="Tracking number"/>: </label>
            ${shipment?.referenceNumbers ? shipment.referenceNumbers.first() : ''}
        </td>
    </tr>
    <tr>
        <td class="b-0">
            <label><warehouse:message code="deliveryNote.driverName.label" default="Driver name"/>: </label>
            ${shipment?.driverName ?: ''}
        </td>
    </tr>
    <tr>
        <td class="b-0">
            <label><warehouse:message code="deliveryNote.comments.label" default="Comments"/>: </label>
            ${shipment?.additionalInformation ?: ''}
        </td>
    </tr>
</table>

