<script>
	$(document).ready(function() {
		$("#searchable-suggest").blur( function () { 
			$(this).focus();
		});
	});
</script>

		<div id="itemSearchForm" >
			<h2><warehouse:message code="shipping.itemSearch.label"/>:</h2>
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
								<b><warehouse:message code="shipping.or.label"/></b>
							</td>
							<td style="text-align: center">						
								<button class="show-item-form">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" style="vertical-align: middle"/>
									&nbsp; <warehouse:message code="shipping.addItemNotInInventory.label"/>
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
				<h2><warehouse:message code="shipping.enterQuantityAndRecipient.label"/></h2>  
				<table>
					<tbody>
						<g:render template="itemFoundFields" model="['containerId':containerId]"/>		
					</tbody>
				</table>
			</g:form>
			<button class="show-search-form">&lsaquo; <warehouse:message code="shipping.returnToSearch.label"/></button>
		
		</div>							
		
		<div id="itemEntryForm" style="display: none">
			<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItemEntry"/>
			<g:form name="editItemEntry" action="createShipment">
				<h2><warehouse:message code="shipping.enterItemDetails.label"/></h2>  
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
			<button class="show-search-form">&lsaquo; <warehouse:message code="shipping.returnToSearch.label"/></button>
		</div>
		
	</td>
</tr>		
				
