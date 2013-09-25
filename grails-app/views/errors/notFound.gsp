<html>
<head>
	
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'notFound.label', default: 'Page Not Found')}" />
	<title><warehouse:message code="notFound.label" default="Page Not Found"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="title"><warehouse:message code="notFound.label" default="Page Not Found"/></content>
    <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
	
	
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
				<warehouse:message code="errors.accessDenied.message" default="Apologies, but I can't find that page."/>
				 
			</div>
			<div style="padding-left: 45px;" >
				<img src="${createLinkTo(dir:'images',file:'jgreenspan.jpg')}"/>
			</div>
		</div>
	</div>
	<script>
		//$(function() {
			//$('.nailthumb-container img').nailthumb({width : 100, height : 100});
		//});
	</script>	
</body>