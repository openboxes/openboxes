<span>
	<g:form method="GET" controller="inventory" action="browse" style="display: inline;">
		<g:textField id="${attrs.id}" name="searchTerms" class="${attrs.cssClass}" type="text" size="${attrs.size}"
			value="${attrs.value}" style="width: ${attrs.width}px; display: ${attrs.display};"/> 	
		<g:hiddenField name="resetSearch" value="${true }"/>							
		<g:hiddenField name="categoryId" value="${session?.rootCategory?.id }"/>	
		<g:hiddenField name="searchPerformed" value="${true }"/>
		<g:hiddenField name="showHiddenProducts" value="on"/>
		<g:hiddenField name="showOutOfStockProducts" value="on"/>			
	</g:form>
</span>
	
	
	<script>
		$(document).ready(function() {
			$("#${attrs.id}").watermark("${warehouse.message(code:'inventory.search.label')}");
	      	$("#${attrs.id}").autocomplete( { 
	      		source: function(req, resp) {
			  		$.getJSON('${attrs.jsonUrl}', req, function(data) {
						var suggestions = [];
						$.each(data, function(i, item) {
							suggestions.push(item);
						});
						resp(suggestions);
					});
	      		},
	      		select: function(event, ui) {
		      		console.log(event);
		      		console.log(ui);
		      		window.location = ui.item.url;
			      		return false;
			  	}
      		});
      	});		
	</script>
</span>		