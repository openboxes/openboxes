<html>
    <head>
        <title><g:layoutTitle default="Grails" /></title>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />
    </head>
    <body>
        <div id="header">
	  <div id="banner">
	    <div id="grailsLogo" class="logo">
	      <a class="home" href="${createLink(uri: '/')}">
		<span id="grailsAppName">wareh<img src="${resource(dir:'images',file:'warehouse.png')}" height="32" width="32" alt="Warehouse" border="0" style="vertical-align: middle;" />use</span></a>
	    </div>
	  </div>
	  <div id="globalNav">
	    <div id="spinner" class="spinner" style="display:none;">
	      <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	    </div>
	    <ul>


	      <g:if test="${session.user}">
		<li>Hello ${session.user.username}!</li>
		<li><g:link class="list" controller="user" action="profile"><g:message code="default.profile.label"  default="My Profile"/></g:link></li>
		<li><g:link class="list" controller="user" action="logout"><g:message code="default.logout.label"  default="Logout"/></g:link></li>
	      </g:if>
	      <g:elseif test="${!session.user}">
		<li><g:link class="list" controller="user" action="login"><g:message code="default.login.label" default="Login"/></g:link></li>
	      </g:elseif>


	    </ul>
	  </div>
	</div>
        <div id="content">
	  <g:layoutBody />
	</div>
	<div id="footer">
	  &copy; 2010 <a href="http://www.pih.org">PIH&trade;</a> Warehouse &nbsp;&nbsp; | &nbsp;&nbsp;
	  Version: 0.0.1
	</div>


    </body>
</html>