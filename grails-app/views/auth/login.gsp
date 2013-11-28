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

                                    <td>

                                    </td>
                                </tr>
								<tr>
									<td class="center middle ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
										<g:textField class="text" id="username" name="username" value="${userInstance?.username?:params.username}" style="font-size:2em" />
									</td>
								</tr>
								<tr>
									<td class="center middle ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
										<g:passwordField class="text" id="password" name="password" value="${userInstance?.password?:params.password}" style="font-size:2em" />
									</td>
								</tr>

                                <%--
                                <tr>
                                    <td class="left middle ${hasErrors(bean: userInstance, field: 'warehouse', 'errors')}">
                                        <g:select name="location.id" value="${userInstance?.warehouse}" from="${session.loginLocations}"
                                            noSelection="['null':'']" class="chzn-select-deselect"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="left middle ${hasErrors(bean: userInstance, field: 'rememberLastLocation', 'errors')}">
                                        <g:checkBox name="rememberLastLocation" value="${userInstance?.rememberLastLocation}"/>
                                        <label for="rememberLastLocation">${warehouse.message(code:'user.rememberLastLocation.label', default: 'Remember my location and log me in automatically.')}</label>
                                    </td>
                                </tr>
                                --%>
								<tr>
									<td class="middle center">
										<button type="submit" class="button icon lock big" id="loginButton">
											<g:message code="auth.login.label"/>
										</button> 
										<%-- 
										&nbsp;|&nbsp;											
										<g:link class="list" controller="auth" action="forgotPassword"><warehouse:message code="auth.forgotPassword.label" default="Forgot your password?"/></g:link>										
										--%>
															
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
