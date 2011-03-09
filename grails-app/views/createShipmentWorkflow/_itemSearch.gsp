		<div id="itemSearchForm" >
			<g:form name="editItem" action="createShipment">
				<h2><g:message code="inventoryItem.search.label" default="Search name, description, lot/serial number ..." /></h2>
				<div style="text-align: center;">
					<table style="display: inline;">
						<tbody>
							<tr>
								<td style="text-align: center">
									<g:autoSuggestSearchable name="searchable" jsonUrl="/warehouse/json/searchInventoryItems" />
									&nbsp;
								</td>
							</tr>
							<tr>
								<td style="text-align: center">
									<b>-OR-</b>
								</td>
							</tr>
							<tr>
								<td style="text-align: center">						
									<button class="show-item-form">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" style="vertical-align: middle"/>
									&nbsp; Add a new item</button>
								
														
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</g:form>
		</div>
		
		<div id="itemFoundForm" style="display: none">
			<g:form name="editItem" action="createShipment">
				<h2><g:message code="inventoryItem.enterQuantity.label" default="Enter quantity and recipient" /></h2>  
				<g:hiddenField id="container-id" name="container.id" value="${containerId}" size="20" />			
				<table>
					<tbody>
						<g:render template="itemFoundFields" model="['containerId':containerId]"/>		
					</tbody>
				</table>
			</g:form>
			<button class="show-search-form">&lsaquo; Return to search</button>
		
		</div>							
		
		<div id="itemEntryForm" style="display: none">
			<g:form name="editItem" action="createShipment">
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
		
		<script>
			$(document).ready(function() {
				$(".show-item-form").click(function(event) {
					$("#itemSearchForm").hide();
					$("#itemFoundForm").hide();
					$("#itemEntryForm").show();
					event.preventDefault();
				});

				$(".show-search-form").click(function(event) {
					$("#itemSearchForm").show();
					$("#itemFoundForm").hide();
					$("#itemEntryForm").hide();
					event.preventDefault();
				});
				
			});
		</script>			
	</td>
</tr>