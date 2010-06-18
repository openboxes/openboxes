<html>
  <head>
    <title>Simple JQuery Datepicker example</title>
    <g:javascript library="jquery" />
    <!-- Dynamically include jquery-ui resources -->
    <!-- <jqui:resources components="datepicker" mode="normal" theme="ui-darkness" /> -->
	<!-- Manually include jquery-ui resources -->
	<link href="${createLinkTo(dir:'js/jquery.ui/css/cupertino', file:'jquery-ui-1.8.2.custom.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery-ui-1.8.2.custom.min.js')}" type="text/javascript" ></script>

	<script type="text/javascript"> 
		$(document).ready(function() { 
			$("#datepicker").datepicker({dateFormat: 'yy/mm/dd'}); 
		}) 
	</script>
	</head> 
	<body>
		<div>
			<p>Between <input type="text" id="datepicker"> </p> 
		</div>	
	</body> 
</html>