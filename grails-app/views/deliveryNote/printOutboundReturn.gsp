<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title><g:message code="requisition.deliveryNote.label" default="Delivery Note"/></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'buttons.css')}" type="text/css" media="all" />
    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
    <g:render template="styles"/>
</head>

<body class="${params.orientation ?: 'portrait'}">

    <g:render template="printToolbar"/>

    <g:render template="printHeader" model="[
        title         : g.message(code: 'requisition.deliveryNote.label', default: 'Delivery Note'),
        documentNumber: shipment.shipmentNumber,
        documentName  : shipment.name,
        origin        : shipment.origin,
        destination   : shipment.destination,
        shipDate      : shipment.expectedShippingDate,
        receivedDate  : shipment?.receipts ? shipment.receipts.last()?.actualDeliveryDate : null,
    ]"/>

    <g:render template="addressSection" model="[origin: shipment.origin, destination: shipment.destination]"/>

    <div class="content">
        <g:render template="printOutboundReturnTable" model="[shipment: shipment]"/>
        <g:render template="notesSection" model="[shipment: shipment]"/>
        <g:render template="signaturesSection"/>
    </div>

    <script type="text/javascript">
        $(document).ready(function () {
            $("#print-page").click(function (event) {
                window.print();
                return false;
            });

            $("#select-orientation").change(function () {
                var selected = this.value;
                if ('URLSearchParams' in window) {
                    var searchParams = new URLSearchParams(window.location.search);
                    searchParams.set("orientation", selected);
                    window.location.search = searchParams.toString();
                }
            });
        });
    </script>
</body>
</html>
