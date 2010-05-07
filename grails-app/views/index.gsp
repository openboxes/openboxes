<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
	<!--
	    Specify content to overload like global navigation links,
	    page titles, etc.
	-->
	<content tag="globalLinks"></content>
	<content tag="pageTitle">Welcome to the <strong>PIH&copy;</strong> Warehouse</content>
    </head>
    <body>        
	<div class="body">
	    <div>
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
	    </div>
	</div>
    </body>
</html>