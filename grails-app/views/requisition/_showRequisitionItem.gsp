<g:set var="quantityRemaining" value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<tr class="${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled()||requisitionItem?.isChanged())?'canceled':''}">
    <td>
        <g:if test="${requisitionItem?.isCanceled()}">
            <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"
        </g:if>
        <g:elseif test="${requisitionItem?.isChanged()}">
            <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"
        </g:elseif>
        <g:elseif test="${requisitionItem?.isSubstituted()}">
            <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"
        </g:elseif>
        <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
            <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"
        </g:elseif>
        <g:elseif test="${requisitionItem?.isPending()}">
            <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"
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
    <td class="product">
        <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id }">
            ${requisitionItem?.product?.productCode}
            <format:metadata obj="${requisitionItem?.product?.name}" />
            <g:if test="${requisitionItem?.productPackage}">
                (${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity})
            </g:if>
            <g:else>
                (EA/1)
            </g:else>
        </g:link>

    </td>
    <td class="quantity center">
        <g:showQuantity requisitionItem="${requisitionItem}"/>
    </td>
    <td class="quantity center">
        ${requisitionItem?.totalQuantity()}
    </td>
    <td class="quantityPicked center">
        ${requisitionItem?.calculateQuantityPicked()?:0}
    </td>
    <td class="quantityCanceled center">
        ${requisitionItem?.quantityCanceled?:0}
    </td>
    <td class="quantityRemaining center">
        ${requisitionItem?.calculateQuantityRemaining()?:0}
    </td>
    <td>
        ${requisitionItem?.product?.unitOfMeasure?:"EA" }
    </td>
    <td class="center">
        ${requisitionItem?.orderIndex}
    </td>

</tr>

