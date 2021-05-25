<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="bootstrap" />
	<title><warehouse:message code="auth.title"/></title>
	<script src="${createLinkTo(dir:'js/', file:'detect_timezone.js')}" type="text/javascript" ></script>
    <link rel="stylesheet" href="${createLinkTo(dir:'css/', file:'login.css')}">
</head>
<body class="text-center">

<div class="vertical-center">

    <div class="container">
        <div class="login-form">

            <g:if test="${flash.message}">
                <div class="alert alert-primary">${flash.message}</div>
            </g:if>

            <g:hasErrors bean="${userInstance}">
               <div class="errors">
                   <g:renderErrors bean="${userInstance}" as="list" />
               </div>
            </g:hasErrors>


            <g:form controller="auth" action="handleLogin" method="post">
                <g:hiddenField name="targetUri" value="${params?.targetUri}" />
                <g:hiddenField id="browserTimezone" name="browserTimezone" />

                <h2 class="text-center">
                    <i class="fa fa-arrow-circle-o-right"></i>
                    <warehouse:message code="default.login.label" default="Login"/>
                </h2>
                <div class="form-group">
                    <div class="input-group">
                        <div class="input-group-prepend">
                            <span class="input-group-text">
                                <span class="fa fa-user"></span>
                            </span>
                        </div>
                        <input type="text" class="form-control" id="username" name="username" placeholder="Email or username" required="required">
                    </div>
                </div>

                <div class="form-group">
                    <div class="input-group">
                        <div class="input-group-prepend">
                            <span class="input-group-text">
                                <i class="fa fa-lock"></i>
                            </span>
                        </div>
                        <input type="password" class="form-control" name="password" placeholder="Password" required="required">
                    </div>
                </div>
                <div class="form-group">
                    <button type="submit" class="btn btn-success btn-block login-btn"><g:message code="auth.login.label"/></button>
                </div>
                <g:set var="providers" value="${grailsApplication.config.openboxes.oauth2Providers.findAll { Boolean.valueOf(it.value.enabled) }}"></g:set>
                <g:if test="${providers.size()}">
                    <div class="or-seperator"><i>or</i></div>
                    <div class="text-center social-btn">
                        <g:each var="provider" in="${providers}">
                            <g:set var="providerConfig" value="${provider.value}"/>
                            <g:if test="${Boolean.valueOf(providerConfig.enabled)}">
                                <g:link controller="openIdConnect" action="authenticate" id="${provider.key}" class="${providerConfig.btnClass}">
                                    <i class="${providerConfig?.iconClass}"></i>
                                    Sign in with <b>${providerConfig?.title?:provider.key}</b>
                                </g:link>
                            </g:if>
                        </g:each>
                    </div>
                </g:if>
            </g:form>
            <div class="hint-text">
                <warehouse:message code="auth.newuser.text"/>
                <g:link controller="auth" action="signup" class="text-success">
                    <warehouse:message code="auth.signup.label" default="Signup"/>
                </g:link>
            </div>
        </div>
    </div>
</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var timezone = jzTimezoneDetector.determine_timezone().timezone; // Now you have an instance of the TimeZone object.
			$("#browserTimezone").val(timezone.olson_tz); // Set the user timezone offset as a hidden input
		});
	</script>
</body>
</html>
