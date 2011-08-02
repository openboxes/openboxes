<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title>Signup</title>
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
								<a class="home" href="${createLink(uri: '/dashboard/index')}" style="text-decoration: none">						    	
						    		<img src="${createLinkTo(dir:'images/icons/',file:'logo.gif')}" alt="Your Boxes. You're Welcome." 
						    			style="vertical-align: absmiddle"/>
						    			<span style="font-size: 2em; vertical-align: top;">openboxes</span>
							    </a>					
							</div>			
						</legend>
						<table>
							<tbody>

								<tr>
									<td colspan="2">
										Enter your account details below.
									</td>	
								</tr>
					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="firstName"><warehouse:message code="user.firstName.label" default="First Name" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
					                    <g:textField name="firstName" value="${userInstance?.firstName}" />
					                </td>
					            </tr>
	
					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="lastName"><warehouse:message code="user.lastName.label" default="Last Name" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
					                    <g:textField name="lastName" value="${userInstance?.lastName}" />
					                </td>
					            </tr>

					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="email"><warehouse:message code="user.email.label" default="Email" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
					                    <g:textField name="email" value="${userInstance?.email}" />
					                </td>
					            </tr>
					        
								<tr class="prop">
									<td colspan="2"><hr/></td>
								</tr>
						
					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="username"><warehouse:message code="user.username.label" default="Username" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
					                    <g:textField name="username" value="${userInstance?.username}" />
					                </td>
					            </tr>

	
					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="password"><warehouse:message code="user.password.label" default="Password" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
					                    <g:passwordField name="password" value="${userInstance?.password}" />
					                </td>
					            </tr>
						    
					            <tr class="prop">
					                <td valign="top" class="name">
					                  <label for="passwordConfirm"><warehouse:message code="user.email.label" default="Confirm Password" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
					                    <g:passwordField name="passwordConfirm" value="${userInstance?.passwordConfirm}" />
					                </td>
					            </tr>	
	
								<tr class="prop">	
									<td class=""></td>					
									<td valign="top" style="text-align: right">
										<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt=""/> Signup</button>					   
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="" colspan="2">
										<div style="text-align: left">				
											Already have an account? <g:link class="list" controller="auth" action="login"><warehouse:message code="default.login.label" default="Login"/></g:link>
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
       
