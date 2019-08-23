<input id="autosuggest-${attrs.id}"
    data-bind="${attrs.dataBind ?: ''}"
	type="text" size="${attrs.size }" 
	class="autocomplete ${attrs.class}" 
	placeholder="${attrs.placeholder }"
	name="${attrs.name}" value="${attrs?.value?.encodeAsHTML()}">

<script type="text/javascript" language="javascript">
	$(document).ready(function() {
		$("#autosuggest-${attrs.id}").autocomplete({
			delay: ${attrs.delay?:300},
			minLength: ${attrs.minLength?:1},
			dataType: 'json',
			source: function(req, add) {
  				var currentLocationId = $("#currentLocationId").val();
				$.getJSON('${attrs.jsonUrl}', { term: req.term, warehouseId: currentLocationId, field: '${attrs.name}' }, function(data) {
					var items = [];
					$.each(data, function(i, item) { items.push(item); });
					add(items);
				});
			},
			focus: function(event, ui) {
				return false;
			},
			select: function(event, ui) {	
				var continueWithUpdate = true;
				var promptOnMatch = "${attrs.promptOnMatch}";
				
				if (promptOnMatch) { 
					var name = "${attrs.name}";
					var value = ui.item.value;
					
					var promptMessage = "${warehouse.message(code:'default.promptOnMatch.message', default: 'Are you sure?')}"
					promptMessage = promptMessage.replace(/(\{\{0\}\}|\{0\})/g, function(m){ return m == '{0}' ? name : m});
					promptMessage = promptMessage.replace(/(\{\{1\}\}|\{1\})/g, function(m){ return m == '{1}' ? value : m});
					
					continueWithUpdate = confirm(promptMessage);
				}
				if (ui.item && continueWithUpdate) { 
					$(this).val(ui.item.value);					
				}			
				return false;
			}
			
		});
	});
</script>
