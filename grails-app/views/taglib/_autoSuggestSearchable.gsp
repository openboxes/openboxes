<span>
    <style>
    #${attrs.id}-suggest {
        background-image: url('${request.contextPath}/images/icons/silk/magnifier.png');
        background-repeat: no-repeat;
        background-position: center left;
        padding-left: 20px;
    }
    .ui-autocomplete-term { font-weight: bold; color: #DDD; }
    </style>

    <input id="${attrs.id}-suggest" type="text" name="${attrs.name}.name"
           value="${attrs.valueName}" style="width: ${attrs.width}; display: ${attrs.suggestDisplay};" class="${attrs.styleClass}">

    <script>
        $(document).ready(function() {
        $("#${attrs.id}-suggest").autocomplete({
            width: '${attrs.width}',
            minLength: ${attrs.minLength},
            dataType: 'json',
            highlight: true,
            scroll: true,
            autoFocus: true,
            autoFill: true,
            source: function(request, response){
                $.getJSON('${attrs.jsonUrl}', request, function(data) {
                        var suggestions = [];
									$.each(data, function(i, item) {
                            suggestions.push(item);
                        });
                        response(suggestions);
                    });
            },
            focus: function(event, ui) {
                return false;
            },
            change: function(event, ui) {
                return false;
            },
            select: function(event, ui) {
                // set text display
                $("#lotNumber-text").html(ui.item.lotNumber);
                $("#product-text").html(ui.item.productName);
                $("#quantity-text").html(ui.item.quantity);
                $("#expirationDate-text").html(ui.item.expirationDate);

                // set hidden values
                $("#productId").val(ui.item.productId);
                $("#lotNumber-suggest").val(ui.item.lotNumber);
                $("#inventoryItemId").val(ui.item.id);

                // Update on hand quantity
                updateQuantityOnHand();

                $("#itemFoundForm").show();
                $("#itemSearchForm").hide();
                $("#quantity").focus();

            }
        }).data("autocomplete")._renderItem = function( ul, item ) {
            var text = item.label;
            if (item.description) {
                text += "<br/><small class='fade'>" + item.description + "</small>"
            }
            return $( "<li></li>" ).data( "item.autocomplete", item).append("<a>" + text + "</a>").appendTo( ul );
        };
    });

    </script>
</span>
