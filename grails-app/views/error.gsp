<!doctype html>
<html>
    <head>
        <title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
        <meta name="layout" content="main">
        <g:if env="development"><asset:stylesheet src="errors.css"/></g:if>
    </head>
    <body>
        <content tag="page.actions">
            test
            <nav class="navbar">
                <ul class="nav navbar-nav">
                    <li class="nav-item">
                        <g:link controller="dashboard" action="index" onClick="javascript:window.history.go(-1);" class="btn btn-info">
                            <g:message code="default.reportAsError.label"/>&nbsp;
                        </g:link>
                        <g:link controller="dashboard" action="index" onClick="javascript:window.history.go(-1);" class="btn btn-danger">
                            <g:message code="default.ignoreError.label"/>&nbsp;
                        </g:link>
                    </li>
                </ul>
            </nav>
        </content>
        <div class="container-fluid">
            <g:if test="${Throwable.isInstance(exception)}">
                <g:renderException exception="${exception}" />
            </g:if>
            <g:elseif test="${request.getAttribute('javax.servlet.error.exception')}">
                <g:renderException exception="${request.getAttribute('javax.servlet.error.exception')}" />
            </g:elseif>
            <g:else>
                <ul class="errors">
                    <li>An error has occurred</li>
                    <li>Exception: ${exception}</li>
                    <li>Message: ${message}</li>
                    <li>Path: ${path}</li>
                </ul>
            </g:else>
        </div>
    </body>
</html>
