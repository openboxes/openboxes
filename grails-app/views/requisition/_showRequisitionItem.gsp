<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.requisition.RequisitionItemStatus" %>
<g:set var="quantityRemaining" value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<tr class="${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled())?'canceled':''}">

    <td class="middle center">
        ${i+1}
    </td>
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
    <%--
    <td>
        <g:set var="value" value="${formatNumber(number:requisitionItem.calculatePercentageCompleted(),maxFractionDigits: 0) }" />
        <div id="progressbar-${requisitionItem?.id }" class="progressbar" style="width: 100px;"></div>
        <script type="text/javascript">
            $(function() {
                $( "#progressbar-${requisitionItem?.id }" ).progressbar({value: ${value}});
            });
        </script>
    </td>
    <td>
        ${value }%
    </td>
    --%>
    <td class="middle">
        <div class="tag tag-alert">
            <g:if test="${requisitionItem?.status==RequisitionItemStatus.APPROVED && requisitionItem?.requisition?.status == RequisitionStatus.ISSUED}">
                <format:metadata obj="${requisitionItem?.requisition?.status}"/>
            </g:if>
            <g:else>
                <format:metadata obj="${requisitionItem?.status}"/>
            </g:else>
        </div>
    </td>
    <td class="middle">
        <%--
        <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id }">
            <img src="${resource(dir:'images/icons/silk',file:'clipboard.png')}"/>
        </g:link>
        --%>
        <g:if test="${requisitionItem?.isCanceled()}">
            <div class="canceled">
                <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                    ${requisitionItem?.product?.productCode}
                    ${requisitionItem?.product?.name}
                </g:link>
            </div>
        </g:if>
        <g:elseif test="${requisitionItem?.isSubstituted()}">
            <div class="canceled">
            <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                ${requisitionItem?.product?.productCode}
                ${requisitionItem?.product?.name}
            </g:link>
            </div>
            <div>
            <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.substitutionItem?.product?.id}">
                ${requisitionItem?.substitutionItem?.product?.productCode}
                ${requisitionItem?.substitutionItem?.product?.name}
            </g:link>
            </div>
        </g:elseif>
        <g:else>
            <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
            ${requisitionItem?.product?.productCode}
            <format:product product="${requisitionItem?.product}"/>
            </g:link>
        </g:else>
    </td>
    <td class="middle center">
        ${requisitionItem?.product?.unitOfMeasure?:"EA" }
    </td>
    <td class="middle center border-left">
        <g:if test="${requisitionItem?.isSubstituted()}">
            <span class="canceled">${requisitionItem?.quantity?:0}</span>
            <span>${requisitionItem?.substitutionItem?.quantity?:0}</span>
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
            <div>
                ${requisitionItem?.substitutionItem?.quantityApproved?:0}
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
                ${requisitionItem?.substitutionItem?.calculateQuantityPicked()?:0}
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
</tr>

