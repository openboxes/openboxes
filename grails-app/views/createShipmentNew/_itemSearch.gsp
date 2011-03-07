
		<div id="itemSearchForm">
			<h2><g:message code="inventoryItem.search.label" default="Search name, description, lot/serial number ..." /></h2>  
			<table>
				<tbody>
					<tr>
						<td>
							<g:autoSuggestSearchable name="searchable" jsonUrl="/warehouse/json/searchInventoryItems" />
						</td>
						<td>
							<b>-OR-</b>
						</td>
						<td>
							<button id="show-new-item-form">Add a new item</button>
						
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		
		<div id="itemFoundForm" style="display: none">
			<h2><g:message code="inventoryItem.enterQuantity.label" default="Enter quantity and recipient" /></h2>  
			<g:render template="itemFoundFields" model="['containerId':containerId]"/>		
			<button id="show-search-form">&lsaquo; Return to search</button>
			
		</div>
		
		
		<div id="itemEntryForm" style="display: none">
			<h2><g:message code="inventoryItem.enterItem.label" default="Enter item details" /></h2>  
			<g:render template="itemFields" model="['containerId':containerId]"/>		
			<button id="show-search-form">&lsaquo; Return to search</button>
		</div>
		
		<script>
			$(document).ready(function() {
				$("#show-new-item-form").click(function() {
					$("#itemSearchForm").hide();
					$("#itemQuantityForm").hide();
					$("#itemEntryForm").show();
				});

				$("#show-search-form").click(function() {
					$("#itemSearchForm").show();
					$("#itemQuantityForm").hide();
					$("#itemEntryForm").hide();
				});
				
			});
		</script>			
		