<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>
        <g:layoutTitle default="Grails"/>
    </title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>

    <asset:stylesheet src="application.css"/>

    <g:layoutHead/>
</head>

<body>

<header>
    <nav class="navbar navbar-expand-md navbar-light bg-light" role="navigation">
        <div class="navbar-brand">
            <g:displayLogo location="${session?.warehouse?.id}" includeLink="${false}"/>
        </div>
        <button class="navbar-toggler" type="button" data-toggle="collapse"
                data-target="#navbarContent" aria-controls="navbarContent" aria-expanded="false"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" aria-expanded="false" id="navbarContent">
            <ul class="navbar-nav">

                <li class="nav-item ">
                    <g:link controller="dashboard" action="index" class="nav-link">
                        <i class="fa fa-step-backward"></i>
                        <warehouse:message code="default.ignoreError.label"/>&nbsp;
                    </g:link>
                </li>
            </ul>
        </div>
    </nav>
</header>

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
