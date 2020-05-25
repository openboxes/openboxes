<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'notFound.label', default: 'Page Not Found')}" />
	<title><warehouse:message code="notFound.label" default="Page Not Found"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="title"><warehouse:message code="notFound.label" default="Page Not Found"/></content>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<div class="summary">
			<div class="title middle">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'error.png')}" style="vertical-align: middle"/>
				<g:if test="${params.id}">
					<warehouse:message code="errors.resourceWithIdNotFound.title"
									   default="Sorry, a resource with ID {0} could not be found." args="[params.id]"/>
				</g:if>
				<g:else>
					<g:message code="errors.resourceNotFound.title" default="Resource Not Found"/> (404)
				</g:else>
			</div>
		</div>
		<div id="doc" style="">
			<div class="triangle-isosceles title">
				<g:if test="${request?.exception?.message}">
					${request?.exception?.message}
				</g:if>

				<warehouse:message code="errors.resourceNotFound.message" default="Sorry, that resource could not be found."/>
			</div>
			<div class="left">
			<img src="${createLinkTo(dir:'images',file:'jgreenspan.png')}"/>

			</div>
		</div>
	</div>
</body>
