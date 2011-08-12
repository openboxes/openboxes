<div id="dlgAddItem" title="${warehouse.message(code:'shipping.addAnItem.label')}" style="padding: 10px; display: none;" >
	
	<g:render template="itemSearch" model="['containerId':addItemToContainerId]"/>		
	
</div>	
		     
<script>
	$(document).ready(function() {

		
		$("#dlgAddItem").dialog({ 
			autoOpen: true, 
			modal: true, 
			width: '600px', 
			open: function() { }
		});				
		
		$(".show-item-form").click(function(event) {
			$("#itemSearchForm").hide();
			$("#itemFoundForm").hide();
			$("#itemEntryForm").show();

			// To prevent button from submitting form
			event.preventDefault();
		});

		$(".show-search-form").click(function(event) {
			$("#itemSearchForm").show();
			$("#itemFoundForm").hide();
			$("#itemEntryForm").hide();
			$("[name='searchable.name']").val('');
			// To prevent button from submitting form
			event.preventDefault();
		});
		
	});
</script>