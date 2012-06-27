<tr class="${counter%2==0?'even':'odd' } ${cssClass}">
	<td class="middle center">
		<g:checkBox id="${inventoryItem?.productGroup?.id }" name="productGroup.id" 
				class="checkbox" style="top:0em;" checked="${false }" 
					value="${inventoryItem?.productGroup?.id }" disabled="true"/>
	</td>																
	<td class="center middle">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'magnifier.png')}" id="${inventoryItem?.productGroup?.id }" class="expandable" alt="" class="middle"/>
		<%-- 
		<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="" class="middle"/>
		<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_toggle_minus.png')}" alt="" class="middle"/>
		--%>
	</td>
	<td class="middle">
		<span id="${inventoryItem?.productGroup?.id }" class="expandable">
		${inventoryItem?.productGroup?.description } (${inventoryItem?.productGroup?.products?.size() })		
		</span>
		<%--
		${inventoryItem?.productGroup?.products }
		--%>
		<%-- 
		<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
			<g:if test="${inventoryItem?.product?.name?.trim()}">
				<format:product product="${inventoryItem?.product}"/> 
			</g:if>
			<g:else>
				<warehouse:message code="product.untitled.label"/>
			</g:else>
		</g:link> 
		--%>
	</td>
	<td class="checkable middle center" style="width: 20%">
		<span class="fade">
			<warehouse:message code="default.various.label"/>
		</span>
		
	</td>
	<td class="checkable middle center" style="width: 7%; border-left: 1px solid lightgrey;">
		<g:if test="${inventoryItem?.supported }">																
			${inventoryItem?.quantityToReceive?:0}
		</g:if>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
	<td class="checkable middle center" style="width: 7%; border-right: 1px solid lightgrey;">
		<g:if test="${inventoryItem?.supported }">																
			${inventoryItem?.quantityToShip?:0}
		</g:if>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
	<td class="checkable middle center" style="width: 7%;">
		<g:if test="${inventoryItem?.supported }">																
			<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
				${inventoryItem?.quantityOnHand?:0}
			</g:link>
		</g:if>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
</tr>
<tr class="productGroupProducts" id="productGroupProducts-${inventoryItem?.productGroup?.id }" style="display: none;">
	<td colspan="7">
		<div class="box">
			<table>
				<g:each var="groupedInventoryItem" in="${inventoryItem?.inventoryItems }">
					<g:set var="counter" value="${counter+1 }"/>
					<g:render template="browseProduct" model="[counter:counter,inventoryItem:groupedInventoryItem,cssClass:'productGroupProduct']"/>
				</g:each>
			</table>
		</div>
	</td>
</tr>
