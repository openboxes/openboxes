<div id="dlgAddItem" title="${warehouse.message(code:'shipping.addItem.label')}" style="padding: 10px; display: none;" >
	
	
	<div id="itemSearchForm" >
		<h4><warehouse:message code="shipping.itemSearch.label"/></h4>
		<div style="text-align: left;">
			<table>
				<tbody>
					<tr>
						<td style="text-align: left">
							<g:autoSuggestSearchable id="searchable" name="searchable"
								jsonUrl="${request.contextPath }/json/findInventoryItems" width="575" styleClass="text"/>
							&nbsp;
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
	
	<div id="itemFoundForm" style="display: none">
		<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItemFound"/>
		<g:form name="editItemFound" action="createShipment">
			<h3>
				<warehouse:message code="shipping.enterQuantityAndRecipient.label"/>
			</h3>  
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
			$(this).focus();
		});

		
		$("#dlgAddItem").dialog({ 
			autoOpen: true, 
			modal: true, 
			width: 600,			
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