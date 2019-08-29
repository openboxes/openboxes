<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="auth.signup.label"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<style>
		#hd { display: none; }
        input, select { width: 100%; }
        .required { color: red}
	</style>

		

</head>
<body>
	<div class="body">
		<g:form controller="auth" action="handleSignup" method="POST">		  
		    <div class="dialog">
				<div id="signupForm">
					<g:if test="${flash.message}">
					    <div class="message">${flash.message}</div>
					</g:if>		
			
					<g:hasErrors bean="${userInstance}">
					   <div class="errors">
					       <g:renderErrors bean="${userInstance}" as="list" />
					   </div>
					</g:hasErrors>		
								
					<div id="loginBox" class="box">
						<h2>
							<img src="${createLinkTo(dir:'images/icons/silk',file:'lock.png')}" class="middle"/> Signup for an account
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
									<td class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
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
										<div style="width: 235px">
											<g:select name="locale" from="${ grailsApplication.config.openboxes.locale.supportedLocales.collect{ new Locale(it) } }"
													  optionValue="displayName" value="${userInstance?.locale}" noSelection="['':'']" class="chzn-select-deselect"/>
										</div>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="locale"><warehouse:message
												code="default.timezone.label" default="Timezone" /></label></td>
									<td valign="top" class="value">
										<g:if test="${timezones}">
											<g:selectTimezone id="timezone" name="timezone" value="${userInstance?.timezone}"
															  noSelection="['':'']"
															  class="chzn-select-deselect"/>
										</g:if>
										<g:else>
											<g:textField name="timezone" value="${userInstance?.timezone}" size="40" class="text medium"/>
										</g:else>
									</td>
								</tr>

                                <tr class="prop">
                                    <td class="name">
                                        <label for="interest"><warehouse:message code="user.interest.label" default="Interest" /></label>
                                    </td>
                                    <td class="value">
                                        <select id="interest" name="interest" class="chzn-select-deselect" value="${params.interest}">
                                            <option value="none"></option>
                                            <option value="looking">I'm just poking around - don't mind me</option>
                                            <option value="personal">I'm evaluating OpenBoxes for personal use</option>
                                            <option value="company">I'm evaluating OpenBoxes for my company</option>
                                            <option value="contribute">I'd like to contribute to OpenBoxes</option>
                                            <option value="contact">I have no idea what I'm doing, please contact me</option>
                                            <option value="other">Other</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr class="prop">
									<td class="name">
                                        <label for="comments"><warehouse:message
                                                code="default.comments.label" default="Comments" /></label>
                                    </td>
									<td valign="top">
                                        <g:textArea name="comments" style="width: 100%;" rows="5"
                                                    placeholder="What features are important to you? Do you need help getting started?">${params?.comments}</g:textArea>

									</td>
								</tr>


								<tr class="prop">
									<td class="name middle right"></td>
									<td valign="top">
										<button type="submit" class="button icon approve">
											<warehouse:message code="auth.signup.label"/>
										</button>
									</td>
								</tr>
								<tr class="prop">
									<td colspan="2">
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
</body>
</html>
       
