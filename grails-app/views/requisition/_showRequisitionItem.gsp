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
        <format:metadata obj="${requisitionItem?.status}"/>
    </td>
    <td class="middle">
        <%--
        <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id }">
            <img src="${resource(dir:'images/icons/silk',file:'clipboard.png')}"/>
        </g:link>
        --%>
        <g:if test="${requisitionItem?.isCanceled()}">
            <div class="canceled">
                ${requisitionItem?.product?.productCode}
                ${requisitionItem?.product?.name} (${requisitionItem?.product?.unitOfMeasure})
            </div>
        </g:if>
        <g:elseif test="${requisitionItem?.isSubstituted()}">
            <div class="canceled">
                ${requisitionItem?.product?.productCode}
                ${requisitionItem?.product?.name} (${requisitionItem?.product?.unitOfMeasure})
            </div>
            <div>
                ${requisitionItem?.substitutionItem?.product?.productCode}
                ${requisitionItem?.substitutionItem?.product?.name} (${requisitionItem?.substitutionItem?.product?.unitOfMeasure})
            </div>
        </g:elseif>
        <g:else>
            ${requisitionItem?.product?.productCode}
            <format:product product="${requisitionItem?.product}"/>
            (${requisitionItem?.product?.unitOfMeasure})
        </g:else>
    </td>
    <td class="middle center">
        ${requisitionItem?.product?.unitOfMeasure?:"EA" }
    </td>
    <td class="middle center border-left">
        <g:if test="${requisitionItem?.isSubstituted()}">
            <div class="canceled">
                ${requisitionItem?.quantity?:0}
            </div>
            <div>
                ${requisitionItem?.substitutionItem?.quantity?:0}
            </div>
        </g:if>
        <g:elseif test="${requisitionItem?.isChanged()}">
            <div class="canceled">
                ${requisitionItem?.quantity?:0}
            </div>
            <div>
                ${requisitionItem?.modificationItem?.quantity?:0}
            </div>
        </g:elseif>
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
            <div class="canceled">
                ${requisitionItem?.quantityApproved?:0}
            </div>
            <div>
                ${requisitionItem?.substitutionItem?.quantityApproved?:0}
            </div>
        </g:if>
        <g:elseif test="${requisitionItem?.isChanged()}">
            <div class="canceled">
                ${requisitionItem?.quantityApproved?:0}
            </div>
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
    <td class="middle center border-left">
        <g:if test="${requisitionItem?.isSubstituted()}">
            <div class="canceled">
                ${requisitionItem?.calculateQuantityPicked()?:0}
            </div>
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
            <div class="canceled">
                ${requisitionItem?.calculateQuantityPicked()?:0}
            </div>
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
            <div class="canceled">
                ${requisitionItem?.calculateQuantityRemaining()?:0}
            </div>
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
            <div class="canceled">
                ${requisitionItem?.calculateQuantityRemaining()?:0}
            </div>
            <div>
                ${requisitionItem?.modificationItem?.calculateQuantityRemaining()?:0}
            </div>
        </g:elseif>
        <g:else>
            ${requisitionItem?.calculateQuantityRemaining()?:0}
        </g:else>

    </td>
</tr>

