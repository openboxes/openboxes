<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="createProduct.label"/></title>
</head>
<body>
	<div class="body">
		
		<g:render template="header" model="['currentState':'error']"/>
				
		<g:form action="create">
			<div class="dialog box">
				<p>
					
					Sorry, but the system has reported an unrecoverable error.
				</p>
			</div>
			<div class="buttons center">
				<g:submitButton class="confirm" name="confirm" value="OK" />
			</div>
		</g:form>
	</div>
</body>
</html>