<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="print" />
    <title><warehouse:message code="report.showShippingReport.label" /></title>

    <style type="text/css">
        table { width: 100%; }
        body {
            font-family: arial, sans-serif ;
            font-size: 10px;
        }
        th, td {
            padding: 4px 4px 4px 4px ;
            text-align: center ;
        }
        th {
            border-bottom: 2px solid #333333 ;
        }
        td {
            border-bottom: 1px dotted #999999 ;
        }
        tfoot td {
            border-bottom-width: 0px ;
            border-top: 2px solid #333333 ;
            padding-top: 20px ;
        }
        .report-section {
            margin: 50px 0 50px 0;
        }
        .title {
            font-size: 32px;
            float: left;
        }
        .subtitle {
            font-size: 16px;
        }
        .logo {
            float: right;
        }
        .clearfix {
            overflow: auto;
        }

    </style>
</head>
<body>

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${command}">
        <div class="errors">
            <g:renderErrors bean="${command}" as="list" />
        </div>
    </g:hasErrors>

    <div class="report-header report-section clearfix">
        <div class="logo">
            <rendering:inlineImage bytes="${logo.bytes}" mimeType="${logo.mimeType}"/>
        </div>
        <div class="title">
            <warehouse:message code="report.shippingReport.heading"/>
        </div>
    </div>

    <div class="report-summary report-section">
        <table>
            <tr>
                <th width="25%">
                    <label>
                        <warehouse:message code="report.containerNumber.label"/>
                    </label>
                </th>
                <td width="25%">
                    ${command?.shipment?.shipmentNumber } ${command?.shipment?.name }
                </td>
                <th width="25%">
                    <label>
                        <warehouse:message code="report.plate.label"/>
                    </label>
                </th>
                <td width="25%">
                    ${command?.shipment?.getReferenceNumber('License Plate Number')?.identifier }
                </td>
            </tr>
            <tr>
                <th>
                    <label>
                        <warehouse:message code="report.origin.label"/>
                    </label>
                </th>
                <td class="value underline">
                    <span class="value">${command?.shipment?.origin?.name }</span>
                </td>
                <th>
                    <label>
                        <warehouse:message code="report.destination.label"/>
                    </label>
                </th>
                <td class="value underline">
                    <span class="value">${command?.shipment?.destination?.name }</span>
                </td>
            </tr>
        </table>
    </div>


    <g:set var="status" value="${0 }"/>

    <div class="report-details report-section">
        <table>
            <thead>
            <tr>
                <th rowspan="2" class="center bottom">
                    <warehouse:message code="report.number.label"/><!-- No., Number -->
                </th>
                <th rowspan="2" class="center bottom">
                    <warehouse:message code="shipping.container.label"/>
                </th>
                <th rowspan="2" class="bottom">
                    <warehouse:message code="report.productDescription.label"/><!-- Description produit, Product description -->
                </th>
                <th rowspan="2" class="center bottom">
                    <warehouse:message code="report.lotNumber.label"/><!-- No lot, Lot Number -->
                </th>
                <th rowspan="2" class="center bottom">
                    <warehouse:message code="report.expirationDate.label"/><!-- Exp, Expiration date -->
                </th>
                <%--
                <th rowspan="2" class="center bottom">
                    <warehouse:message code="report.quantityPerBox.label"/><!-- Qté en caisse, Qty in case -->
                </th>
                --%>
                <th colspan="2" class="center bottom">
                    <warehouse:message code="report.quantityDelivered.label"/><!-- Livré, Delivered -->
                </th>
                <th colspan="2" class="center bottom">
                    <warehouse:message code="report.quantityReceived.label"/><!-- Reçu, Received -->
                </th>
            </tr>
            <tr>
                <th class="center">
                    <warehouse:message code="report.quantityPerBox.label"/><!-- Qté caisse, Qty per box -->
                </th>
                <th class="center">
                    <warehouse:message code="report.quantityTotal.label"/><!-- Qté, Qty -->
                </th>
                <th class="center">
                    <warehouse:message code="report.quantityPerBox.label"/><!-- Qté caisse, Qty per box -->
                </th>
                <th class="center">
                    <warehouse:message code="report.quantityTotal.label"/><!-- Qté, Qty -->
                </th>
            </tr>
            </thead>

            <tbody>

            <g:set var="previousContainer"/>
            <g:set var="shipmentItemsByContainer" value="${command?.checklistReportEntryList?.groupBy { it?.shipmentItem?.container } }"/>
            <g:each var="checklistEntry" in="${command?.checklistReportEntryList }" status="i">
                <g:set var="rowspan" value="${shipmentItemsByContainer[checklistEntry?.shipmentItem?.container]}"/>
                <tr>
                    <td class="center">
                        ${i+1 }
                    </td>
                    <td class="center" rowspan="${rowspan }">
                        <g:if test="${checklistEntry?.shipmentItem?.container != previousContainer }">
                            <g:if test="${checklistEntry?.shipmentItem?.container }">
                                ${checklistEntry?.shipmentItem?.container?.name}
                            </g:if>
                            <g:else>
                                <warehouse:message code="shipping.unpacked.label"/>
                            </g:else>
                        </g:if>
                    </td>
                    <td>
                        <format:product product="${checklistEntry?.shipmentItem?.product}"/>
                    </td>
                    <td>
                        ${checklistEntry?.shipmentItem?.lotNumber }
                    </td>
                    <td>
                        <format:expirationDate obj="${checklistEntry?.shipmentItem?.expirationDate }"/>

                    </td>
                    <td>

                    </td>
                    <td class="center">
                        ${checklistEntry?.shipmentItem?.quantity }
                        ${checklistEntry?.shipmentItem?.product?.unitOfMeasure?:"EA" }
                    </td>
                    <td>

                    </td>
                    <td class="center">
                        ${checklistEntry?.shipmentItem?.quantity }
                        ${checklistEntry?.shipmentItem?.product?.unitOfMeasure?:"EA" }
                    </td>
                </tr>
                <g:set var="previousContainer" value="${checklistEntry?.shipmentItem?.container}"/>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="report-footer report-section">
        <table>
            <tr>
                <th width="25%">
                    <label><warehouse:message code="report.preparedBy.label"/></label><!--  Préparé par, Prepared by -->
                </th>
                <td width="25%">

                </td>
                <th width="25%">
                    <label><warehouse:message code="report.receivedBy.label"/></label><!-- Reçu par, Received by -->
                </th>
                <td width="25%">

                </td>
            </tr>
            <tr>
                <th>
                    <label><warehouse:message code="report.deliveredBy.label"/></label><!-- Livré par, Delivered/supplied by -->
                </th>
                <td>

                </td>
                <th>
                    <label><warehouse:message code="report.receivedOn.label"/></label><!-- Reçu le, Received on -->
                </th>
                <td>

                </td>
            </tr>
            <tr>
                <th>
                    <label><warehouse:message code="report.deliveredOn.label"/></label><!-- Livré le, Delivered/supplied on -->
                </th>
                <td>

                </td>
                <th>
                    <label><warehouse:message code="report.verifiedOn.label"/></label><!-- Vérifié le, Verified on -->
                </th>
                <td>

                </td>
            </tr>
            <tr>
                <th>
                    <label><warehouse:message code="report.transportedBy.label"/></label><!-- Transporté, Carrier -->
                </th>
                <td>

                </td>
                <th>
                    <label><warehouse:message code="report.verifiedBy.label"/></label><!-- Vérifié par, Verified by -->
                </th>
                <td>

                </td>
            </tr>

        </table>
    </div>


</body>
</html>