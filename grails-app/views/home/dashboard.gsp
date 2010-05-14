<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle">Dashboard</content>		
		<content tag="pageTitle">Dashboard</content>
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global"/></content>
		<content tag="localLinks"><g:render template="local"/></content>		
    </head>
    <body>        
		<div class="body">
	    	<div>	    	
	    		<g:if test="${!session.user}">
					<g:form controller="user" action="doLogin" method="post">		  
					    <div class="dialog">
							<g:if test="${flash.message}">
							    <div class="message">${flash.message}</div>
							</g:if>
							<table class="userForm">
							    <tr class='prop'>
								<td valign='top' style='text-align:left;' width='20%'>
								    <label for='email'>Username:</label>
								</td>
								<td valign='top' style='text-align:left;' width='80%'>
								    <input id="username" type='text' name='username' value='${user?.username}' />
								</td>
							    </tr>
							    <tr class='prop'>
								<td valign='top' style='text-align:left;' width='20%'>
								    <label for='password'>Password:</label>
								</td>
								<td valign='top' style='text-align:left;' width='80%'>
								    <input id="password" type='password' name='password' value='${user?.password}' />
								</td>
							    </tr>
							    <tr class='prop'>
								<td valign='top' style='text-align:left;' width='20%'>
								    <label for='warehouse'>Select your warehouse:</label>
								</td>
								<td valign='top' style='text-align:left;' width='80%'>
								    <g:select name="warehouse.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value=""/>
								</td>
							    </tr>
							</table>
					    </div>
					    <div class="buttons">
							<span class="button">
							    <g:submitButton name="login" class="save" value="${message(code: 'default.button.login.label', default: 'Login')}" />
							</span>
					    </div>
					</g:form>
				</g:if>
				<g:else>
					You are already logged in as ${session.user.username}.  Please select an option from the 
					menu on the left.
				</g:else>
	    	</div>
		</div>
    </body>
</html>