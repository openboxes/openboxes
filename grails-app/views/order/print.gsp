<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="print" />
    <title><warehouse:message code="order.print.label" default="Print purchase order" /></title>
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
    <div class="clear"></div>


<div>
    <table>
        <tr>
            <td>
                <i><warehouse:message code="order.purchaseFrom.label" default="Purchase From"/></i><br/>
                <b>${orderInstance?.origin?.name }</b><br/>
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
            </td>
            <td>
                <i><warehouse:message code="order.deliverTo.label" default="Deliver To"/></i><br/>
                ${orderInstance?.orderedBy?.name }<br/>
                <b>${orderInstance?.destination?.name }</b><br/>
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
            </td>
        </tr>
    </table>
</div>
<div>
    <table >
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
                                <th class="left bottom">
                                    <warehouse:message code="orderItem.quantity.label" default="Quantity"/>
                                </th>
                                <th class="left bottom">
                                    <warehouse:message code="product.unitOfMeasure.label" default="Unit of measure"/>
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

                        <g:each var="orderItem" in="${orderInstance?.orderItems }" status="i">
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
                                    <g:formatNumber number="${orderItem?.unitPrice}" type="currency" currencyCode="USD"/>
                                </td>
                                <td class="right">
                                    <g:formatNumber number="${orderItem?.totalPrice()}" type="currency" currencyCode="USD"/>
                                </td>
                            </tr>

                        </g:each>
                        </tbody>
                        <tfoot>
                            <tr>
                                <th colspan="6">
                                    <warehouse:message code="default.total.label"/>
                                </th>
                                <th class="right"><g:formatNumber number="${orderInstance?.totalPrice()}" type="currency" currencyCode="USD"/></th>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </td>
        </tr>
    </table>
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