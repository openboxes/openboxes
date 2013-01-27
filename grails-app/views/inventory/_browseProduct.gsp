<tr class="${counter%2==0?'even':'odd' } ${cssClass}">
	<td>
		<g:if test="${inventoryItem?.product?.images }">
			<div class="nailthumb-container">
				<g:set var="image" value="${inventoryItem?.product?.images?.sort()?.first()}"/>
				<img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" />		
			</div>
		</g:if>
		<g:else>
			<div class="nailthumb-container">
				<img src="${resource(dir: 'images', file: 'default-product.png')}" />		
			</div>
		</g:else>
	</td>
	<td>
		<div class="action-menu hover">
			<button class="action-btn">
				<img
					src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
					style="vertical-align: middle" />
			</button>
			<div class="actions left">
				<div class="action-menu-item">
					<g:link controller="inventoryItem" action="showStockCard" params="['product.id': inventoryItem?.product?.id]">
						<img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}"/>&nbsp;
						<warehouse:message code="inventory.showStockCard.label"/>
					</g:link>
				</div>
				<div class="action-menu-item">
					<g:link controller="product" action="edit" id="${inventoryItem?.product?.id }">
						<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
						<warehouse:message code="product.edit.label"/>
					</g:link>
				</div>
				<div class="action-menu-item">
					<g:link controller="inventoryItem" action="showTransactionLog" params="['product.id': inventoryItem?.product?.id, 'disableFilter':true]">
						<img src="${resource(dir: 'images/icons/silk', file: 'calendar.png')}"/>&nbsp;
						<warehouse:message code="inventory.showTransactionLog.label"/>
					</g:link>
				</div>
			</div>
		</div>	
	</td>
	<td class="middle center">
		<g:checkBox id="${inventoryItem?.product?.id }" name="product.id" 
			class="checkbox" style="top:0em;" checked="${false }" 
				value="${inventoryItem?.product?.id }" />
	</td>	
		<%-- 
	<td class="checkable center middle">
		<img src="${resource(dir: 'images/icons/inventoryStatus', file: inventoryItem?.inventoryLevel?.status?.name()?.toLowerCase() + '.png')}" 
			alt="${inventoryItem?.inventoryLevel?.status?.name() }" title="${inventoryItem?.inventoryLevel?.status?.name() }" style="vertical-align: middle;"/>
		
	</td>
		--%>
	<td class="checkable middle">
		<span class="fade">${inventoryItem?.product?.productCode }</span>	
	</td>
	<td class="checkable middle">			
		<g:link name="productLink" controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
			<span title="${inventoryItem?.product?.description }" class="popover-trigger" data-id="${inventoryItem?.product?.id }">				
				<g:if test="${inventoryItem?.product?.name?.trim()}">
					${inventoryItem?.product?.name}
				</g:if>
				<g:else>
					<warehouse:message code="product.untitled.label"/>
				</g:else>
			</span>
		</g:link>	
	</td>
	<td class="checkable middle left">
		<span class="fade">${inventoryItem?.product?.manufacturer }</span>
	</td>
	<td class="checkable middle left">
		<span class="fade">${inventoryItem?.product?.brandName}</span>	
	</td>
	<td class="checkable middle left">
		<span class="fade">${inventoryItem?.product?.manufacturerCode }</span>
	</td>
	<td class="checkable middle center" style="width: 7%; border-left: 1px solid lightgrey;">
		<g:if test="${inventoryItem?.supported }">																
			<g:formatNumber number="${inventoryItem?.quantityToReceive?:0}"/>
		</g:if>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
	<td class="checkable middle center" style="width: 7%; border-right: 1px solid lightgrey;">
		<g:if test="${inventoryItem?.supported }">																
			<g:formatNumber number="${inventoryItem?.quantityToShip?:0}"/>
		</g:if>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
	<td class="checkable middle center" style="width: 7%;">
		<g:if test="${inventoryItem?.supported }">																
			<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
				<g:formatNumber number="${inventoryItem?.quantityOnHand?:0}"/>
			</g:link>
		</g:if>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
</tr>