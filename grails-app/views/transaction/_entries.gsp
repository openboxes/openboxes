<g:if test="${transactionInstance?.id }">
    <div class="box">
        <h2>${warehouse.message(code: 'transaction.transactionEntries.label')}</h2>
        <table id="prodEntryTable">
            <thead>
            <tr class="odd">
                <th></th>
                <th><warehouse:message code="product.label"/></th>
                <th style="text-align: center"><warehouse:message code="location.binLocation.label"/></th>
                <th style="text-align: center"><warehouse:message code="product.lotNumber.label"/></th>
                <th style="text-align: center"><warehouse:message code="product.expirationDate.label"/></th>
                <th style="text-align: center"><warehouse:message code="default.qty.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:set var="transactionSum" value="${0 }"/>
            <g:set var="transactionCount" value="${0 }"/>

            <g:if test="${transactionInstance?.transactionEntries }">
                <g:each in="${transactionInstance?.transactionEntries }" var="transactionEntry" status="status">
                    <g:set var="selected" value="${transactionEntry?.inventoryItem?.product?.id == params?.product?.id}"/>
                    <g:set var="transactionSum" value="${transactionSum + transactionEntry?.quantity}"/>
                    <g:set var="transactionCount" value="${transactionCount+1 }"/>
                    <tr class="${status%2?'odd':'even' } ${selected?'selected':''}">
                        <td>
                            <g:link controller="transactionEntry" action="edit" id="${transactionEntry?.id}">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" style="vertical-align: middle"/>
                            </g:link>

                        </td>
                        <td style="text-align: left;">
                            <g:link controller="inventoryItem" action="showStockCard" params="['product.id':transactionEntry?.inventoryItem?.product?.id]">
                                ${transactionEntry?.inventoryItem?.product?.productCode}
                                <format:product product="${transactionEntry?.inventoryItem?.product}"/>
                            </g:link>
                        </td>
                        <td class="center">
                            ${transactionEntry?.binLocation?.name }
                        </td>
                        <td class="center">
                            ${transactionEntry?.inventoryItem?.lotNumber }
                        </td>
                        <td class="center">
                            <format:expirationDate obj="${transactionEntry?.inventoryItem?.expirationDate}"/>
                        </td>
                        <td class="center">
                            ${transactionEntry?.quantity}
                        </td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="4">
                        <div class="empty center">
                            <warehouse:message code="transaction.noEntries.message"/>
                        </div>
                    </td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>
</g:if>
