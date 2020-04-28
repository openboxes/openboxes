<%@ page defaultCodec="html" %>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="print" />
    <title><warehouse:message code="order.print.label" default="Print order" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
    <style>
        table.order-items{ border-collapse:collapse; }
        table.order-items tr { border: none; }
        table.order-items td { border-right: solid 1px lightgrey; border-left: solid 1px lightgrey; margin: 10px; padding: 10px;}
        table.order-items th { border: solid 1px lightgrey; margin: 10px; padding: 10px;}

    </style>
</head>
<body>

<div id="print-button">
    <h2>
        <warehouse:message code="order.print.label" default="Print order"/>
        <div  class="right">
            <button type="button" onclick="window.print()">
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
                <g:displayReportLogo/>
            </div>
        </div>
        <div class="right">
            <div class="title">
                <warehouse:message code="enum.OrderTypeCode.${orderInstance?.orderTypeCode}"/>
            </div>
            <table width="25%">
                <tr>
                    <td>
                        <warehouse:message code="order.orderNumber.label"/>
                    </td>
                    <td>
                        <b>${orderInstance?.orderNumber}</b>
                    </td>
                </tr>
                <tr>
                    <td>
                        <warehouse:message code="order.dateOrdered.label" default="Date ordered"/>
                    </td>
                    <td>
                        <b><g:formatDate date="${new Date()}" format="dd MMM yyyy"/></b>
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td></td>
                </tr>
            </table>

        </div>
    </div>
    <div class="clear"></div>

    <div class="content">


        <div class="report-summary" >
            <table>
                <tr>
                    <td colspan="2">
                        <h2>Summary</h2>
                    </td>
                </tr>
                <tr>
                    <td width="50%">
                        <table>
                            <tr>
                                <td class="top left" width="25%">
                                    <label><warehouse:message code="order.orderedFrom.label" default="Supplier"/></label>
                                </td>
                                <td class="top left" width="75%">
                                    <b>${orderInstance?.origin?.name }</b>
                                    <g:if test="${orderInstance?.origin?.address}">
                                        <br/>
                                        ${orderInstance?.origin?.address?.address}<br/>
                                        <g:if test="${orderInstance?.origin?.address?.address2}">
                                            ${orderInstance?.origin?.address?.address2}<br/>
                                        </g:if>
                                        ${orderInstance?.origin?.address?.city},
                                        ${orderInstance?.origin?.address?.stateOrProvince}<br/>
                                        ${orderInstance?.origin?.address?.country}<br/>
                                        ${orderInstance?.destination?.address?.description}
                                    </g:if>
                                </td>
                            </tr>
                        </table>


                    </td>
                    <td width="50%">
                        <table>
                            <tr>
                                <td class="top" width="25%">
                                    <label><warehouse:message code="order.shipTo.label" default="Ship To"/></label>
                                </td>
                                <td class="top left" width="75%">
                                    <div>
                                        <b>${orderInstance?.destination?.name }</b>
                                    </div>
                                    <div>
                                        c/o ${orderInstance?.orderedBy?.name }
                                    </div>
                                    <g:if test="${orderInstance?.destination?.address}">
                                        <br/>
                                        ${orderInstance?.destination?.address?.address}<br/>
                                        <g:if test="${orderInstance?.destination?.address?.address2}">
                                            ${orderInstance?.destination?.address?.address2}<br/>
                                        </g:if>
                                        ${orderInstance?.destination?.address?.city}
                                        ${orderInstance?.destination?.address?.stateOrProvince}<br/>
                                        ${orderInstance?.destination?.address?.country}<br/>
                                        ${orderInstance?.destination?.address?.description}
                                    </g:if>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
        <div class="report-details">
            <table >
                <tr>
                    <td colspan="5">
                        <h2>Details</h2>
                    </td>
                </tr>
                <tr>
                    <td colspan="5">
                        <div class="list">
                            <g:set var="status" value="${0 }"/>
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
                                            <warehouse:message code="orderItem.totalPrice.label" default="Total amount"/>
                                        </th>

                                    </tr>
                                </thead>

                                <tbody>

                                <g:each var="orderItem" in="${orderInstance?.listOrderItems() }" status="i">
                                    <tr style="${i%2?'odd':'even'}">
                                        <td class="center">
                                            ${i+1 }
                                        </td>
                                        <td>
                                            ${orderItem?.product?.productCode}
                                        </td>
                                        <td>
                                            <format:product product="${orderItem?.product}"/>
                                        </td>
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
                                            <g:formatNumber number="${orderItem?.totalPrice()}"/>
                                        </td>
                                    </tr>

                                </g:each>
                                </tbody>
                                <tfoot>
                                    <tr>
                                        <th colspan="6" class="right">
                                            <warehouse:message code="default.subtotal.label" default="Subtotal"/>
                                        </th>
                                        <th class="right">
                                            <g:formatNumber number="${orderInstance?.subtotal}"/>
                                            ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </th>
                                    </tr>
                                    <tr>
                                        <th colspan="6" class="right">
                                            <warehouse:message code="default.adjustments.label" default="Adjustments"/>
                                        </th>
                                        <th class="right">
                                            <g:formatNumber number="${orderInstance?.totalAdjustments}"/>
                                            ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </th>
                                    </tr>
                                    <tr>
                                        <th colspan="6" class="right">
                                            <warehouse:message code="default.total.label"/>
                                        </th>
                                        <th class="right">
                                            <g:formatNumber number="${orderInstance?.total}"/>
                                            ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </th>
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
