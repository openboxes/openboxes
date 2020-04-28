<%@ page defaultCodec="html" %>
<div class="page-content">
    <table id="requisition-items" class="fs-repeat-header w100">
        <thead>
            <tr>
                <th><warehouse:message code="report.number.label"/></th>
                <g:if test="${requisitionItems.find { it.requisition?.shipment?.shipmentItems?.any { it.container }}}">
                    <th><warehouse:message code="packLevel1.label"/></th>
                </g:if>
                <g:if test="${requisitionItems.find { it.requisition?.shipment?.shipmentItems?.any { it.container && it.container?.parentContainer }}}">
                    <th><warehouse:message code="packLevel2.label"/></th>
                </g:if>
                <th>${warehouse.message(code: 'product.productCode.label')}</th>
                <th class="left">${warehouse.message(code: 'product.label')}</th>
                <th>${warehouse.message(code: 'deliveryNote.totalRequested.label', default: "Total Requested")}</th>
                <th>${warehouse.message(code: 'deliveryNote.totalDelivered.label', default: "Total Delivered")}</th>
                <th>${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th>${warehouse.message(code: 'deliveryNote.deliveredByLot.label', default: "Delivered by Lot")}</th>
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
                    <g:set var="inventoryItemMap" value="${requisitionItem?.retrievePicklistItems()?.findAll { it.quantity > 0 }?.groupBy { it?.inventoryItem }}"/>
                    <g:set var="shipmentItems" value="${requisitionItem?.requisition?.shipment?.shipmentItems?.findAll { it.requisitionItem == requisitionItem }}"/>
                    <g:set var="picklistItemsGroup" value="${inventoryItemMap?.values()?.toList()}"/>
                    <g:set var="numInventoryItem" value="${inventoryItemMap?.size() ?: 1}"/>
                </g:if>
                <g:else>
                    <g:set var="numInventoryItem" value="${1}"/>
                </g:else>
                <g:set var="backgroundColor" value="${(i % 2) == 0 ? '#fff' : '#f7f7f7'}"/>
                <g:set var="j" value="${0}"/>
                <g:while test="${j < numInventoryItem}">
                    <tr class="prop" style="background-color: ${backgroundColor}">
                        <g:if test="${j==0}">
                            <td class="center middle" rowspan="${numInventoryItem}">
                                    ${i + 1}
                            </td>
                            <g:if test="${requisitionItems.find { it.requisition?.shipment?.shipmentItems?.any { it.container }}}">
                            <td class="middle center" rowspan="${numInventoryItem}">
                                <g:each in="${shipmentItems}" var="shipmentItem">
                                    <div>
                                        ${shipmentItem.container?.parentContainer?.name ?: shipmentItem?.container?.name}
                                    </div>
                                </g:each>
                            </td>
                            </g:if>
                            <g:if test="${requisitionItems.find { it.requisition?.shipment?.shipmentItems?.any { it.container }}}">
                                <td class="center middle" rowspan="${numInventoryItem}">
                                    <g:each in="${shipmentItems}" var="shipmentItem">
                                        <div>
                                            <g:if test="${shipmentItem?.container?.parentContainer && shipmentItem.container}">
                                                ${shipmentItem.container?.name}
                                            </g:if>
                                            <g:elseif test="${shipmentItem?.container}">
                                                -
                                            </g:elseif>
                                        </div>
                                    </g:each>
                                </td>
                            </g:if>
                            <td class="center middle" rowspan="${numInventoryItem}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.productCode}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.productCode}
                            </td>
                            <td class="middle" rowspan="${numInventoryItem}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.name}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.name}
                            </td>
                            <td class="center middle" rowspan="${numInventoryItem}">
                                <g:if test="${j==0}">
                                    <g:if test="${requisitionItem.parentRequisitionItem?.isChanged()}">
                                        <div>
                                            ${requisitionItem?.parentRequisitionItem?.quantity ?: 0}
                                            ${requisitionItem?.parentRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div class="${requisitionItem?.status}">
                                            ${requisitionItem?.quantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                                        </div>
                                    </g:else>
                                </g:if>
                            </td>
                            <td class="center middle" rowspan="${numInventoryItem}">
                                <g:if test="${j==0}">
                                    <div class="${requisitionItem?.status}">
                                        ${requisitionItem?.totalQuantityPicked() ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                                    </div>
                                </g:if>
                            </td>
                        </g:if>
                        <td class="middle center">
                            <g:if test="${picklistItemsGroup}">
                                ${picklistItemsGroup[j]?.first()?.inventoryItem?.lotNumber}
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItemsGroup}">
                                <g:formatDate date="${picklistItemsGroup[j]?.first()?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                            </g:if>
                        </td>
                        <td class="center middle">
                            <g:if test="${picklistItemsGroup}">
                                <g:set var="picklistItemsGroupQuantity" value="${picklistItemsGroup[j]?.sum { it?.quantity }}"/>
                                ${picklistItemsGroupQuantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                        </td>
                        <g:if test="${j==0}">
                            <td class="middle" rowspan="${numInventoryItem}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.cancelReasonCode}">
                                    <g:if test="${requisitionItem.parentRequisitionItem?.isSubstituted()}">
                                        ${warehouse.message(code:'requisitionItem.substituted.label')}
                                    </g:if>
                                    <g:elseif test="${requisitionItem.parentRequisitionItem?.isChanged()}">
                                        ${warehouse.message(code:'requisitionItem.modified.label')}
                                    </g:elseif>
                                    <i>
                                        <g:set var="reasonCode" value="${requisitionItem?.parentRequisitionItem?.cancelReasonCode}"/>
                                        ${warehouse.message(code:'enum.ReasonCode.' + (reasonCode?.contains("(") ? reasonCode?.split("\\(",2)[1].replace(")","") : reasonCode))}
                                    </i>
                                    <g:if test="${requisitionItem?.parentRequisitionItem?.cancelComments}">
                                        <blockquote>
                                            ${requisitionItem?.parentRequisitionItem?.cancelComments}
                                        </blockquote>
                                    </g:if>
                                </g:if>
                                <g:if test="${requisitionItem?.cancelReasonCode}">
                                    <g:if test="${requisitionItem?.isCanceled()}">
                                        ${warehouse.message(code:'requisitionItem.canceled.label')}
                                    </g:if>
                                    <i>
                                        ${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.cancelReasonCode)}
                                    </i>
                                    <g:if test="${requisitionItem?.cancelComments}">
                                        <blockquote>
                                            ${requisitionItem?.cancelComments}
                                        </blockquote>
                                    </g:if>
                                </g:if>
                                <g:if test="${requisitionItem?.pickReasonCode}">
                                    <div>
                                        ${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.pickReasonCode)}
                                    </div>
                                </g:if>
                            </td>
                        </g:if>

                        <% j++ %>
                    </tr>
                </g:while>
            </g:each>
        </tbody>
    </table>
</div>
