<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditItem").dialog({ autoOpen: true, modal: true, width: '600px' });				
	});
</script>


<div id="dlgEditItem" title="${warehouse.message(code:'shipping.editItem.label')}" style="padding: 10px; display: none;" >
	<g:if test="${itemToEdit}">
		<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="editItem"/>
		<g:form name="editItem" action="createShipment">
			<table>
				<tbody>
					<g:hiddenField name="item.id" value="${itemToEdit.id }"/>	
					<g:render template="itemFields" model="['item':itemToEdit]"/>
				</tbody>
			</table>
		</g:form>														
	</g:if>
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
			$("[name='searchable.name']").val('');
			$("#searchable-suggest").focus();
			event.preventDefault();
		});
		
		$("#searchable-suggest").focus();
	});
</script>			
		     
		     

