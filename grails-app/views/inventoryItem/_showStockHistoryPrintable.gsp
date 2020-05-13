<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<html>
<head>
<style>
    .print-history {
        display: inline-block;
    }
    .noBorder {
        border:none !important;
    }
</style>
</head>

<body>
<div>
    <table>
        <thead>
            <td class="noBorder">
                <div id="product-header" class="divHeader">
                    <div id="product-title" class="title">
                        <small>${commandInstance?.product?.productCode }</small>
                        <g:link controller="inventoryItem" action="showStockCard" params="['product.id': commandInstance?.product?.id]">
                            ${commandInstance?.product?.name }
                        </g:link>
                    </div>
                    <div id="product-catalogs">
                        <g:each var="productCatalog" in="${commandInstance?.product?.productCatalogs }">
                            <g:link controller="inventory" action="browse" params="['catalogs':productCatalog.id]">
                                <span class="tag tag-info" title="${g.message(code: 'productCatalog.label')}">${productCatalog.name }</span>
                            </g:link>
                        </g:each>
                    </div>
                    <div id="product-tags">
                        <g:each var="tag" in="${commandInstance?.product?.tags }">
                            <g:link controller="inventory" action="browse" params="['tags':tag.id]">
                                <span class="tag tag-success" title="${g.message(code: 'tag.label', default: 'Tag')}">${tag.tag }</span>
                            </g:link>
                        </g:each>
                    </div>
                </div>
            </td>
        </thead>
        <tbody style="margin: 0; padding: 0;">
            <td class="noBorder">
                <h2>
                    <div>
                        <warehouse:message code="inventory.stockHistory.label"/>
                        <div class="print-history">
                            <g:link controller="inventoryItem" action="showStockHistory" params="[print:true]" id="${commandInstance.product.id}" class="button">
                                <img src="${createLinkTo(dir:'images/icons',file:'pdf.png')}" />
                                ${warehouse.message(code: 'inventory.exportPdf.label', default: 'Export to PDF')}
                            </g:link>
                        </div>
                    </div>
                </h2>
                <table class="stockHistory">
                    <thead>
                    <tr class="odd">
                        <th>

                        </th>
                        <th>
                            ${warehouse.message(code: 'default.date.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'default.time.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'default.createdBy.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'transaction.label')}
                        </th>
                        <th>
                            ${warehouse.message(code: 'default.originOrDestination.label', default: "Origin / Destination")}
                        </th>
                        <th class="border-right">
                            ${warehouse.message(code: 'default.reference.label')}
                        </th>
                        <th class="border-right middle center">
                            ${warehouse.message(code: 'default.comments.label')}
                        </th>
                        <th class="border-right">
                            ${warehouse.message(code: 'inventoryItem.binLocation.label')}
                        </th>
                        <th class="border-right">
                            ${warehouse.message(code: 'inventoryItem.lotNumber.label')}
                        </th>

                        <th class="border-right center" width="7%">
                            ${warehouse.message(code: 'transaction.count.label', default: 'Count')}
                        </th>
                        <th class="border-right center" width="7%">
                            ${warehouse.message(code: 'transaction.credit.label', default: 'Credit')}
                        </th>
                        <th class="border-right center" width="7%">
                            ${warehouse.message(code: 'transaction.debit.label', default: 'Debit')}
                        </th>
                        <th class="center" width="7%">
                            ${warehouse.message(code: 'stockCard.balance.label', default: 'Balance')}
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:set var="count" value="${0}"/>
                    <g:each var="stockHistoryEntry" in="${stockHistoryList}" status="status">
                        <g:set var="rowClass" value=""/>
                        <g:if test="${!stockHistoryEntry.isSameTransaction}">
                            <g:set var="rowClass" value="${(count++%2==0)?'even':'odd' }"/>
                            <g:if test='${stockHistoryEntry?.isBaseline}'>
                                <g:set var="rowClass" value="${rowClass} border-top"/>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:set var="rowClass" value="${(count%2==0)?'odd':'even' }"/>
                        </g:else>

                        <tr class="${rowClass}">
                            <td  class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <g:if test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}"/>
                                    </g:if>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}" />
                                    </g:elseif>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.INVENTORY}">
                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'calculator_edit.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}" />
                                    </g:elseif>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY}">
                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'calculator.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}"/>
                                    </g:elseif>
                                </g:if>
                            </td>
                            <td nowrap="nowrap" class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <format:date obj="${stockHistoryEntry?.transaction?.transactionDate}" format="dd/MMM/yyyy"/>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <format:date obj="${stockHistoryEntry?.transaction?.transactionDate}" format="hh:mma"/>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <div title="${stockHistoryEntry?.transaction?.dateCreated}">
                                        ${stockHistoryEntry?.transaction?.createdBy?.name?:g.message(code:'default.unknown.label')}
                                    </div>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <g:link controller="inventory" action="showTransaction" id="${stockHistoryEntry?.transaction?.id }">
                                        <format:metadata obj="${stockHistoryEntry?.transaction?.transactionType}"/>
                                        <g:if test="${stockHistoryEntry?.transaction?.transactionNumber}">
                                            &rsaquo;
                                            ${stockHistoryEntry?.transaction?.transactionNumber }
                                        </g:if>
                                    </g:link>
                                    <g:if test="${stockHistoryEntry?.transaction?.comment}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}" class="middle" title="${stockHistoryEntry?.transaction?.comment}"/>
                                    </g:if>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.transaction?.source }">
                                    ${stockHistoryEntry?.transaction?.source?.name }
                                </g:if>
                                <g:elseif test="${stockHistoryEntry?.transaction?.destination }">
                                    ${stockHistoryEntry?.transaction?.destination?.name }
                                </g:elseif>

                            </td>

                            <td class="border-right middle">

                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <div >
                                        <g:if test="${stockHistoryEntry?.transaction?.incomingShipment }">
                                            <g:link controller="shipment" action="showDetails" id="${stockHistoryEntry?.transaction?.incomingShipment?.id }">
                                                <div class="ellipsis" title="${stockHistoryEntry?.transaction.incomingShipment?.shipmentNumber } &rsaquo; ${stockHistoryEntry?.transaction.incomingShipment?.name }">
                                                    <g:message code="shipment.label"/> &rsaquo;
                                                    ${stockHistoryEntry?.transaction.incomingShipment?.shipmentNumber } &rsaquo;
                                                    ${stockHistoryEntry?.transaction.incomingShipment?.name }
                                                </div>
                                            </g:link>
                                        </g:if>
                                        <g:elseif test="${stockHistoryEntry?.transaction?.outgoingShipment }">
                                            <g:link controller="shipment" action="showDetails" id="${stockHistoryEntry?.transaction?.outgoingShipment?.id }">
                                                <div class="ellipsis" title="${stockHistoryEntry?.transaction.outgoingShipment?.shipmentNumber } &rsaquo; ${stockHistoryEntry?.transaction.outgoingShipment?.name }">
                                                    <g:message code="shipment.label"/> &rsaquo;
                                                    ${stockHistoryEntry?.transaction.outgoingShipment?.shipmentNumber } &rsaquo;
                                                    ${stockHistoryEntry?.transaction.outgoingShipment?.name }
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                        <g:elseif test="${stockHistoryEntry?.transaction?.requisition }">
                                            <g:link controller="requisition" action="show" id="${stockHistoryEntry?.transaction?.requisition?.id }">
                                                <div title="${stockHistoryEntry?.transaction?.requisition?.requestNumber } &rsaquo; ${stockHistoryEntry?.transaction?.requisition?.name }">
                                                    <g:message code="requisition.label"/> &rsaquo;
                                                    ${stockHistoryEntry?.transaction?.requisition?.requestNumber } &rsaquo;
                                                    ${stockHistoryEntry?.transaction?.requisition?.name }
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                        <g:elseif test="${stockHistoryEntry?.transaction?.order }">
                                            <g:link controller="order" action="show" id="${stockHistoryEntry?.transaction?.order?.id }">
                                                <div title="${stockHistoryEntry?.transaction?.order?.name }">
                                                    <format:metadata obj="${stockHistoryEntry?.transaction?.order?.orderTypeCode }"/>
                                                    &rsaquo;
                                                    ${stockHistoryEntry?.transaction?.order?.orderNumber }
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                        <g:elseif test="${stockHistoryEntry?.transaction?.localTransfer?.sourceTransaction?.requisition}">
                                            <g:set var="requisition" value="${stockHistoryEntry?.transaction?.localTransfer?.sourceTransaction?.requisition}"/>
                                            <g:link controller="requisition" action="show" id="${stockHistoryEntry?.requisition?.id }">
                                                <div title="${stockHistoryEntry?.requisition?.requestNumber }&rsaquo; ${stockHistoryEntry?.requisition?.name }">
                                                    <g:message code="requisition.label"/> &rsaquo;
                                                    ${stockHistoryEntry?.requisition?.requestNumber }&rsaquo;
                                                    ${stockHistoryEntry?.requisition?.name }
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                        <g:elseif test="${stockHistoryEntry?.transaction?.localTransfer?.destinationTransaction?.requisition}">
                                            <g:set var="requisition" value="${transaction?.localTransfer?.destinationTransaction?.requisition}"/>
                                            <g:link controller="requisition" action="show" id="${stockHistoryEntry?.requisition?.id }">
                                                <div title="${stockHistoryEntry?.requisition?.requestNumber } &rsaquo; ${stockHistoryEntry?.requisition?.name }">
                                                    <g:message code="requisition.label"/> &rsaquo;
                                                    ${stockHistoryEntry?.requisition?.requestNumber } &rsaquo;
                                                    ${stockHistoryEntry?.requisition?.name }
                                                </div>
                                            </g:link>
                                        </g:elseif>

                                        <g:else>
                                        </g:else>
                                    </div>
                                </g:if>
                            </td>
                            <td class="border-right middle center">
                                <g:if test="${stockHistoryEntry?.comments}">
                                    <g:if test="${params.print}">
                                        ${stockHistoryEntry.comments}
                                    </g:if>
                                    <g:else>
                                        <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}" class="middle" title="${stockHistoryEntry.comments}"/>
                                    </g:else>
                                </g:if>
                            </td>


                            <td class="border-right center middle">
                                <g:if test="${stockHistoryEntry?.binLocation}">
                                    ${stockHistoryEntry?.binLocation?.name}
                                </g:if>
                                <g:else>
                                    <div class="fade">${g.message(code: 'default.label')}</div>
                                </g:else>
                            </td>

                            <td class="border-right center middle">
                                <span class="lotNumber">
                                    ${stockHistoryEntry?.inventoryItem?.lotNumber}
                                </span>
                            </td>
                            <td class="border-right center middle">
                                <g:if test="${stockHistoryEntry?.transactionCode in [org.pih.warehouse.inventory.TransactionCode.INVENTORY, org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY] }">
                                    <span class="balance">
                                        <g:formatNumber number="${stockHistoryEntry?.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/>
                                    </span>
                                </g:if>
                            </td>

                            <td class="border-right center middle">
                                <g:if test="${stockHistoryEntry.isCredit}">
                                    <span class="credit">
                                        <g:formatNumber number="${stockHistoryEntry?.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/>
                                    </span>

                                </g:if>
                            </td>
                            <td  class="border-right center middle">
                                <g:if test="${stockHistoryEntry.isDebit}">
                                    <span class="debit"><g:formatNumber number="${stockHistoryEntry?.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/></span>
                                </g:if>
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${stockHistoryEntry?.balance?:0}" format="###,###.#" maxFractionDigits="1"/>
                            </td>
                        </tr>
                    </g:each>
                    <g:unless test="${stockHistoryList }">
                        <tr>
                            <td colspan="11" class="even center">
                                <div class="empty fade">
                                    <warehouse:message code="transaction.noTransactions.label"/>
                                </div>
                            </td>
                        </tr>
                    </g:unless>
                    </tbody>
                    <tfoot style="display: table-row-group">
                    <tr class="odd">
                        <th colspan="7" class="left border-right">
                            <warehouse:message code="stockCard.totals.label" default="Totals"/>
                        </th>
                        <th class="center border-right">

                        </th>
                        <th class="center border-right">

                        </th>
                        <th></th>
                        <th class="center border-right">
                            <g:formatNumber number="${totalCount?:0}" format="#,###"/>
                        </th>
                        <th class="center border-right">
                            <g:formatNumber number="${totalCredit?:0}" format="#,###"/>
                        </th>
                        <th class="center border-right">
                            (<g:formatNumber number="${totalDebit?:0}" format="#,###"/>)
                        </th>
                        <th class="center">
                            <g:formatNumber number="${totalBalance?:0}" format="#,###"/>
                        </th>
                    </tr>
                    </tfoot>
                </table>
            </td>
        </tbody>
    </table>
</div>
</body>
</html>
