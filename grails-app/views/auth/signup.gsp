<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="auth.signup.label"/></title>
	<script src="${resource(dir:'js/', file:'detect_timezone.js')}" type="text/javascript" ></script>
	<g:if test="${grailsApplication.config.openboxes.signup.recaptcha.enabled}">
	<script src="https://www.google.com/recaptcha/api.js" async defer></script>
	</g:if>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<style>
		#hd { display: none; }
        input, select { width: 100%; }
        .required { color: red}
	</style>
</head>
<body>
	<div class="body">
		<g:form name="handleSignup" controller="auth" action="handleSignup" method="POST">
		    <div class="dialog">
				<div id="signupForm">
					<g:if test="${flash.message}">
					    <div class="message" role="status" aria-label="message">${flash.message}</div>
					</g:if>

					<g:hasErrors bean="${userInstance}">
					   <div class="errors" role="alert" aria-label="error-message">
					       <g:renderErrors bean="${userInstance}" as="list" />
					   </div>
					</g:hasErrors>
					<div id="loginBox" class="box">
						<h2>
							<img src="${resource(dir:'images/icons/silk',file:'lock.png')}" class="middle"/> Signup for an account
						</h2>
                        <table>
							<tbody>
					            <tr class="prop">
					                <td class="name middle right">
                                        <span class="required">*</span>
					                    <label for="firstName"><warehouse:message code="user.firstName.label" default="First Name" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
					                    <g:textField name="firstName" value="${userInstance?.firstName}" class="text" size="40"/>
					                </td>
					            </tr>

					            <tr class="prop">
					                <td class="name middle right">
                                        <span class="required">*</span>
					                    <label for="lastName"><warehouse:message code="user.lastName.label" default="Last Name" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
					                    <g:textField name="lastName" value="${userInstance?.lastName}" class="text" size="40"/>
					                </td>
					            </tr>

								<tr class="prop">
									<td class="name middle right">
                                        <span class="required">*</span>
										<label for="email"><warehouse:message code="user.email.label" default="Email" /></label>
									</td>
									<td class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
										<g:textField name="email" value="${userInstance?.email}" class="text" size="40"/>
									</td>
								</tr>

					            <tr class="prop">
					                <td class="name middle right">
                                        <span class="required">*</span>
					                    <label for="password"><warehouse:message code="user.password.label" default="Password" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
					                    <g:passwordField name="password" value="${userInstance?.password}" class="text" size="40"/>
					                </td>
					            </tr>

					            <tr class="prop">
					                <td class="name middle right">
                                        <span class="required">*</span>
                                        <label for="passwordConfirm"><warehouse:message code="user.confirmPassword.label" default="Confirm Password" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
					                    <g:passwordField name="passwordConfirm" value="${userInstance?.passwordConfirm}" class="text" size="40" />
					                </td>
					            </tr>
								<tr class="prop">
									<td class="name middle right">
										<label for="locale"><warehouse:message code="default.locale.label"/></label>
									</td>
									<td class="value ${hasErrors(bean: userInstance, field: 'locale', 'errors')}">
										<g:selectLocale name="locale" value="${params?.locale?:userInstance?.locale}" noSelection="['':'']" class="chzn-select-deselect"/>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="locale"><warehouse:message
												code="default.timezone.label" default="Timezone" /></label></td>
									<td valign="top" class="value">
										<g:selectTimezone id="timezone" name="timezone" value="${params?.timezone?:userInstance?.timezone}"
														  noSelection="['':'']"
														  class="chzn-select-deselect"/>
									</td>
								</tr>
								<g:if test="${grailsApplication.config.openboxes.signup.additionalQuestions.enabled}">
									<g:each var="question" in="${grailsApplication.config.openboxes.signup.additionalQuestions.content}">
										<tr class="prop">
											<td class="name">
												<label for="${question.id}">
													${question.label}
												</label>
											</td>
											<td class="value">
												<g:if test="${question.options}">
													<g:set var="questionKey" value="additionalQuestions.${question.id}"/>
													<g:set var="selectedKey" value="${params[questionKey]}"/>
													<select name="${questionKey}" class="chzn-select-deselect">
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
											</td>
										</tr>
									</g:each>
								</g:if>
                                <tr class="prop">
									<td class="name">
                                        <label for="additionalQuestions.comments"><warehouse:message
                                                code="default.comments.label" default="Comments" /></label>
                                    </td>
									<td valign="top">
                                        <g:textArea name="additionalQuestions.comments" rows="5" class="text large"
                                                    placeholder="Tell us more about yourself. What features are important to you? Do you need help getting started?">${params?.comments}</g:textArea>
									</td>
								</tr>
								<tr class="prop">
									<td class="name middle right"></td>
									<td valign="top">
										<g:if test="${grailsApplication.config.openboxes.signup.recaptcha.enabled}">
											<button type="submit" class="button block g-recaptcha"
													data-sitekey="${grailsApplication.config.openboxes.signup.recaptcha.v2.siteKey}"
													data-callback="validateRecaptchaToken"
													data-action="submit">
												<img src="${resource(dir:'images/icons/silk',file:'accept.png')}" class="middle"/>
												<warehouse:message code="auth.signup.label"/>
											</button>
										</g:if>
										<g:else>
											<button type="submit" class="button block">
												<warehouse:message code="auth.signup.label"/>
											</button>
										</g:else>
									</td>
								</tr>
								<tr class="prop">
									<td class="name">
									</td>
									<td class="value">
										<warehouse:message code="auth.alreadyHaveAccount.text"/>
										<g:link class="list" controller="auth" action="login">
											<warehouse:message code="auth.login.label" default="Login"/>
										</g:link>
                                        <div class="right">
                                            <span class="required">*</span> denotes required fields
                                        </div>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</g:form>
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

