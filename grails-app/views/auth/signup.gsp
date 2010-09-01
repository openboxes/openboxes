<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title>Warehouse &gt; Signup</title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->

		

</head>
<body>
	<div class="body">
		<g:form controller="auth" action="signup" method="post">		  
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
						<legend>Request an Account</legend>		
							
						<table>
							<tbody>

							<tr>
								<td colspan="2">
									Enter account information below.
								</td>	
							</tr>

					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="email"><g:message code="user.name.label" default="Email" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
					                    <g:textField name="email" value="${userInstance?.email}" />
					                </td>
					            </tr>

					            
					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="firstName"><g:message code="user.name.label" default="First Name" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
					                    <g:textField name="firstName" value="${userInstance?.firstName}" />
					                </td>
					            </tr>
	
					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="lastName"><g:message code="user.name.label" default="Last Name" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
					                    <g:textField name="lastName" value="${userInstance?.lastName}" />
					                </td>
					            </tr>
					        
							<tr class="prop">
								<td colspan="2"><hr/></td>
							</tr>
						
	
					            <tr class="prop">
					                <td valign="top" class="name">
					                    <label for="password"><g:message code="user.password.label" default="Password" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
					                    <g:passwordField name="password" value="${userInstance?.password}" />
					                </td>
					            </tr>
						    
					            <tr class="prop">
					                <td valign="top" class="name">
					                  <label for="passwordConfirm"><g:message code="user.email.label" default="Confirm Password" /></label>
					                </td>
					                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
					                    <g:passwordField name="passwordConfirm" value="${userInstance?.passwordConfirm}" />
					                </td>
					            </tr>	
	
								<tr class="prop">	
									<td class="name"></td>					
									<td valign="top" style="text-align: right">
										<div class="buttons">		
											<div class="button" >
												<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt=""/> Signup</button>					   
											</div>		
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
       
