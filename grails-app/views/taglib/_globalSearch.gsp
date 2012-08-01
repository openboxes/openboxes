<span>
	<g:form method="GET" controller="inventory" action="browse" style="display: inline;">
		<g:textField id="${attrs.id}" name="searchTerms" class="${attrs.cssClass}" type="text" size="${attrs.size}"
			value="${attrs.value}" style="width: ${attrs.width}px; display: ${attrs.display};"/> 	
		<g:hiddenField name="resetSearch" value="${true }"/>							
		<g:hiddenField name="categoryId" value="${session?.rootCategory?.id }"/>	
		<g:hiddenField name="searchPerformed" value="${true }"/>
		<g:hiddenField name="showHiddenProducts" value="on"/>
		<g:hiddenField name="showOutOfStockProducts" value="on"/>
		<%-- 
		<span class="" style="background-color:white; color: #333; padding: 0; margin: 0;">
			${session.rootCategory }
		</span>
		<select name="categoryId" class="hidden">
			<option value="${session.rootCategory?.id }">${session.rootCategory?.name }</option>
			<g:each var="category" in="${session?.rootCategory?.categories }">
				<option value="${category.id }">${category.name }</option>
			</g:each>
		</select>
		--%>									
	</g:form>
</span>
	
	
	<script>
		$(document).ready(function() {
			$("#${attrs.id}").watermark("${warehouse.message(code:'inventory.search.label')}");
	      	$("#${attrs.id}").autocomplete( { 
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
		      		
			  	}
      		});
      	});
		/*
      	$("#${attrs.id}2").autocomplete({
            width: ${attrs.width},
            minLength: ${attrs.minLength},
            dataType: 'json',
            highlight: true,
            //selectFirst: true,
            scroll: true,
            autoFocus: true,
            autoFill: true,
            //scrollHeight: 300,
			//define callback to format results
			source: function(request, response){			
				\$.getJSON('${attrs.jsonUrl}', request, function(data) {
					var suggestions = [];
					\$.each(data, function(i, item) {
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
				return false;
			}
		});
		*/
	</script>
</span>		