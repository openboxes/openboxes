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
		<h1><warehouse:message code="errors.accessDenied.label" default="Access Denied"/></h1>
		<div style="width: 25%;">
			<div class="triangle-isosceles">
				<warehouse:message code="errors.accessDenied.message" default="Apologies, but you are not authorized to view this page."/> 
			</div>
			<div style="padding-left: 45px;">
				<img src="${resource(dir:'images',file:'jgreenspan.jpg')}"/>
			</div>
		</div>
			
		
		
		
		
	</div>
</body>