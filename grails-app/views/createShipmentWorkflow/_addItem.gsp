<div id="dlgAddItem" title="${warehouse.message(code:'shipping.addItem.label')}" style="padding: 10px; display: none;" >

	<div id="itemSearchForm" >
		<h4><warehouse:message code="shipping.itemSearch.label"/></h4>
		<div style="text-align: center;">
			<table>
				<tbody>
					<tr>
						<td class="center">
							<g:autoSuggestSearchable id="searchable" name="searchable" minLength="3" width="600" placeholder="Enter at least 3 characters to view results"
								jsonUrl="${request.contextPath }/json/findInventoryItems" styleClass="middle text"/>
                            <div class="fade">Type at least 3 characters to see results</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
	
	<div id="itemFoundForm" style="display: none">
		<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItemFound"/>
		<g:form name="editItemFound" action="createShipment">
			<table>
				<tbody>
					<g:render template="itemFoundFields" model="['containerId':addItemToContainerId]"/>		
				</tbody>
			</table>
		</g:form>
	</div>							
</div>
<script>
	$(document).ready(function() {
		$("#searchable-suggest").blur( function () { 
		   	$("#dialog-quantity").focus();
		});
		
		$("#dlgAddItem").dialog({ 
			autoOpen: true, 
			modal: true, 
			width: 700,
			open: function() { }
		});				
		
		$(".show-search-form").click(function(event) {
			$("#itemSearchForm").show();
			$("#itemFoundForm").hide();
			$("[name='searchable.name']").val('');
			$("[name='searchable.name']").focus();
			// To prevent button from submitting form
			event.preventDefault();
		});
		
	});
</script>