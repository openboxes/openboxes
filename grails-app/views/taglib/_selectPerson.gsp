<input type="hidden" id="hidden-${attrs.id}" name="${attrs.name}" value="${attrs?.selectedPerson?.id}">
<input type="text" id="select-${attrs.id}" name="select-${attrs.id}" size="${attrs.size}" value="${attrs?.selectedPerson?.name}" class="autocomplete text" />
<script language="javascript">
    $(function() {
        $( "#select-${attrs.id}" ).autocomplete({
            width: '${attrs.width?:100}',
            minLength: '${attrs.minLength?:2}',
            dataType: 'json',
            highlight: true,
            scroll: true,
            autoFill: true,
            //define callback to format results
            source: function( request, response ) {
                $.ajax({
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
                    },
                    error: function(xhr) {
                        console.log($(this));
                        console.log(xhr);
                    }
                });
            },
            select: function( event, ui ) {
                console.log("SELECT!!")
                console.log(event);
                console.log(ui);
                $("#hidden-${attrs.id}").val(ui.item.value);
                $("#select-${attrs.id}").val(ui.item.label);
                $("#select-${attrs.id}").trigger("selected");
                return false;
            },
            change: function(event, ui) {
                if (!ui.item) {
                    $("#hidden-${attrs.id}").val("");
                    $("#select-${attrs.id}").val("");
                    $("#select-${attrs.id}").trigger("selected");
                }
            }
        });
    });
</script>
