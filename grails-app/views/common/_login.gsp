
<%@ page import="org.pih.warehouse.Warehouse" %>
<g:form controller="auth" action="doLogin" method="post">		  
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
				    <label for='warehouse.id'>Select your warehouse:</label>
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