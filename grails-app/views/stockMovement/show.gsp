<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.core.Role" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.requisition.RequisitionSourceType" %>
<%@ page import="org.pih.warehouse.inventory.StockMovementStatusCode" %>
<%@ page import="org.pih.warehouse.core.ActivityCode"%>
<%@ page import="org.pih.warehouse.core.Constants" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'badge.css')}" type="text/css" />
    <g:set var="entityName" value="${warehouse.message(code: 'stockMovement.label', default: 'Stock Movement')}" />
    <title>
        <warehouse:message code="stockMovement.label"/>
    </title>
</head>
<body>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message" role="status" aria-label="message">${flash.message}</div>
    </g:if>
    <g:if test="${flash.error}">
        <div class="error">${flash.error}</div>
    </g:if>

    <g:render template="summary" model="[shipmentInstance:stockMovement?.shipment, requisition: stockMovement?.requisition]"/>

    <div class="button-bar ">
        <g:if test="${stockMovement?.documents}">
            <div class="right">
                <div class="button-group">
                    <g:if test="${!stockMovement.isReturn}">
                        <g:link controller="stockMovement" action="addComment" id="${stockMovement?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />&nbsp;
                            <warehouse:message code="requisition.addComment.label" default="Add comment" />
                        </g:link>
                    </g:if>
                    <g:link controller="stockMovement" action="addDocument" class="button" id="${stockMovement?.id}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />&nbsp;
                        <warehouse:message code="stockMovement.uploadDocuments.label" />
                    </g:link>
                    <span class="action-menu">
                        <button class="action-btn button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'page_save.png')}" />
                            &nbsp; <warehouse:message code="default.button.download.label"/>
                            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                        </button>
                        <div class="actions">
                            <g:each var="document" in="${stockMovement?.documents}">
                                <g:if test="${!document.hidden}">
                                    <div class="action-menu-item">
                                        <g:if test="${document.downloadOptions}">
                                            <g:each var="downloadOption" in="${document.downloadOptions}">
                                                <g:link url="${downloadOption.uri}" target="_blank">
                                                    <img src="${resource(dir: 'images/icons/silk', file: 'page.png')}" class="middle"/>&nbsp;
                                                    ${downloadOption.name}
                                                </g:link>
                                            </g:each>
                                        </g:if>
                                        <g:else>
                                            <g:link url="${document.uri}" target="_blank">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'page.png')}" class="middle"/>&nbsp;
                                                ${document.name}
                                            </g:link>
                                        </g:else>
                                    </div>
                                </g:if>
                            </g:each>
                        </div>
                    </span>
                </div>
            </div>
        </g:if>

        <div class="button-group">
            <g:hideIfIsNonInventoryManagedAndCanSubmitRequest>
                <g:link
                    controller="stockMovement"
                    action="list"
                    class="button"
                    params="[direction: stockMovement?.destination == currentLocation ? 'INBOUND' : 'OUTBOUND']"
                >
                    <img src="${resource(dir: 'images/icons/silk', file: 'text_list_bullets.png')}" />&nbsp;
                    <warehouse:message code="default.button.list.label" />
                </g:link>
            </g:hideIfIsNonInventoryManagedAndCanSubmitRequest>
            <g:link controller="stockMovement" action="create" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.button.create.label" />
            </g:link>
        </div>
        <div class="button-group">
            <%-- TODO  Move status to stock movement; make consistent across all types --%>
            <g:set var="hasBeenPlaced" value="${stockMovement?.hasBeenShipped() || stockMovement?.hasBeenPartiallyReceived()}"/>
            <g:set var="isSameOrigin" value="${stockMovement?.origin?.id==session.warehouse.id}"/>
            <g:set var="isSameDestination" value="${stockMovement?.destination?.id==session.warehouse.id}"/>
            <g:set var="userHasRequestApproverRole" value="${false}"/>
            <g:isUserInAllRoles location="${stockMovement?.origin?.id}" roles="${[RoleType.ROLE_REQUISITION_APPROVER]}">
                <g:set var="userHasRequestApproverRole" value="${true}"/>
            </g:isUserInAllRoles>
            <g:set var="isApprovalRequired" value="${stockMovement?.isApprovalRequired()}"/>
            <g:if test="${!isApprovalRequired}">
                <g:if test="${stockMovement?.order}">
                    <g:link controller="stockTransfer" action="edit" id="${stockMovement?.order?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
                        <warehouse:message code="default.button.edit.label" />
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="stockMovement" action="edit" id="${stockMovement?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
                        <warehouse:message code="default.button.edit.label" />
                    </g:link>
                </g:else>
                <g:link controller="partialReceiving" action="create" id="${stockMovement?.shipment?.id}" class="button">
                    <img src="${resource(dir: 'images/icons/', file: 'handtruck.png')}" />&nbsp;
                    <warehouse:message code="default.button.receive.label" />
                </g:link>

                <g:if test="${grailsApplication.config.openboxes.receiving.showAutoReceiveButton}">
                    <g:link controller="partialReceiving" action="autoreceive" id="${stockMovement?.shipment?.id}" class="button">
                        <img src="${resource(dir: 'images/icons/', file: 'handtruck.png')}" />&nbsp;
                        <warehouse:message code="default.button.autoreceive.label" />
                    </g:link>
                </g:if>

                <g:isUserAdmin>
                    <g:if test="${stockMovement?.hasBeenReceived() || stockMovement?.hasBeenPartiallyReceived()}">
                        <g:link controller="partialReceiving" action="rollbackLastReceipt" id="${stockMovement?.shipment?.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_rotate_anticlockwise.png')}" />&nbsp;
                            <warehouse:message code="stockMovement.rollbackLastReceipt.label" />
                        </g:link>
                    </g:if>
                    <g:elseif test="${stockMovement?.hasBeenIssued() || ((stockMovement?.hasBeenShipped() ||
                            stockMovement?.hasBeenPartiallyReceived()) && stockMovement?.isFromOrder)}">
                        <g:link controller="stockMovement" action="rollback" id="${stockMovement.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_rotate_anticlockwise.png')}" />&nbsp;
                            <warehouse:message code="default.button.rollback.label" />
                        </g:link>
                    </g:elseif>
                </g:isUserAdmin>
                    <g:set var="isPending" value="${stockMovement?.isPending() || !stockMovement?.shipment?.currentStatus}" />
                    <g:set var="originIsDepot" value="${stockMovement?.origin?.isDepot()}" />
                    <g:if test="${isPending && (isSameOrigin || !originIsDepot) && !stockMovement?.electronicType}">
                        <g:if test="${stockMovement?.order}">
                            <g:isUserAdmin>
                                <g:link class="button" controller="stockTransfer" action="remove" id="${stockMovement?.id}" params="[orderId: stockMovement?.order?.id]"
                                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />
                                    &nbsp;<warehouse:message code="default.button.delete.label" />
                                </g:link>
                            </g:isUserAdmin>
                        </g:if>
                        <g:else>
                            <g:link controller="stockMovement" action="remove" id="${stockMovement.id}" params="[show:true]" class="button"
                                    onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                    disabledMessage="You do not have minimum required role to delete stock movement"
                            >
                                <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />&nbsp;
                                <warehouse:message code="default.button.delete.label" />
                            </g:link>
                        </g:else>
                    </g:if>
                    <g:if test="${isPending && (isSameOrigin || isSameDestination || !originIsDepot) && stockMovement?.electronicType}">
                        <g:link controller="stockRequest" action="remove" id="${stockMovement.id}" params="[show:true]" class="button"
                                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                disabledMessage="You do not have minimum required role to delete stock request"
                        >
                            <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />&nbsp;
                            <warehouse:message code="default.button.delete.label" />
                        </g:link>
                    </g:if>

                <g:isSuperuser>
                    <a href="javascript:void(0);" class="button btn-show-dialog"
                        data-height="600" data-width="1000"
                       data-title="${warehouse.message(code:'default.button.synchronize.label', default: 'Synchronize')}"
                       data-url="${request.contextPath}/stockMovement/synchronizeDialog/${stockMovement?.id}">
                        <img src="${resource(dir: 'images/icons/silk', file: 'arrow_join.png')}" />&nbsp;
                        <warehouse:message code="default.button.synchronize.label" default="Synchronize"/>
                    </a>
                </g:isSuperuser>
            </g:if>
            <g:if test="${isApprovalRequired}">
                <g:if test="${stockMovement?.canUserEdit(session?.user?.id, currentLocation)}">
                    <g:link
                            class="button"
                            controller="stockMovement"
                            action="edit"
                            id="${stockMovement?.id}"
                    >
                        <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
                        <warehouse:message code="default.button.edit.label" />
                    </g:link>
                </g:if>
                <g:supports location="${stockMovement.origin?.id}" activityCode="${ActivityCode.APPROVE_REQUEST}">
                    <g:if test="${stockMovement?.requisition?.status == RequisitionStatus.PENDING_APPROVAL && userHasRequestApproverRole}">
                        <g:link
                                class="button"
                                controller="stockMovement"
                                action="updateStatus"
                                id="${stockMovement.id}"
                                params="[status: StockMovementStatusCode.APPROVED]"
                                disabled="${!stockMovement.pendingApproval}"
                                disabledMessage="Request is not pending approval"
                        >
                            <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}" />&nbsp;
                            <g:message code="request.approval.approve.label"  default="Approve" />
                        </g:link>
                        <g:link
                                class="button"
                                controller="stockRequest"
                                action="reject"
                            id="${stockMovement.id}"
                                disabled="${!stockMovement.pendingApproval}"
                                disabledMessage="Request is not pending approval"
                        >
                            <img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}" />&nbsp;
                            <g:message code="request.approval.reject.label"  default="Reject" />
                        </g:link>
                    </g:if>

                    <g:if test="${stockMovement?.canRollbackApproval(session?.user?.id, currentLocation)}" >
                        <g:link controller="stockRequest" action="rollbackApproval" id="${stockMovement.id}" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}" />&nbsp;
                            <g:message code="request.approval.rollback.label"  default="Rollback Approval" />
                        </g:link>
                    </g:if>
                </g:supports>
            </g:if>
        </div>
    </div>
    <div class="yui-gf">
        <div class="yui-u first">
            <aside class="box" aria-label="Details">
                <h2><warehouse:message code="default.details.label" /></h2>
                <div class="dialog">

                    <table>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.identifier.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.identifier}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.status.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.displayStatus?.label}
                            </td>
                        </tr>
                        <g:if test="${stockMovement?.shipment?.isFromPurchaseOrder}">
                            <tr class="prop">
                                <td class="name">
                                    <g:message code="stockMovement.originCode.label"/>
                                </td>
                                <td class="value">
                                    ${stockMovement?.origin?.organization?.code}
                                </td>
                            </tr>
                        </g:if>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.origin.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.origin?.name}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.destination.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.destination?.name}
                            </td>
                        </tr>
                        <g:if test="${session.warehouse == stockMovement?.origin}">
                            <tr class="prop">
                                <td class="name">
                                    <warehouse:message code="stockMovement.requestType.label"/>
                                </td>
                                <td class="value">
                                    <format:metadata obj="${stockMovement?.requestType}"/>
                                </td>
                            </tr>
                        </g:if>
                        <g:if test="${stockMovement?.isElectronicType()}">
                            <tr class="prop">
                                <td class="name">
                                    <g:message
                                            code="stockMovement.desiredDateOfDelivery"
                                            default="Desired date of delivery"
                                    />
                                </td>
                                <td class="value">
                                    <g:if test="${stockMovement?.requisition?.dateDeliveryRequested}">
                                        <g:formatDate
                                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                date="${stockMovement?.requisition?.dateDeliveryRequested}"
                                        />
                                    </g:if>
                                    <g:else>
                                        <warehouse:message code="default.none.label" />
                                    </g:else>
                                </td>
                            </tr>
                        </g:if>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.stocklist.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.stocklist?.name?:warehouse.message(code:"default.none.label")}
                            </td>
                        </tr>
                        <g:if test="${stockMovement?.requisition?.approvers}">
                            <g:set var="approversList" value="${stockMovement?.requisition?.approvers?.join(', ')}" />
                            <tr class="prop">
                                <td class="name">
                                    <warehouse:message code="stockMovement.approvers.label"/>
                                </td>
                                <td class="value">
                                    ${approversList}
                                </td>
                            </tr>
                        </g:if>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.comments.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.comments?:warehouse.message(code:"default.none.label")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.trackingNumber.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.trackingNumber?:warehouse.message(code:"default.none.label")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.driverName.label"/>
                            </td>
                            <td class="value">
                                ${stockMovement?.driverName?:warehouse.message(code:"default.none.label")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="shipping.shipmentType.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.shipmentType}">
                                    <format:metadata obj="${stockMovement?.shipmentType?.name}"/>
                                </g:if>
                                <g:else>
                                    ${warehouse.message(code:"default.none.label")}
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="shipping.totalValue.label"/>
                            </td>
                            <td class="value">
                                <g:hasRoleFinance onAccessDenied="${warehouse.message(code:'errors.blurred.message', args: [warehouse.message(code:'default.none.label')])}">
                                    <g:formatNumber format="###,###,##0.00" number="${stockMovement?.shipment?.calculateTotalValue() ?: 0.00 }" />
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </g:hasRoleFinance>
                            </td>
                        </tr>
                        <g:if test="${stockMovement?.shipment?.orders}">
                            <tr class="prop">
                                <td class="name">
                                    <warehouse:message code="order.label"/>
                                </td>
                                <td class="value">
                                    <g:each var="order" in="${stockMovement?.shipment?.orders}">
                                        <g:link controller="order" action="show" id="${order?.id}" params="[override:true]">
                                            ${warehouse.message(code:'default.view.label', args: [warehouse.message(code: 'order.label')])}
                                            ${order.orderNumber}
                                        </g:link>
                                    </g:each>
                                </td>
                            </tr>
                        </g:if>
                        <g:isSuperuser>
                            <g:if test="${stockMovement.requisition}">
                                <tr class="prop">
                                    <td class="name">
                                        <warehouse:message code="requisition.label"/>
                                    </td>
                                    <td class="value">
                                        <g:link controller="requisition" action="show" id="${stockMovement?.requisition?.id}" params="[override:true]">
                                            ${warehouse.message(code:'default.view.label', args: [warehouse.message(code: 'requisition.label')])}
                                            ${stockMovement?.requisition?.requestNumber}
                                        </g:link>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${stockMovement.shipment}">
                                <tr class="prop">
                                    <td class="name">
                                        <warehouse:message code="shipment.label"/>
                                    </td>
                                    <td class="value">
                                        <g:link controller="shipment" action="showDetails" id="${stockMovement?.shipment?.id}" params="[override:true]">
                                            ${warehouse.message(code:'default.view.label', args: [warehouse.message(code: 'shipment.label')])}
                                            ${stockMovement?.shipment?.shipmentNumber}
                                        </g:link>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${stockMovement.shipment?.incomingTransactions}">
                                <tr class="prop">
                                    <td class="name">
                                        <warehouse:message code="default.inbound.label"/>
                                    </td>
                                    <td class="value">
                                        <g:each var="inboundTransaction" in="${stockMovement?.shipment?.incomingTransactions}">
                                            <div>
                                                <g:link controller="inventory" action="showTransaction" id="${inboundTransaction?.id}">
                                                    ${warehouse.message(code:'default.view.label', args: [warehouse.message(code: 'transaction.label')])}
                                                    ${inboundTransaction?.transactionNumber?:inboundTransaction?.id}
                                                </g:link>
                                            </div>
                                        </g:each>
                                    </td>
                                </tr>
                            </g:if>
                            <g:if test="${stockMovement.shipment?.outgoingTransactions}">
                                <tr class="prop">
                                    <td class="name">
                                        <warehouse:message code="default.outbound.label"/>
                                    </td>
                                    <td class="value">
                                        <g:each var="outboundTransaction" in="${stockMovement?.shipment?.outgoingTransactions}">
                                            <div>
                                                <g:link controller="inventory" action="showTransaction" id="${outboundTransaction?.id}">
                                                    ${warehouse.message(code:'default.view.label', args: [warehouse.message(code: 'transaction.label')])}
                                                    ${outboundTransaction?.transactionNumber?:outboundTransaction?.id}
                                                </g:link>
                                            </div>
                                        </g:each>
                                    </td>
                                </tr>
                            </g:if>
                        </g:isSuperuser>
                    </table>
                </div>
            </aside>
            <aside class="box" aria-label="Auditing">
                <h2><warehouse:message code="default.auditing.label"/></h2>
                <div class="dialog">

                    <table>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.dateRequested.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.dateRequested}">
                                    <span title="${g.formatDate(date:stockMovement?.dateRequested)}">
                                        <g:formatDate
                                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                date="${stockMovement.dateRequested}"
                                        />
                                    </span>
                                    <g:if test="${stockMovement?.requestedBy}">
                                        <warehouse:message code="default.by.label"/>
                                        ${stockMovement?.requestedBy?.name}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>

                        <g:if test="${stockMovement?.requisition?.approvedBy || stockMovement?.requisition?.rejectedBy}">
                            <g:set var="person" value="${stockMovement?.requisition?.approvedBy ?: stockMovement?.requisition?.rejectedBy}" />
                            <g:set var="date" value="${stockMovement?.requisition?.dateApproved ?: stockMovement?.requisition?.dateRejected}" />
                            <g:set var="label" value="${stockMovement?.requisition?.dateApproved ? 'dateApproved' : 'dateRejected'}" />
                            <tr class="prop">
                                <td class="name">
                                    <g:message code="stockMovement.${label}.label"/>
                                </td>
                                <td class="value">
                                    <g:if test="${date && person}">
                                        <span title="${g.formatDate(date: date)}">
                                            <g:formatDate format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}" date="${date}"/>
                                        </span>
                                        <g:message code="default.by.label"/>
                                        ${person}
                                    </g:if>
                                    <g:else>
                                        <g:message code="default.none.label"/>
                                    </g:else>
                                </td>
                            </tr>
                        </g:if>

                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.dateShipped.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.shipment?.hasShipped()}">
                                    <span title="${g.formatDate(date: stockMovement?.dateShipped, format: Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT)}">
                                        <g:formatDate
                                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                date="${stockMovement?.dateShipped}"
                                        />
                                    </span>
                                    <g:if test="${stockMovement?.shipment?.shippedBy}">
                                        <warehouse:message code="default.by.label"/>
                                        ${stockMovement?.shipment?.shippedBy}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="stockMovement.dateReceived.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.shipment?.receipts}">
                                    <g:each var="receipt" in="${stockMovement?.shipment?.receipts}">
                                        <span title="${g.formatDate(date: receipt?.actualDeliveryDate, format: Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT)}">
                                            <g:formatDate
                                                    format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                    date="${receipt?.actualDeliveryDate}"
                                            />
                                        </span>
                                        <g:if test="${receipt.recipient}">
                                            <warehouse:message code="default.by.label"/>
                                            ${receipt.recipient?.name}
                                        </g:if>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="default.none.label"/>
                                </g:else>

                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="default.dateCreated.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.dateCreated}">
                                    <span title="${g.formatDate(date: stockMovement?.dateCreated, format: Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT)}">
                                        <g:formatDate
                                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                date="${stockMovement?.dateCreated}"
                                        />
                                    </span>
                                    <g:if test="${stockMovement?.createdBy}">
                                        <warehouse:message code="default.by.label"/>
                                        ${stockMovement?.createdBy?.name}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <warehouse:message code="default.lastUpdated.label"/>
                            </td>
                            <td class="value">
                                <g:if test="${stockMovement?.lastUpdated}">
                                    <span title="${g.formatDate(date: stockMovement?.lastUpdated, format: Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT)}">
                                        <g:formatDate
                                                format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                                date="${stockMovement?.lastUpdated}"
                                        />
                                    </span>
                                    <g:if test="${stockMovement?.updatedBy}">
                                        <warehouse:message code="default.by.label"/>
                                        ${stockMovement?.updatedBy?.name}
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="default.none.label"/>
                                </g:else>
                            </td>
                        </tr>
                    </table>

                </div>
            </aside>
        </div>
        <div class="yui-u">
            <div class="tabs">
                <ul aria-label="tabs">
                    <g:if test="${!stockMovement?.origin?.isSupplier()}">
                        <li role="tab">
                            <g:link controller="stockMovement" action="requisition" params="${[ id: stockMovement?.id ]}">
                                <g:message code="requestDetails.label"/>
                            </g:link>
                        </li>
                    </g:if>
                    <li role="tab">
                        <g:link controller="stockMovement" action="packingList" params="${[ id: stockMovement?.id ]}">
                            <g:message code="shipping.packingList.label" />
                        </g:link>
                    </li>
                    <li role="tab">
                        <g:link controller="stockMovement" action="receipts" params="${[ id: stockMovement?.id ]}">
                            <g:message code="receipts.label" default="Receipts"/>
                        </g:link>
                    </li>
                    <li role="tab">
                        <g:link controller="stockMovement" action="events" params="${[ id: stockMovement?.id ]}">
                            <g:message code="events.label" default="Event"/>
                        </g:link>
                    </li>
                    <li role="tab">
                        <g:link controller="stockMovement" action="documents" params="${[ id: stockMovement?.id ]}">
                            <g:message code="documents.label" default="Documents"/>
                        </g:link>
                    </li>
                    <g:if test="${!stockMovement.isReturn}">
                        <g:set var="comments" value="${stockMovement?.requisition?.comments ?: stockMovement?.shipment?.comments}" />
                        <g:set var="commentCount" value="${comments?.size()}" />
                        <li
                            role="tab"
                            data-count="${commentCount < 1000 ? commentCount : '999+' }"
                            class="${commentCount > 0 ? 'tab-badge' : ''}"
                        >
                            <g:link controller="stockMovement" action="comments" params="${[ id: stockMovement?.id ]}">
                                <g:message code="comments.label" default="Comments"/>
                            </g:link>
                        </li>
                    </g:if>
                    <li role="tab">
                          <g:link controller="stockMovement"
                                  action="schedule"
                                  id="${stockMovement?.id}"
                                  params="[id: stockMovement?.id]">
                            <warehouse:message
                              code="stockMovement.schedule.label"
                              default="Scheduling"/>
                          </g:link>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function() {

      const stockMovementDirection = ${stockMovement?.destination?.id == currentLocation?.id} ? 'inbound' : 'outbound';
      applyActiveSection(stockMovementDirection);

        $(".tabs").tabs({
            cookie : {
                expires : 1
            },
            selected: ${(stockMovement?.shipment?.currentStatus == ShipmentStatusCode.PENDING && stockMovement?.origin?.id == session.warehouse.id) || stockMovement?.origin?.isSupplier()} ? 0 : 1
        });
    });
</script>

</body>
</html>
