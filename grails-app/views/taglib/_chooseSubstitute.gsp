<input type="hidden" id="productId" name="productId" value="${attrs.productId}"/>
<input type="hidden" id="productPackageId" name="productPackageId" value="${attrs.productPackageId}"/>
<input type="hidden" id="productPackageQty" name="productPackageQty" value="${attrs.productPackageQty}"/>
<input id="autocomplete-${attrs.id}" size="${attrs.size }" class="autocomplete ${attrs.styleClass}" type="text"
       name="${attrs.name}" value="${attrs.valueName}" style="width: ${attrs.width}px;">
<script language="javascript">
	$(document).ready(function() {

		$("#autocomplete-${attrs.id}").click(function() {
			$(this).trigger("focus");
		});
		$("#autocomplete-${attrs.id}").blur(function() {
			return false;
		});
		$("#autocomplete-${attrs.id}").autocomplete({
			width: '${attrs.width}',
			minLength: '${attrs.minLength}',
			dataType: 'json',
			highlight: true,
			scroll: true,
			autoFill: true,
			source: function(req, resp){
  					var currentLocationId = $("#currentLocationId").val();
				$.getJSON('${attrs.jsonUrl}', { term: req.term, warehouseId: currentLocationId }, function(data) {
					var items = [];
					$.each(data, function(i, item) {
						items.push(item);
					});
					resp(items);
				});
			},
			focus: function(event, ui) {
				return false;
			},
			change: function(event, ui) {
				// If the user does not select a value, we remove the value
				if (!ui.item) { 
					$(this).prev().val("null");  // set the id to null so that we don't get exception
					$(this).val("");				// set the value in the textbox to empty string
                    $("#productPackageQty").val(0).trigger('change');
				}
				return false;
			},
			select: function(event, ui) {
                console.log(event);
                console.log(ui);
				if (ui.item) {
                    $("#productId").val(ui.item.id);
                	$("#productPackageId").val(ui.item.productPackageId);
                    $("#productPackageQty").val(ui.item.productPackageQty).trigger('change');
                    $(this).val(ui.item.value);
				}
				return false;
			}
		});
	});
</script>
