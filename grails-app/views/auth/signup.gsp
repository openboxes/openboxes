<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="auth.signup.label"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->

		

</head>
<body>

	<style>
		#hd { display: none; }  	
	</style>

	<div class="body">
		<g:form controller="auth" action="handleSignup" method="post">		  
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
			
					<fieldset> 			
						<legend>		
							<div id="logo">
					    		<img src="${createLinkTo(dir:'images/icons/',file:'logo.gif')}" alt="Your Boxes. You're Welcome."/>
				    			<span class="middle title" style="font-size: 2em; color: #666;"><warehouse:message code="default.openboxes.label"/></span>
							</div>			
						</legend>
						<table>
							<tbody>
								<tr>
									<td colspan="2">
								<%-- 
										<warehouse:message code="auth.enterAccountDetails.text"/>
								--%>
									</td>	
								</tr>
					            <tr class="prop">
					                <td class="name">
					                    <label for="firstName"><warehouse:message code="user.firstName.label" default="First Name" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
					                    <g:textField name="firstName" value="${userInstance?.firstName}" class="text"/>
					                </td>
					            </tr>
	
					            <tr class="prop">
					                <td class="name">
					                    <label for="lastName"><warehouse:message code="user.lastName.label" default="Last Name" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
					                    <g:textField name="lastName" value="${userInstance?.lastName}" class="text"/>
					                </td>
					            </tr>

					            <tr class="prop">
					                <td class="name">
					                    <label for="email"><warehouse:message code="user.email.label" default="Email" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
					                    <g:textField name="email" value="${userInstance?.email}" class="text"/>
					                </td>
					            </tr>
						
					            <tr class="prop">
					                <td class="name">
					                    <label for="username"><warehouse:message code="user.username.label" default="Username" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
					                    <g:textField name="username" value="${userInstance?.username}" class="text" />
					                </td>
					            </tr>

	
					            <tr class="prop">
					                <td class="name">
					                    <label for="password"><warehouse:message code="user.password.label" default="Password" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
					                    <g:passwordField name="password" value="${userInstance?.password}" class="text"/>
					                </td>
					            </tr>
						    
					            <tr class="prop">
					                <td class="name">
					                  <label for="passwordConfirm"><warehouse:message code="user.confirmPassword.label" default="Confirm Password" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
					                    <g:passwordField name="passwordConfirm" value="${userInstance?.passwordConfirm}" class="text" />
					                </td>
					            </tr>	
	                            <tr class="prop">
	                                <td class="name">
	                                  <label for="locale"><warehouse:message code="default.locale.label"/></label>
	                                </td>
	                                <td class="value ${hasErrors(bean: userInstance, field: 'locale', 'errors')}">
	                                    <g:select name="locale" from="${ grailsApplication.config.locale.supportedLocales.collect{ new Locale(it) } }" optionValue="displayName" value="${userInstance?.locale}" noSelection="['':'']" class="large"/>
	                                </td>
	                            </tr>	
								<tr class="prop">	
									<td class="name"></td>					
									<td valign="top">
										<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" alt=""/> <warehouse:message code="auth.signup.label"/></button>					   
										&nbsp;
										<g:link class="list" controller="auth" action="login">
											<warehouse:message code="default.button.cancel.label" />
										</g:link>
										
										
									</td>
								</tr>
								<tr class="prop">
									<td class="name" colspan="2">
										<div style="text-align: left">				
											<warehouse:message code="auth.alreadyHaveAccount.text"/> <g:link class="list" controller="auth" action="login"><warehouse:message code="auth.login.label" default="Login"/></g:link>
										</div>
									</td>
								</tr>								
								
							</tbody>	
						</table>						
					</fieldset> 
				</div>			
			</div>
		</g:form>		
	</div>
</body>
</html>
       
