<div class="summary">
    <table width="50%">
        <tbody>
        <tr class="odd">
            <g:hasRoleInvoice>
                <td width="1%">
                    <g:render template="/invoice/actions" model="[invoiceInstance:invoiceInstance]"/>
                </td>
            </g:hasRoleInvoice>
            <td>
                <div class="title">
                    ${invoiceInstance?.invoiceNumber}
                </div>
            </td>
            <%--
            <td class="top right" width="1%">
                <div class="tag tag-alert">
                    <format:metadata obj="${invoiceInstance?.status}"/>
                </div>
            </td>
            --%>
        </tr>
        </tbody>
    </table>
</div>
<div class="buttonBar">
    <div class="button-container">
        <g:hasRoleInvoice>
            <g:link controller="invoice" action="list" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
                <warehouse:message code="default.list.label" args="[g.message(code: 'invoices.label')]" default="List Invoices"/>
            </g:link>
            <g:link controller="invoice" action="create" id="${invoiceInstance?.id}" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                <warehouse:message code="invoice.editInvoice.label" default="Edit Invoice"/>
            </g:link>
            <g:if test="${invoiceInstance?.dateSubmitted}">
                <g:link class="button" controller="invoice" action="rollback" id="${invoiceInstance?.id}">
                    <img src="${resource(dir: 'images/icons/silk', file: 'arrow_rotate_anticlockwise.png')}" />&nbsp;
                    <warehouse:message code="invoice.rollback.label" default="Rollback"/>
                </g:link>
            </g:if>
        </g:hasRoleInvoice>
    </div>
</div>
