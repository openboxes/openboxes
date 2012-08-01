<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'accessDenied.label', default: 'Access Denied')}" />
	<title><warehouse:message code="access.accessDenied.label" default="Access Denied"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="access.accessDenied.label" default="Access Denied"/></content>
	
	
</head>

<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<div class="error">
		</div>
		
		
		<h1>Access Denied</h1>
		<div class="box middle center">
			<img src="${createLinkTo(dir:'images/icons',file:'logo.png')}" class="home"/>
		</div>
		
		
		
	</div>
</body>