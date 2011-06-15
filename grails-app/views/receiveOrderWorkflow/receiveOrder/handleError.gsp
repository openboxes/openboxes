<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>System Error</title>

</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${orderCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderCommand}" as="list" />
			</div>
		</g:hasErrors>
		<g:hasErrors bean="${orderListCommand}">
			<div class="errors">
				<g:renderErrors bean="${orderListCommand}" as="list" />
			</div>
		</g:hasErrors>
		<div class="dialog">
			<fieldset>
				<g:render template="../order/header" model="[orderInstance:order]"/>
				<g:render template="progressBar" model="['state':'']"/>		
				<g:form action="receiveOrder" autocomplete="false">
					
					There was an error, please try again
				</g:form>
			</fieldset>
		</div>
	</div>
</body>
</html>