<html>
  <head>
    <title>Simple JQuery AutoSuggest example</title>
	<meta name="layout" content="custom" />
</head> 
<body>
	<g:render template="menu"/>
	
	<table border="0" cellpadding="5" cellspacing="5">
		<tr class="prop">
			<td class="name" valign="top">Person</td>
			<td class="value">
				<g:autoSuggest id="person-lookup" name="person" jsonUrl="${request.contextPath }/json/findPersonByName"/>				
			</td>
		</tr>
		<tr class="prop">
			<td class="name" valign="top">Product</td>
			<td class="value">
				<g:autoSuggest id="product-lookup" name="product" jsonUrl="${request.contextPath }/json/findProductByName"/>	
			</td>
		</tr>
	</table>
	
		
</body> 
</html>