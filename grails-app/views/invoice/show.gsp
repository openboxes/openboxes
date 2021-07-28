<%@ page import="org.pih.warehouse.order.Order" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="default.view.label" args="['Invoice']" /></title>
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
                <g:render template="summary" model="[invoiceInstance:invoiceInstance]"/>
                <div class="yui-gf">
                    <div class="yui-u first">
                        <div class="box">
                            <h2>
                                <warehouse:message code="invoice.invoiceHeader.label" default="Invoice Header"/>
                            </h2>
                            <table>
                                <tbody>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.invoiceNumber.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.invoiceNumber}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.vendorInvoiceNumber.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.vendorInvoiceNumber}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.vendor.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.party?.code} ${invoiceInstance?.party?.name}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.partyFrom.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.partyFrom?.name}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="default.createdBy.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.createdBy}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.currency.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.currencyUom?.name}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.invoiceType.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.invoiceType?.name}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.totalValue.label"/></label>
                                            <small>${warehouse.message(code: 'invoice.localCurrency.label', default: 'Local Currency')}</small>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.totalValue} ${invoiceInstance?.currencyUom?.code}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.totalValue.label"/></label>
                                            <small>${warehouse.message(code: 'invoice.defaultCurrency.label', default: 'Default Currency')}</small>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.totalValueNormalized} ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </td>
                                    </tr>
                                    <g:if test="${invoiceInstance?.orders}">
                                        <tr class="prop">
                                            <td class="name">
                                                <g:message code="orders.label"/>
                                            </td>
                                            <td class="value">
                                                <g:each var="order" in="${invoiceInstance?.orders}">
                                                    <g:link controller="order" action="show" id="${order?.id}" target="_blank">
                                                        ${g.message(code:'default.view.label', args: [g.message(code: 'order.label')])}
                                                        ${order.orderNumber}
                                                    </g:link>
                                                </g:each>
                                            </td>
                                        </tr>
                                    </g:if>
                                    <g:if test="${invoiceInstance?.shipments}">
                                        <tr class="prop">
                                            <td class="name">
                                                <g:message code="shipments.label"/>
                                            </td>
                                            <td class="value">
                                                <g:each var="shipment" in="${invoiceInstance?.shipments}">
                                                    <g:link controller="stockMovement" action="show" id="${shipment?.id}" target="_blank">
                                                        ${g.message(code:'default.view.label', args: [g.message(code: 'shipment.label')])}
                                                        ${shipment.shipmentNumber}
                                                    </g:link>
                                                </g:each>
                                            </td>
                                        </tr>
                                    </g:if>
                                </tbody>
                            </table>
                        </div>
                        <div class="box">
                            <h2><g:message code="default.auditing.label"/></h2>
                            <table>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.createdBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${invoiceInstance?.createdBy?.name }</div>
                                        <small><format:date obj="${invoiceInstance?.dateCreated}"/></small>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.updatedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${invoiceInstance?.updatedBy?.name }</div>
                                        <small><format:date obj="${invoiceInstance?.lastUpdated}"/></small>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="invoice.invoiced.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div><format:date obj="${invoiceInstance?.dateInvoiced}"/></div>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="invoice.submitted.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div><format:date obj="${invoiceInstance?.datePosted}"/></div>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="yui-u">
                        <div class="tabs tabs-ui">
                            <ul>
                                <li><a href="#tabs-items"><warehouse:message code="invoice.invoiceItems.label" default="Invoice Items"/></a></li>
                                <li><a href="#tabs-documents"><warehouse:message code="document.documents.label" default="Documents"/></a></li>
                            </ul>
                            <div id="tabs-items" class="ui-tabs-hide">
                                <g:render template="/invoice/invoiceItems"/>
                            </div>
                            <div id="tabs-documents" class="ui-tabs-hide">
                                <g:render template="/invoice/documents"/>
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
            });
          });
        </script>
    </body>
</html>
