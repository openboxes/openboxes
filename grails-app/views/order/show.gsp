<%@ page import="org.pih.warehouse.order.Order" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'order.label', default: 'Order').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
        <style>
            .canceled-item { background-color: grey; }
        </style>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">

                <g:render template="summary" model="[orderInstance:orderInstance,currentState:'showOrder']"/>

                <g:hasErrors bean="${orderInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${orderInstance}" as="list" />
                    </div>
                </g:hasErrors>

                <div class="yui-gf">

                    <div class="yui-u first">
                        <div id="details" class="box">
                            <h2>
                                <warehouse:message code="order.orderHeader.label" default="Order Header"/>
                            </h2>
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderNumber.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.orderNumber}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.status.label" /></label>
                                    </td>
                                    <td valign="top" id="status" class="value">
                                        <format:metadata obj="${orderInstance?.displayStatus}"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderTypeCode.label" /></label>
                                    </td>
                                    <td valign="top" id="orderTypeCode" class="value">
                                        <format:metadata obj="${orderInstance?.orderType?.name}"/>
                                    </td>
                                </tr>
                                <g:if test="${orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()}">
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="order.originCode.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${orderInstance?.origin?.organization?.code }
                                        </td>
                                    </tr>
                                </g:if>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.origin.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.origin?.name}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.destination.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.destination?.name}
                                    </td>
                                </tr>
                                <g:if test="${orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()}">
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="paymentTerm.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            <g:if test="${orderInstance?.paymentTerm}">
                                                <div>${orderInstance?.paymentTerm?.name}</div>
                                            </g:if>
                                            <g:else>
                                                <g:message code="default.none.label"/>
                                            </g:else>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="order.paymentMethodType.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            <g:if test="${orderInstance?.paymentMethodType}">
                                                <div>${orderInstance?.paymentMethodType?.name}</div>
                                            </g:if>
                                            <g:else>
                                                <g:message code="default.none.label"/>
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:if>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.subtotal.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.subtotal?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="orderAdjustments.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.totalAdjustments?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.totalPrice.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.total?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>

                                </tbody>
                            </table>
                        </div>
                        <div class="box">
                            <h2><g:message code="default.auditing.label"/></h2>
                            <table>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.orderedBy}">
                                            <div>${orderInstance?.orderedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateOrdered}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.approvedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.approvedBy}">
                                            <div>${orderInstance?.approvedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateApproved}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.completedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.completedBy}">
                                            <div>${orderInstance?.completedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateCompleted}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.createdBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${orderInstance?.createdBy?.name }</div>
                                        <small><format:date obj="${orderInstance?.dateCreated}"/></small>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                            <label><warehouse:message code="default.updatedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${orderInstance?.updatedBy?.name }</div>
                                        <small><format:date obj="${orderInstance?.lastUpdated}"/></small>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="yui-u">
                        <div class="tabs tabs-ui">
                            <ul>
                                <li><a href="#tabs-summary"><warehouse:message code="default.summary.label" default="Summary"/></a></li>
                                <li><a href="#tabs-items"><warehouse:message code="order.itemStatus.label" default="Item Status"/></a></li>
                                <li><a href="#tabs-itemDetails"><warehouse:message code="order.itemDetails.label" default="Item Details"/></a></li>
                                <g:if test="${orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()}">
                                    <li><a href="#tabs-adjustments"><warehouse:message code="orderAdjustments.label"/></a></li>
                                    <li><a href="#tabs-shipments"><warehouse:message code="shipments.label"/></a></li>
                                    <li><a href="#tabs-invoices"><warehouse:message code="invoices.label"/></a></li>
                                </g:if>
                                <li><a href="#tabs-documents"><warehouse:message code="documents.label"/></a></li>
                                <li><a href="#tabs-comments"><warehouse:message code="comments.label" default="Comments"/></a></li>

                            </ul>
                            <div id="tabs-summary" class="ui-tabs-hide">
                                <g:render template="/order/orderSummary"/>
                            </div>
                            <div id="tabs-items" class="ui-tabs-hide">
                                <g:render template="/order/itemStatus"/>
                            </div>
                            <div id="tabs-itemDetails" class="ui-tabs-hide">
                                <g:render template="/order/itemDetails"/>
                            </div>
                            <g:if test="${orderInstance.orderType?.code == OrderTypeCode.PURCHASE_ORDER.name()}">
                                <div id="tabs-adjustments" class="ui-tabs-hide">
                                    <g:render template="/order/orderAdjustments"/>
                                </div>
                                <div id="tabs-shipments" class="ui-tabs-hide">
                                    <g:render template="/order/orderShipments"/>
                                </div>
                                <div id="tabs-invoices" class="ui-tabs-hide">
                                    <g:render template="/order/orderInvoices"/>
                                </div>
                            </g:if>
                            <div id="tabs-documents" class="ui-tabs-hide">
                                <g:render template="/order/orderDocuments"/>
                            </div>
                            <div id="tabs-comments" class="ui-tabs-hide">
                                <g:render template="/order/orderComments"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            $(document).ready(function() {
                $(".tabs").tabs({
                    cookie: {
                        expires: 1
                    },
                    selected: ${params.tab ? params.tab : 0}
                });
            });
        </script>
    </body>
</html>
