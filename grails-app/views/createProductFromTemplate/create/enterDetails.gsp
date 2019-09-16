<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="createProductFromTemplate.label"/></title>
</head>
<body>
	<div class="body">
			
				
		<g:render template="header" model="['currentState':'enterDetails']"/>
		
		<g:form action="create">
			<div class="dialog box">
				<g:if test="${templateName }">		
					<g:render template="${templateName }"/>
				</g:if>
				<g:else>
					No template ${templateName }
				</g:else>
			</div>
			<div class="buttons">
				<g:submitButton class="back" name="back" value="Back" />
				<g:submitButton class="next" name="next" value="Next" />
				<g:submitButton class="cancel" name="cancel" value="Cancel" />
			</div>
		</g:form>
	</div>
</body>
</html>