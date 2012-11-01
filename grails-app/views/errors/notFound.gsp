<html>
<head>
	
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'notFound.label', default: 'Page Not Found')}" />
	<title><warehouse:message code="notFound.label" default="Page Not Found"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle"><warehouse:message code="notFound.label" default="Page Not Found"/></content>
	
	
</head>

<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<h1><warehouse:message code="notFound.label" default="Page Not Found"/></h1>
		<div style="width: 25%;">
			<div class="triangle-isosceles">
				Apologies, but we can't find the page you're looking for. 
			</div>
			<div style="padding-left: 45px;">
				<img src="${createLinkTo(dir:'images/icons',file:'logo.png')}"/>
			</div>
		</div>
	</div>
</body>