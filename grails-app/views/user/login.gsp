<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>User Login</title>
  </head>
  <body>

    <div class="nav">
	<span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
    </div>


    <div class="body">
      <h1><g:message code="default.welcome.label" default="Welcome to the PIH warehouse app" /></h1><br/>
      <div width="50%">
	<g:form action="doLogin" method="post">
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
		  <label for='warehouse'>Select local warehouse:</label>
		</td>
		<td valign='top' style='text-align:left;' width='80%'>
		  <g:select name="warehouse.id" from="${org.pih.warehouse.Warehouse.list()}" optionKey="id" value=""/>
		</td>
	      </tr>




	    </table>
	  </div>
	  <div class="buttons">
	    <span class="formButton">
	      <input type="submit" value="Login"></input>
	    </span>
	  </div>
	</g:form>
      </div>
    </div>
  </body>
</html>
