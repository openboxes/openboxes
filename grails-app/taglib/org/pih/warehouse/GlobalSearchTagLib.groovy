package org.pih.warehouse

import java.text.SimpleDateFormat;

class GlobalSearchTagLib {
		
	def globalSearch = { attrs, body ->
		
		def id = (attrs.id) ? attrs.id : "globalSearch_" + (new Random()).nextInt()
		def name = (attrs.name) ? attrs.name : attrs.id
		def value = (attrs.value)?:"";
		def width = (attrs.width) ?: 200;
		def minLength = (attrs.minLength) ?: 1;
		def jsonUrl = (attrs.jsonUrl) ?: "";
		def cssClass= (attrs.cssClass) ?:""
		def size = (attrs.size)?:"30"
		def display = (attrs.display)?:"visible"
		
		
		
		def html = """
			<span>
				<input id="${id}" class="${cssClass}" type="text" name="${name}" size="${size}"
					value="${value}" style="width: ${width}px; display: ${display};"> 	
				
				<script>
					\$(document).ready(function() {
				      	\$("#${id}").autocomplete( { 
				      		source: function(req, resp) {
						  		\$.getJSON('${jsonUrl}', req, function(data) {
									var suggestions = [];
									\$.each(data, function(i, item) {
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
				      	\$("#${id}2").autocomplete({
				            width: ${width},
				            minLength: ${minLength},
				            dataType: 'json',
				            highlight: true,
				            //selectFirst: true,
				            scroll: true,
				            autoFocus: true,
				            autoFill: true,
				            //scrollHeight: 300,
							//define callback to format results
							source: function(request, response){			
								\$.getJSON('${jsonUrl}', request, function(data) {
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
		""";
			
		
		out << html; 
	}
	
}