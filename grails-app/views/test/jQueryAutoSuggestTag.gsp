<html>
  <head>
    <title>Simple JQuery Datepicker example</title>
    <g:javascript library="jquery" />
   
	<!-- Manually include jquery-ui resources -->
	<link href="${createLinkTo(dir:'js/jquery.ui/css/cupertino', file:'jquery-ui-1.8.2.custom.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery-ui-1.8.2.custom.min.js')}" type="text/javascript" ></script>	
	
</head> 
<body>
	
	<table border="1" cellpadding="5" cellspacing="5">
		<tr>
			<td valign="top">Person</td>
			<td>
				<g:autoSuggest name="carrier" jsonUrl="${request.contextPath }/json/findPersonByName"/>				
			</td>
		</tr>
		<tr>
			<td valign="top">Product</td>
			<td>
				<g:autoSuggest name="product" jsonUrl="${request.contextPath }/json/findProductsByName"/>	
			</td>
		</tr>
	
	</table>
	
		
</body> 
</html>