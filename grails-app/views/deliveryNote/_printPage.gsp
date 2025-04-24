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
                <th>${g.message(code: 'product.productCode.label')}</th>
                <th class="left">${g.message(code: 'product.label')}</th>
                <th>${g.message(code: 'deliveryNote.totalRequested.label', default: "Total Requested")}</th>
                <th>${g.message(code: 'deliveryNote.totalDelivered.label', default: "Total Delivered")}</th>
                <th>${g.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${g.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th>${g.message(code: 'deliveryNote.splitQuantity.label', default: "Split Quantity")}</th>
                <th>${g.message(code: 'requisitionItem.cancelReasonCode.label')}</th>
                <th>${g.message(code: 'deliveryNote.received.label', default: "Received")}</th>
                <th>${g.message(code: 'deliveryNote.comment.label', default: "Comment")}</th>
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
            <g:if test="${"PRODUCT".equalsIgnoreCase(sortOrder as String)}">
                %{-- Requisition items are already sorted by product at this point so no need to re-sort. --}%
            </g:if>
            <g:else>
                <g:set var="requisitionItems" value="${requisitionItems?.sort()}"/>
            </g:else>
            <g:each in="${requisitionItems}" status="i" var="requisitionItem">
                <g:if test="${picklist}">
                    <g:set var="inventoryItemMap" value="${requisitionItem?.retrievePicklistItems()?.findAll { it.quantity > 0 }?.groupBy { it?.inventoryItem }}"/>
                    <g:set var="shipmentItems" value="${requisitionItem?.requisition?.shipment?.shipmentItems?.findAll { it.requisitionItem == requisitionItem }}"/>
                    <g:set var="picklistItemsGroup" value="${inventoryItemMap?.values()?.toList()}"/>
                    <g:set var="shipmentItemCount" value="${shipmentItems.size() ?: 1}"/>
                </g:if>
                <g:set var="backgroundColor" value="${(i % 2) == 0 ? '#fff' : '#f7f7f7'}"/>
                <g:set var="j" value="${0}"/>
                <g:while test="${j < shipmentItemCount}">
                    <g:if test="${shipmentItems}">
                        <g:set var="shipmentItem" value="${shipmentItems[j]}"/>
                        <g:set var="inventoryItem" value="${shipmentItems[j].inventoryItem}"/>
                    </g:if>
                    <g:elseif test="${picklistItemsGroup}">
                        <g:set var="inventoryItem" value="${picklistItemsGroup[j]?.first()?.inventoryItem}" />
                    </g:elseif>
                    <g:else>
                        <g:set var="inventoryItem" value="${requisitionItem?.shipmentItems?.size() > 0 ? requisitionItem?.shipmentItems?.toList()?.first()?.inventoryItem : null}"/>
                    </g:else>
                    <tr class="prop" style="background-color: ${backgroundColor}">
                        <g:if test="${j==0}">
                            <td class="center middle" rowspan="${shipmentItemCount}">
                                ${i + 1}
                            </td>
                        </g:if>
                        <g:if test="${requisitionItems.find { it.requisition?.shipment?.hasChildContainer()}}">
                            <td class="middle center">
                                ${shipmentItem?.container?.parentContainer?.name ?: shipmentItem?.container?.name}
                            </td>
                        </g:if>
                        <g:if test="${requisitionItems.find { it.requisition?.shipment?.hasParentContainer()}}">
                            <td class="center middle">
                                ${shipmentItem?.container?.parentContainer ? shipmentItem?.container?.name : ''}
                            </td>
                        </g:if>
                        <g:if test="${j==0}">
                            <td class="center middle" rowspan="${shipmentItemCount}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.productCode}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.productCode}
                            </td>
                            <td class="middle" rowspan="${shipmentItemCount}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.product?.displayNameOrDefaultName}
                                    </div>
                                </g:if>
                                ${requisitionItem?.product?.displayNameOrDefaultName}
                            </td>
                            <td class="center middle" rowspan="${shipmentItemCount}">
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
                            </td>
                            <td class="center middle" rowspan="${shipmentItemCount}">
                                <div class="${requisitionItem?.status}">
                                    ${requisitionItem?.totalQuantityPicked() ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                                </div>
                            </td>
                        </g:if>
                        <td class="middle center">
                            <g:if test="${inventoryItem}">
                                ${inventoryItem?.lotNumber}
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${inventoryItem}">
                                <g:formatDate date="${inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                            </g:if>
                        </td>
                        <td class="center middle">
                            <g:if test="${shipmentItem}">
                                ${shipmentItem?.quantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                            <g:elseif test="${picklistItemsGroup}">
                                <g:set var="picklistItemsGroupQuantity" value="${picklistItemsGroup[j]?.sum { it?.quantity }}"/>
                                ${picklistItemsGroupQuantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:elseif>
                        </td>
                        <g:if test="${j==0}">
                            <td class="middle" rowspan="${shipmentItemCount}">
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
                        <td class="middle">
                            <g:if test="${shipmentItem?.quantityReceived}">
                                ${shipmentItem.quantityReceived}
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${shipmentItem?.comments}">
                                ${shipmentItem?.getComments()?.join(', ')}
                            </g:if>
                        </td>
                        <% j++ %>
                    </tr>
                </g:while>
            </g:each>
        </tbody>
    </table>
</div>
