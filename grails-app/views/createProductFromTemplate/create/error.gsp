<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="createProductFromTemplate.label"/></title>
</head>
<body>
	<div class="body">
		
		<g:render template="header" model="['currentState':'saveProduct']"/>
		
		<g:form action="create">
			<div class="dialog box">
				<p>Sorry, but the system has reported an unrecoverable error.</p>
			</div>
			<div class="buttons center">
				<g:submitButton class="startOver" name="startOver" value="Start Over" />
			</div>
		</g:form>
	</div>
</body>
</html>