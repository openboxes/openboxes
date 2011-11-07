
 
<div> 	

	<div id="inventoryView" style="text-align: left; border: 0px solid lightgrey;">	
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
				<g:set var="count" value="${0 }"/>
				<g:each var="itemInstance" in="${commandInstance?.inventoryItemList }" status="status">	
					<g:if test="${commandInstance.quantityByInventoryItemMap.get(itemInstance)}">		
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
										<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
									</button>
									<div class="actions">
										<g:render template="editItemDialog" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]"/>
										<g:render template="adjustStock" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
										<g:render template="addToShipment" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />
									</div>
								</div>
							</td>															
							<td class="top">
								${itemInstance?.lotNumber ?: '<span class="fade"><warehouse:message code="default.none.label"/></span>' }
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
					</g:if>
				</g:each>
				<g:unless test="${commandInstance?.totalQuantity != 0}">
					<tr>
						<td colspan="5">
							<div class="padded center fade">
								<warehouse:message code="inventory.noItemsCurrentlyInStock.message" args="[format.product(product:commandInstance?.productInstance)]"/>
							</div>
						</td>
					</tr>
				</g:unless>				
			</tbody>
			<tfoot>
				<tr class="prop odd" style="border-top: 1px solid lightgrey; border-bottom: 0px solid lightgrey">
					<td></td>
					<td colspan="2" class="left">
						<label><warehouse:message code="inventory.onHandQuantity.label" default="On-hand quantity"/></label>
					</td>
					<td class="center">
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
							&nbsp;
						</td>
					</g:hasErrors>
				</tr>
			</tfoot>
		</table>										
		
	</div>	
</div>
