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
					
						<div style="padding: 15px;">
							    
							<div>
								<label for="username">Username or email:</label>
							</div>
							<div>
								<input type="text" class="large" name="username" id="username" size="43" value="${userInstance?.username}">
								<br/><span class="fade">e.g. <b>manager</b></span>															
							</div> 
							<div>
								<label for="password">Password:</label>
							</div>
							<div>
								<input type="password" class="large" name="password" id="password" size="43" value="${userInstance?.password}">
								<br/><span class="fade">e.g. <b>password</b></span>															
							</div>	
							<div style="text-align: right;">
								<span class="buttons" >
									<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt=""/> Login</button>					   
								</span>
							</div>  					
						</div>
					</fieldset> 
				</div>
			
			<%-- 
			    <div class="buttons">
					<span class="button">
						<g:submitButton name="login" class="save" value="${message(code: 'default.button.login.label', default: 'Login')}" />
					</span>	
			    </div>
		    --%>	
			<br/>
		</g:form>
		

	</div>
</body>
</html>
