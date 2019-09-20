
<span id="${id}-span" class="span" style="text-align: left; display: ${spanDisplay};">${valueName}</span>
<input id="${id}-value" class="value" type="hidden" name="${name}.id" value="${valueId}"/>
<input id="${id}-suggest" type="text"
       class="autocomplete ${styleClass}" name="${name}.name" placeholder="${placeholder}" value="${valueName}"
       style="width: ${width}px; display: ${suggestDisplay};">

<script language="javascript">
    $(document).ready(function() {
        $("#${id}-suggest").click(function() {
            $(this).trigger("focus");
        });
        $("#${id}-suggest").blur(function() {
            return false;
        });
        $("#${id}-span").click(function() {
            return false;
        });

        $("#${id}-suggest").autocomplete({
            delay: ${attrs.delay?:300},
            minLength: ${minLength},
            dataType: 'json',
            //define callback to format results
            source: function(req, add){
                var currentLocationId = $("#currentLocationId").val();
								$.getJSON('${jsonUrl}', { term: req.term, warehouseId: currentLocationId }, function(data) {
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
				    $(this).val(ui.item.label);
                }
				$("#${id}-suggest").trigger("selected");
                return false;
            }
        }).data("autocomplete" )._renderItem = function( ul, item ) {
            var li = $("<li>").data("item.autocomplete", item );
            if(item.type == 'Product'){
                var text = item.quantity == null ? item.label : item.label + " QTY: " + item.quantity;
                li.append("<a>" + text + "</a>" );
            }else{
                li.append("<span class='product-group'>" + item.label + "</span>" );
            }
            li.appendTo(ul);
            return li;
        };
    });
</script>
