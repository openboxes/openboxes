<g:set var="selected" value="${requisitionItem == selectedRequisitionItem}"/>
<g:set var="quantityOnHand"
	value="${quantityOnHandMap[requisitionItem?.product?.id]} "/>
<g:set var="quantityRemaining"
	value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<%-- Need to hack this in since the quantityOnHand value was a String --%>
<g:set var="quantityOnHand" value="${quantityOnHand.toInteger()}"/>
<g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand > requisitionItem?.totalQuantity()) }"/>
<tr class="${(i % 2) == 0 ? 'even' : 'odd'} ${!selectedRequisitionItem?'':selected?'selected':'unselected'}" ><%--${isAvailable?'success':'error'}--%>
    <td>
        <g:if test="${isAvailable}">
            <div class="success">${warehouse.message(code:'inventory.available.label', default:'Available')}</div>
        </g:if>
        <g:else>
            <div class="error">${warehouse.message(code:'inventory.unavailable.label',default:'Unavailable')}</div>
        </g:else>
    </td>
	<td class="product">
		<g:if test="${isChild }">
			<img src="${resource(dir: 'images/icons', file: 'indent.gif')}" class="middle"/>
		</g:if>
		<format:metadata
			obj="${requisitionItem?.product?.name}" />
        (${requisitionItem?.productPackage?.uom?.code?:"EA" }/${requisitionItem?.productPackage?.quantity?:1 })
    </td>
    <td class="quantity center">
        <label>${requisitionItem?.totalQuantity()} EA</label>
        <g:if test="${requisitionItem?.productPackage}">
            <div class="fade box">
                ${requisitionItem?.quantity} x ${(requisitionItem?.productPackage?.quantity?:1)} ${(requisitionItem?.productPackage?.uom?.code?:"EA")}
            </div>
        </g:if>
    </td>
    <td class="quantity center">
        <label>${requisitionItem?.totalQuantityCanceled()} EA</label>
        <g:if test="${requisitionItem?.productPackage}">
            <div class="fade box">
                ${requisitionItem?.quantityCanceled} x ${(requisitionItem?.productPackage?.quantity?:1) } ${(requisitionItem?.productPackage?.uom?.code?:"EA")}
            </div>
        </g:if>
    </td>
	<td class="quantity center">
        <label>${quantityOnHand?:0 } ${requisitionItem?.product.unitOfMeasure?:"EA" }</label>
	</td>
	<%-- 
	<td class="quantity right">
		${quantityAvailableToPromiseMap[requisitionItem?.product?.id]} 
		${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	--%>
	<td class="center">
        <%--
        <g:link controller="requisition" action="review" id="${requisition?.id }" params="['requisitionItem.id':requisitionItem?.id]" class="button">
            <warehouse:message code="default.button.change.label"/>
        </g:link>
        --%>
        <g:link controller="requisitionItem" action="change" id="${requisitionItem?.id }" class="button">
            <warehouse:message code="default.button.change.label"/>
        </g:link>
        <g:link controller="requisitionItem" action="delete" id="${requisitionItem?.id }" class="button"
                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
            <warehouse:message code="default.button.delete.label"/>
        </g:link>
        <%--
        <g:link controller="requisitionItem" action="uncancel" id="${requisitionItem?.id }" class="button"
                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
            <warehouse:message code="default.button.uncancel.label"/>
        </g:link>
        --%>
	</td>
</tr>
