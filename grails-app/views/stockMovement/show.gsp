<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
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

    <div class="summary">
        <div class="tag tag-alert right">
            <format:metadata obj="${stockMovement?.requisition?.status }"/>
        </div>

        <div class="title">
            ${entityName} &rsaquo; ${stockMovement?.identifier} ${stockMovement?.name}
        </div>

    </div>

    <div class="button-bar ">

        <%--
            <g:if test="${stockMovement.id}">
                <div class="right">
                    <div class="button-group">
                        <g:link controller="picklist" action="renderPdf" id="${stockMovement?.id}" target="_blank" class="button">
                            <img src="${resource(dir: 'images/icons', file: 'pdf.png')}" />&nbsp;
                            ${warehouse.message(code: 'picklist.button.print.label', default: 'Download pick list')}
                        </g:link>
                        <g:link controller="picklist" action="print" id="${stockMovement?.id}" target="_blank" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                            ${warehouse.message(code: 'picklist.button.print.label', default: 'Print pick list')}
                        </g:link>
                        <g:link controller="deliveryNote" action="print" id="${stockMovement?.id}" target="_blank" class="button">
                            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                            ${warehouse.message(code: 'deliveryNote.button.print.label', default: 'Print delivery note')}
                        </g:link>
                    </div>
                </div>
            </g:if>
        --%>

        <div class="button-group">
            <g:link controller="stockMovement" action="list" class="button icon arrowleft">
                <warehouse:message code="default.list.label" args="[g.message(code: 'stockMovements.label')]"/>
            </g:link>
        </div>
        <div class="button-group">
            <g:link controller="stockMovement" action="index" class="button icon add">
                <warehouse:message code="default.create.label" args="[g.message(code: 'stockMovement.label')]"/>
            </g:link>
        </div>

        <div class="button-group">
            <g:link controller="stockMovement" action="index" id="${stockMovement.id}" class="button icon edit">
                <warehouse:message code="default.edit.label" args="[g.message(code: 'stockMovement.label')]"/>
            </g:link>
        </div>

        <g:if test="${stockMovement?.requisition?.status==RequisitionStatus.ISSUED}">
            <div class="button-group">
                <g:link controller="partialReceiving" action="create" id="${stockMovement?.shipment?.id}" class="button icon approve">
                    <warehouse:message code="default.receive.label" args="[g.message(code: 'stockMovement.label')]"/>
                </g:link>
            </div>
        </g:if>

        <g:isSuperuser>
            <div class="button-group">
                <g:link controller="stockMovement" action="delete" id="${stockMovement.id}" class="button icon remove"
                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                    <warehouse:message code="default.delete.label" args="[g.message(code: 'stockMovement.label')]"/>
                </g:link>
            </div>
        </g:isSuperuser>


    </div>


<div class="yui-gf">
    <div class="yui-u first">
        <div class="box">
            <h2><g:message code="default.header.label" default="Header"/></h2>
            <div>

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
                            <format:metadata obj="${stockMovement?.status}"/>
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
                            ${stockMovement?.shipmentType?.name}
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="shipping.shipment.label"/>
                        </td>
                        <td class="value">
                            <g:link controller="shipment" action="showDetails" id="${stockMovement?.shipment?.id}">
                                ${g.message(code:'default.view.label', args: [g.message(code: 'shipment.label')])}
                            </g:link>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="requisition.label"/>
                        </td>
                        <td class="value">
                            <g:link controller="requisition" action="show" id="${stockMovement?.id}">
                                ${g.message(code:'default.view.label', args: [g.message(code: 'requisition.label')])}
                            </g:link>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="stockMovement.dateRequested.label"/>
                        </td>
                        <td class="value">
                            <g:formatDate date="${stockMovement.dateRequested}"/>

                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="stockMovement.dateShipped.label"/>
                        </td>
                        <td class="value">
                            <g:formatDate date="${stockMovement.dateShipped}"/>

                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="default.dateCreated.label"/>
                        </td>
                        <td class="value">
                            <g:formatDate date="${stockMovement?.requisition?.dateCreated}"/>

                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="default.lastUpdated.label"/>
                        </td>
                        <td class="value">
                            <g:formatDate date="${stockMovement?.requisition?.lastUpdated}"/>
                        </td>
                    </tr>
                </table>

            </div>
        </div>
    </div>
    <div class="yui-u">

        <div class="tabs">
            <ul>
                <li>
                    <a href="#details-tab">
                        <warehouse:message code="requisition.label"/>
                    </a>
                </li>
                <li>
                    <a href="${request.contextPath}/stockMovement/shipments/${stockMovement?.id}">
                        <warehouse:message code="shipments.label" default="Shipments"/>
                    </a>
                </li>
                <li>
                    <a href="${request.contextPath}/stockMovement/receipts/${stockMovement?.id}">
                        <warehouse:message code="receipts.label" default="Receipts"/>
                    </a>
                </li>
                <li>
                    <a href="#documents-tab">
                        <warehouse:message code="documents.label" default="Documents"/>
                    </a>
                </li>
                <%--
                <li>
                    <a href="#comments-tab">
                        <warehouse:message code="comments.label" default="Comments"/>
                    </a>
                </li>
                <li>
                    <a href="#events-tab">
                        <warehouse:message code="events.label" default="Comments"/>
                    </a>
                </li>
                <li>
                    <a href="${request.contextPath}/stockMovement/transactions/${stockMovement?.id}">
                        <warehouse:message code="transactions.label" default="Transactions"/>
                    </a>
                </li>
                --%>
                </ul>
                <div id="details-tab">
                    <div class="box">
                        <h2><warehouse:message code="requisition.label"/></h2>

                        <div>
                            <table>

                                <tr>
                                    <th></th>
                                    <th><g:message code="default.status.label"/></th>
                                    <th><g:message code="product.label"/></th>
                                    <th><g:message code="product.uom.label"/></th>
                                    <th width="1%"><g:message code="stockMovement.quantityRequested.label"/></th>
                                </tr>

                                <g:each var="stockMovementItem" in="${stockMovement.lineItems}" status="i">
                                <g:set var="requisitionItem" value="${stockMovementItem?.requisitionItem}"/>
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                    <td class="center">
                                        ${i+1}
                                    </td>
                                    <td class="">
                                        <div class="tag tag-success">
                                            <g:if test="${requisitionItem?.isSubstituted()}">
                                                <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"/>
                                            </g:if>
                                            <g:elseif test="${requisitionItem?.isSubstitution()}">
                                                <img src="${resource(dir:'images/icons',file:'indent.gif')}"/>
                                            </g:elseif>
                                            <g:elseif test="${requisitionItem?.isChanged()}">
                                                <img src="${resource(dir:'images/icons/silk',file:'pencil.png')}"/>
                                            </g:elseif>
                                            <g:elseif test="${requisitionItem?.isCanceled()}">
                                                <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
                                            </g:elseif>
                                            <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
                                                <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"/>
                                            </g:elseif>
                                            <g:elseif test="${requisitionItem?.isPending()}">
                                                <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
                                            </g:elseif>
                                            <format:metadata obj="${requisitionItem?.status}"/>
                                        </div>
                                    </td>
                                    <td>
                                        ${stockMovementItem?.product?.productCode} ${stockMovementItem?.product?.name}
                                    </td>
                                    <td class="center">
                                        ${stockMovementItem?.quantityRequested?:0}
                                        ${stockMovementItem.product?.unitOfMeasure?:g.message(code:'default.each.label')}
                                    </td>

                                </tr>
                                </g:each>


                            </table>

                        </div>
                    </div>
                </div>
                <div id="documents-tab">
                    <div class="box">
                        <table>
                            <tr>
                                <th><g:message code="document.label"/></th>
                            </tr>
                            <g:each var="document" in="${stockMovement.documents}" status="i">
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                    <td><g:link url="${document.uri}" target="_blank">${document.name}</g:link></td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        $(".tabs").tabs({
            cookie : {
                expires : 1
            }
        });
    });
</script>

</body>
</html>