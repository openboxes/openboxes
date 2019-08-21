<span>
	<g:form method="GET" controller="dashboard" action="globalSearch" style="display: inline;">
		<g:textField id="${attrs.id}" name="searchTerms" class="globalSearch top" type="text" size="${attrs.size}"
            placeholder="${warehouse.message(code:'globalSearch.placeholder.label')}"
			value="${attrs.value}"/>
		<g:hiddenField name="resetSearch" value="${true }"/>							
		<g:hiddenField name="categoryId" value="${session?.rootCategory?.id }"/>	
		<g:hiddenField name="searchPerformed" value="${true }"/>
		<g:hiddenField name="showHiddenProducts" value="on"/>
		<g:hiddenField name="showOutOfStockProducts" value="on"/>			
	</g:form>
</span>
<script>
    $(document).ready(function() {
        $("#${attrs.id}").autocomplete( {
            delay: ${grailsApplication.config.openboxes.typeahead.delay},
            minLength: ${grailsApplication.config.openboxes.typeahead.minLength},
            source: function(req, resp) {

				$.getJSON('${attrs.jsonUrl}', req, function(data) {
					var suggestions = [];
					$.each(data, function(i, item) {
						suggestions.push(item);
					});
					resp(suggestions);
				})
            },
            select: function(event, ui) {
                window.location = ui.item.url;
                return false;
            },
            focus: function(event, ui) {
                this.value = ui.item.label;
                event.preventDefault(); // Prevent the default focus behavior.
            }
        });
    });
</script>
</span>
