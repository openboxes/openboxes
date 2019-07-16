<%@ page import="org.apache.commons.lang.StringEscapeUtils" defaultCodec="html" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Techseria</title>
    <g:javascript library="jquery" plugin="jquery"/>
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
        <warehouse:message code="report.shippingReport.title"/>
    </h1>
    <br>
</div>
<hr/>
<table border="0">
    <tr>
        <td>
            <table>
                <tr class="header">
                    <td class="name" width="25%">
                        <label><warehouse:message code="shipping.shipmentNumber.label"/>:</label>
                    </td>
                    <td>
                        ${shipment.shipmentNumber}
                    </td>
                </tr>
                <tr class="header">
                    <td class="name">
                        <label><warehouse:message code="shipping.shipmentType.label"/>:</label>
                    </td>
                    <td>
                        <format:metadata obj="${shipment.shipmentType?.name}"/>
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
        </td>
        <td class="top">
            <table border="0">
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="shipping.expectedShippingDate.label"/>:</label>
                    </td>
                    <td>
                        <g:formatDate
                                date="${shipment?.expectedShippingDate}" format="MMM d, yyyy"/>
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="shipping.actualShippingDate.label"
                                                  default="Actual shipping date"/>:</label>
                    </td>
                    <td>
                        <g:if test="${shipment?.actualShippingDate}">
                            <g:formatDate
                                    date="${shipment?.actualShippingDate}" format="MMM d, yyyy"/>
                        </g:if>
                        <g:else>
                            <label><warehouse:message code="default.notAvailable.label"
                                                      default="Not available"/></label>
                        </g:else>

                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="shipping.expectedDeliveryDate.label"
                                                  default="Expected arrival date"/>:</label>
                    </td>
                    <td>
                        <g:if test="${shipment.expectedDeliveryDate}">
                            <g:formatDate
                                    date="${shipment?.expectedDeliveryDate}" format="MMM d, yyyy"/>
                        </g:if>
                        <g:else>
                            <label><warehouse:message code="default.notAvailable.label"
                                                      default="Not available"/></label>
                        </g:else>
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="shipping.actualDeliveryDate.label"
                                                  default="Actual delivery date"/>:</label>
                    </td>
                    <td>
                        <g:if test="${shipment?.actualDeliveryDate}">
                            <g:formatDate
                                    date="${shipment?.actualDeliveryDate}" format="MMM d, yyyy"/>

                        </g:if>
                        <g:else>
                            <label><warehouse:message code="default.notAvailable.label"
                                                      default="Not available"/></label>

                        </g:else>
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="comment.label" default="Comments"/>:</label>
                    </td>
                    <td>
                        %{-- <g:formatDate
                                 date="${new Date()}" format="MMM d, yyyy hh:mma"/>--}%
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

<br><br>
<table class="fs-repeat-header w100" border="1">
    <thead>
    <tr>
        <th><warehouse:message code="react.stockMovement.pallet.label" default="Pallet"/></th>
        <th>${warehouse.message(code: 'container.label')}</th>
        <th>${warehouse.message(code: 'location.bin.label')}</th>
        <th>SKU</th>
        <th>MFG</th>
        <th>${warehouse.message(code: 'product.vendor.label')}</th>
        <th>${warehouse.message(code: 'product.label')}</th>
        <th>${warehouse.message(code: 'shipmentItem.lotNumber.label', default: "Lot Number")}</th>
        <th>${warehouse.message(code: 'default.expires.label')}</th>
        <th>${warehouse.message(code: 'productComponent.quantity.label', default: "Quantity")}</th>
        <th>${warehouse.message(code: 'default.units.label')}</th>
        <th>${warehouse.message(code: 'shipping.recipient.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${shipment.shipmentItems}">
        <tr>
            <g:if test="${it?.container?.parentContainer}">
                <td>
                    ${it?.container?.parentContainer?.name}
                </td>
                <td>
                    ${it.container?.name}
                </td>
            </g:if>
            <g:elseif test="${it.container}">
                <td>
                    ${it.container?.name}
                </td>
                <td>

                </td>
            </g:elseif>
           <g:else>
               <td></td>
               <td></td>
           </g:else>
            <td>
                ${it?.binLocation}
            </td>
            <td>
                ${it.inventoryItem?.product?.productCode}
            </td>
            <td>
                ${it.inventoryItem?.product?.manufacturerCode}
            </td>
            <td>
                ${it?.inventoryItem?.product?.vendorCode}
            </td>
            <td>
                ${it.inventoryItem?.product?.name}
            </td>
            <td>
                ${it?.inventoryItem?.lotNumber}
            </td>
            <td>
                ${it?.inventoryItem?.expirationDate}
            </td>
            <td>
                ${it?.quantity}
            </td>
            <td></td>
            <td>
                ${it?.recipient?.name}
            </td>

        </tr>
    </g:each>
    </tbody>
</table>
</body>
</html>