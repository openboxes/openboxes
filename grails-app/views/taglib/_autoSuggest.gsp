<span id="${attrs.id}-span" class="span" style="text-align: left; display: ${attrs.spanDisplay};">${attrs.valueName}</span>
<input id="${attrs.id}-id" class="value" type="hidden" name="${attrs.name}.id" value="${attrs.valueId}" ${attrs.valueDataBind}/>
<input id="${attrs.id}-value" class="value" type="hidden" name="${attrs.name}.value" value="${attrs.valueId}" ${attrs.valueDataBind }/>
<input id="${attrs.id}-suggest" type="text" ${attrs.textDataBind}
       class="autocomplete text ${attrs.styleClass}" name="${attrs.name}-name" placeholder="${attrs.placeholder}" value="${attrs.valueName}"
       style="width: ${attrs.width}px; display: ${attrs.suggestDisplay};" size="${attrs.size}">

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
            delay: ${attrs.delay?:300},
            minLength: ${attrs.minLength?:1},
            dataType: 'json',
            //define callback to format results
            source: function(req, add){
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
                console.log("changed: ", ui.item);
                // If the user does not select a value, we remove the value
                if (!ui.item) {
                    $(this).prev().val("null").trigger("change");
                    $(this).val("");				// set the value in the textbox to empty string
                    $("#${attrs.id}-suggest").trigger("selected");
                }
                return false;
            },
            select: function(event, ui) {
                console.log("selected: ", ui.item);
                if (ui.item) {
                    // Set the id of the item selected
                    $("#${attrs.id}-id").val(ui.item.id).trigger("change");

                    // Set a hidden value that is passed back to the server
                    $(this).prev().val(ui.item.label).trigger("change");

                    // Sets the text value displayed to the user
                    $(this).val(ui.item.label).trigger("change");
                }

                // Set focus on the next field
                $(this).focusNextInputField();

                // Trigger the select
                $("#${attrs.id}-suggest").trigger("selected");
                return false;
            }
        }).data("autocomplete")._renderItem = function( ul, item ) {
            var text = item.label;
            if (item.description) {
                text += "<br/><small class='fade'>" + item.description + "</small>"
            }
            return $( "<li></li>" ).data( "item.autocomplete", item).append("<a>" + text + "</a>").appendTo( ul );
        };
    });


    $.fn.focusNextInputField = function() {
        return this.each(function() {
            var fields = $(this).parents('form:eq(0),body').find('button,input,textarea,select');
            var index = fields.index( this );
            if ( index > -1 && ( index + 1 ) < fields.length ) {
                fields.eq( index + 1 ).focus();
            }
            return false;
        });
    };
</script>