<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'errors.dataAccess.label', default: 'Data Access Error')}" />
	<title><warehouse:message code="errors.dataAccess.label" default="Data Access Error"/></title>
	<content tag="title"><warehouse:message code="dataAccess.label" default="Page Not Found"/></content>
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
                <warehouse:message code="errors.dataAccess.label" default="Data Access Error"/>
            </div>
        </div>

		<div style="width: 25%;">
			<div class="triangle-isosceles">
				<warehouse:message code="errors.dataAccess.message" default="Apologies, but you just tried to do something unspeakable to the database."/>
			</div>
			<div style="padding-left: 45px;" class="nailthumb-container">
				<img src="${createLinkTo(dir:'images',file:'jgreenspan.png')}"/>
			</div>
		</div>

        <h2>Error Details</h2>
        <div class="message top">
            <strong>Error ${request?.'javax.servlet.error.status_code'}:</strong>
            ${request?.'javax.servlet.error.message'?.encodeAsHTML()}<br/>
            <strong>Servlet:</strong> ${request?.'javax.servlet.error.servlet_name'}<br/>
            <strong>URI:</strong> ${request?.'javax.servlet.error.request_uri'}<br/>
            <g:if test="${exception}">
                <strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
                <strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
                <strong>Class:</strong> ${exception.className} <br />
                <strong>At Line:</strong> [${exception.lineNumber}] <br />
                <strong>Code Snippet:</strong><br />
                <div class="snippet">
                    <g:each var="cs" in="${exception.codeSnippet}">
                        ${cs?.encodeAsHTML()}<br />
                    </g:each>
                </div>
            </g:if>
        </div>
        <g:if test="${exception}">
            <h2>Stack Trace</h2>
            <div class="stack">
                <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
            </div>
        </g:if>
	</div>
	<script>
		$(function() {
			$('.nailthumb-container img').nailthumb({width : 100, height : 100});
		});
	</script>
</body>
