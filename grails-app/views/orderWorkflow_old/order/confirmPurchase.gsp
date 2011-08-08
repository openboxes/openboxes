
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title>Confirm Purchase</title>
</head>
<body>
	<div class="body">
		<h1>Confirm Purchase</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="purchaseOrder" method="post">
			<div class="dialog">
				<h2>Please confirm your purchase details:</h2>
				<h4>Items</h4>
				<g:each in="${cartItems}">
					<p>
						${it}
					</p>
				</g:each>
			</div>
			<div class="buttons">
				<span class="formButton"> <g:submitButton name="back"
						value="Back"></g:submitButton> <g:submitButton name="confirm"
						value="Confirm"></g:submitButton> </span>
			</div>
		</g:form>
	</div>
</body>
</html>