<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Techseria</title>
    <style>
    table {
        border-collapse: collapse;
        page-break-inside: auto;
    }

    thead {
        display: table-header-group;
    }

    tr {
        page-break-inside: avoid;
        page-break-after: auto;
    }

    td {
        vertical-align: top;
    }

    th {
        background-color: lightgrey;
        font-weight: bold;
    }

    table {
        -fs-table-paginate: paginate;
        page-break-inside: avoid;
        border-collapse: collapse;
        border-spacing: 0;
        margin: 5px;

    }

    .tdspace {
        padding-bottom: 1.5em;
        vertical-align:top;
    }

    body {
        font: 11px "lucida grande", verdana, arial, helvetica, sans-serif;
        font-weight: bold;
    }
    </style>
    <link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'main.css')}" type="text/css"
          media="print, screen, projection"/>
</head>

<body>
<div>
    <br>

    <h1 style="text-align: center;font-weight: bold;font-size: 2em;">
        Commercial Invoice
    </h1>
    <br>
</div>
<hr/>
<br>
<table>
    <tr class="header">
        <td class="name" width="12%">
            <label><warehouse:message code="shipping.shipmentNumber.label"/>:</label>
        </td>
        <td>
            ${shipment.shipmentNumber}
        </td>
    </tr>
    <tr class="header">
        <td class="name">
            <label><warehouse:message code="requisition.origin.label"/>:</label>
        </td>
        <td>
            ${shipment.origin?.name}
        </td>
    </tr>
    <tr class="header">
        <td class="name">
            <label><warehouse:message code="requisition.destination.label"/>:</label>
        </td>
        <td>
            ${shipment.destination?.name}
        </td>
    </tr>
</table>
<br><br>
<table class="fs-repeat-header w100" border="1">
    <thead>
    <tr>
        <th><label><warehouse:message code="certificateOfDonation.number.label"/></label></th>
        <th><label><warehouse:message code="certificateOfDonation.code.label"/></label></th>
        <th><label><warehouse:message code="certificateOfDonation.itemDescription.label"/></label></th>
        <th>UoM</th>
        <th><label><warehouse:message code="certificateOfDonation.batchNumber.label"/></label></th>
        <th><label><warehouse:message code="certificateOfDonation.expDate.label"/></label></th>
        <th><label><warehouse:message code="certificateOfDonation.quantity.label"/></label></th>
        <th><label><warehouse:message code="certificateOfDonation.unitPrice.label"/></label></th>
        <th><label><warehouse:message code="certificateOfDonation.totalCost.label"/></label></th>
    </tr>
            <% def totalPrice = 0 %>
    <tr>
        <g:each in="${shipment.shipmentItems}" status="i" var="ship" >
            <%
                def totalCost = 0
                if (ship?.product?.pricePerUnit) {
                    totalCost = ship?.quantity*ship?.product?.pricePerUnit
                }
            %>
            <tr>
                <td>
                    ${i}
                </td>
                <td>
                    ${ship.inventoryItem?.product?.productCode}
                </td>
                <td>
                    ${ship.inventoryItem?.product?.name}
                </td>
                <td>
                    ${ship.inventoryItem?.product?.unitOfMeasure}
                </td>
                <td>
                    ${ship.inventoryItem?.lotNumber}
                </td>
                <td>
                    ${ship.inventoryItem?.expirationDate}
                </td>
                <td>
                    ${ship.quantity}
                </td>
                <td>
                    <g:if test="${ship?.product?.pricePerUnit}">
                        ${ship?.product?.pricePerUnit}
                    </g:if>
                    <g:else>
                        0
                    </g:else>
                </td>
                <td>
                    ${totalCost}
                    <% totalPrice+=totalCost; %>
                </td>
            </tr>
        </g:each>
    <tr>
        <td colspan="8" style="text-align: right">
            Total
        </td>
        <td>
            ${totalPrice}
        </td>
    </tr>
    </thead>
</table>
<br><br>
<div>
<table border="1" style="width: 20% !important; float:left !important; margin-left: 20%">
    <tbody>
    <tr>
        <td width="30%" class="tdspace" >Prepared on</td>
        <td></td>
    </tr>
    <tr>
        <td class="tdspace">By</td>
        <td></td>
    </tr>
    <tr>
        <td class="tdspace">Signature</td>
        <td></td>
    </tr>
    </tbody>
</table>

<table border="1" style="width: 20% !important; float:left !important; margin-left: 10%">
    <tbody>
    <tr>
        <td width="30%" class="tdspace" >Prepared on</td>
        <td></td>
    </tr>
    <tr>
        <td class="tdspace">By</td>
        <td></td>
    </tr>
    <tr>
        <td class="tdspace">Signature</td>
        <td></td>
    </tr>
    </tbody>
</table>
</div>

<div>
    <table border="1" style="width: 20% !important; float:left !important; margin-left: 20%;margin-top: 5%">
        <tbody>
        <tr>
            <td width="30%" class="tdspace" >Checked on</td>
            <td></td>
        </tr>
        <tr>
            <td class="tdspace">By</td>
            <td></td>
        </tr>
        <tr>
            <td class="tdspace">Signature</td>
            <td></td>
        </tr>
        <tr>
            <td class="tdspace">Position</td>
            <td></td>
        </tr>
        </tbody>
    </table>

    <table border="1" style="width: 20% !important; float:left !important; margin-left: 10%;margin-top: 5%">
        <tbody>
        <tr>
            <td width="30%" class="tdspace" >Checked on</td>
            <td></td>
        </tr>
        <tr>
            <td class="tdspace">By</td>
            <td></td>
        </tr>
        <tr>
            <td class="tdspace">Signature</td>
            <td></td>
        </tr>
        <tr>
            <td class="tdspace">Position</td>
            <td></td>
        </tr>
        </tbody>
    </table>
</div>
</body>
</html>