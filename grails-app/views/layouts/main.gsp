<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>
        <g:layoutTitle default="OpenBoxes"/>
    </title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>

    <asset:stylesheet src="application.css"/>

    <g:layoutHead/>
</head>

<body>

<header>
    <nav class="navbar navbar-expand-md navbar-light" role="navigation">
        <a href="#" class="navbar-brand">
            <g:displayLogo location="${session?.warehouse?.id}" includeLink="${false}"/>
        </a>
        <button class="navbar-toggler" type="button" data-toggle="collapse"
                data-target="#navbarContent" aria-controls="navbarContent" aria-expanded="false"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" aria-expanded="false" id="navbarContent">
            <ul class="nav navbar-nav">
                <li class="nav-item">
                    <g:link controller="dashboard" action="index" class="nav-link">
                        <warehouse:message code="dashboard.label"/>
                    </g:link>
                </li>
            </ul>
        </div>
    </nav>
</header>

<ol class="breadcrumb">
    <li class="breadcrumb-item">
        <g:link controller="dashboard" action="index">Home</g:link>
    </li>
    <g:if test="${session?.user && session?.warehouse}">
        <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
        <li class="breadcrumb-item">
            <a class="btn-show-dialog" href="javascript:void(-1);"
               data-title="${g.message(code:'dashboard.chooseLocation.label')}"
               data-url="${request.contextPath}/dashboard/changeLocation?targetUri=${targetUri}">
                ${session?.warehouse?.name }
            </a>
        </li>
    </g:if>
    <g:if test="${controllerName }">
        <li class="breadcrumb-item">
            <g:link controller="${controllerName }" action="index">
                <warehouse:message code="${controllerName + '.label'}" />
            </g:link>
        </li>
    </g:if>
    <g:if test="${g.layoutTitle() && !actionName.equals('index') && !actionName.equals('list') }">
        <li class="breadcrumb-item active">
            <a href="#">${g.layoutTitle()}</a>
        </li>
    </g:if>
</ol>

<g:pageProperty name="page.actions"/>

<g:layoutBody/>

<div class="footer row" role="contentinfo">
    <g:if test="${session?.user && session?.warehouse}">
        <div id="ft" role="contentinfo">
            <g:include controller="dashboard" action="footer"/>
        </div>
    </g:if>

</div>


<div id="spinner" class="spinner" style="display:none;">
    <g:message code="spinner.alt" default="Loading&hellip;"/>
</div>

<asset:javascript src="application.js"/>

</body>
</html>
