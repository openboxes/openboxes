
 <style>
 
 </style>


<div> 	
	<h2 class="fade"><warehouse:message code="inventory.currentLotNumbers.label"/></h2>
	<div id="inventoryView" style="text-align: left;" class="list">	
		<table border="0" style="">
			<thead>
				<tr class="odd">
					<th class="left" style=""><warehouse:message code="default.actions.label"/></th>												
					<th><warehouse:message code="default.lotSerialNo.label"/></th>
					<th><warehouse:message code="default.expires.label"/></th>
					<th class="center middle" ><warehouse:message code="default.qty.label"/></th>
					<g:hasErrors bean="${flash.itemInstance}">													
						<th></th>
					</g:hasErrors>
				</tr>											
			</thead>
			<tbody>
				<g:if test="${!commandInstance?.inventoryItemList}">
					<tr class="even" style="min-height: 100px;">
						<td colspan="5" style="text-align: center; vertical-align: middle">
							<warehouse:message code="inventory.noItemsCurrentlyInStock.label" args="[format.product(product:commandInstance?.productInstance)]"/>
						</td>
					</tr>
				</g:if>
				<g:set var="count" value="${0 }"/>
				<g:each var="itemInstance" in="${commandInstance?.inventoryItemList }" status="status">	
						<g:set var="quantity" value="${commandInstance.quantityByInventoryItemMap.get(itemInstance)}"/>		
						<!-- only show items with quantities -->	
						<g:set var="itemQuantity" value="${commandInstance.quantityByInventoryItemMap.get(itemInstance) }"/>
						<g:set var="selected" value="${params?.inventoryItem?.id && (itemInstance.id == Integer.valueOf(params?.inventoryItem?.id)) }"/>
						<g:set var="styleClass" value="${(count++%2==0)?'even':'odd' }"/>
						<g:if test="${selected }">
							<g:set var="styleClass" value="selected-row"/>
						</g:if>
						
						<style>
							.selected-row { background-color: lightyellow; } 
						</style>			
						<tr class="${styleClass} prop">
							<td class="top" style="text-align: left; width: 10%" nowrap="nowrap">
								<div class="action-menu">
									<button class="action-btn">
										<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
										&nbsp;<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
									</button>
									<div class="actions">
										<g:render template="editItemDialog" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]"/>
										<g:render template="adjustStock" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
										<g:render template="addToShipment" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
									</div>
								</div>
							</td>															
							<td class="top">
								${itemInstance?.lotNumber?:'<span class="fade"><warehouse:message code="default.none.message"/></span>' }
								<g:link action="show" controller="inventoryItem" id="${itemInstance?.id }">
								</g:link>
							</td>														
							<td class="top">
								<g:if test="${itemInstance?.expirationDate}">
									<format:expirationDate obj="${itemInstance?.expirationDate}"/>
								</g:if>
								<g:else>
									<span class="fade"><warehouse:message code="default.never.message"/></span>
								</g:else>
							</td>
							<td class="top center">
								<g:set var="styleClass" value=""/>
								<g:if test="${itemQuantity<0}">
									<g:set var="styleClass" value="color: red;"/>																	
								</g:if>
								<span style="${styleClass}">${itemQuantity }</span> 
															
							</td>	
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
						</tr>

				</g:each>
			</tbody>
			<g:if test="${commandInstance?.inventoryItemList}">
				<tfoot>
					<tr class="prop" style="height: border: 0px;">
						<td colspan="2" style="text-align: left; border: 0px;">
						</td>
						<td style="border: 0px;"></td>
						<td style="text-align: center; vertical-align: middle; border: 0px;">
							<span style="font-size: 1em;"> 
								<g:set var="styleClass" value="color: black;"/>																	
								<g:if test="${commandInstance.totalQuantity < 0}">
									<g:set var="styleClass" value="color: red;"/>																	
								</g:if>														
								<span style="${styleClass }">${commandInstance.totalQuantity }</span> 
							</span>
						</td>
						<g:hasErrors bean="${flash.itemInstance}">
							<td style="border: 0px;">
							
							</td>
						</g:hasErrors>
					</tr>
				</tfoot>
			</g:if>
		</table>										
	</div>	

</div>
