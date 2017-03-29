<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="print" />
    <title><warehouse:message code="report.showTransactionReport.label" /></title>
</head>
<body>

<g:if test="${command.product }">
    <style>
    .debit:before { content: '-'; }
    .debit:after { content: ''; }
    .credit:before { content: '+'; }
    .credit:after { content: ''; }
    .product_inventory { font-weight: bold; }
    .inventory { font-weight: bold; }
    </style>
    <g:set var="i" value='${0 }'/>
    <g:each var="entry" in="${command?.entries }">
        <g:if test="${command?.product == entry?.value?.product}">
            <div class="buttonBar">
                <g:link class="button" controller="report" action="generateTransactionReport" params="['location.id':command?.location?.id,'category.id':command?.category.id,'startDate':format.date(obj:command.startDate,format:'MM/dd/yyyy'),'endDate':format.date(obj:command.endDate,format:'MM/dd/yyyy'),'includeChildren':params.includeChildren]" style="display: inline">
                    <warehouse:message code="report.backToInventoryReport.label"/>
                </g:link>

                <g:link class="button" controller="report" action="generateTransactionReport" params="['product.id':command?.product?.id,'category.id':command?.category?.id,'location.id':command?.location?.id,startDate:params.startDate,endDate:params.endDate,showEntireHistory:true,'includeChildren':params.includeChildren]">
                    <warehouse:message code="report.showEntireHistory.label"/></g:link>

                <g:link class="button" controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">
                    <warehouse:message code="report.showStockCard.label"/></g:link>
            </div>
            <g:each var="itemEntry" in="${entry.value.entries}">
                <div class="box">
                    <h2>
                        ${entry.key }
                        ${itemEntry.key.lotNumber ?: 'EMPTY'}
                        <span class="circle">${itemEntry?.value?.quantityRunning}</span>
                    </h2>

                    <table style="border: 1px solid lightgrey" class="report">
                        <thead>
                        <tr class="${i++%2?'odd':'even' }">
                            <th wdith="1%">
                                <warehouse:message code="report.transactionDate.label"/>
                            </th>
                            <th wdith="1%">
                                <warehouse:message code="report.transactionTime.label" default="Time"/>
                            </th>
                            <th>
                                <warehouse:message code="report.transactionType.label"/>
                            </th>
                            <th class="center">
                                <warehouse:message code="report.quantityChange.label"/>
                            </th>
                            <th class="center">
                                <warehouse:message code="report.quantityBalance.label"/>
                            </th>
                        </tr>
                        </thead>
                        <g:if test="${itemEntry.value.transactionEntries}">
                            <tbody>
                            <tr>
                                <td><format:date obj="${command?.startDate}" format="MMM dd yyyy"/></td>
                                <td><format:date obj="${command?.startDate}" format="hh:mma"/></td>
                                <td><warehouse:message code="report.initialQuantity.label"/></td>
                                <td></td>
                                <td class="center">${itemEntry?.value?.quantityInitial }</td>
                            </tr>
                            <g:each var="row" in="${itemEntry.value.transactionEntries}">
                                <g:set var="transactionTypeCode" value="${row?.transactionEntry?.transaction?.transactionType?.transactionCode?.toString()?.toLowerCase()}"/>
                                <tr class="${i++%2?'odd':'even' }">
                                    <td>
                                        <format:date obj="${row.transactionEntry?.transaction?.transactionDate}" format="MMM dd yyyy"/>
                                    </td>
                                    <td>
                                        <format:date obj="${row.transactionEntry?.transaction?.transactionDate}" format="hh:mma"/>
                                    </td>
                                    <td>
                                        <g:link controller="inventory" action="showTransaction" id="${row?.transactionEntry?.transaction?.id }">
                                            <format:metadata obj="${row.transactionEntry?.transaction?.transactionType}"/>
                                        </g:link>
                                    </td>
                                    <td class="center">
                                        <span class="${transactionTypeCode}">${row.transactionEntry?.quantity }</span>
                                    </td>
                                    <td class="center">${row.balance }</td>
                                </tr>
                            </g:each>

                            </tbody>
                            <tfoot>
                            <tr>
                                <td><format:date obj="${command?.endDate}" format="MMM dd yyyy"/></td>
                                <td><format:date obj="${command?.endDate}" format="hh:mma"/></td>

                                <td><warehouse:message code="report.finalQuantity.label"/></td>
                                <td></td>
                                <td class="center">${itemEntry?.value?.quantityFinal }</td>
                            </tr>
                            </tfoot>
                        </g:if>
                        <g:else>
                            <tbody>
                            <tr>
                                <td colspan="5">
                                    <warehouse:message code="transaction.noTransactions.label"/>
                                </td>
                            </tr>
                            </tbody>
                        </g:else>
                    </table>
                </div>
            </g:each>
        </g:if>
    </g:each>
</g:if>
<g:else>
    <div>

        <h1 align="center">
            <g:message code="report.transactionReport.title"/>
        </h1>
        <h3 align="center">${format.date(obj:command?.startDate, format: 'MMM dd, yyyy')} - ${format.date(obj:command?.endDate, format: 'MMM dd, yyyy')}</h3>

        <g:set var="status" value="${0 }"/>
        <g:each var="productEntry" in="${command?.productsByCategory }" status="i">
            <g:set var="category" value="${productEntry.key }"/>

            <div class="box">


                <h2>
                    <format:category category="${category}"/>
                </h2>

                <div class="list">
                    <table class="report">
                        <thead>
                        <tr style="border-top: 1px solid lightgrey;">
                            <th rowspan="2" class="left bottom">
                                <warehouse:message code="report.product.label"/>
                            </th>
                            <th rowspan="2" class="center bottom total start">
                                <warehouse:message code="report.initialQuantity.label"/>
                            </th>
                            <td colspan="${(command.showTransferBreakdown) ? 3 + (transferInLocations?.size?:0) : 3}" class="center total">
                                <label>
                                    <warehouse:message code="report.incomingQuantity.label"/>
                                </label>
                            </td>
                            <td colspan="${(command.showTransferBreakdown) ? 6 + (transferOutLocations?.size?:0) : 6}" class="center total">
                                <label>
                                    <warehouse:message code="report.outgoingQuantity.label"/>
                                </label>
                            </td>
                            <th rowspan="2" class="center bottom total end">
                                <warehouse:message code="report.finalQuantity.label"/>
                            </th>
                        </tr>

                        <tr style="border-top: 1px solid lightgrey;">
                            <th class="right">
                                <warehouse:message code="report.incomingTransferQuantity.label"/>
                            </th>
                            <g:if test="${command.showTransferBreakdown }">
                                <g:each var="location" in="${transferInLocations }">
                                    <th class="right bottom">${location.name.substring(0,3) }</th>
                                </g:each>
                            </g:if>
                            <th class="right nowrap">
                                <warehouse:message code="report.adjustedInQuantity.label"/>
                            </th>
                            <th class="right total">
                                <warehouse:message code="report.incomingTotalQuantity.label"/>
                            </th>
                            <th class="right">
                                <warehouse:message code="report.outgoingTransferQuantity.label"/>
                            </th>
                            <g:if test="${command.showTransferBreakdown }">
                                <g:each var="location" in="${transferOutLocations }">
                                    <th class="right">${location.name.substring(0,3) }</th>
                                </g:each>
                            </g:if>
                            <th class="right">
                                <warehouse:message code="report.expiredQuantity.label"/>
                            </th>
                            <th class="right">
                                <warehouse:message code="report.consumedQuantity.label"/>
                            </th>
                            <th class="right">
                                <warehouse:message code="report.damagedQuantity.label"/>
                            </th>
                            <th class="right">
                                <warehouse:message code="report.adjustedOutQuantity.label"/>
                            </th>
                            <th class="right total">
                                <warehouse:message code="report.outgoingTotalQuantity.label"/>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each var="product" in="${productEntry.value }" status="j">
                            <g:set var="entry" value="${command.entries[product].totals }"/>
                            <tr class="${j%2 ? 'even' : 'odd' }">
                                <td class="left" style="width: 35%">
                                    <format:product product="${product }"/>
                                </td>
                                <td class="right total start nowrap">
                                    <span class="${(entry?.quantityInitial>=0)?'credit':'debit'}">
                                        ${entry?.quantityInitial ?: 0}
                                    </span>
                                </td>
                                <td class="right nowrap">
                                    <span class="${(entry?.quantityTransferredIn>=0)?'credit':'debit'}">
                                        ${entry?.quantityTransferredIn ?: 0}
                                    </span>
                                </td>
                                <td class="right nowrap">
                                    <span class="${(entry?.quantityFound>=0)?'credit':'debit'}">${entry?.quantityFound ?: 0}</span>
                                </td>
                                <td class="right total nowrap">
                                    <span class="${(entry?.quantityTotalIn>=0)?'credit':'debit'}">
                                        ${entry?.quantityTotalIn ?: 0}
                                    </span>
                                </td>
                                <td class="right nowrap">
                                    <span class="${(entry?.quantityTransferredOut>0)?'debit':'credit'}">${entry?.quantityTransferredOut?:0}</span>
                                </td>
                                <td class="right nowrap">
                                    <span class="${(entry?.quantityExpired>0)?'debit':'credit'}">${entry?.quantityExpired ?: 0}</span>
                                </td>
                                <td class="right nowrap">
                                    <span class="${(entry?.quantityConsumed>0)?'debit':'credit'}">${entry?.quantityConsumed ?: 0}</span>
                                </td>
                                <td class="right nowrap">
                                    <span class="${(entry?.quantityDamaged>0)?'debit':'credit'}">${entry?.quantityDamaged ?: 0}</span>
                                </td>
                                <td class="right nowrap">
                                    <span class="${(entry?.quantityLost>=0)?'credit':'debit'}">${entry?.quantityLost ?: 0}</span>
                                </td>
                                <td class="right total nowrap">
                                    <span class="${(entry?.quantityTotalOut>0)?'debit':'credit'}">${entry?.quantityTotalOut ?: 0 }</span>
                                </td>
                                <td class="right total end nowrap">
                                    <span class="${(entry?.quantityFinal>=0)?'credit':'debit'}">${entry?.quantityFinal ?: 0}</span>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </g:each>
    </div>
</g:else>
</body>
</html>