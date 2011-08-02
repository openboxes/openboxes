<html>
    <head>
        <title><g:layoutTitle default="Grails" /></title>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />
	<style type="text/css" media="screen">

		      #nav {
		      margin-top:20px;
		      margin-left:30px;
		      width:228px;
		      float:left;

	      }
	      .homePagePanel * {
		      margin:0px;
	      }
	      .homePagePanel .panelBody ul {
		      list-style-type:none;
		      margin-bottom:10px;
	      }
	      .homePagePanel .panelBody h1 {
		      text-transform:uppercase;
		      font-size:1.1em;
		      margin-bottom:10px;
	      }
	      .homePagePanel .panelBody {
		  background: url(images/leftnav_midstretch.png) repeat-y top;
		      margin:0px;
		      padding:15px;
	      }
	      .homePagePanel .panelBtm {
		  background: url(images/leftnav_btm.png) no-repeat top;
		      height:20px;
		      margin:0px;
	      }

	      .homePagePanel .panelTop {
		  background: url(images/leftnav_top.png) no-repeat top;
		      height:11px;
		      margin:0px;
	      }
	      h2 {
		      margin-top:15px;
		      margin-bottom:15px;
		      font-size:1.2em;
	      }
	      #pageBody {
		      margin-left:280px;
		      margin-right:20px;
	      }
	</style>
    </head>
    <body>
        <div id="header">
	  <div id="banner">
	    <div id="grailsLogo" class="logo">
	      <a class="home" href="${createLink(uri: '/')}">
		<%--<span id="grailsAppName">wareh<img src="${resource(dir:'images',file:'warehouse.png')}" height="32" width="32" alt="Warehouse" border="0" style="vertical-align: middle;" />use</span>--%>
		<img src="${resource(dir:'images',file:'logo.png')}" width="200" height="61.5" alt="Warehouse - You're Welcome" />
	      </a>
	    </div>
	  </div>
	  <div id="globalNav">
	    <div id="spinner" class="spinner" style="display:none;">
	      <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	    </div>
	    <ul>
	      <g:if test="${session.user}">
		<li>Hello ${session.user.username}!</li>
		<li><g:link class="list" controller="user" action="profile"><warehouse:message code="default.profile.label"  default="My Profile"/></g:link></li>
		<li><g:link class="list" controller="user" action="logout"><warehouse:message code="default.logout.label"  default="Logout"/></g:link></li>
	      </g:if>
	      <g:elseif test="${!session.user}">
		<li><g:link class="list" controller="user" action="login"><warehouse:message code="default.login.label" default="Login"/></g:link></li>
	      </g:elseif>
	    </ul>
	  </div>
	</div>
        <div id="content">
	  <g:layoutBody />
	</div>

      <div id="nav">
	menu
      </div>

      <div id="footer">
	  &copy; 2010 <a href="http://www.pih.org">PIH&trade;</a> Warehouse &nbsp;&nbsp; | &nbsp;&nbsp;
	  Version: 0.0.1
	</div>


    </body>
</html>