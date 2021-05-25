<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="bootstrap" />
	<title><g:message code="auth.signup.label"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<script src="${createLinkTo(dir:'js/', file:'detect_timezone.js')}" type="text/javascript" ></script>
	<g:if test="${grailsApplication.config.openboxes.signup.recaptcha.enabled}">
	    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
	</g:if>
    <link rel="stylesheet" href="${createLinkTo(dir:'css/', file:'login.css')}">
    <link rel="stylesheet" href="${createLinkTo(dir:'css/', file:'floating-labels.css')}">
</head>
<body class="text-center">

<div class="vertical-center">

    <div class="container">
        <div class="login-form">
            <g:if test="${flash.message}">
                <div class="alert alert-primary">${flash.message}</div>
            </g:if>

            <g:hasErrors bean="${userInstance}">
               <div class="alert alert-danger">
                   <g:renderErrors bean="${userInstance}" as="list" />
               </div>
            </g:hasErrors>


			<g:form name="handleSignup" controller="auth" action="handleSignup" method="POST">
                <h2 class="text-center">
                    <i class="fa fa-user-circle-o"></i>
                    <warehouse:message code="default.signup.label" default="Signup for an account"/>
                </h2>
                <div class="form-label-group">
                    <g:textField name="firstName" value="${userInstance?.firstName}" class="form-control required"
                                 placeholder="${g.message(code: 'user.firstName.label')}" autofocus="autofocus"/>
                    <label for="firstName">${g.message(code: "user.firstName.label")}</label>
                </div>
                <div class="form-label-group">
                    <g:textField name="lastName" value="${userInstance?.lastName}" class="form-control required"
                                 placeholder="${g.message(code: 'user.lastName.label')}"/>
                    <label for="lastName">${g.message(code: "user.lastName.label")}</label>
                </div>

                <div class="form-label-group">
                    <g:textField id="email" name="email" value="${userInstance?.email}" class="form-control required"
                                 placeholder="${g.message(code: 'user.email.label')}" placeholder-shown="test"/>
                    <label for="email">${g.message(code: "user.email.label")}</label>
                </div>
                <div class="form-label-group">
                    <g:passwordField id="password" name="password" value="${userInstance?.password}" class="form-control"
                                     placeholder="${g.message(code: 'user.password.label')}"/>
                    <label for="password">${g.message(code: "user.password.label")}</label>
                </div>
                <div class="form-label-group">
                    <g:passwordField id="passwordConfirm" name="passwordConfirm" value="${userInstance?.passwordConfirm}" class="form-control"
                                     placeholder="${g.message(code: 'user.confirmPassword.label')}"/>
                    <label for="passwordConfirm">${g.message(code: "user.confirmPassword.label")}</label>
                </div>
                <div class="form-group">
                    <a href="#collapse" data-toggle="collapse" class="text-center">Additional options</a>
                </div>
                <div id="collapse" class="collapse in">
                    <div class="form-row">
                        <div class="form-group col-6">
                            <label for="timezone" class="control-label">${g.message(code: "default.timezone.label")}</label>
                            <g:selectTimezone id="timezone" name="timezone" value="${params?.timezone?:userInstance?.timezone}"
                                              class="form-control" placeholder="${g.message(code: "default.timezone.label")}"
                                              noSelection="['':'']" />
                        </div>
                        <div class="form-group col-6">
                            <label for="locale" class="control-label">${g.message(code: "default.locale.label")}</label>
                            <g:set var="localeCode" value="default.locale.code"/>
                            <g:selectLocale id="locale" name="locale" value="${params?.locale?:userInstance?.locale?:'en'}"
                                            placeholder="${g.message(code: 'default.locale.label')}"
                                            noSelection='["":""]' class="form-control" />
                        </div>
                    </div>
                    <g:if test="${grailsApplication.config.openboxes.signup.additionalQuestions.enabled}">
                        <g:each var="question" in="${grailsApplication.config.openboxes.signup.additionalQuestions.content}">
                            <div class="form-group">
                                <label for="${question.id}" class="control-label">
                                    ${question.label}
                                </label>
                                <g:if test="${question.options}">
                                    <g:set var="questionKey" value="additionalQuestions.${question.id}"/>
                                    <g:set var="selectedKey" value="${params[questionKey]}"/>
                                    <select name="${questionKey}" class="form-control">
                                        <g:each var="option" in="${question.options}">
                                            <g:if test="${option.key == selectedKey}">
                                                <option value="${option.key}" selected>${option.value}</option>
                                            </g:if>
                                            <g:else>
                                                <option value="${option.key}">${option.value}</option>
                                            </g:else>
                                        </g:each>
                                    </select>
                                </g:if>
                            </div>
                        </g:each>
                    </g:if>
                    <div class="form-group">
                        <label for="additionalQuestions.comments" class="control-label">
                            <warehouse:message code="default.comments.label" default="Comments" />
                        </label>
                        <g:textArea name="additionalQuestions.comments" rows="3" class="form-control"
                                    placeholder="Tell us more about requirements. What features are important to you? Do you need help getting started?">${params?.comments}</g:textArea>
                    </div>
                </div>

                <div class="form-group">
                    <g:if test="${grailsApplication.config.openboxes.signup.recaptcha.enabled}">
                        <button type="submit" class="btn btn-success btn-block login-btn g-recaptcha"
                                data-sitekey="${grailsApplication.config.openboxes.signup.recaptcha.v2.siteKey}"
                                data-callback="validateRecaptchaToken"
                                data-action="submit">
                            <warehouse:message code="auth.signup.label"/>
                        </button>
                    </g:if>
                    <g:else>
                        <button type="submit" class="btn btn-outline-primary btn-block login-btn">
                            <g:message code="auth.signup.label"/>
                        </button>
                    </g:else>
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
                                    Sign up with <b>${providerConfig?.title?:provider.key}</b>
                                </g:link>
                            </g:if>
                        </g:each>
                    </div>
                </g:if>


            </g:form>
            <div class="hint-text">
                <warehouse:message code="auth.alreadyHaveAccount.text"/>
                <g:link controller="auth" action="login" class="text-success">
                    <warehouse:message code="auth.login.label" default="Login"/>
                </g:link>
            </div>
        </div>
    </div>
</div>
<script>
	function validateRecaptchaToken(token) {
		$("#handleSignup")
		.submit();
	}

	$(document).ready(function() {
		var timezone = jzTimezoneDetector.determine_timezone().timezone;
		if (timezone) {
			$("#timezone")
			.val(timezone.olson_tz)
			.trigger("chosen:updated");
		}
	});
</script>
</body>
</html>
