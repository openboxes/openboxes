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

    <g:render template="summary" model="[shipmentInstance:stockMovement?.shipment, requisition: stockMovement?.requisition]"/>

    <div class="button-bar ">
        <g:if test="${stockMovement.documents}">
            <div class="right">
                <span class="action-menu">
                    <button class="action-btn button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page.png')}" style="vertical-align: middle" />
                        &nbsp; <g:message code="default.download.label"/>
                    </button>
                    <div class="actions">
                        <g:each var="document" in="${stockMovement.documents}">
                            <div class="action-menu-item">
                                <g:link url="${document.uri}" target="_blank">

                                    <img src="${createLinkTo(dir: 'images/icons/silk', file: 'page.png')}" class="middle"/>&nbsp;
                                    ${document.name}
                                </g:link>
                            </div>
                        </g:each>
                    </div>
                </span>

            </div>
        </g:if>

        <div class="button-group">

            <g:link controller="stockMovement" action="list" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'text_list_bullets.png')}" />&nbsp;
                <warehouse:message code="default.button.list.label" />
            </g:link>

            <g:link controller="stockMovement" action="index" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.button.create.label" />
            </g:link>

            <g:link controller="stockMovement" action="index" id="${stockMovement.id}" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
                <warehouse:message code="default.button.edit.label" />
            </g:link>
            <g:if test="${stockMovement?.requisition?.status==RequisitionStatus.ISSUED}">
                <g:link controller="partialReceiving" action="create" id="${stockMovement?.shipment?.id}" class="button">
                    <img src="${resource(dir: 'images/icons/', file: 'handtruck.png')}" />&nbsp;
                    <warehouse:message code="default.button.receive.label" />
                </g:link>
            </g:if>

            <g:isSuperuser>
                <g:link controller="stockMovement" action="delete" id="${stockMovement.id}" class="button"
                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                    <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" />&nbsp;
                    <warehouse:message code="default.button.delete.label" />
                </g:link>
            </g:isSuperuser>
        </div>
    </div>


<div class="yui-gf">
    <div class="yui-u first">
        <div class="box">
            <h2><g:message code="stockMovement.label" /></h2>
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
                            ${stockMovement?.shipmentType?.name}
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <warehouse:message code="shipping.totalValue.label"/>
                        </td>
                        <td class="value">
                            <g:formatNumber format="###,###,##0.00" number="${shipmentInstance?.totalValue ?: 0.00 }" />
                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                        </td>
                    </tr>
                    <g:isSuperuser>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="shipping.shipment.label"/>
                            </td>
                            <td class="value">
                                <g:link controller="shipment" action="showDetails" id="${stockMovement?.shipment?.id}" params="[override:true]">
                                    ${g.message(code:'default.view.label', args: [g.message(code: 'shipment.label')])}
                                </g:link>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <g:message code="requisition.label"/>
                            </td>
                            <td class="value">
                                <g:link controller="requisition" action="show" id="${stockMovement?.id}" params="[override:true]">
                                    ${g.message(code:'default.view.label', args: [g.message(code: 'requisition.label')])}
                                </g:link>
                            </td>
                        </tr>
                    </g:isSuperuser>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="stockMovement.dateRequested.label"/>
                        </td>
                        <td class="value">
                            <span title="${g.formatDate(date:stockMovement?.dateRequested)}">
                                <g:prettyDateFormat date="${stockMovement.dateRequested}"/>
                            </span>
                            <g:if test="${stockMovement?.requisition?.requestedBy}">
                                <g:message code="default.by.label"/>
                                ${stockMovement?.requisition?.requestedBy?.name}
                            </g:if>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="stockMovement.dateShipped.label"/>
                        </td>
                        <td class="value">
                            <span title="${g.formatDate(date:stockMovement?.dateShipped)}">
                                <g:prettyDateFormat date="${stockMovement.dateShipped}"/>
                            </span>
                            <g:if test="${stockMovement?.shipment?.createdBy}">
                                <g:message code="default.by.label"/>
                                ${stockMovement?.shipment?.createdBy?.name}
                            </g:if>

                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="stockMovement.dateReceived.label"/>
                        </td>
                        <td class="value">
                            <g:each var="receipt" in="${stockMovement?.shipment?.receipts}">
                                <span title="${g.formatDate(date:receipt?.actualDeliveryDate)}">
                                    <g:prettyDateFormat date="${receipt?.actualDeliveryDate}"/>
                                </span>
                                <g:if test="${receipt.recipient}">
                                    <g:message code="default.by.label"/>
                                    ${receipt.recipient?.name}
                                </g:if>
                            </g:each>

                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="default.dateCreated.label"/>
                        </td>
                        <td class="value">
                            <span title="${g.formatDate(date:stockMovement?.requisition?.dateCreated)}">
                                <g:prettyDateFormat date="${stockMovement?.requisition?.dateCreated}"/>
                            </span>
                            <g:if test="${stockMovement?.requisition?.createdBy}">
                                <g:message code="default.by.label"/>
                                ${stockMovement?.requisition?.createdBy?.name}
                            </g:if>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <g:message code="default.lastUpdated.label"/>
                        </td>
                        <td class="value">
                            <span title="${g.formatDate(date:stockMovement?.requisition?.lastUpdated)}">
                                <g:prettyDateFormat date="${stockMovement?.requisition?.lastUpdated}"/>
                            </span>
                            <g:message code="default.by.label"/>
                            ${stockMovement?.requisition?.updatedBy.name}
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
                                    <th><g:message code="product.productCode.label"/></th>
                                    <th><g:message code="product.label"/></th>
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
                                        <g:link controller="inventoryItem" action="showStockCard" id=${stockMovementItem?.product?.id}">
                                            ${stockMovementItem?.product?.productCode}
                                        </g:link>
                                    </td>
                                    <td>
                                        <g:link controller="inventoryItem" action="showStockCard" id="${stockMovementItem?.product?.id}">
                                            ${stockMovementItem?.product?.name}
                                        </g:link>
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
                        <h2><warehouse:message code="documents.label"/></h2>
                        <table>
                            <tr>
                                <th></th>
                                <th><g:message code="document.name.label"/></th>
                                <th><g:message code="documentType.label"/></th>
                                <th><g:message code="document.contentType.label"/></th>
                                <th></th>
                            </tr>
                            <g:each var="document" in="${stockMovement.documents}" status="i">
                                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                    <td>
                                        <g:set var="f" value="${document?.contentType}"/>
                                        <g:if test="${f?.endsWith('jpg')||f?.endsWith('png')||f?.endsWith('gif') }">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'picture.png')}"/>
                                        </g:if>
                                        <g:elseif test="${f?.endsWith('pdf') }">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_acrobat.png')}"/>
                                        </g:elseif>
                                        <g:elseif test="${f?.endsWith('document')||f?.endsWith('msword') }">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_word.png')}"/>
                                        </g:elseif>
                                        <g:elseif test="${f?.endsWith('excel')||f?.endsWith('sheet')||f?.endsWith('csv') }">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}"/>
                                        </g:elseif>
                                        <g:elseif test="${f?.endsWith('html')}">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'html.png')}"/>
                                        </g:elseif>
                                        <g:elseif test="${f?.endsWith('gzip')||f?.endsWith('jar')||f?.endsWith('zip')||f?.endsWith('tar') }">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_compressed.png')}"/>
                                        </g:elseif>
                                        <g:else>
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white.png')}"/>
                                        </g:else>
                                    </td>
                                    <td>${document.name}</td>
                                    <td>${document.documentType}</td>
                                    <td>${document.contentType}</td>
                                    <td>
                                        <g:link url="${document.uri}" target="_blank" class="button">
                                            <warehouse:message code="default.button.download.label"/>
                                        </g:link>
                                    </td>
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