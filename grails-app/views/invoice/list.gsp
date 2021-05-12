<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="default.list.label" args="[g.message(code: 'invoices.label')]" /></title>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="buttonBar">
        <g:link controller="invoice" action="list" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
            <warehouse:message code="default.list.label" args="[g.message(code: 'invoices.label')]" default="List Invoices"/>
        </g:link>
    </div>

    <div class="yui-gf">
        <div class="yui-u first">
            <g:render template="filters" model="[]"/>
        </div>
        <div class="yui-u">
            <div class="box">
                <h2>
                    <warehouse:message code="default.list.label" args="[g.message(code: 'invoices.label')]" />
                </h2>
                <table>
                    <thead>
                    <tr>
                        <th>${warehouse.message(code: 'default.actions.label')}</th>
                        <th>${warehouse.message(code: 'default.numItems.label')}</th>
                        <th>${warehouse.message(code: 'default.status.label')}</th>
                        <th>${warehouse.message(code: 'invoice.invoiceType.label')}</th>
                        <th>${warehouse.message(code: 'invoice.invoiceNumber.label')}</th>
                        <th>${warehouse.message(code: 'invoice.vendor.label')}</th>
                        <th>${warehouse.message(code: 'invoice.vendorInvoiceNumber.label')}</th>
                        <th>${warehouse.message(code: 'invoice.totalValue.label')}</th>
                        <th>${warehouse.message(code: 'invoice.currency.label')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:unless test="${invoices}">
                        <tr class="prop">
                            <td colspan="15">
                                <div class="empty fade center">
                                    <warehouse:message code="invoices.none.message"/>
                                </div>
                            </td>
                        </tr>
                    </g:unless>
                    <g:each var="invoiceInstance" in="${invoices}" status="i">
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                            <td class="middle" width="1%">
                                <div class="action-menu">
                                    <g:render template="/invoice/actions" model="[invoiceId:invoiceInstance?.invoice?.id]"/>
                                </div>
                            </td>
                            <td class="middle">
                                <div class="count">${invoiceInstance?.itemCount}</div>
                            </td>
                            <td class="middle">
                                <div class="tag">
                                    <format:metadata obj="${invoiceInstance?.status}"/>
                                </div>
                            </td>
                            <td class="middle">
                                <div>
                                    <format:metadata obj="${invoiceInstance?.invoiceTypeCode}"/>
                                </div>
                            </td>
                            <td class="middle">
                                <g:link action="show" id="${invoiceInstance?.invoice?.id}">
                                    ${invoiceInstance?.invoiceNumber}
                                </g:link>
                            </td>
                            <td class="middle">
                                <div>${invoiceInstance?.partyCode} ${invoiceInstance?.partyName}</div>
                            </td>
                            <td class="middle">
                                <div>${invoiceInstance?.vendorInvoiceNumber}</div>
                            </td>
                            <td class="middle">
                                <div><g:formatNumber number="${invoiceInstance?.invoice?.totalValue}"/></div>
                            </td>
                            <td class="middle">
                                <div>${invoiceInstance?.currency}</div>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <div class="paginateButtons">
                    <g:set var="pageParams" value="${pageScope.variables['params']}"/>
                    <g:paginate total="${invoices?.totalCount}" params="${params}"/>
                </div>
            </div>
        </div>

    </div>
</div>
</body>
</html>
