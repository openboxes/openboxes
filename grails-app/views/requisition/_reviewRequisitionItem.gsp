<g:set var="quantityOnHand" 
	value="${quantityOnHandMap[requisitionItem?.product?.id]} "/>
<g:set var="quantityRemaining"
	value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<g:set var="isAvailable" value="${quantityOnHand > 0 && quantityOnHand > requisitionItem?.quantity }"/>
<tr class="${(i % 2) == 0 ? 'even' : 'odd'} ${isAvailable?'success':'error'}" >
	<td class="center">
		<g:checkBox name="selectItem" class="selectItem" id="${requsitionItem?.id }"/>
	</td>
	<td>
		<img src="${createLinkTo(dir:'images/icons/silk',file:'hourglass.png')}" />
		<span class="status"><warehouse:message code="default.status.pending.label" default="Pending"/></span>	
	</td>
	<td class="product">										
		<g:if test="${isChild }">
			<img src="${resource(dir: 'images/icons', file: 'indent.gif')}" class="middle"/>
		</g:if>
		<format:metadata
			obj="${requisitionItem?.product?.name}" />
	</td>
	<td class="quantity right">
		${requisitionItem?.quantity} 
		${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	<td class="quantity right">
		${quantityOnHand }
		${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	<%-- 
	<td class="quantity right">
		${quantityAvailableToPromiseMap[requisitionItem?.product?.id]} 
		${requisitionItem?.product.unitOfMeasure?:"EA" }
	</td>
	--%>									
	
	
	<td>
		<g:formatDate date="${requisitionItem?.lastUpdated }" format="MMM dd, yyyy hh:mm a"/>
	</td>
	<td>
		<g:link controller="requisitionItem" action="change" id="${requisitionItem?.id }" class="button">
			<warehouse:message code="default.button.change.label"/>
		</g:link>
		<g:link controller="requisitionItem" action="delete" id="${requisitionItem?.id }" class="button">
			<warehouse:message code="default.button.delete.label"/>
		</g:link>
	</td>
</tr>