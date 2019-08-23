<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<title><warehouse:message code="auth.title"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<script src="${createLinkTo(dir:'js/', file:'detect_timezone.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript" ></script>
	<style>
		input, button { width: 100%; font-size: 2em; text-align: center; padding: 10px; }
	</style>
</head>
<body>
	<div class="body">
		<g:form controller="auth" action="handleLogin" method="post">	
		
			<g:hiddenField name="targetUri" value="${params?.targetUri}" />
			<g:hiddenField id="browserTimezone" name="browserTimezone" />
			  
		    <div id="loginContainer" class="dialog">
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
                        <h2>
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'lock.png')}" class="middle"/>
                            <warehouse:message code="default.login.label" default="Login"/>
                        </h2>

						<table>
							<tbody>
								<tr>
									<td class="left middle ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
										<g:textField class="text" id="username" name="username" value="${userInstance?.username?:params.username}" />
									</td>
								</tr>
								<tr>
									<td class="left middle ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
										<g:passwordField class="text" id="password" name="password" value="${userInstance?.password?:params.password}" />
									</td>
								</tr>
								<tr>
									<td class="middle center">
										<button type="submit" class="button big" id="loginButton">
											<g:message code="auth.login.label"/>
										</button> 															
									</td>
								</tr>

                                <tr class="">
                                    <td class="middle left" colspan="2">
                                        <warehouse:message code="auth.newuser.text"/>
                                        <g:link class="list" controller="auth" action="signup"><warehouse:message code="auth.signup.label" default="Signup"/></g:link>
                                    </td>
                                </tr>

                            </tbody>
						</table>
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
