<div class="button-bar">
    <g:isSuperuser>
        <g:link controller="shipment" action="syncTransactions" id="${shipmentInstance?.id}" class="button">
            <g:message code="shipment.syncTransactions.label" default="Sync Transactions"/>
        </g:link>
    </g:isSuperuser>
</div>

<div id="transactions" class="box">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_switch_bluegreen.png')}" style="vertical-align: middle"/>
        <label><warehouse:message code="shipping.transactions.label"/></label>
    </h2>
    <g:if test="${shipmentInstance?.outgoingTransactions || shipmentInstance?.incomingTransactions }">
        <table>
            <thead>
            <tr>
                <th><warehouse:message code="default.date.label"/></th>
                <th><warehouse:message code="default.time.label"/></th>
                <th><warehouse:message code="transaction.type.label"/></th>
                <th><warehouse:message code="transaction.transactionNumber.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:set var="i" value="${0 }"/>
            <g:each var="transaction" in="${shipmentInstance?.incomingTransactions}">
                <tr class="${i++ % 2 ? 'even' : 'odd' }">
                    <td>
                        <g:formatDate date="${transaction?.transactionDate}" format="MMM d, yyyy"/>
                    </td>
                    <td>
                        <g:formatDate date="${transaction?.transactionDate}" format="hh:mma"/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                            <format:metadata obj="${transaction?.transactionType}"/>
                        </g:link>
                    </td>
                    <td>
                        <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                            ${transaction?.transactionNumber}
                        </g:link>
                    </td>
                </tr>
            </g:each>
            <g:each var="transaction" in="${shipmentInstance?.outgoingTransactions}">
                <tr class="${i++ % 2 ? 'even' : 'odd' }">
                    <td>
                        <g:formatDate date="${transaction?.transactionDate}" format="MMM d, yyyy"/>
                    </td>
                    <td>
                        <g:formatDate date="${transaction?.transactionDate}" format="hh:mma"/>
                    </td>
                    <td>
                        <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                            <format:metadata obj="${transaction?.transactionType}"/>
                        </g:link>
                    </td>
                    <td>
                        <g:link controller="inventory" action="showTransaction" id="${transaction?.id }">
                            ${transaction?.transactionNumber }
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="center empty fade">
            <g:message code="transaction.noTransactions.label"/>
        </div>
    </g:else>
</div>
