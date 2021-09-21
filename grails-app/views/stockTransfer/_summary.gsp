<div id="order-summary" class="summary">
    <table width="50%">
        <tbody>
        <tr class="odd">
            <td>
                <div class="title">
                    ${orderInstance?.orderNumber} <small><format:date obj="${orderInstance?.dateCreated}"/></small>
                </div>
            </td>
            <td class="top right" width="1%">
                <div class="tag tag-alert">
                    <format:metadata obj="${orderInstance?.status}"/>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div class="buttonBar">
    <div class="button-container">
        <g:link controller="stockTransfer" action="list" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'application_view_list.png')}" />&nbsp;
            <warehouse:message code="default.list.label" args="[g.message(code: 'inventory.stockTransfers.label')]" default="List Stock Transfers"/>
        </g:link>

        <g:link controller="stockTransfer" action="create" id="${orderInstance?.id}" class="button">
            <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
            <warehouse:message code="inventory.editStockTransfer.label" default="Edit Stock Transfer"/>
        </g:link>

        <div class="button-group right">
            <g:link controller="stockTransfer" action="print" id="${orderInstance?.id}" class="button" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                <warehouse:message code="inventory.printStockTransfer.label" default="Print Stock Transfer"/>
            </g:link>
        </div>
    </div>
</div>
