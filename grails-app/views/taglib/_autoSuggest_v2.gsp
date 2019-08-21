<span id="${attrs.id}-span" class="span" style="text-align: left; display: ${attrs.spanDisplay};">${attrs.valueName}</span>
<input id="${attrs.id}-value" class="value" type="hidden" name="${attrs.name}.id" value="${attrs.valueId}"/>
<input id="${attrs.id}-suggest" size="${attrs.size }" class="autocomplete ${attrs.styleClass}" type="text" name="${attrs.name}.name" value="${attrs.valueName}" style="width: ${attrs.width}px; display: ${attrs.suggestDisplay};">
	
<script language="javascript">
	$(document).ready(function() {
		$("#${attrs.id}-suggest").click(function() {
			$(this).trigger("focus");
		});
		$("#${attrs.id}-suggest").blur(function() {
			return false;
		});
		$("#${attrs.id}-span").click(function() {
			return false;
		});

		$("#${attrs.id}-suggest").autocomplete({
			width: '${attrs.width}',
			minLength: '${attrs.minLength}',
			dataType: 'json',
			highlight: true,
			scroll: true,
			autoFill: true,
			source: function(req, add){							
				alert("source");
  					var currentLocationId = $("#currentLocationId").val();
				$.getJSON('${attrs.jsonUrl}', { term: req.term, warehouseId: currentLocationId }, function(data) {
					var items = [];
					$.each(data, function(i, item) {
						items.push(item);
					});
					add(items);
				});
			},
			focus: function(event, ui) {
				return false;
			},
			change: function(event, ui) {
				// If the user does not select a value, we remove the value
				if (!ui.item) { 
					$(this).prev().val("null");  // set the user.id to null
					$(this).val("");				// set the value in the textbox to empty string
				}
				return false;
			},
			select: function(event, ui) {
				if (ui.item) { 
					$(this).prev().val(ui.item.value);
					$(this).val(ui.item.valueText);
				}
				return false;
			}
		});
	});
</script>
