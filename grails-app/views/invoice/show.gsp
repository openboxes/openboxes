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
                                            ${invoiceInstance?.vendor}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td valign="top" class="name">
                                            <label><warehouse:message code="invoice.partyFrom.label"/></label>
                                        </td>
                                        <td valign="top" class="value">
                                            ${invoiceInstance?.buyerOrganization}
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
                            </table>
                        </div>
                    </div>
                    <div class="yui-u">
                        <g:render template="/invoice/invoiceItems"/>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
