<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="errors.methodNotAllowed.label" default="Method Not Allowed Denied"/></title>
	<content tag="pageTitle"><warehouse:message code="errors.methodNotAllowed.label" default="Method Not Allowed"/></content>


</head>

<body>
	<div class="body">


		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>

		<div class="summary">
			<div class="title">
				<g:message code="errors.methodNotAllowed.label" default="Method Not Allowed"/> (405)
			</div>
		</div>
		<div style="width: 25%;">
			<div class="triangle-isosceles">
				<warehouse:message code="errors.methodNotAllowed.message" default="Apologies, but you are not allowed to do *that* on that page."/>
			</div>
			<div style="padding-left: 45px;">
				<img src="${createLinkTo(dir:'images',file:'jgreenspan.png')}"/>
			</div>
		</div>
	</div>
</body>
