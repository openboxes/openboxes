<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="auth.title"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<script src="${createLinkTo(dir:'js/', file:'detect_timezone.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript" ></script>
</head>
<body>
	<style>
		#hd { display: none; }  
		#loginBox { height: 215px; }
	</style>
	


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
					
								
						<div id="loginBox" class="box">
		    				
							<table>
								<tbody>
									<tr>
										<td class="center middle">
										
											<div id="logo">
										    	<img src="${createLinkTo(dir:'images/icons/',file:'logo24.png')}" title="${warehouse.message(code:'default.tagline.label') }" class="middle"/>&nbsp;
												<span class="middle title" style="font-size: 2em; color: #666; vertical-align: middle;"><warehouse:message code="default.openboxes.label"/></span>
											</div>	
																
										</td>
									</tr>
									<tr>
										<%-- 
										<td class="right middle">
											<label for="email" class="loginField"><warehouse:message code="user.username.label" default="Username" /></label>
										</td>
										--%>
										<td class="center middle ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
											<g:textField class="text" id="username" name="username" value="${userInstance?.username}" size="35" />
										</td>
									</tr>
									<tr>
										<%--
										<td class="right middle">
											<label for="password" class="loginField"><warehouse:message code="user.password.label" default="Password" /></label>
										</td>
										 --%>
										<td class="center middle ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
											<g:passwordField class="text" id="password" name="password" value="${userInstance?.password}" size="35" />
										</td>
									</tr>
									<tr>
										<td colspan="2" class="middle center">
											<button type="submit" class="positive big" id="loginButton">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" class="middle"/>&nbsp;						
												<g:message code="auth.login.label"/> &nbsp;
											</button>												
										</td>
									</tr>
									
								</tbody>
							</table>
						</div>
						<div style="text-align: right">				
							<warehouse:message code="auth.newuser.text"/> &nbsp;<g:link class="list" controller="auth" action="signup"><warehouse:message code="auth.signup.label" default="Signup"/></g:link>
						</div>
						
				</div>
			</div>
		</g:form>
	</div>
	
	<script type="text/javascript"> 	
		$(document).ready(function() {

			var timezone = jzTimezoneDetector.determine_timezone().timezone; // Now you have an instance of the TimeZone object.
			$("#browserTimezone").val(timezone.olson_tz); // Set the user timezone offset as a hidden input
            $("#username").watermark("${warehouse.message(code:'login.username.label')}");
		    $("#password").watermark("${warehouse.message(code:'login.password.label')}");
			$("#username").focus();

            openboxes.expireFromLocal();
		});
	</script>	
</body>
</html>
