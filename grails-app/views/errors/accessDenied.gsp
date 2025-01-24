<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="pageTitle" value="${warehouse.message(code: 'errors.accessDenied.label', default: 'Access Denied')}" />
	<title>${pageTitle}</title>
	<content tag="pageTitle">${pageTitle}</content>
</head>
<body>
<g:if test="${flash.message}">
    <div class="message" role="status" aria-label="message">
        ${flash.message}
    </div>
</g:if>

<div class="summary">
    <div class="title">
        <g:message code="errors.accessDenied.label" default="Access Denied"/>
    </div>
</div>
	<div id="doc">
        <div class="triangle-isosceles">
            <warehouse:message code="errors.accessDenied.message" default="Apologies, but you are not authorized to access this page or to perform this action."/>
        </div>
        <div class="left">
            <img src="${resource(dir:'images',file:'jgreenspan.png')}"/>
        </div>
	</div>
</body>
