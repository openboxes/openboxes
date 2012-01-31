<html>
  <head>
    <title>Simple JQuery Autocomplete example</title>   
	<meta name="layout" content="custom" />	
</head> 
<body>
	<g:render template="menu"/>
	
	<div>
		<input id="person-id" type="hidden" name="person.id"/>
		<span id="person-name"></span>		
		<br/>
		<input id="person-suggest" type="text" name="person-suggest" style="width: 300px;"> 		
	</div>		
	<script>
		$(document).ready(function() {
			$('#person-suggest').focus();
	      	$("#person-suggest").autocomplete({
	            width: 400,
	            minLength: 2,
	            dataType: 'json',
	            highlight: true,
	            selectFirst: true,
	            scroll: true,
	            autoFill: true,
	            //scrollHeight: 300,
				//define callback to format results
				source: function(req, add){
					$.getJSON("searchByName", req, function(data) {
						var people = [];
						$.each(data, function(i, item){
							people.push(item);
						});
						add(people);
					});
		      	},
		        focus: function(event, ui) {			        
		      		$('#person-suggest').val(ui.item.label);					
		      		return false;
		        },	
				select: function(event, ui) {	
					search_option = ui.item;		
					$('#person-suggest').val("");
					$('#person-id').val(ui.item.value);
					$('#person-name').html(ui.item.label);					
					return false;
				}
			});
		});
	</script>		
		
		
	
	
</body> 
</html>