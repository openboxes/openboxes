<g:set var="selected" value="${requisitionItem == selectedRequisitionItem }"/>
<g:set var="noneSelected" value="${!selectedRequisitionItem }"/>
<tr class="${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled()||requisitionItem?.isChanged())?'canceled':''} ${selected ?'selected-middle':'unselected'}">
    <td class="left">
        <a name="${selectedRequisitionItem?.id}"></a>
        <g:if test="${!isChild }">
            <g:render template="/requisitionItem/actions" model="[requisition:requisition,requisitionItem:requisitionItem]"/>
        </g:if>
    </td>
    <td>
        ${requisitionItem?.product?.productCode}
    </td>
    <td>
        <g:link controller="requisition" action="pick" id="${requisition.id }" params="['requisitionItem.id':requisitionItem?.id]">
            <format:product product="${requisitionItem?.product}"/>
        </g:link>
    </td>
    <td>
        <g:if test="${requisitionItem?.productPackage}">
            ${requisitionItem?.productPackage?.uom?.code}/${requisitionItem?.productPackage?.quantity}
        </g:if>
        <g:else>
            ${requisitionItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
        </g:else>
    </td>
    <td class="center">
        ${requisitionItem?.quantity?:0 }
    </td>
    <td class="center">
        ${requisitionItem?.calculateQuantityPicked()?:0 }
    </td>
    <td class="center">
        ${requisitionItem?.quantityCanceled?:0}
    </td>
    <td class="center">
        ${requisitionItem?.calculateQuantityRemaining()?:0 }
    </td>
    <td>
        <g:set var="value" value="${((requisitionItem?.calculateQuantityPicked()?:0)+(requisitionItem?.quantityCanceled?:0))/(requisitionItem?.quantity?:1) * 100 }" />
        <div id="progress-bar-${requisitionItem?.id }" class="progress-bar" style="width: 100px;"></div>
        <script type="text/javascript">
            $(function() {
                $( "#progress-bar-${requisitionItem?.id }" ).progressbar({value: ${value} });
            });
        </script>
    </td>
    <td>
        ${formatNumber(number: value, maxFractionDigits: 0)}%
    </td>
</tr>

