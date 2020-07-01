<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'stockMovement.label', default: 'Stock Movement')}" />
    <title>
        <warehouse:message code="stockMovement.label"/>
    </title>
</head>
<body>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:if test="${flash.error}">
        <div class="error">${flash.error}</div>
    </g:if>

    <g:render template="summary" model="[shipmentInstance:stockMovement?.shipment, requisition: stockMovement?.requisition]"/>

    <div class="button-bar ">
        <g:if test="${stockMovement?.documents}">
            <div class="right">
                <div class="button-group">
                    <g:link controller="stockMovement" action="addDocument" class="button" id="${stockMovement?.id}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
                        <warehouse:message code="stockMovement.uploadDocuments.label" />
                    </g:link>
                    <span class="action-menu">
                        <button class="action-btn button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'page_save.png')}" />
                            &nbsp; <g:message code="default.button.download.label"/>
                            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                        </button>
                        <div class="actions">
                            <g:each var="document" in="${stockMovement?.documents}">
                                <g:if test="${!document.hidden}">
                                    <div class="action-menu-item">
                                        <g:link url="${document.uri}" target="_blank">
                                            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'page.png')}" class="middle"/>&nbsp;
                                            ${document.name}
                                        </g:link>
                                    </div>
                                </g:if>
                            </g:each>
                        </div>
                    </span>
                </div>
            </div>
        </g:if>

        <div class="button-group">
            <g:link controller="stockMovement" action="list" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'text_list_bullets.png')}" />&nbsp;
                <warehouse:message code="default.button.list.label" />
            </g:link>
            <g:link controller="stockMovement" action="create" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.button.create.label" />
            </g:link>
        </div>
        <div class="button-group">
            <g:link controller="stockMovement" action="edit" id="${stockMovement?.id}" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
                <warehouse:message code="default.button.edit.label" />
            </g:link>

            <%-- TODO  Move status to stock movement; make consistent across all types --%>
            <g:set var="hasBeenIssued" value="${stockMovement?.requisition?.status==RequisitionStatus.ISSUED}"/>
            <g:set var="hasBeenReceived" value="${stockMovement?.shipment?.currentStatus==ShipmentStatusCode.RECEIVED}"/>
            <g:set var="hasBeenPartiallyReceived" value="${stockMovement?.shipment?.currentStatus==ShipmentStatusCode.PARTIALLY_RECEIVED}"/>
            <g:set var="hasBeenShipped" value="${stockMovement?.shipment?.currentStatus==ShipmentStatusCode.SHIPPED}"/>
            <g:set var="hasBeenPending" value="${stockMovement?.shipment?.currentStatus==ShipmentStatusCode.PENDING}"/>
            <g:set var="hasBeenPlaced" value="${hasBeenShipped || hasBeenPartiallyReceived}"/>
            <g:set var="isFromOrder" value="${stockMovement?.isFromOrder}"/>
            <g:set var="isSameLocation" value="${stockMovement?.destination?.id==session.warehouse.id}"/>
            <g:set var="disableReceivingButton" value="${!(hasBeenIssued || (hasBeenPlaced && isFromOrder)) || !isSameLocation || !hasBeenPlaced || hasBeenReceived}"/>
            <g:set var="showRollbackLastReceiptButton" value="${hasBeenReceived || hasBeenPartiallyReceived}"/>
            <g:if test="${hasBeenReceived}">
                <g:set var="disabledMessage" value="${g.message(code:'stockMovement.hasAlreadyBeenReceived.message', args: [stockMovement?.identifier])}"/>
            </g:if>
            <g:elseif test="${!hasBeenPlaced && isFromOrder}">
                <g:set var="disabledMessage" value="${g.message(code:'stockMovement.hasNotBeenPlaced.message', args: [stockMovement?.identifier])}"/>
            </g:elseif>
            <g:elseif test="${!(hasBeenShipped || hasBeenPartiallyReceived)}">
                <g:set var="disabledMessage" value="${g.message(code:'stockMovement.hasNotBeenShipped.message', args: [stockMovement?.identifier])}"/>
            </g:elseif>
            <g:elseif test="${!hasBeenIssued}">
                <g:set var="disabledMessage" value="${g.message(code:'stockMovement.hasNotBeenIssued.message', args: [stockMovement?.identifier])}"/>
            </g:elseif>
            <g:elseif test="${!isSameLocation}">
                <g:set var="disabledMessage" value="${g.message(code:'stockMovement.isDifferentLocation.message')}"/>
            </g:elseif>
            <g:link controller="partialReceiving" action="create" id="${stockMovement?.shipment?.id}" class="button"
                    disabled="${disableReceivingButton}" disabledMessage="${disabledMessage}">
                <img src="${resource(dir: 'images/icons/', file: 'handtruck.png')}" />&nbsp;
                <warehouse:message code="default.button.receive.label" />
            </g:link>
            <g:isUserAdmin>
                <g:if test="${showRollbackLastReceiptButton}">
                    <g:link controller="partialReceiving" action="rollbackLastReceipt" id="${stockMovement?.shipment?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />&nbsp;
                        <warehouse:message code="stockMovement.rollbackLastReceipt.label" />
                    </g:link>
                </g:if>
                <g:elseif test="${hasBeenIssued || (hasBeenPlaced && isFromOrder)}">
                    <g:link controller="stockMovement" action="rollback" id="${stockMovement.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />&nbsp;
                        <warehouse:message code="default.button.rollback.label" />
                    </g:link>
                </g:elseif>
                <g:if test="${hasBeenPending || !stockMovement?.shipment?.currentStatus}">
                    <g:link controller="stockMovement" action="remove" id="${stockMovement.id}" params="[show:true]" class="button"
                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />&nbsp;
                        <warehouse:message code="default.button.delete.label" />
                    </g:link>
                </g:if>
            </g:isUserAdmin>
        </div>
    </div>
    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2><g:message code="default.details.label" /></h2>
                <div class="dialog">

                    <table>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.identifier.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.identifier}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.status.label"/>
                            </td>
                            <td class="value">
                                <format:metadata obj="${stockMovement?.shipment?.status?:stockMovement?.requisition?.status }"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.origin.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.origin?.name}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.destination.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.destination?.name}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.stocklist.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.stocklist?.name?:"N/A"}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.comments.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.comments?:g.message(code:"default.none.label")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.trackingNumber.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.trackingNumber?:g.message(code:"default.none.label")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.driverName.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.driverName?:g.message(code:"default.none.label")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="shipping.shipmentType.label"/>
                            </td>
                            <td class="value">
                                <format:metadata obj="${stockMovement?.shipmentType?.name}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="shipping.totalValue.label"/>
                            </td>
                            <td class="value">
                                <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: [g.message(code:'default.none.label')])}">
                                    <g:formatNumber format="###,###,##0.00" number="${stockMovement?.shipment?.calculateTotalValue() ?: 0.00 }" />
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </g:hasRoleFinance>
                            </td>
                        </tr>
                        <g:isSuperuser>
                            <g:if test="${stockMovement?.shipment?.orders}">
                                <tr class="prop">
                                    <td class="name">
                                        <g:message code="order.label"/>
                                    </td>
                                    <td class="value">
                                        <g:each var="order" in="${stockMovement?.shipment?.orders}">
                                            <g:link controller="order" action="show" id="${order?.id}" params="[override:true]">
                                                ${g.message(code:'default.view.label', args: [g.message(code: 'order.label')])}
                                                ${order.orderNumber}
                                            </g:link>
                                        </g:each>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${stockMovement.requisition}">
                                <tr class="prop">
                                    <td class="name">
                                        <g:message code="requisition.label"/>
                                    </td>
                                    <td class="value">
                                        <g:link controller="requisition" action="show" id="${stockMovement?.requisition?.id}" params="[override:true]">
                                            ${g.message(code:'default.view.label', args: [g.message(code: 'requisition.label')])}
                                            ${stockMovement?.requisition?.requestNumber}
                                        </g:link>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${stockMovement.shipment}">
                                <tr class="prop">
                                    <td class="name">
                                        <g:message code="shipment.label"/>
                                    </td>
                                    <td class="value">
                                        <g:link controller="shipment" action="showDetails" id="${stockMovement?.shipment?.id}" params="[override:true]">
                                            ${g.message(code:'default.view.label', args: [g.message(code: 'shipment.label')])}
                                            ${stockMovement?.shipment?.shipmentNumber}
                                        </g:link>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${stockMovement.shipment?.incomingTransactions}">
                                <tr class="prop">
                                    <td class="name">
                                        <g:message code="default.inbound.label"/>
                                    </td>
                                    <td class="value">
                                        <g:each var="inboundTransaction" in="${stockMovement?.shipment?.incomingTransactions}">
                                            <g:link controller="inventory" action="showTransaction" id="${inboundTransaction?.id}">
                                                ${g.message(code:'default.view.label', args: [g.message(code: 'transaction.label')])}
                                                ${inboundTransaction?.transactionNumber?:inboundTransaction?.id}
                                            </g:link>
                                        </g:each>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${stockMovement.shipment?.outgoingTransactions}">
                                <tr class="prop">
                                    <td class="name">
                                        <g:message code="default.outbound.label"/>
                                    </td>
                                    <td class="value">
                                        <g:each var="outboundTransaction" in="${stockMovement?.shipment?.outgoingTransactions}">
                                            <g:link controller="inventory" action="showTransaction" id="${outboundTransaction?.id}">
                                                ${g.message(code:'default.view.label', args: [g.message(code: 'transaction.label')])}
                                                ${outboundTransaction?.transactionNumber?:outboundTransaction?.id}
                                            </g:link>
                                        </g:each>
                                    </td>
                                </tr>
                            </g:if>
                        </g:isSuperuser>
                    </table>
                </div>
            </div>
            <div class="box">
                <h2><g:message code="default.auditing.label"/></h2>
                <div class="dialog">

                    <table>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.dateRequested.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.dateRequested}">
                                    <span title="${g.formatDate(date:stockMovement?.dateRequested)}">
                                        <g:formatDate format="MMMM dd, yyyy" date="${stockMovement.dateRequested}"/>
                                    </span>
                                    <g:if test="${stockMovement?.requestedBy}">
                                        <g:message code="default.by.label"/>
                                        ${stockMovement?.requestedBy?.name}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.dateShipped.label"/>
                                <g:if test="${stockMovement?.shipment?.status?.code==org.pih.warehouse.shipping.ShipmentStatusCode.PENDING}">
                                    <small><g:message code="default.expected.label"/></small>
                                </g:if>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.dateShipped}">
                                    <span title="${g.formatDate(date:stockMovement?.dateShipped)}">
                                        <g:formatDate format="MMMM dd, yyyy" date="${stockMovement?.dateShipped}"/>
                                    </span>
                                    <g:if test="${stockMovement?.shipment?.createdBy}">
                                        <g:message code="default.by.label"/>
                                        ${stockMovement?.shipment?.createdBy?.name}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="stockMovement.dateReceived.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.shipment?.receipts}">
                                    <g:each var="receipt" in="${stockMovement?.shipment?.receipts}">
                                        <span title="${g.formatDate(date:receipt?.actualDeliveryDate)}">
                                            <g:formatDate format="MMMM dd, yyyy" date="${receipt?.actualDeliveryDate}"/>
                                        </span>
                                        <g:if test="${receipt.recipient}">
                                            <g:message code="default.by.label"/>
                                            ${receipt.recipient?.name}
                                        </g:if>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <g:message code="default.none.label"/>
                                </g:else>

                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="default.dateCreated.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.dateCreated}">
                                    <span title="${g.formatDate(date:stockMovement?.dateCreated)}">
                                        <g:formatDate format="MMMM dd, yyyy" date="${stockMovement?.dateCreated}"/>
                                    </span>
                                    <g:if test="${stockMovement?.createdBy}">
                                        <g:message code="default.by.label"/>
                                        ${stockMovement?.createdBy?.name}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="default.lastUpdated.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.lastUpdated}">
                                    <span title="${g.formatDate(date:stockMovement?.lastUpdated)}">
                                        <g:formatDate format="MMMM dd, yyyy" date="${stockMovement?.lastUpdated}"/>
                                    </span>
                                    <g:if test="${stockMovement?.updatedBy}">
                                        <g:message code="default.by.label"/>
                                        ${stockMovement?.updatedBy?.name}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                    </table>

                </div>
            </div>
        </div>
        <div class="yui-u">
            <div class="tabs">
                <ul>
                    <g:if test="${!stockMovement?.origin?.isSupplier()}">
                        <li>
                            <a href="${request.contextPath}/stockMovement/requisition/${stockMovement?.id}">
                                <warehouse:message code="requestDetails.label"/>
                            </a>
                        </li>
                    </g:if>
                    <li>
                        <a href="${request.contextPath}/stockMovement/packingList/${stockMovement?.id}">
                            <warehouse:message code="shipping.packingList.label" />
                        </a>
                    </li>
                    <li>
                        <a href="${request.contextPath}/stockMovement/receipts/${stockMovement?.id}">
                            <warehouse:message code="receipts.label" default="Receipts"/>
                        </a>
                    </li>
                    <li>
                        <a href="${request.contextPath}/stockMovement/documents/${stockMovement?.id}">
                            <warehouse:message code="documents.label" default="Documents"/>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        $(".tabs").tabs({
            cookie : {
                expires : 1
            },
            selected: ${stockMovement?.shipment?.currentStatus >= ShipmentStatusCode.SHIPPED} ? 1 : 0
        });
    });
</script>

</body>
</html>
