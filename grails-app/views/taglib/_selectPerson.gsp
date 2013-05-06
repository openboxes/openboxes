
<input type="hidden" id="hidden-${attrs.name}" name="${attrs.name}.id" value="${attrs?.selectedPerson?.id}">
<input type="text" id="select-${attrs.name}" name="select-${attrs.name}" size="${attrs.size}" value="${attrs?.selectedPerson?.name}" class="autocomplete text" />
<div id="div-${attrs.name}" style="display: none;">
    <span id="text-${attrs.name}" class="" style="">${attrs?.selectedPerson?.name}</span>
    <%--<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" id="edit-${attrs.name}"/>--%>
    <img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" id="clear-${attrs.name}"/>
</div>
<script language="javascript">
    $(function() {
        $( "#select-${attrs.name}" ).autocomplete({
            width: '${attrs.width?:100}',
            minLength: '${attrs.minLength?:2}',
            dataType: 'json',
            highlight: true,
            //selectFirst: true,
            scroll: true,
            autoFill: true,
            //scrollHeight: 300,
            //define callback to format results
            source: function( request, response ) {
                $.ajax({
                    //url: "http://ws.geonames.org/searchJSON",
                    url: "${request.contextPath}/json/findPersonByName",
                    dataType: "json",
                    data: {
                        term: request.term
                    },
                    success: function( data ) {
                        console.log(data);
                        console.log(response);
                        response( $.map( data, function( item ) {
                            console.log(item);
                            return {
                                label: item.label,
                                value: item.value
                            }
                        }));
                        //var items = [];
                        //$.each(data, function(i, item) {
                        //    items.push(item);
                        //});
                        //response(items);

                    },
                    error: function(xhr) {
                        console.log($(this));
                        console.log(xhr);
                        //alert("error " + xhr.statusText);

                    }
                });
            },
            select: function( event, ui ) {
                console.log("SELECT!!")
                console.log(event);
                console.log(ui);
                $("#hidden-${attrs.name}").val(ui.item.value);
                $("#select-${attrs.name}").val(ui.item.label).hide();
                $("#text-${attrs.name}").text(ui.item.label).parent().show();
                $("#select-${attrs.name}").trigger("selected");
                return false;
            }
        });

        $("#select-${attrs.name}").blur(function() {
            // If the user clears the textbox and tabs out of the field we need to clear the hidden field
            if ($(this).val() == '') {
                $("#hidden-${attrs.name}").val('');
            }
            // Otherwise, if user tabs out, we should hide the select and show the text
            else {
                $("#select-${attrs.name}").hide();
                $("#text-${attrs.name}").parent().show();
            }
        });

        $("#clear-${attrs.name}").click(function() {
            $("#select-${attrs.name}").show();
            $("#hidden-${attrs.name}").val('');
            $("#select-${attrs.name}").val('');
            $("#text-${attrs.name}").parent().hide();
        });

        $("#text-${attrs.name}").click(function() {
            $("#select-${attrs.name}").show();
            $("#select-${attrs.name}").select();
            $("#text-${attrs.name}").parent().hide();
        });
    });


    /*
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
        //$("#${attrs.id}-suggest").css('width', '300px');

        $("#${attrs.id}-suggest").autocomplete({
            width: '${attrs.width}',
            minLength: '${attrs.minLength}',
            dataType: 'json',
            highlight: true,
            //selectFirst: true,
            scroll: true,
            autoFill: true,
            //scrollHeight: 300,
            //define callback to format results
            source: function(req, add){
                var currentLocationId = $("#currentLocationId").val();
                $.getJSON('/json/findPersonByName', { term: req.term, warehouseId: currentLocationId }, function(data) {
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
    });*/

</script>