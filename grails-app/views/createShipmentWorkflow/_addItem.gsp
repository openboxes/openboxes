<div id="dlgAddItem" title="Add an Item" style="padding: 10px; display: none;" >
	<g:render template="itemSearch" />
</div>		
		     
<script>
	$(document).ready(function() {
		$("#dlgAddItem").dialog({ 
			autoOpen: true, modal: true, width: '600px'
		});				
		
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