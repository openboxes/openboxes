<g:hasErrors bean="${transaction}">
	<div class="errors">
		<g:renderErrors bean="${transaction}" as="list" />
	</div>
</g:hasErrors>
<div style="border: 1px solid lightgrey;">
	<table>
		<thead>
			<tr class="odd">
				<th class="left" style=""><warehouse:message code="default.actions.label"/></th>												
				<th><warehouse:message code="default.lotSerialNo.label"/></th>
				<th><warehouse:message code="default.expires.label"/></th>
				<th class="center middle" ><warehouse:message code="default.qty.label"/></th>
			</tr>											
		</thead>
		<tbody  >
			<g:set var="count" value="${0 }"/>
			<g:each var="itemInstance" in="${commandInstance?.inventoryItemList }" status="status">	
				<g:if test="${commandInstance.quantityByInventoryItemMap.get(itemInstance)}">		
					<!-- only show items with quantities -->	
					<g:set var="itemQuantity" value="${commandInstance.quantityByInventoryItemMap.get(itemInstance) }"/>
					<g:set var="selected" value="${params?.inventoryItem?.id && (itemInstance.id == params?.inventoryItem?.id) }"/>
					<g:set var="styleClass" value="${(count++%2==0)?'even':'odd' }"/>
					<g:if test="${selected }">
						<g:set var="styleClass" value="selected-row"/>
					</g:if>
					
					<style>
						.selected-row { background-color: lightyellow; } 
					</style>			
					<tr class="${styleClass} prop">
						<td class="top" style="text-align: left; width: 10%" nowrap="nowrap">
                            <g:render template="actionsCurrentStock" model="[itemInstance:itemInstance,itemQuantity:itemQuantity]" />
						</td>
						<td class="top">
						
							<span class="lotNumber">
								${itemInstance?.lotNumber ?: '<span class="fade"><warehouse:message code="default.none.label"/></span>' }
							</span>
							<g:link action="show" controller="inventoryItem" id="${itemInstance?.id }">
							</g:link>
						</td>														
						<td class="top">
							<g:if test="${itemInstance?.expirationDate}">
								<format:expirationDate obj="${itemInstance?.expirationDate}"/>
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.never.label"/></span>
							</g:else>
						</td>
						<td class="top center">
							<g:set var="styleClass" value=""/>
							<g:if test="${itemQuantity<0}">
								<g:set var="styleClass" value="color: red;"/>																	
							</g:if>
							<span style="${styleClass}">${g.formatNumber(number: itemQuantity, format: '###,###,###') }</span>
							<span class="">
								<g:if test="${productInstance?.unitOfMeasure }">
									<format:metadata obj="${productInstance?.unitOfMeasure}"/>
								</g:if>
								<g:else>
									${warehouse.message(code:'default.each.label') }
								</g:else>
							</span>								
									
														
						</td>
						<%-- 
						<g:hasErrors bean="${flash.itemInstance}">
							<td>
								<g:if test="${selected }">
									<div class="errors dialog">
										<g:eachError bean="${flash.itemInstance}">
											<warehouse:message error="${it}"/>
										</g:eachError>																	
									</div>
								</g:if>																									
							</td>
						</g:hasErrors>
						--%>	
					</tr>
				</g:if>
			</g:each>
			
			<g:if test="${commandInstance?.totalQuantity == 0}">
				<tr>
					<td colspan="5">
						<div class="padded center fade">
							<warehouse:message code="inventory.noItemsCurrentlyInStock.message" args="[format.product(product:commandInstance?.productInstance)]"/>
						</div>
					</td>
				</tr>
			</g:if>
		</tbody>
		<tfoot>
			<tr class="prop odd" style="border-top: 1px solid lightgrey; border-bottom: 0px solid lightgrey">
				<td colspan="3" class="left">
				</td>
				<td class="center">
					<span style="font-size: 1em;"> 
						<g:set var="styleClass" value="color: black;"/>																	
						<g:if test="${commandInstance.totalQuantity < 0}">
							<g:set var="styleClass" value="color: red;"/>																	
						</g:if>														
						<span style="${styleClass }" id="totalQuantity">${g.formatNumber(number: commandInstance.totalQuantity, format: '###,###,###') }</span>
						<span class="">
							<g:if test="${productInstance?.unitOfMeasure }">
								<format:metadata obj="${productInstance?.unitOfMeasure}"/>
							</g:if>
							<g:else>
								${warehouse.message(code:'default.each.label') }
							</g:else>
						</span>					
					</span>
				</td>
				<g:hasErrors bean="${flash.itemInstance}">
					<td style="border: 0px;">
						&nbsp;
					</td>
				</g:hasErrors>
			</tr>
		</tfoot>
	</table>
</div>