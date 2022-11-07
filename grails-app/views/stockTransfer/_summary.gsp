<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<%@ page import="org.pih.warehouse.order.OrderType" %>

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

        <g:set var="disabledMessage" value="${g.message(code:'inventory.stockTransfers.editCompleted')}"/>

        <g:if test="${orderInstance.isOutbound(session?.warehouse)}">
            <g:link controller="stockTransfer" action="createOutboundReturn" id="${orderInstance?.id}" class="button"
                    disabled="${orderInstance?.status >= OrderStatus.COMPLETED}"
                    disabledMessage="${disabledMessage}">
                <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                <warehouse:message code="inventory.editStockTransfer.label" default="Edit Stock Transfer"/>
            </g:link>
        </g:if>
        <g:elseif test="${orderInstance.isInbound(session?.warehouse)}">
            <g:link controller="stockTransfer" action="createInboundReturn" id="${orderInstance?.id}" class="button"
                    disabled="${orderInstance?.status >= OrderStatus.COMPLETED}"
                    disabledMessage="${disabledMessage}">
                <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                <warehouse:message code="inventory.editStockTransfer.label" default="Edit Stock Transfer"/>
            </g:link>
        </g:elseif>
        <g:elseif test="${orderInstance.orderNumber.startsWith(grailsApplication.config.openboxes.stockTransfer.binReplenishment.prefix)}">
            <g:link controller="replenishment" action="create" id="${orderInstance?.id}" class="button"
                    disabled="${orderInstance?.status >= OrderStatus.COMPLETED}"
                    disabledMessage="${disabledMessage}">
                <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                <warehouse:message code="inventory.editStockTransfer.label" default="Edit Stock Transfer"/>
            </g:link>
        </g:elseif>
        <g:else>
            <g:link controller="stockTransfer" action="create" id="${orderInstance?.id}" class="button"
                    disabled="${orderInstance?.status >= OrderStatus.COMPLETED}"
                    disabledMessage="${disabledMessage}">
                <img src="${resource(dir: 'images/icons/silk', file: 'cart_edit.png')}" />&nbsp;
                <warehouse:message code="inventory.editStockTransfer.label" default="Edit Stock Transfer"/>
            </g:link>
        </g:else>
        <g:isUserInRole roles="[org.pih.warehouse.core.RoleType.ROLE_SUPERUSER, org.pih.warehouse.core.RoleType.ROLE_ADMIN, org.pih.warehouse.core.RoleType.ROLE_MANAGER]">
            <g:if test="${orderInstance?.status == OrderStatus.PENDING || orderInstance?.status == OrderStatus.APPROVED}">
                <g:link class="button" controller="stockTransfer" action="eraseStockTransfer" id="${orderInstance?.id}"
                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                    <img src="${resource(dir: 'images/icons/silk', file:'delete.png')}" />
                    <g:message code="default.button.delete.label"/>
                </g:link>
            </g:if>
        </g:isUserInRole>

        <div class="button-group right">
            <g:link controller="stockTransfer" action="print" id="${orderInstance?.id}" class="button" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                <warehouse:message code="inventory.printStockTransfer.label" default="Print Stock Transfer"/>
            </g:link>
        </div>
    </div>
</div>
