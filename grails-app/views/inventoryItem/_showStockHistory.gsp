<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<%--
<div class="left smpad">
	Showing ${commandInstance?.allTransactionLogMap?.keySet()?.size() } of ${commandInstance?.allTransactionLogMap?.keySet()?.size() } transaction(s)
</div>				
--%>

<div class="box">
    <h2><warehouse:message code="inventory.stockHistory.label"/></h2>
    <g:form method="GET" action="showTransactionLog">
        <g:hiddenField name="product.id" value="${commandInstance?.product?.id }"/>
            <table>
                <g:set var="enableFilter" value="${!params.disableFilter}"/>
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
                        <th class="border-right">
                            ${warehouse.message(code: 'shipment.label')} /
                            ${warehouse.message(code: 'requisition.label')}
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
                    <g:set var="transactionMap" value="${commandInstance?.getTransactionLogMap(enableFilter.toBoolean())}"/>
                    <g:if test="${!transactionMap }">
                        <tr>
                            <td colspan="10" class="even center">
                                <div class="empty fade">
                                    <warehouse:message code="transaction.noTransactions.label"/>
                                    <%--
                                    <warehouse:message code="transaction.noTransactions.message" args="[format.metadata(obj:commandInstance?.transactionType),commandInstance?.startDate,commandInstance?.endDate]"/>
                                    --%>
                                </div>
                            </td>
                        </tr>
                    </g:if>
                    <g:else>
                        <g:set var="balanceBefore" value="${0}"/>
                        <g:set var="balance" value="${0}"/>
                        <g:set var="balanceByInventoryItem" value="${[:]}"/>
                        <g:set var="totalQuantityChange" value="${0 }"/>
                        <g:set var="totalDebit" value="${0 }"/>
                        <g:set var="totalCredit" value="${0 }"/>
                        <g:set var="count" value="${0}"/>
                        <g:set var="previousTransaction" value='${null }'/>
                        <g:each var="transaction" in="${transactionMap?.keySet()?.sort {it.transactionDate} }" status="status">

                            <%
                                if(transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY) {
                                    balanceByInventoryItem = [:]
                                }
                            %>

                            <g:each var="transactionEntry" in="${transaction.transactionEntries.findAll { it.inventoryItem.product?.id ==  commandInstance?.product?.id}}" status="status2">
                                <g:set var="rowClass" value=""/>
                                <%
                                    if (!balanceByInventoryItem[transactionEntry.inventoryItem]) {
                                        balanceByInventoryItem[transactionEntry.inventoryItem] = 0
                                    }

                                    //balanceByInventoryItem[transactionEntry.inventoryItem]+=transactionEntry?.quantity
                                    if(transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT) {
                                        balanceByInventoryItem[transactionEntry.inventoryItem]-=transactionEntry?.quantity
                                    }
                                    else if(transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT) {
                                        balanceByInventoryItem[transactionEntry.inventoryItem]+=transactionEntry?.quantity
                                    }
                                    else if(transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.INVENTORY) {
                                        balanceByInventoryItem[transactionEntry.inventoryItem]=transactionEntry?.quantity
                                    }
                                    else if(transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY) {
                                        balanceByInventoryItem[transactionEntry.inventoryItem]=transactionEntry?.quantity
                                    }
                                %>
                                <g:if test="${previousTransaction?.id != transaction?.id}">
                                    <g:set var="rowClass" value="${(count++%2==0)?'even':'odd' }"/>
                                    <g:if test='${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY}'>
                                        <g:set var="rowClass" value="${rowClass} border-top"/>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <g:set var="rowClass" value="${(count%2==0)?'odd':'even' }"/>
                                </g:else>
                                <tr class="${rowClass}">
                                    <g:if test="${previousTransaction?.id != transaction?.id}">
                                        <td  class="middle">
                                            <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png' )}"/>
                                            </g:if>
                                            <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png' )}" />
                                            </g:elseif>
                                            <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.INVENTORY}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'clipboard.png' )}" />
                                            </g:elseif>
                                            <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'calculator.png' )}" />
                                            </g:elseif>
                                        </td>
                                        <td nowrap="nowrap" class="middle">
                                            <format:date obj="${transaction?.transactionDate}" format="dd/MMM/yyyy"/>
                                        </td>
                                        <td class="middle">
                                            <format:date obj="${transaction?.transactionDate}" format="hh:mma"/>
                                        </td>
                                        <td>
                                            ${transaction?.createdBy?.name}
                                        </td>
                                        <td class="middle">
                                            <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">

                                                <format:metadata obj="${transaction?.transactionType}"/>
                                                <g:if test="${transaction?.source }">
                                                    ${warehouse.message(code:'default.from.label')}
                                                    ${transaction?.source?.name }
                                                </g:if>
                                                <g:elseif test="${transaction?.destination }">
                                                    ${warehouse.message(code:'default.to.label')}
                                                    ${transaction?.destination?.name }
                                                </g:elseif>
                                                <g:if test="${transaction?.transactionNumber}">
                                                    &rsaquo; ${transaction?.transactionNumber }
                                                </g:if>
                                            </g:link>
                                        </td>

                                        <td class="border-right middle">
                                            <div>
                                                <g:if test="${transaction?.incomingShipment }">
                                                    <g:link controller="shipment" action="showDetails" id="${transaction?.incomingShipment?.id }">
                                                        ${transaction.incomingShipment?.shipmentNumber } &rsaquo;
                                                        ${transaction.incomingShipment?.name }
                                                    </g:link>
                                                </g:if>
                                                <g:elseif test="${transaction?.outgoingShipment }">
                                                    <g:link controller="shipment" action="showDetails" id="${transaction?.outgoingShipment?.id }">
                                                        ${transaction.outgoingShipment?.shipmentNumber } &rsaquo;
                                                        ${transaction.outgoingShipment?.name }
                                                    </g:link>
                                                </g:elseif>
                                                <g:elseif test="${transaction?.requisition }">
                                                    <g:link controller="requisition" action="show" id="${transaction?.requisition?.id }">
                                                        ${transaction?.requisition?.requestNumber } &rsaquo;
                                                        ${transaction?.requisition?.name }
                                                    </g:link>
                                                </g:elseif>
                                                <g:elseif test="${transaction?.localTransfer?.sourceTransaction?.requisition}">
                                                    <g:set var="requisition" value="${transaction?.localTransfer?.sourceTransaction?.requisition}"/>
                                                    <g:link controller="requisition" action="show" id="${requisition?.id }">
                                                        ${requisition?.requestNumber } &rsaquo;
                                                        ${requisition?.name }
                                                    </g:link>
                                                </g:elseif>
                                                <g:elseif test="${transaction?.localTransfer?.destinationTransaction?.requisition}">
                                                    <g:set var="requisition" value="${transaction?.localTransfer?.destinationTransaction?.requisition}"/>
                                                    <g:link controller="requisition" action="show" id="${requisition?.id }">
                                                        ${requisition?.requestNumber } &rsaquo;
                                                        ${requisition?.name }
                                                    </g:link>
                                                </g:elseif>

                                                <g:else>
                                                    <%--
                                                    <span class="fade">${warehouse.message(code:'default.none.label') }</span>
                                                    --%>
                                                </g:else>



                                            </div>
                                        </td>
                                    </g:if>
                                    <g:else>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td class='border-right'></td>
                                    </g:else>
                                    <td class="border-right center middle">
                                        <span class="lotNumber">${transactionEntry?.inventoryItem?.lotNumber}</span>
                                    </td>
                                    <td class="border-right center middle">
                                        <g:if test="${transaction?.transactionType?.transactionCode in [org.pih.warehouse.inventory.TransactionCode.INVENTORY, org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY] }">
                                            <span class="balance">
                                                <g:formatNumber number="${transactionEntry.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/>
                                            </span>
                                        </g:if>
                                    </td>

                                    <td class="border-right center middle">
                                        <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                            <span class="credit">
                                                <g:formatNumber number="${transactionEntry.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/>
                                            </span>
                                            <g:set var="totalCredit" value="${totalCredit + transactionEntry.quantity }"/>
                                        </g:if>
                                    </td>
                                    <td  class="border-right center middle">
                                        <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                            <span class="debit"><g:formatNumber number="${transactionEntry.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/></span>
                                            <g:set var="totalDebit" value="${totalDebit + transactionEntry.quantity }"/>
                                        </g:if>
                                    </td>
                                    <td class="center middle">
                                        <g:formatNumber number="${balanceByInventoryItem?.values()?.sum()?:0}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                </tr>
                                <g:set var="previousTransaction" value="${transaction}"/>
                            </g:each>

                            <%--
                            <tr class="transaction ${(status%2==0)?'even':'odd' } prop border-top">
                                <td nowrap="nowrap">
                                    <format:date obj="${transaction?.transactionDate}" format="dd/MMM/yyyy"/>
                                </td>
                                <td>
                                    <format:date obj="${transaction?.transactionDate}" format="hh:mma"/>
                                </td>
                                <td>
                                    <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                                        ${transaction?.transactionNumber }
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                                        <format:metadata obj="${transaction?.transactionType}"/>
                                        <g:if test="${transaction?.source }">
                                            ${warehouse.message(code:'default.from.label')}
                                            ${transaction?.source?.name }
                                        </g:if>
                                        <g:elseif test="${transaction?.destination }">
                                            ${warehouse.message(code:'default.to.label')}
                                            ${transaction?.destination?.name }
                                        </g:elseif>
                                    </g:link>

                                </td>
                                <td class="border-right">
                                    <div>
                                        <g:if test="${transaction?.incomingShipment }">
                                            <g:link controller="shipment" action="showDetails" id="${transaction?.incomingShipment?.id }">
                                                ${transaction.incomingShipment?.shipmentNumber } |
                                                ${transaction.incomingShipment?.name }
                                            </g:link>
                                        </g:if>
                                        <g:elseif test="${transaction?.outgoingShipment }">
                                            <g:link controller="shipment" action="showDetails" id="${transaction?.outgoingShipment?.id }">
                                                ${transaction.outgoingShipment?.shipmentNumber } |
                                                ${transaction.outgoingShipment?.name }
                                            </g:link>
                                        </g:elseif>
                                        <g:elseif test="${transaction?.requisition }">
                                            <g:link controller="requisition" action="show" id="${transaction?.requisition?.id }">
                                                ${transaction?.requisition?.requestNumber } |
                                                ${transaction?.requisition?.name }
                                            </g:link>
                                        </g:elseif>
                                        <g:else>
                                            <span class="fade">${warehouse.message(code:'default.none.label') }</span>
                                        </g:else>
                                    </div>
                                </td>
                                <td class="border-right center">
                                    <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                        ${quantityChange }
                                    </g:if>
                                </td>
                                <td  class="border-right center">
                                    <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                        (${quantityChange })
                                    </g:if>
                                </td>
                                <td class="border-right center">
                                    ${balance}
                                </td>
                            </tr>
                            <g:set var="previousMonth" value="${transaction?.transactionDate?.month}"/>
                            --%>
                        </g:each>
                        <%--
                        <!-- Commented out because it's a little confusing -->
                        <tr class="prop" style="height: 3em;">
                            <td colspan="3" style="text-align: right; font-size: 1.5em; vertical-align: middle;">
                                Recent changes
                            </td>
                            <td style="text-align: center; font-size: 1.5em; vertical-align: middle">
                                <g:if test="${totalQuantityChange>0}">${totalQuantityChange}</g:if>
                                <g:else><span style="color: red;">${totalQuantityChange}</span></g:else>
                            </td>
                        </tr>
                        --%>
                    </g:else>
                </tbody>
                <tfoot>
                    <tr class="odd">
                        <th colspan="6" class="left border-right">
                            <warehouse:message code="stockCard.totals.label" default="Totals"/>
                        </th>
                        <th class="center border-right">

                        </th>
                        <th class="center border-right">

                        </th>
                        <th class="center border-right">
                            <g:formatNumber number="${totalCredit?:0}" format="#,###"/>
                        </th>
                        <th class="center border-right">
                                (<g:formatNumber number="${totalDebit?:0}" format="#,###"/>)
                        </th>
                        <th class="center">
                            <g:formatNumber number="${balanceByInventoryItem?.values()?.sum()?:0}" format="#,###"/>
                        </th>
                    </tr>
                </tfoot>
            </table>
    </g:form>
</div>


	