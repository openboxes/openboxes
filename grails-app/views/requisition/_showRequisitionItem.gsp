<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.requisition.RequisitionItemStatus" %>
<g:set var="quantityRemaining" value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<tr class="${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled())?'canceled':''}">

    <td class="middle center">
        ${i+1}
    </td>
    <g:if test="${!requestTab}">
        <td class="middle center">
            <g:if test="${requisitionItem?.isSubstituted()}">
                <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"
            </g:if>
            <g:elseif test="${requisitionItem?.isSubstitution()}">
                <img src="${resource(dir:'images/icons',file:'indent.gif')}"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isChanged()}">
                <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isPending()}">
                <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isCanceled()}">
                <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
                <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"/>
            </g:elseif>
        </td>
    </g:if>
    <td class="middle">
        <div class="tag ${requisitionItem.isCanceled() || requisitionItem.isCanceledDuringPick() ? 'tag-danger' :
                requisitionItem.isSubstituted() || requisitionItem.isReduced() ? 'tag-warning' : 'tag-alert'}">
            <g:if test="${requisitionItem.isCanceled() || requisitionItem.isCanceledDuringPick()}">
                <g:message code="enum.RequisitionItemStatus.CANCELED"/>
            </g:if>
            <g:elseif test="${requisitionItem.isReduced() && !requisitionItem.isSubstituted()}">
                <g:message code="enum.RequisitionItemStatus.REDUCED"/>
            </g:elseif>
            <g:elseif test="${requisitionItem.isIncreased() && !requisitionItem.isSubstituted()}">
                <g:message code="enum.RequisitionItemStatus.INCREASED"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.status==RequisitionItemStatus.APPROVED && requisitionItem?.requisition?.status == RequisitionStatus.ISSUED}">
                <format:metadata obj="${requisitionItem?.requisition?.status}"/>
            </g:elseif>
            <g:else>
                <format:metadata obj="${requisitionItem?.status}"/>
            </g:else>
        </div>
    </td>
    <g:if test="${requestTab}">
        <td class="middle">
            <g:if test="${requisitionItem?.isCanceled()}">
                <div class="canceled">
                    <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                        ${requisitionItem?.product?.productCode}
                    </g:link>
                </div>
            </g:if>
            <g:elseif test="${requisitionItem?.isSubstituted()}">
                <div class="canceled">
                    <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                        ${requisitionItem?.product?.productCode}
                    </g:link>
                </div>
                <g:each var="substitutionItem" in="${requisitionItem.substitutionItems}">
                    <div>
                        <g:link controller="inventoryItem" action="showStockCard" id="${substitutionItem?.product?.id}">
                            ${substitutionItem?.product?.productCode}
                        </g:link>
                    </div>
                </g:each>
            </g:elseif>
            <g:else>
                <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                    ${requisitionItem?.product?.productCode}
                </g:link>
            </g:else>
        </td>
    </g:if>
    <td class="middle">
        <g:if test="${requisitionItem?.isCanceled()}">
            <div class="canceled">
                <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                    ${requisitionItem?.product?.name}
                </g:link>
            </div>
        </g:if>
        <g:elseif test="${requisitionItem?.isSubstituted()}">
            <div class="canceled">
            <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                ${requisitionItem?.product?.name}
            </g:link>
            </div>
            <g:each var="substitutionItem" in="${requisitionItem.substitutionItems}">
                <div>
                    <g:link controller="inventoryItem" action="showStockCard" id="${substitutionItem?.product?.id}">
                        ${substitutionItem?.product?.name}
                    </g:link>
                </div>
            </g:each>
        </g:elseif>
        <g:else>
            <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
            <format:product product="${requisitionItem?.product}"/>
            </g:link>
        </g:else>
    </td>
    <td class="middle center">
        ${requisitionItem?.product?.unitOfMeasure?:"EA" }
    </td>
    <td class="middle center border-left">
        <g:if test="${requisitionItem?.isSubstituted()}">
            <g:if test="${requisitionItem?.quantity == requisitionItem.substitutionItems.sum{ it.quantity }}">
                ${requisitionItem?.quantity?:0}
            </g:if>
            <g:else>
                <div class="canceled">${requisitionItem?.quantity?:0}</div>
                <g:each var="substitutionItem" in="${requisitionItem.substitutionItems}">
                    <div>
                        <g:link controller="inventoryItem" action="showStockCard" id="${substitutionItem?.product?.id}">
                            ${substitutionItem?.quantity}
                        </g:link>
                    </div>
                </g:each>
            </g:else>
        </g:if>
        <g:elseif test="${requisitionItem?.isCanceled()}">
            <div class="canceled">
                ${requisitionItem?.quantity?:0}
            </div>
        </g:elseif>
        <g:else>
            ${requisitionItem?.quantity?:0}
        </g:else>
    </td>
    <td class="middle center">
        <g:if test="${requisitionItem?.isSubstituted()}">
            <g:set var="totalQtyApproved" value="${requisitionItem.substitutionItems.sum { it.quantityApproved }}"/>
            <div>
                ${totalQtyApproved?:0}
            </div>
        </g:if>
        <g:elseif test="${requisitionItem?.isChanged()}">
            <div>
                ${requisitionItem?.modificationItem?.quantityApproved?:0}
            </div>
        </g:elseif>
        <g:elseif test="${requisitionItem?.isCanceled()}">
            <div class="canceled">
                ${requisitionItem?.quantityApproved?:0}
            </div>
        </g:elseif>
        <g:else>
            <div class="approved">
                ${requisitionItem?.quantityApproved?:0}
            </div>
        </g:else>

    </td>
    <td class="middle center">
        <g:if test="${requisitionItem?.isSubstituted()}">
            <div>
                ${requisitionItem?.calculateQuantityPicked()?:0}
            </div>
        </g:if>
        <g:elseif test="${requisitionItem?.isCanceled()}">
            <div class="canceled">
                ${requisitionItem?.calculateQuantityPicked()?:0}
            </div>
        </g:elseif>
        <g:elseif test="${requisitionItem?.isChanged()}">
            <div>
                ${requisitionItem?.modificationItem?.calculateQuantityPicked()?:0}
            </div>
        </g:elseif>
        <g:else>
            ${requisitionItem?.calculateQuantityPicked()?:0}
        </g:else>

    </td>
    <g:if test="${!requestTab}">
        <td class="middle center">
            <g:if test="${requisitionItem?.isSubstituted()}">
                <div>
                    ${requisitionItem?.substitutionItem?.calculateQuantityRemaining()?:0}
                </div>
            </g:if>
            <g:elseif test="${requisitionItem?.isCanceled()}">
                <div class="canceled">
                    ${requisitionItem?.calculateQuantityRemaining()?:0}
                </div>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isChanged()}">
                <div>
                    ${requisitionItem?.modificationItem?.calculateQuantityRemaining()?:0}
                </div>
            </g:elseif>
            <g:else>
                ${requisitionItem?.calculateQuantityRemaining()?:0}
            </g:else>
        </td>
    </g:if>
    <g:if test="${requestTab}">
        <td class="middle center">
            <g:if test="${requisitionItem?.isCanceled()}">
                <div>
                    ${requisitionItem?.quantityAdjusted?:0}
                </div>
            </g:if>
            <g:else>
                ${requisitionItem?.quantityAdjusted?:0}
            </g:else>
        </td>
        <td class="middle center">
            <g:if test="${requisitionItem?.isSubstituted()}">
                <div>
                    ${requisitionItem?.quantityIssued?:0}
                </div>
            </g:if>
            <g:elseif test="${requisitionItem?.isCanceled()}">
                <div class="canceled">
                    ${requisitionItem?.quantityIssued?:0}
                </div>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isChanged()}">
                <div>
                    ${requisitionItem?.modificationItem?.quantityIssued?:0}
                </div>
            </g:elseif>
            <g:else>
                ${requisitionItem?.quantityIssued?:0}
            </g:else>
        </td>
        <td class="middle center">
            <g:set var="pickReasonCode" value="${requisitionItem?.modificationItem?.pickReasonCode ?: requisitionItem?.substitutionItem?.pickReasonCode ?: requisitionItem?.pickReasonCode}"/>
            <g:if test="${requisitionItem?.cancelReasonCode || pickReasonCode }">
                <div title="${requisitionItem?.cancelReasonCode ? 'Edit reason code: ' + requisitionItem?.cancelReasonCode : ''}
${pickReasonCode ? 'Pick reason code: ' + pickReasonCode : ''}">
                    <img src="${createLinkTo(dir:'images/icons/silk',file:'note.png')}" />
                </div>
            </g:if>
            <g:else>
                <div class="fade"><g:message code="default.empty.label"/></div>
            </g:else>
        </td>
    </g:if>
</tr>

