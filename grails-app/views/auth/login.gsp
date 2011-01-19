<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title>Login</title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
</head>
<body>
	<style>
		#hd { display: none; }  
		
	</style>

	<div class="body">
		<g:form controller="auth" action="handleLogin" method="post">	
		
			<g:hiddenField name="targetUri" value="${params?.targetUri}" />
			  
		    <div class="dialog">

				<div id="loginForm">
					<g:hasErrors bean="${userInstance}">
					   <div class="errors">
					       <g:renderErrors bean="${userInstance}" as="list" />
					   </div>
					</g:hasErrors>		
					
					<g:if test="${flash.message}">
					    <div class="message">${flash.message}</div>
					</g:if>				
					
					
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
						<script>	
							jQuery(document).ready(function() {
								// focus on the first text input field in the first field on the page
								jQuery("select:first", document.forms[0]).focus();
							});	
						</script>		

						<table>
							<tbody>

								<tr class="">
									<td colspan="2">
										
									</td>	
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="email" class="loginField"><g:message code="user.usernameOrEmail.label" default="Username" /></label>
									</td>
									<td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
										<g:textField class="loginField" name="username" value="${userInstance?.username}" size="25" />
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="password" class="loginField"><g:message code="user.password.label" default="Password" /></label>
									</td>
									<td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
										<g:passwordField class="loginField" name="password" value="${userInstance?.password}" size="25" />
									</td>
								</tr>
								<tr class="prop">
									
									<td colspan="2" valign="top" style="text-align: center;">
										<span class="buttons" >
											<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt=""/> Login</button>												
										</span>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="" colspan="2">
										<div style="text-align: left">				
											Are you a new user? <g:link class="list" controller="auth" action="signup"><g:message code="default.signup.label" default="Signup"/></g:link>
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
