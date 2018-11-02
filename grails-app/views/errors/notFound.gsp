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

		<div class="summary">
			<div class="title">
				<g:message code="errors.resourceNotFound.message" default="Resource Not Found"/> (404)
			</div>
		</div>
		<div style="width: 25%;">
			<div class="triangle-isosceles">
				<g:if test="${request?.exception?.message}">
					${request?.exception?.message}
				</g:if>
				<g:elseif test="${params.id}">
					<warehouse:message code="errors.resourceWithIdNotFound.message"
									   default="Sorry, a resource with ID {0} could not be found." args="[params.id]"/>
				</g:elseif>
				<g:else>
					<warehouse:message code="errors.resourceNotFound.message" default="Sorry, that resource could not be found."/>
				</g:else>

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