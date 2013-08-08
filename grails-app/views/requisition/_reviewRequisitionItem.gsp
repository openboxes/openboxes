<g:set var="selected" value="${requisitionItem == selectedRequisitionItem}"/>
<g:set var="quantityOnHand" value="${quantityOnHandMap[requisitionItem?.product?.id]} "/>
<g:set var="quantityOnHandForSubstitution" value="${quantityOnHandMap[requisitionItem?.substitution?.product?.id]} "/>
<g:set var="quantityRemaining" value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<%-- Need to hack this in since the quantityOnHand value was a String --%>
<g:set var="isCanceled" value="${requisitionItem?.isCanceled()}"/>
<g:set var="isChanged" value="${requisitionItem?.isChanged()}"/>
<g:set var="hasSubstitution" value="${requisitionItem?.hasSubstitution()}"/>
<g:set var="quantityOnHand" value="${quantityOnHand.toInteger()}"/>
<g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>
<g:set var="isAvailableForSubstitution" value="${hasSubstitution && (quantityOnHandForSubstitution > 0) && (quantityOnHandForSubstitution >= requisitionItem?.substitution?.totalQuantity()) }"/>
<%--<tr class="${(i % 2) == 0 ? 'even' : 'odd'} ${!selectedRequisitionItem?'':selected?'selected':'unselected'} ${isAvailable?'':'error'}">--%>
<tr class="${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled())?'canceled':''} ())?'canceled':''}">
    <%--${isAvailable?'success':'error'}--%>
    <td class="left">
        <a name="${selectedRequisitionItem?.id}"></a>
        <g:if test="${!isChild }">
            <g:render template="/requisitionItem/actions" model="[requisition:requisition,requisitionItem:requisitionItem]"/>
        </g:if>
    </td>
    <td class="center">
        <div class="${isCanceled?'canceled':''}" title="${requisitionItem?.cancelReasonCode}">
            ${requisitionItem.status}
        </div>
        <%--
        <g:if test="${requisitonItem?.isApproved()}">
            <warehouse:message code="enum.RequisitionItemStatus.APPROVED" default="Approved"/>
        </g:if>

        <g:if test="${requisitionItem?.isCanceled()}">
            <warehouse:message code="enum.RequisitionItemStatus.CANCELLED" default="Cancelled"/>
            <g:if test="${requisitionItem?.isSubstitution()}">
                <warehouse:message code="enum.requisitionItemStatus.SUBSTITUTED" default="Substituted"/>
            </g:if>
        </g:if>
        <g:elseif test="${requisitionItem?.isChanged()}">
            <warehouse:message code="enum.requisitionItemStatus.CHANGED" default="Changed"/>
        </g:elseif>
        <g:else>
            ${warehouse.message(code:'default.pending.label')}
        </g:else>
        --%>
    </td>
    <td class="center">
        ${requisitionItem?.product?.productCode}
    </td>


    <td class="product">
        <%--
		<g:if test="${isChild }">
			<img src="${resource(dir: 'images/icons', file: 'indent.gif')}" class="middle"/>
		</g:if>
        --%>
        <g:if test="${isCanceled||hasSubstitution}">
            <div class="canceled">
                ${requisitionItem?.product?.productCode}
                <format:metadata obj="${requisitionItem?.product?.name}" />
            </div>
            <div class="">
                ${requisitionItem?.change?.product?.productCode}
                <format:metadata obj="${requisitionItem?.change?.product?.name}" />
            </div>
        </g:if>
        <g:else>
            <div>
                ${requisitionItem?.product?.productCode}
                <format:metadata obj="${requisitionItem?.product?.name}" />
            </div>
        </g:else>

    </td>
    <td class="quantity center">
        <div class="${isCanceled||isChanged?'canceled':''}">
            ${requisitionItem?.quantity}
            ${requisitionItem?.productPackage?.uom?.code?:"EA" }/${requisitionItem?.productPackage?.quantity?:"1" }
        </div>
        <g:if test="${requisitionItem?.change}">
            ${requisitionItem?.change?.quantity}
            ${requisitionItem?.change?.productPackage?.uom?.code?:"EA"}/${requisitionItem?.change?.productPackage?.quantity?:"1"}
        </g:if>
    </td>
    <td class="center">
        <div class="${isCanceled||isChanged?'canceled':''}">
            ${requisitionItem?.totalQuantity()} EA/1
        </div>
        <g:if test="${requisitionItem?.change}">
            ${requisitionItem?.change?.totalQuantity()} EA/1
        </g:if>
    </td>
    <td class="center">
        <g:if test="${isAvailable||isAvailableForSubstitution}">
            <div class="available">
                ${warehouse.message(code:'inventory.available.label', default:'Available')}
            </div>
        </g:if>
        <g:else>
            <div class="unavailable">
                <%--
                <img src="${resource(dir: 'images/icons/silk', file: 'emoticon_unhappy.png')}" />
                --%>
                ${warehouse.message(code:'inventory.unavailable.label',default:'Unavailable')}
            </div>
        </g:else>
        <g:if test="${requisitionItem?.hasSubstitution()}">
            <div class="${isCanceled||isChanged?'canceled':''}">${quantityOnHand?:0} EA/1</div>
            ${quantityOnHandForSubstitution?:0} EA/1
        </g:if>
        <g:else>
            ${quantityOnHand?:0} EA/1
        </g:else>
    </td>
    <td class="center">
        ${requisitionItem.orderIndex+1}
    </td>

    <%--
    <td class="quantity center">
        <label>${requisitionItem?.totalQuantityCanceled()} EA</label>
        <g:if test="${requisitionItem?.productPackage}">
            <div class="fade box">
                ${requisitionItem?.quantityCanceled} x ${(requisitionItem?.productPackage?.quantity?:1) } ${(requisitionItem?.productPackage?.uom?.code?:"EA")}
            </div>
        </g:if>
    </td>
    --%>

	<%-- 
	<td class="quantity right">
		${quantityAvailableToPromiseMap[requisitionItem?.product?.id]} 
		${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	--%>

</tr>
