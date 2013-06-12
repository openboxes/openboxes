<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<%--
<div class="left smpad">
	Showing ${commandInstance?.allTransactionLogMap?.keySet()?.size() } of ${commandInstance?.allTransactionLogMap?.keySet()?.size() } transaction(s)
</div>				
--%>
<div>		

	<div>
		<g:form method="GET" action="showTransactionLog">
			<g:hiddenField name="product.id" value="${commandInstance?.productInstance?.id }"/>

				<!--  Filter -->
			
				<table class="box" style="border-top: 0;">

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
                                ${warehouse.message(code: 'transaction.transactionNumber.label')}
                            </th>
                            <th>
                                ${warehouse.message(code: 'default.type.label')}
                            </th>

                            <th class="border-right">
                                ${warehouse.message(code: 'shipment.label')} /
                                ${warehouse.message(code: 'requisition.label')}
                            </th>
                            <th class="border-right">
                                ${warehouse.message(code: 'inventoryItem.lotNumber.label')}
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
                                <td colspan="6" class="even center">
                                    <div class="fade padded">
                                        <warehouse:message code="transaction.noTransactions.label"/>
                                        <%--
                                        <warehouse:message code="transaction.noTransactions.message" args="[format.metadata(obj:commandInstance?.transactionType),commandInstance?.startDate,commandInstance?.endDate]"/>
                                        --%>
                                    </div>
                                </td>
                            </tr>
                        </g:if>
                        <g:else>
                            <g:set var="balance" value="${0}"/>
                            <g:set var="balanceByInventoryItem" value="${[:]}"/>
                            <g:set var="totalQuantityChange" value="${0 }"/>
                            <g:set var="totalDebit" value="${0 }"/>
                            <g:set var="totalCredit" value="${0 }"/>
                            <g:set var="count" value="${0}"/>
                            <g:each var="transaction" in="${transactionMap?.keySet()?.sort {it.transactionDate} }" status="status">
                                <g:each var="transactionEntry" in="${transaction.transactionEntries.findAll { it.inventoryItem.product ==  commandInstance?.productInstance}}" status="status2">
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
                                    <tr class="transaction ${(count++%2==0)?'even':'odd' } prop border-top">
                                        <td style="text-align: center">
                                            <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'decline.png' )}"/>
                                            </g:if>
                                            <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'add.png' )}" />
                                            </g:elseif>
                                            <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.INVENTORY}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'clipboard.png' )}" />
                                            </g:elseif>
                                            <g:elseif test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY}">
                                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'clipboard.png' )}" />
                                            </g:elseif>
                                        </td>
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
                                            <span class="lotNumber">${transactionEntry?.inventoryItem?.lotNumber}</span>
                                        </td>
                                        <td class="border-right center">
                                            <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.CREDIT}">
                                                ${transactionEntry.quantity  }
                                                <g:set var="totalCredit" value="${totalCredit + transactionEntry.quantity }"/>
                                            </g:if>
                                        </td>
                                        <td  class="border-right center">
                                            <g:if test="${transaction?.transactionType?.transactionCode== org.pih.warehouse.inventory.TransactionCode.DEBIT}">
                                                (${transactionEntry.quantity })
                                                <g:set var="totalDebit" value="${totalDebit + transactionEntry.quantity }"/>
                                            </g:if>
                                        </td>
                                        <td class="center">

                                            ${balanceByInventoryItem?.values()?.sum()?:0}
                                        </td>
                                    </tr>

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
                                <g:formatNumber number="${totalCredit}" format="#,###"/>
                            </th>
                            <th class="center border-right">
                                (<g:formatNumber number="${totalDebit}" format="#,###"/>)
                            </th>
                            <th class="center">
                                <g:formatNumber number="${balanceByInventoryItem?.values()?.sum()?:0}" format="#,###"/>
                            </th>

                        </tr>
                    </tfoot>
				</table>
		</g:form>
	</div>
</div>

	