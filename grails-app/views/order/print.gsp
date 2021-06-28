<%@ page defaultCodec="html" %>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="print" />
    <title><warehouse:message code="order.print.label" default="Print order" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
    <style>
        .report-details table, .report-details tr { page-break-inside: auto; page-break-after: auto; page-break-before: avoid; }
        table.order-items{ border-collapse:collapse; }
        table.order-items tr { border: none; page-break-inside: avoid; }
        table.order-items td { border: solid 1px lightgrey; margin: 10px; padding: 10px;}
        table.order-items th { border: solid 1px lightgrey; margin: 10px; padding: 10px; }
    </style>
</head>
<body>
<div id="print-button">
    <h2>
        <warehouse:message code="order.print.label" default="Print order"/>
        <div class="right buttons-container">
            <g:select id="select-orientation" name="orientation" from="['', 'portrait', 'landscape']" value="${params.orientation}"></g:select>
            <button id="print-page" type="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                ${warehouse.message(code:"default.button.print.label")}
            </button>
        </div>
    </h2>
</div>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<g:hasErrors bean="${orderInstance}">
    <div class="errors">
        <g:renderErrors bean="${orderInstance}" as="list" />
    </div>
</g:hasErrors>

<g:if test="${orderInstance}">
    <div class="header">
        <div class="left">
            <div class="logo">
                <g:displayReportLogo />
            </div>
        </div>
        <div class="left">
            ${orderInstance?.destinationParty?.name }
            <g:if test="${orderInstance?.destinationParty?.defaultLocation?.address}">
                <br/>
                ${orderInstance?.destinationParty?.defaultLocation?.address?.address}<br/>
                <g:if test="${orderInstance?.destinationParty?.defaultLocation?.address?.address2}">
                    ${orderInstance?.destinationParty?.defaultLocation?.address?.address2}<br/>
                </g:if>
                ${orderInstance?.destinationParty?.defaultLocation?.address?.city},
                ${orderInstance?.destinationParty?.defaultLocation?.address?.stateOrProvince}
                ${orderInstance?.destinationParty?.defaultLocation?.address?.postalCode}
                <br/>
                ${orderInstance?.destinationParty?.defaultLocation?.address?.country}<br/>
            </g:if>
        </div>
        <div class="right">
            <div class="title">
                ${orderInstance?.orderType?.name}
            </div>
            <table width="25%">
                <tr>
                    <td>
                        <label><warehouse:message code="order.orderNumber.label"/></label>
                    </td>
                    <td>
                        ${orderInstance?.orderNumber}
                    </td>
                </tr>
                <tr>
                    <td>
                        <label><warehouse:message code="order.dateOrdered.label" default="Date ordered"/></label>
                    </td>
                    <td>
                        <g:formatDate date="${new Date()}" format="dd MMM yyyy"/>
                    </td>
                </tr>
                <tr>
                    <td class="top">
                        <label><warehouse:message code="purchaseOrder.buyer.label" default="Buyer"/></label>
                    </td>
                    <td class="top left">
                        ${orderInstance?.destinationParty?.name}
                    </td>
                </tr>
                <tr>
                    <td class="top">
                        <label><warehouse:message code="order.paymentTerm.label" default="Payment Term"/></label>
                    </td>
                    <td class="top left">
                        ${orderInstance?.paymentTerm?.name?:message(code:'default.none.label')}
                    </td>
                </tr>
                <tr>
                    <td class="top">
                        <label><warehouse:message code="order.paymentMethodType.label" default="Payment Method"/></label>
                    </td>
                    <td class="top left">
                        ${orderInstance?.paymentMethodType?.name?:message(code:'default.none.label')}
                    </td>
                </tr>
            </table>

        </div>
    </div>

    <div class="content">
        <div class="report-summary" >
            <table>
                <tr>
                    <td width="50%">
                        <table>
                            <tr>
                                <td class="top left">
                                    <h3><warehouse:message code="order.orderedFrom.label" default="Supplier"/></h3>
                                    ${orderInstance?.origin?.name }<br/>
                                    <g:if test="${orderInstance?.origin?.address}">
                                        ${orderInstance?.origin?.address?.address}<br/>
                                        <g:if test="${orderInstance?.origin?.address?.address2}">
                                            ${orderInstance?.origin?.address?.address2}<br/>
                                        </g:if>
                                        ${orderInstance?.origin?.address?.city},
                                        ${orderInstance?.origin?.address?.stateOrProvince}
                                        ${orderInstance?.origin?.address?.postalCode}<br/>
                                        ${orderInstance?.origin?.address?.country}<br/>
                                        ${orderInstance?.origin?.address?.description}
                                    </g:if>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td width="50%">
                        <table>
                            <tr>
                                <td class="top">
                                    <h3><warehouse:message code="order.shipTo.label" default="Ship To"/></h3>
                                    <div>${orderInstance?.destination?.name }</div>
                                    <div>
                                        c/o ${orderInstance?.orderedBy?.name }
                                    </div>
                                    <g:if test="${orderInstance?.destination?.address}">
                                        <g:if test="${orderInstance?.destination?.address?.address}">
                                            ${orderInstance?.destination?.address?.address}<br/>
                                        </g:if>
                                        <g:if test="${orderInstance?.destination?.address?.address2}">
                                            ${orderInstance?.destination?.address?.address2}<br/>
                                        </g:if>
                                        <g:if test="${orderInstance?.destination?.address?.city}">
                                            ${orderInstance?.destination?.address?.city}
                                        </g:if>
                                        <g:if test="${orderInstance?.destination?.address?.stateOrProvince}">
                                            ${orderInstance?.destination?.address?.stateOrProvince}<br/>
                                        </g:if>
                                        <g:if test="${orderInstance?.destination?.address?.country}">
                                            ${orderInstance?.destination?.address?.country}<br/>
                                        </g:if>
                                        <g:if test="${orderInstance?.destination?.address?.description}">
                                            ${orderInstance?.destination?.address?.description}
                                        </g:if>
                                    </g:if>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
        <div class="report-details">
            <table>
                <tr>
                    <td colspan="5">
                        <div class="dialog">
                            <g:set var="status" value="${0}"/>
                            <g:set var="columnsNumber" value="${8}"/>
                            <table class="order-items">
                                <thead>
                                    <tr>
                                        <th class="center bottom">
                                            <warehouse:message code="number.label" default="No."/>
                                        </th>
                                        <th class="bottom">
                                            <warehouse:message code="product.productCode.label"/>
                                        </th>
                                        <th class="bottom">
                                            <warehouse:message code="product.name.label"/>
                                        </th>
                                        <g:if test="${orderInstance.orderItems.any { it.productSupplier?.supplierCode } }">
                                            <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                                            <th class="center">
                                                <warehouse:message code="product.supplierCode.label"/>
                                            </th>
                                        </g:if>
                                        <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerName } }">
                                            <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                                            <th class="center">
                                                <warehouse:message code="product.manufacturer.label"/>
                                            </th>
                                        </g:if>
                                        <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerCode } }">
                                            <g:set var="columnsNumber" value="${columnsNumber.toInteger() + 1}"/>
                                            <th class="center">
                                                <warehouse:message code="product.manufacturerCode.label"/>
                                            </th>
                                        </g:if>
                                        <th class="center bottom">
                                            <warehouse:message code="orderItem.quantity.label" default="Quantity"/>
                                        </th>
                                        <th class="center bottom">
                                            <warehouse:message code="product.uom.label" default="UOM"/>
                                        </th>
                                        <th class="center bottom">
                                            <warehouse:message code="orderItem.unitPrice.label" default="Unit price"/>
                                        </th>
                                        <th class="center bottom">
                                            <warehouse:message code="orderItem.subtotal.label" default="Subtotal"/>
                                        </th>
                                        <th class="center bottom">
                                            <warehouse:message code="orderItem.total.label" default="Total"/>
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                <g:set var="orderItems" value="${orderInstance?.listOrderItems()}"/>
                                <g:unless test="${orderItems}">
                                    <tr>
                                        <td colspan="8" class="empty fade center">
                                            <g:message code="default.noItems.label"/>
                                        </td>
                                    </tr>
                                </g:unless>
                                <g:each var="orderItem" in="${orderItems}" status="i">
                                    <tr>
                                        <td class="center">
                                            ${i+1 }
                                        </td>
                                        <td>
                                            ${orderItem?.product?.productCode}
                                        </td>
                                        <td>
                                            <format:product product="${orderItem?.product}"/>
                                        </td>
                                        <g:if test="${orderInstance.orderItems.any { it.productSupplier?.supplierCode } }">
                                            <td>
                                                ${orderItem?.productSupplier?.supplierCode}
                                            </td>
                                        </g:if>
                                        <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerName } }">
                                            <td>
                                                ${orderItem?.productSupplier?.manufacturerName}
                                            </td>
                                        </g:if>
                                        <g:if test="${orderInstance.orderItems.any { it.productSupplier?.manufacturerCode } }">
                                            <td>
                                                ${orderItem?.productSupplier?.manufacturerCode}
                                            </td>
                                        </g:if>
                                        <td class="right">
                                            ${orderItem?.quantity }
                                        </td>
                                        <td class="center">
                                            ${orderItem?.unitOfMeasure}
                                        </td>
                                        <td class="right">
                                            <g:formatNumber number="${orderItem?.unitPrice}" />
                                        </td>
                                        <td class="right">
                                            <g:formatNumber number="${orderItem?.subtotal}"/>
                                        </td>
                                        <td class="right">
                                            <g:formatNumber number="${orderItem?.total}"/>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                                <tfoot>
                                    <tr>
                                        <th colspan="${columnsNumber-2}" class="right" style="border: none">
                                            <warehouse:message code="default.subtotal.label" default="Subtotal"/>
                                        </th>
                                        <td colspan="2" class="right">
                                            <g:formatNumber number="${orderInstance?.subtotal}"/>
                                            ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </td>
                                    </tr>
                                    <g:each var="orderAdjustment" in="${orderInstance?.orderAdjustments.findAll { !(it.orderItem || it.canceled) }.sort { it.totalAdjustments }.reverse() }">
                                        <tr>
                                            <th colspan="${columnsNumber-1}" class="right" style="border: none">
                                                <g:if test="${orderAdjustment.description}">
                                                    ${orderAdjustment.description}
                                                </g:if>
                                                <g:else>
                                                    <format:metadata obj="${orderAdjustment.orderAdjustmentType}"/>
                                                    <g:if test="${orderAdjustment.percentage}">
                                                        (${orderAdjustment.percentage}%)
                                                    </g:if>
                                                </g:else>
                                            </th>
                                            <td colspan="2" class="right">
                                                <g:formatNumber number="${orderAdjustment.totalAdjustments}"/>
                                                ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                            </td>
                                        </tr>
                                    </g:each>
                                    <tr>
                                        <th colspan="${columnsNumber-2}" class="right" style="border: none">
                                            <warehouse:message code="default.total.label"/>
                                        </th>
                                        <td colspan="2" class="right">
                                            <g:formatNumber number="${orderInstance?.total}"/>
                                            ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</g:if>

<script>
    $(document).ready(function() {
        $(".filter").change(function() {
            $(this).closest("form").submit();
        });
    });
</script>
</body>
</html>
