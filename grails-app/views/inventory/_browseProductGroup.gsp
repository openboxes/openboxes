<tr class="hasRelated ${counter%2==0?'even':'odd' } ${cssClass}" id="productGroup-${inventoryItem?.productGroup?.id }">
	<%-- 
	<td class="middle center">
		<g:checkBox id="${inventoryItem?.productGroup?.id }" name="productGroup.id" 
				class="checkbox" style="top:0em;" checked="${false }" 
					value="${inventoryItem?.productGroup?.id }" disabled="true"/>
	</td>																
	--%>
	<td class="middle">
		<%-- 
		<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="" class="middle"/>
		<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_toggle_minus.png')}" alt="" class="middle"/>
		--%>
		<span class="action-menu">
			<button class="action-btn">
				<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}"
					style="vertical-align: middle" /> <img
					src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
					style="vertical-align: middle" />
			</button>
			<div class="actions">
				<div class="action-menu-item">
					<div class="action-menu-item">
						<g:link controller="productGroup" action="edit" id="${inventoryItem?.productGroup?.id }"> 
							<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="${warehouse.message(code: 'default.edit.label') }" class="middle"/> 
							&nbsp;<warehouse:message code="productGroup.edit.label" />
						</g:link>
					</div>					
				</div>			
			</div>		
		</span>
	</td>
	<td>
		<!-- Empty -->
	</td>
	<td class="middle">
		<span class="fade">
			<warehouse:message code="default.various.label"/>
		</span>	
	</td>
	<td class="middle">
		<span id="${inventoryItem?.productGroup?.id }" class="expandable">
			${inventoryItem?.productGroup?.description } (${inventoryItem?.productGroup?.products?.size() } ${warehouse.message(code:'products.label') })		
		</span>
		<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_arrow_down.png')}" 
			id="${inventoryItem?.productGroup?.id }" class="expandable" alt="" class="middle"/>
		
		
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
	<td class="checkable middle left">
		<span class="fade">
			<warehouse:message code="default.various.label"/>
		</span>	
	</td>
	<td class="checkable middle left">
		<span class="fade">
			<warehouse:message code="default.various.label"/>
		</span>	
	</td>
	<td class="checkable middle left">
		<span class="fade">
			<warehouse:message code="default.various.label"/>
		</span>	
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
<g:set var="items" value="${inventoryItem?.inventoryItems }"/>
<g:set var="counter2" value="${0 }"/>
<g:each var="groupedInventoryItem" in="${inventoryItem?.inventoryItems }">
	<g:set var="counter2" value="${counter2+1 }"/>	
	<g:set var="cssClass" value="isRelated ${items?.size() == counter2 ? 'lastRelated' : '' } productGroup-${inventoryItem?.productGroup?.id }"/>
	<g:render template="browseProduct" model="[id:productGroup?.id,counter:counter2,inventoryItem:groupedInventoryItem,cssClass:cssClass]"/>
</g:each>
