<html>
  <head>
    <title>Simple JQuery Datepicker example</title>
	<meta name="layout" content="custom" />    	

	<script type="text/javascript"> 
		$(document).ready(function() { 
			$("#datepicker").datepicker({dateFormat: 'yy/mm/dd'}); 
		}) 
	</script>
	</head> 
	<body>
		
		<g:render template="menu"/>
		
		<div>
			<p>Date <input type="text" id="datepicker"> </p> 
		</div>	
	</body> 
</html>