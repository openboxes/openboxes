<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="auth.title"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<script src="${createLinkTo(dir:'js/', file:'detect_timezone.js')}" type="text/javascript" ></script>
</head>
<body>
	<style>
		#hd { display: none; }  
	</style>
	
	<script type="text/javascript"> 	
		jQuery(document).ready(function() {
			jQuery("#usernameField").focus(); // Focus on the first text input field in the page
			var timezone = jzTimezoneDetector.determine_timezone().timezone; // Now you have an instance of the TimeZone object.
			jQuery("#browserTimezone").val(timezone.olson_tz); // Set the user timezone offset as a hidden input
		});	
	</script>

	<div class="body">
		<g:form controller="auth" action="handleLogin" method="post">	
		
			<g:hiddenField name="targetUri" value="${params?.targetUri}" />
			<g:hiddenField id="browserTimezone" name="browserTimezone" />
			  
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
						<legend>							
							<div id="logo">
						    	<img src="${createLinkTo(dir:'images/icons/',file:'logo24.png')}" title="${warehouse.message(code:'default.tagline.label') }" class="middle"/>&nbsp;
								<span class="middle title" style="font-size: 2em; color: #666; vertical-align: middle;"><warehouse:message code="default.openboxes.label"/></span>
							</div>	
						</legend>
						<table>
							<tbody>

								<tr class="">
									<td colspan="2">
										
									</td>	
								</tr>
								<tr class="">
									<td class="right middle">
										<label for="email" class="loginField"><warehouse:message code="user.username.label" default="Username" /></label>
									</td>
									<td class="left middle" ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
										<g:textField class="loginField text" id="usernameField" name="username" value="${userInstance?.username}" size="35" />
									</td>
								</tr>
								<tr class="">
									<td class="right middle">
										<label for="password" class="loginField"><warehouse:message code="user.password.label" default="Password" /></label>
									</td>
									<td class="left middle" ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
										<g:passwordField class="loginField text" name="password" value="${userInstance?.password}" size="35" />
									</td>
								</tr>
								<tr class="">
									<td colspan="2" class="middle center">
										<button type="submit" class="positive big">	
											<img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" class="middle"/>&nbsp;						
											<g:message code="auth.login.label"/> &nbsp;
										</button>												
									</td>
								</tr>
								<tr class="">
									<td colspan="2">
										
									</td>	
								</tr>
							</tbody>
							<tfoot>
								<tr class="" style="background-color: #eee;">
									<td valign="top" class="name" colspan="2">
										<div style="text-align: left">				
											<warehouse:message code="auth.newuser.text"/> &nbsp; <g:link class="list" controller="auth" action="signup"><warehouse:message code="auth.signup.label" default="Signup"/></g:link>
										</div>
									</td>
								</tr>
							
							</tfoot>
						</table>
					</fieldset>
				</div>
			</div>
		</g:form>
		

	</div>
</body>
</html>
