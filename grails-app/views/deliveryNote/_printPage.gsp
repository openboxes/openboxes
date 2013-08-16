<div class="page" style="page-break-after: ${pageBreakAfter};">
    <table id="requisition-items">
        <thead>
            <tr class="theader">
                <th><warehouse:message code="report.number.label"/></th>
                <th class="center">${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th style="min-width: 150px;">${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th class="center">${warehouse.message(code: 'requisitionItem.quantityPicked.label')}</th>
                <th>${warehouse.message(code: 'requisitionItem.cancelReasonCode.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${requisitionItems}">
                <tr>
                    <td colspan="8" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>
            </g:unless>
            <g:each in="${requisitionItems?.sort()}" status="i" var="requisitionItem">
                <g:if test="${picklist}">
                    <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems()}"/>
                    <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}"/>
                </g:if>
                <g:else>
                    <g:set var="numInventoryItem" value="${1}"/>
                </g:else>
                <g:set var="j" value="${0}"/>
                <g:while test="${j < numInventoryItem}">
                    <tr class="prop">
                        <td class="center middle">
                            <g:if test="${j==0}">
                                ${i + 1}
                            </g:if>
                        </td>
                        <td class="center middle">
                            <g:if test="${j==0}">
                                <g:if test="${requisitionItem?.parentRequisitionItem}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.productCode}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.productCode}
                            </g:if>
                        </td>
                        <td class="middle">
                            <g:if test="${j==0}">
                                <g:if test="${requisitionItem?.parentRequisitionItem}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.name}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.name}
                            </g:if>
                        </td>
                        <td class="middle center">
                            ${picklistItems[j]?.inventoryItem?.lotNumber}
                        </td>
                        <td class="middle center">
                            <g:formatDate date="${picklistItems[j]?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
                        </td>
                        <td class="center middle">
                            <g:if test="${requisitionItem?.parentRequisitionItem}">
                                <div class="canceled">
                                    ${requisitionItem?.parentRequisitionItem?.quantity}
                                    ${requisitionItem?.parentRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                                </div>
                            </g:if>
                            ${picklistItems[j]?.quantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                        </td>
                        <td class="center middle">
                            <g:if test="${requisitionItem?.parentRequisitionItem?.cancelReasonCode}">
                                <p>${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.parentRequisitionItem?.cancelReasonCode)}</p>
                                <p class="fade">${requisitionItem?.parentRequisitionItem?.cancelComments}</p>
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.ok.label', default:'Ok')}
                            </g:else>
                        </td>
                        <% j++ %>
                    </tr>
                </g:while>
            </g:each>
        </tbody>
    </table>
</div>