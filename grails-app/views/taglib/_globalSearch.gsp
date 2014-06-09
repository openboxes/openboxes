<span>
	<g:form method="GET" controller="dashboard" action="globalSearch" style="display: inline;">
		<g:textField id="${attrs.id}" name="searchTerms" class="globalSearch top" type="text" size="${attrs.size}"
            placeholder="${warehouse.message(code:'globalSearch.placeholder.label')}"
			value="${attrs.value}"/>
		<g:hiddenField name="resetSearch" value="${true }"/>							
		<g:hiddenField name="categoryId" value="${session?.rootCategory?.id }"/>	
		<g:hiddenField name="searchPerformed" value="${true }"/>
		<g:hiddenField name="showHiddenProducts" value="on"/>
		<g:hiddenField name="showOutOfStockProducts" value="on"/>			
	</g:form>
</span>
	
	
	<script>
		$(document).ready(function() {
	      	$("#${attrs.id}").autocomplete( {
                //minLength: 0,
	      		source: function(req, resp) {
			  		$.getJSON('${attrs.jsonUrl}', req, function(data) {
						var suggestions = [];
						$.each(data, function(i, item) {
							suggestions.push(item);
						});
						resp(suggestions);
					});
	      		},
	      		select: function(event, ui) {
		      		window.location = ui.item.url;
		      		return false;
			  	},
                focus: function(event, ui) {
                    //$( "#${attrs.id}" ).val( ui.item.label );
                    //return false;

                    this.value = ui.item.label;
                    event.preventDefault(); // Prevent the default focus behavior.
                }
      		});
            /*
            $("#${attrs.id}").width(500);
            $("#${attrs.id}").focus(function() {
                $(this).animate({"width": "500px"}, "fast");
                //$(this).width(400);
            });
            $("#${attrs.id}").blur(function() {
                $(this).val('');
                $(this).animate({"width": "300px"}, "fast");
            });
            */



        });

        /*
        $( "#project" ).autocomplete({
            source: projects,
            select: function( event, ui ) {
                $( "#project" ).val( ui.item.label );
                $( "#project-id" ).val( ui.item.value );
                $( "#project-description" ).html( ui.item.desc );
                $( "#project-icon" ).attr( "src", "images/" + ui.item.icon );

                return false;
            }
        })
                .data( "ui-autocomplete" )._renderItem = function( ul, item ) {
            return $( "<li>" )
                    .append( "<a>" + item.label + "<br>" + item.desc + "</a>" )
                    .appendTo( ul );
        };
        */


	</script>
</span>		