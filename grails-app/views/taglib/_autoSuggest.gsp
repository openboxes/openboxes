<span id="${attrs.id}-span" class="span" style="text-align: left; display: ${attrs.spanDisplay};">${attrs.valueName}</span>
<input id="${attrs.id}-id" class="value" type="hidden" name="${attrs.name}.id" value="${attrs.valueId}" ${attrs.valueDataBind}/>
<input id="${attrs.id}-value" class="value" type="hidden" name="${attrs.name}.value" value="${attrs.valueId}" ${attrs.valueDataBind }/>
<input id="${attrs.id}-suggest" type="text" ${attrs.textDataBind}
       class="autocomplete text ${attrs.styleClass}" name="${attrs.name}-name" placeholder="${attrs.placeholder}" value="${attrs.valueName}"
       style="width: ${attrs.width}px; display: ${attrs.suggestDisplay};" size="${attrs.size}">

<script language="javascript">
    $(document).ready(function() {

        $.notify.defaults({autoHideDelay: 1000});

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
            delay: ${attrs.delay?:500},
            minLength: ${attrs.minLength?:3},
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
                    var textField = $(this);
                    var hiddenField = $("#${attrs.id}-id");
                    selectItem(hiddenField, textField, null, "");
                    $(this).notify("Unselected item", {className: "success"});
                }
                return false;
            },
            select: function(event, ui) {
                console.log("selected: ", ui.item);

                if (ui.item) {
                    var textField = $(this);
                    var hiddenField = $("#${attrs.id}-id");

                    // Attempt to create a new person
                    if (ui.item.id == "new") {
                        $.ajax({
                            type: "POST",
                            url: "${request.contextPath}/json/createPerson",
                            data: { name: ui.item.valueText },
                            success: function(data, status, xhr) {
                                selectItem(hiddenField, textField, data.id, data.value);
                                textField.notify("Created " + data.value, {className: "success"});
                            },
                            error: function (xhr, ajaxOptions, thrownError) {
                                if (xhr.responseText) {
                                    let data = JSON.parse(xhr.responseText);
                                    textField.notify("Error: " + data.errorMessage, {className: "error"});
                                }
                                else {
                                    textField.notify("An unexpected error has occurred", {className: "error"});
                                }
                            }
                        });
                    }
                    // Otherwise display selected person
                    else {
                        selectItem(hiddenField, textField, ui.item.id, ui.item.label);
                        $(this).notify("Selected item " + ui.item.label, {className: "success"});
                    }
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

    function selectItem(hiddenField, textField, id, label) {
        // Set the id of the item selected
        hiddenField.val(id).trigger("change");

        // Set a hidden value that is passed back to the server
        textField.prev().val(label).trigger("change");

        // Sets the text value displayed to the user
        textField.val(label).trigger("change");
    }

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
