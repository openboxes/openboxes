
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<tr class="${counter%2==0?'even':'odd' } ${cssClass}">
	<td>
		<g:if test="${inventoryItem?.product?.images }">
			<div class="nailthumb-container">
				<g:set var="image" value="${inventoryItem?.product?.images?.sort()?.first()}"/>
				<img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />		
			</div>
		</g:if>
		<g:else>
			<div class="nailthumb-container">
				<img src="${resource(dir: 'images', file: 'default-product.png')}" style="display:none" />		
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
				<div class="action-menu-info">
					<table style="width: 525px">
						<tr>
							<td style="width: 100px;" class="center middle">
								<g:if test="${inventoryItem?.product?.images }">
									<div class="nailthumb-container-100">
										<g:set var="image" value="${inventoryItem?.product?.images?.sort()?.first()}"/>
										<img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />		
									</div>
								</g:if>
								<g:else>
									<div class="nailthumb-container-100">
										<img src="${resource(dir: 'images', file: 'default-product.png')}" style="display:none" />		
									</div>
								</g:else>				
							
							</td>
							<td>
								<table>
									<tr>
										<td>
											<div class="title">
												<span class="fade">#${inventoryItem?.product?.productCode}</span>
												${inventoryItem?.product?.name}
											</div>
										
										</td>
									</tr>
									<g:if test="${inventoryItem?.product?.productGroups }">
										<tr>
											<td>
												<span style="text-transform:uppercase;" class="fade">
													<format:category category="${inventoryItem?.product?.category }"/> &rsaquo;
													${inventoryItem?.product?.productGroups?.sort()?.first()?.name}
												</span>										
											</td>
										</tr>									
									</g:if>				
									<tr>
										<td>
											<p>${inventoryItem?.product?.description }</p>
										</td>
									</tr>					
								</table>
							</td>
						</tr>
						<tr>
							<td>
							
							</td>
							<td >
								<div class="button-group">
									<g:link class="button" controller="inventoryItem" action="showStockCard" params="['product.id': inventoryItem?.product?.id]">
										<img src="${resource(dir: 'images/icons/silk', file: 'clipboard.png')}"/>
										<warehouse:message code="inventory.showStockCard.label"/>
									</g:link>
									<g:link class="button" controller="product" action="edit" id="${inventoryItem?.product?.id }">
										<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
										<warehouse:message code="product.edit.label"/>
									</g:link>
									<g:link class="button" controller="inventoryItem" action="showTransactionLog" params="['product.id': inventoryItem?.product?.id, 'disableFilter':true]">
										<img src="${resource(dir: 'images/icons/silk', file: 'chart_bar.png')}"/>
										<warehouse:message code="inventory.showTransactionLog.label"/>
									</g:link>
								</div>
							</td>
						</tr>
					</table>

				</div>
			</div>
		</div>
	</td>
	<td class="top center">
		<g:checkBox id="${inventoryItem?.product?.id }" name="product.id" 
			class="checkbox" style="top:0em;" checked="${false }" 
				value="${inventoryItem?.product?.id }" />
	</td>
	<td class="checkable top">
		<span class="fade">${inventoryItem?.product?.productCode }</span>	
	</td>
	<td class="checkable top">			
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
	<td class="checkable top left">
		<span class="fade">${inventoryItem?.product?.manufacturer }</span>
	</td>
	<td class="checkable top left">
		<span class="fade">${inventoryItem?.product?.brandName}</span>	
	</td>
	<td class="checkable top left">
		<span class="fade">${inventoryItem?.product?.manufacturerCode }</span>
	</td>
	<td class="checkable top center" style="width: 7%; border-left: 1px solid lightgrey;">
	
		<g:if test="${!showQuantity }">
			
		</g:if>
		<g:elseif test="${inventoryItem?.supported && showQuantity }">
			<div data-product-id="${inventoryItem?.product?.id }" class="quantityToReceive"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
		</g:elseif>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
	<td class="checkable top center" style="width: 7%; border-right: 1px solid lightgrey;">
		<g:if test="${!showQuantity }">
			
		</g:if>
		<g:elseif test="${inventoryItem?.supported && showQuantity}">
			<div data-product-id="${inventoryItem?.product?.id }" class="quantityToShip"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
		</g:elseif>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
	<td class="checkable top center" style="width: 7%;">
	
		<g:if test="${!showQuantity }">
			<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
				<warehouse:message code="default.clickToView.label"/>
			</g:link>
		</g:if>
		<g:elseif test="${inventoryItem?.supported && showQuantity}">																
			<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
				<div data-product-id="${inventoryItem?.product?.id }" class="quantityOnHand"><img class="spinner" src="${createLinkTo(dir:'images/spinner.gif')}" class="middle"/></div>
			</g:link>
		</g:elseif>
		<g:else>
			<span class="fade"><warehouse:message code="default.na.label"/></span>																
		</g:else>
	</td>
</tr>