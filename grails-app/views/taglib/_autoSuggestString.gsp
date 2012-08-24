<%-- 
<input id="value-${attrs.id}" class="value" type="hidden" name="${attrs.id}" value="${attrs.value}"/>
--%>
<input id="autosuggest-${attrs.id}" 
	type="text" size="${attrs.size }" 
	class="autocomplete ${attrs.class}" 
	placeholder="${attrs.placeholder }"
	name="${attrs.name}" value="${attrs.label}">

<script language="javascript">
	$(document).ready(function() {
		//$("#autosuggest-${attrs.id}").click(function() { $(this).trigger("focus"); });
		//$("#autosuggest-${attrs.id}").blur(function() { return false; });
		//$("#label-${attrs.id}").click(function() { return false; });
		$("#autosuggest-${attrs.id}").autocomplete({
			delay: ${attrs.delay?:300},
			minLength: ${attrs.minLength?:1},
			dataType: 'json',
			source: function(req, add) {
  				var currentLocationId = $("#currentLocationId").val();
				$.getJSON('${attrs.jsonUrl}', { term: req.term, warehouseId: currentLocationId }, function(data) {
					var items = [];
					$.each(data, function(i, item) { items.push(item); });
					add(items);
				});
			},
			focus: function(event, ui) {
				return false;
			},
			/*
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
			*/
		});
	});
</script>