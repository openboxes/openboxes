<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title>Warehouse &gt; Login</title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
</head>
<body>
	<div class="body">
		<g:form controller="auth" action="doLogin" method="post">		  
		    <div class="dialog">
				<div id="loginForm">
					<g:if test="${flash.message}">
					    <div class="message">${flash.message}</div>
					</g:if>		
		
					<g:hasErrors bean="${userInstance}">
					   <div class="errors">
					       <g:renderErrors bean="${userInstance}" as="list" />
					   </div>
					</g:hasErrors>		
		
		
					<fieldset> 			
						<legend>Login</legend>		
					
						
						<script>	
							jQuery(document).ready(function() {
								// focus on the first text input field in the first field on the page
								jQuery("select:first", document.forms[0]).focus();
							});	
						</script>		
					

						<table>
							<tbody>

								<tr class="prop">
									<td valign="top" class="name">
										<label for="email"><g:message code="user.email.label" default="Email" /></label>
									</td>
									<td valign="top" class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
										<g:textField name="email" value="${userInstance?.email}"  />
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name">
										<label for="password"><g:message code="user.password.label" default="Password" /></label>
									</td>
									<td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
										<g:passwordField name="password" value="${userInstance?.password}" />
									</td>
								</tr>
								<tr >
									<td valign="top">

									</td>
									<td valign="top">
										<div style="text-align: right;">
											<div class="buttons" >
												<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt=""/> Login</button>					   
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
