<div style="min-height: 250px;"> 	
	<fieldset>				
		<div id="inventoryView" style="text-align: left;" class="list">										
			<table border="0" style="border:1px solid #f5f5f5;">
				<thead>
					<tr class="even">
						<th>Description</th>
						<th>Serial/Lot Number</th>
						<th>Expires</th>
						<th class="center middle" >Qty</th>
						<g:hasErrors bean="${flash.itemInstance}">													
							<th></th>
						</g:hasErrors>												
						<th class="left" style="">Actions</th>
					</tr>											
				</thead>
				<tbody>
					<g:if test="${!commandInstance?.inventoryItemList}">
						<tr class="odd" style="height: 100px;">
							<td colspan="5" style="text-align: center; vertical-align: middle">
								There are no <b>${commandInstance?.productInstance?.name }</b> items currently in stock.
							</td>
						</tr>
					</g:if>
					<g:set var="count" value="${0 }"/>
					<g:each var="itemInstance" in="${commandInstance?.inventoryItemList }" status="status">	
						<g:if test="${commandInstance.quantityByInventoryItemMap.get(itemInstance)!=0}">			
							<g:set var="itemQuantity" value="${commandInstance.quantityByInventoryItemMap.get(itemInstance) }"/>
							<g:set var="selected" value="${params?.inventoryItem?.id && (itemInstance.id == Integer.valueOf(params?.inventoryItem?.id)) }"/>
							<g:set var="styleClass" value="${(count++%2==0)?'odd':'even' }"/>
							<g:if test="${selected }">
								<g:set var="styleClass" value="selected-row"/>
							</g:if>
							
							<style>
								.selected-row { background-color: lightyellow; } 
							</style>			
							<tr class="${styleClass} prop">
								<td class="top">
									${itemInstance?.product?.name} &rsaquo;
									${(itemInstance?.description)?:'<span class="fade">No description</span>'}
								</td>
							
								<td class="top">
									${itemInstance?.lotNumber?:'<span class="fade">None</span>' }
									<g:link action="show" controller="inventoryItem" id="${itemInstance?.id }">
									</g:link>
								</td>
								<td class="top">
									<g:if test="${itemInstance?.expirationDate}">
										<g:formatDate date="${itemInstance?.expirationDate }" format="MMM yyyy" />
									</g:if>
									<g:else>
										<span class="fade">Never</span>
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
													<g:message error="${it}"/>
												</g:eachError>																	
											</div>
										</g:if>																									
									</td>
								</g:hasErrors>															
								<td class="top" style="text-align: center; width: 120px;">
									<button id="show-action-menu-${itemInstance?.id}" class="show-action-menu">
										Actions<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"/>
									</button>
									<div id="action-menu-${itemInstance?.id}" class="action-menu" style="display: none">
										<hr/>
										<g:render template="editItemDialog" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]"/>
										<g:render template="adjustStock" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />			
										<g:render template="addToShipment" model="[itemInstance:itemInstance, itemQuantity: itemQuantity]" />	
									</div>
								</td>															
					
							</tr>
						</g:if>
					</g:each>
					
					<script type="text/javascript">
						$(document).ready(function(){
							//$("#dlgEditItem-${itemInstance?.id}").dialog({ autoOpen: false, modal: true, width: '600px' });	
							$(".show-action-menu").click(function(event) { 
								$(this).siblings(".action-menu").toggle();
								//$("#actions-${itemInstance?.id}").show();
						        event.preventDefault();
							});					
						});
					</script>	
					
				</tbody>
				<g:if test="${commandInstance?.inventoryItemList}">
					<tfoot>
						<tr class="prop" style="height: 3em;">
							<td colspan="3" style="text-align: left; border: 0px;">
							</td>
							<td style="text-align: center; vertical-align: middle; border: 0px;">
								<span style="font-size: 1.5em;"> 
									<g:set var="styleClass" value="color: black;"/>																	
									<g:if test="${commandInstance.totalQuantity < 0}">
										<g:set var="styleClass" value="color: red;"/>																	
									</g:if>														
									<span style="${styleClass }">${commandInstance.totalQuantity }</span>
									
								</span>
							</td>
							<td style="border: 0px;"></td>
							
						</tr>
					</tfoot>
				</g:if>
			</table>										
		</div>		
	</fieldset>	
</div>
