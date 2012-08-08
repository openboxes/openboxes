<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="default.error.label" /></title>
</head>
<body>
	<div class="body">
		<g:if test="${message}">
			<div class="message">
				${message}
			</div>
		</g:if>
		<g:form action="create">
			<div class="dialog">
				<p>Sorry, but the system has reported an unrecoverable error.</p>
			</div>
			<div class="buttons center">
				<g:submitButton class="confirm" name="confirm" value="OK" />
			</div>
		</g:form>
	</div>
</body>
</html>