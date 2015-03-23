<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="auth.signup.label"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<style>
		#hd { display: none; }
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
						<table>
							<thead>
								<tr>
									<td class="left middle" colspan="2">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'lock.png')}" class="middle"/>
										<span class="middle title">
											Signup for an account
										</span>				
									</td>
								</tr>							
								<tr>
									<td class="left middle" colspan="2">
										<hr/>
									</td>
								</tr>								
							</thead>						
							<tbody>
					            <tr class="propOff">
					                <td class="name middle right" width="35%">
					                    <label for="firstName"><warehouse:message code="user.firstName.label" default="First Name" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
					                    <g:textField name="firstName" value="${userInstance?.firstName}" class="text" size="30"/>
					                </td>
					            </tr>
	
					            <tr class="propOff">
					                <td class="name middle right">
					                    <label for="lastName"><warehouse:message code="user.lastName.label" default="Last Name" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
					                    <g:textField name="lastName" value="${userInstance?.lastName}" class="text" size="30"/>
					                </td>
					            </tr>

					            <tr class="">
					                <td class="name middle right">
					                    <label for="email"><warehouse:message code="user.email.label" default="Email" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
					                    <g:textField name="email" value="${userInstance?.email}" class="text" size="30"/>
					                </td>
					            </tr>
						    <tr class="">
						        <td class="middle right">
						          <label for="locale"><warehouse:message code="default.locale.label"/></label>
						        </td>
						        <td class="value ${hasErrors(bean: userInstance, field: 'locale', 'errors')}">
						            <g:select name="locale" from="${ grailsApplication.config.openboxes.locale.supportedLocales.collect{ new Locale(it) } }"
											  optionValue="displayName" value="${userInstance?.locale}" noSelection="['':'']" class="large"/>
						        </td>
						    </tr>	
						
					            <tr class="">
					                <td class="middle right">
					                    <label for="username"><warehouse:message code="user.username.label" default="Username" /></label>
					                </td>
					                <td class="${hasErrors(bean: userInstance, field: 'username', 'errors')}">
					                    <g:textField name="username" value="${userInstance?.username}" class="text"  size="30" />
					                </td>
					            </tr>

	
					            <tr class="">
					                <td class="name middle right">
					                    <label for="password"><warehouse:message code="user.password.label" default="Password" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
					                    <g:passwordField name="password" value="${userInstance?.password}" class="text" size="30"/>
					                </td>
					            </tr>
						    
					            <tr class="">
					                <td class="middle right">
					                  <label for="passwordConfirm"><warehouse:message code="user.confirmPassword.label" default="Confirm Password" /></label>
					                </td>
					                <td class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
					                    <g:passwordField name="passwordConfirm" value="${userInstance?.passwordConfirm}" class="text" size="30" />
					                </td>
					            </tr>	
								<tr class="">	
									<td class="middle right"></td>					
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
       
