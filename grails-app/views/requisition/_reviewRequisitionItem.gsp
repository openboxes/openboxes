<g:set var="quantityOnHand" 
	value="${quantityOnHandMap[requisitionItem?.product?.id]} "/>
<g:set var="quantityRemaining"
	value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<g:set var="isAvailable" value="${quantityOnHand > 0 && quantityOnHand > requisitionItem?.quantity }"/>
<tr class="${(i % 2) == 0 ? 'even' : 'odd'} ${isAvailable?'success':'error'}" >
    <%--
	<td class="center">
		<g:checkBox name="selectItem" class="selectItem" id="${requsitionItem?.id }"/>
	</td>
	--%>
	<td class="product">
		<g:if test="${isChild }">
			<img src="${resource(dir: 'images/icons', file: 'indent.gif')}" class="middle"/>
		</g:if>
		<format:metadata
			obj="${requisitionItem?.product?.name}" />
        (${requisitionItem?.productPackage?.uom?.code?:"EA" }/${requisitionItem?.productPackage?.quantity?:1 })

    </td>
	<td class="quantity right">
		${requisitionItem?.quantity * (requisitionItem?.productPackage?.quantity?:1) }
        ${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	<td class="quantity right error">
		${quantityOnHand?:0 }
		${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	<%-- 
	<td class="quantity right">
		${quantityAvailableToPromiseMap[requisitionItem?.product?.id]} 
		${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	--%>
	<td>
		<g:link controller="requisitionItem" action="change" id="${requisitionItem?.id }" class="button">
			<warehouse:message code="default.button.change.label"/>
		</g:link>
		<g:link controller="requisitionItem" action="delete" id="${requisitionItem?.id }" class="button"
                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
			<warehouse:message code="default.button.delete.label"/>
		</g:link>
	</td>
</tr>