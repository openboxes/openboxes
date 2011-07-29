<script>
	$(document).ready(function() {
		$("#searchable-suggest").blur( function () { 
			$(this).focus();
		});
	});
</script>

		<div id="itemSearchForm" >
			<h2><g:message code="inventoryItem.search.label" default="Search inventory by name, description, or lot/serial number:" /></h2>
			<div style="text-align: left;">
				<table>
					<tbody>
						<tr>
							<td style="text-align: left">
								<g:autoSuggestSearchable id="searchable" name="searchable" 
									jsonUrl="/warehouse/json/searchInventoryItems" />
								&nbsp;
							</td>
							<%-- 
							<td style="text-align: center">
								<b>-OR-</b>
							</td>
							<td style="text-align: center">						
								<button class="show-item-form">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" style="vertical-align: middle"/>
									&nbsp; Add an item not currently in inventory
								</button>
							</td>
							--%>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
		
		<div id="itemFoundForm" style="display: none">
			<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItemFound"/>
			<g:form name="editItemFound" action="createShipment">
				<h2><g:message code="inventoryItem.enterQuantity.label" default="Enter quantity and recipient" /></h2>  
				<table>
					<tbody>
						<g:render template="itemFoundFields" model="['containerId':containerId]"/>		
					</tbody>
				</table>
			</g:form>
			<button class="show-search-form">&lsaquo; Return to search</button>
		
		</div>							
		
		<div id="itemEntryForm" style="display: none">
			<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItemEntry"/>
			<g:form name="editItemEntry" action="createShipment">
				<h2><g:message code="inventoryItem.enterItem.label" default="Enter item details" /></h2>  
				<g:if test="${item?.id}">
					<g:hiddenField name="item.id" value="${item.id }"/>
				</g:if>
				<g:if test="${containerId}">
					<g:hiddenField name="container.id" value="${containerId }"/>
				</g:if>
				<table>
					<tbody>
						<g:render template="itemFields" model="['containerId':containerId]"/>		
					</tbody>
				</table>
			</g:form>
			<button class="show-search-form">&lsaquo; Return to search</button>
		</div>
		
	</td>
</tr>		
				
