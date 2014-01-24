<div class="page" style="page-break-after: ${pageBreakAfter};">
    <table id="requisition-items">
        <thead>
            <tr class="theader">
                <th><warehouse:message code="report.number.label"/></th>
                <th class="center">${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th style="min-width: 150px;">${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th class="center">${warehouse.message(code: 'requisitionItem.quantityRequested.label')}</th>
                <th class="center">${warehouse.message(code: 'requisitionItem.quantityDelivered.label', default: "Delivered")}</th>
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
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.productCode}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.productCode}
                            </g:if>
                        </td>
                        <td class="middle">
                            <g:if test="${j==0}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.name}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.name}
                            </g:if>

                            <g:if test="${requisitionItem?.parentRequisitionItem?.cancelReasonCode}">
                                <div class="fade">
                                    <i>${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.parentRequisitionItem?.cancelReasonCode)}</i>
                                    <g:if test="${requisitionItem?.parentRequisitionItem?.cancelComments}">
                                        (${requisitionItem?.parentRequisitionItem?.cancelComments})
                                    </g:if>
                                </div>
                            </g:if>
                            <g:if test="${requisitionItem?.cancelReasonCode}">
                                <div class="fade">
                                    <i>${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.cancelReasonCode)}</i>
                                    <g:if test="${requisitionItem?.cancelComments}">
                                        (${requisitionItem?.cancelComments})
                                    </g:if>
                                </div>
                            </g:if>



                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                ${picklistItems[j]?.inventoryItem?.lotNumber}
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                <g:formatDate date="${picklistItems[j]?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
                            </g:if>
                        </td>
                        <td class="center middle">
                            <g:if test="${requisitionItem.parentRequisitionItem?.isChanged()}">
                                <div class="canceled">
                                    ${requisitionItem?.parentRequisitionItem?.quantity ?: 0}
                                    ${requisitionItem?.parentRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                                </div>
                            </g:if>
                            <div class="${requisitionItem?.status}">
                                ${requisitionItem?.quantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </div>



                        </td>
                        <td class="center middle">
                            <g:if test="${picklistItems}">
                                ${picklistItems[j]?.quantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                        </td>

                        <% j++ %>
                    </tr>
                </g:while>
            </g:each>
        </tbody>
    </table>
</div>