<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="print" />
    <title><warehouse:message code="order.print.label" default="Print purchase order" /></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
    <style>
        table.order-items{ border-collapse:collapse; }
        table.order-items tr { border: none; }
        table.order-items td { border-right: solid 1px lightgrey; border-left: solid 1px lightgrey; margin: 10px; padding: 10px;}
        table.order-items th { border: solid 1px lightgrey; margin: 10px; padding: 10px;}
        .section { margin: 10px; }
    </style>
</head>
<body>

<div id="print-button">
    <h2>
        <warehouse:message code="order.print.label" default="Print purchase order"/>
        <div  class="right">
            <button type="button" onclick="window.print()">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
                ${warehouse.message(code:"default.print.label")}
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
    <div class="header section">

        <div class="left">
            <div class="logo">
                <g:if test="${session?.warehouse?.logo }">
                    <a href="${createLink(uri: '/dashboard/index')}">
                        <img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:session?.warehouse?.id)}" class="middle" />
                    </a>
                </g:if>
                <g:else>
                    <a href="${createLink(uri: '/dashboard/index')}">
                        <span class="middle"><warehouse:message code="default.openboxes.label"/></span></a>
                </g:else>
            </div>
        </div>
        <div class="right">
            <div class="title">
                <warehouse:message code="order.label"/>
            </div>
            <table width="auto">
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
                        <label><warehouse:message code="order.orderedBy.label" default="Ordered by"/></label>
                    </td>
                    <td>
                        ${orderInstance?.orderedBy.name}
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
            </table>

        </div>
    </div>
    <div class="clear"></div>

    <div class="content">

        <div class="report-summary section">
            <table width="100%">
                <tr>
                    <td width="55%">
                        <table>
                            <tr>
                                <td class="top left" width="25%">
                                    <label><warehouse:message code="order.orderedFrom.label" default="Supplier"/></label>
                                </td>
                                <td class="top left" width="75%">
                                    ${orderInstance?.origin?.name }
                                    <div class="address">
                                        <g:if test="${orderInstance?.origin?.address}">
                                            ${orderInstance?.origin?.address?.address}<br/>
                                            <g:if test="${orderInstance?.origin?.address?.address2}">
                                                ${orderInstance?.origin?.address?.address2}<br/>
                                            </g:if>
                                            ${orderInstance?.origin?.address?.city},
                                            ${orderInstance?.origin?.address?.stateOrProvince}<br/>
                                            ${orderInstance?.origin?.address?.country}<br/>
                                            ${orderInstance?.destination?.address?.description}
                                        </g:if>
                                        <g:else>
                                            <g:message code="location.noAddress.message" default="No address on record"/>
                                        </g:else>
                                    </div>
                                </td>
                            </tr>
                        </table>


                    </td>
                    <td >
                        <table>
                            <tr>
                                <td class="top" width="25%">
                                    <label><warehouse:message code="order.shipTo.label" default="Ship To"/></label>
                                </td>
                                <td class="top left" width="75%">
                                    <div>
                                        ${orderInstance?.destination?.name }
                                    </div>
                                    <div class="address">
                                        <g:if test="${orderInstance?.destination?.address}">
                                            ${orderInstance?.destination?.address?.address}<br/>
                                            <g:if test="${orderInstance?.destination?.address?.address2}">
                                                ${orderInstance?.destination?.address?.address2}<br/>
                                            </g:if>
                                            ${orderInstance?.destination?.address?.city}
                                            ${orderInstance?.destination?.address?.stateOrProvince}<br/>
                                            ${orderInstance?.destination?.address?.country}<br/>
                                            ${orderInstance?.destination?.address?.description}
                                        </g:if>
                                        <g:else>
                                            <g:message code="location.noAddress.message" default="No address on record"/>
                                        </g:else>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </div>

        <div class="report-details section">
            <g:set var="status" value="${0 }"/>
            <table class="order-items" width="100%">
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
                            ${orderItem?.product?.unitOfMeasure?:"EA"}
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
                        <th colspan="6">
                            <warehouse:message code="default.total.label"/>
                        </th>
                        <th class="right">
                            <g:formatNumber number="${orderInstance?.totalPrice()}"/>
                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                        </th>
                    </tr>
                </tfoot>
            </table>
        </div>
    </div>
</g:if>

</body>
</html>