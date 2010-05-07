<html>
    <head>

	<!-- Include default page title -->
        <title><g:layoutTitle default="Your Warehouse App" /></title>

	<!-- Include Blueprint CSS -->
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'screen.css')}" type="text/css" media="screen, projection">
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'print.css')}" type="text/css" media="print">
	<!--[if IE]><link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'ie.css')}" type="text/css" media="screen, projection"><![endif]-->

	<!-- Include Favicon -->
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

	<!-- Include Main CSS -->
	<%--<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />--%>
        <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
	<%--<bp:blueprintCss/>--%><!-- TODO Would like to use the bp:blueprintCss -->

	<!-- Grails Layout : write head element for page-->
        <g:layoutHead />

	<!-- Include javascript files -->
        <g:javascript library="application" />

	<g:javascript library="jquery"/>

	<!--
	<g:javascript library="yui" />
	<yui:javascript dir="calendar" file="calendar-min.js" />
	-->

	<!-- Include Yahoo UI resources -->
	<!--
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.1/build/fonts/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.1/build/calendar/assets/skins/sam/calendar.css" />
	<script type="text/javascript" src="http://yui.yahooapis.com/2.8.1/build/yahoo-dom-event/yahoo-dom-event.js"></script>

	<script type="text/javascript" src="http://yui.yahooapis.com/2.8.1/build/calendar/calendar-min.js"></script>
	<script type="text/javascript" src="http://yui.yahooapis.com/2.8.1/build/history/history-min.js"></script>
	-->
	<!--
	<link rel="stylesheet" type="text/css" href="http://localhost:8080/warehouse/js/yui/2.7.0/reset-fonts-grids/reset-fonts-grids.css"/>
	<link rel="stylesheet" type="text/css" href="http://localhost:8080/warehouse/js/yui/2.7.0/assets/skins/sam/skin.css"/>
	-->
	<!--
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/utilities/utilities.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/calendar/calendar-min.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/datasource/datasource-beta-min.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/datatable/datatable-beta-min.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/container/container_core-min.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/menu/menu-min.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/calendar/calendar-min.js"></script>
	<script type="text/javascript" src="http://localhost:8080/warehouse/js/yui/2.7.0/history/history-min.js"></script>
	-->


    </head>
    <body>

	  <!-- Main Content Container -->
	  <div class="container">
	      <!-- Header : includes logo, global navigation -->
	      <div id="spinner" class="spinner" style="display:none;">
		<img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	      </div>
	      <div class="logo">
		<img src="${createLinkTo(dir:'images',file:'logo.png')}"  width="200" height="61.5" alt="Your Warehouse. You're Welcome." />
	      </div>
	      <!-- Include pageTitle property -->
	      <g:if test="${pageProperty(name:'page.pageTitle')}">
		  <div id="pageTitle">
		      <br/>
		      <h2><g:pageProperty name="page.pageTitle" /></h2>
		      <hr/>
		      <br/>
		  </div>
	      </g:if>
	      <!-- /Include pageTitle property -->


	    <!-- Include Body -->
  	    <g:layoutBody />
	    <!-- /Include Body -->

	    
	  </div>

	<!-- Include footer -->
	<div id="footer">
	    &copy; 2010 <a href="http://www.pih.org">PIH&trade;</a> Warehouse &nbsp;&nbsp; | &nbsp;&nbsp;Version: <g:meta name="app.version"/>
	</div>







<%--
        <div id="header">
	  <div id="banner">
	    <div id="grailsLogo" class="logo">
	      <a class="home" href="${createLink(uri: '/')}">		
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

	
--%>

    </body>
</html>