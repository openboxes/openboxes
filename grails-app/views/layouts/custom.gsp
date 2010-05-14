<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
  <!-- Include default page title -->
  <title><g:layoutTitle default="Your Warehouse App" /></title>
  <link rel="stylesheet" href="http://yui.yahooapis.com/2.7.0/build/reset-fonts-grids/reset-fonts-grids.css" type="text/css">

  <!-- Include Blueprint CSS -->
  <%--<bp:blueprintCss/>--%><!-- TODO Would like to use the bp:blueprintCss -->
  <link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'screen.css')}" type="text/css" media="screen, projection">
  <link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'print.css')}" type="text/css" media="print">
  <!--[if IE]><link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'ie.css')}" type="text/css" media="screen, projection"><![endif]-->

  <!-- Include Favicon -->
  <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

  <!-- Include Main CSS -->
  <!-- TODO Apparently there's a slight distinction between these two ... need to figure out what that distinction is -->
  <%--<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />--%>
  <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />

  <!-- Grails Layout : write head element for page-->
  <g:layoutHead />

  <!-- Include javascript files -->
  <g:javascript library="application" />
  <g:javascript library="prototype"/>
  
  <!-- Include navigation resources -->
  <%--<nav:resources/>--%>

  <style type="text/css" media="screen">
	/* used to remove the 10px margin that yui adds by default */
	#doc3 {margin:auto;}

	/* used to give the main content some protection against hitting the right edge of the browser */
	#main { margin-right: 30px; }

	/* Need these styles to display the leftnav menu */
	/* TODO Need to look into other sites like the WordPress admin site */
	#nav {
	    margin-top:20px;
	    margin-left:0px;
	    width:228px;
	    float:left;
	    border: 0px solid black;

	}
	.homePagePanel * {
	    margin: 0;
	}
	.homePagePanel .panelBody ul {
	    list-style-type:none;
	    margin-bottom: 10px;
	    margin-left: 0px;
	    border: 0px solid black;
	}
	.homePagePanel .panelBody h1 {
	    text-transform:uppercase;
	    font-size:1.1em;
	    margin-bottom: 10px;
	}
	.homePagePanel .panelBody ul li { 
		margin-top: 10px;
		margin-left: 0px;
		border: 0px solid black;
	
	}	
	.homePagePanel .panelBody {
	    background: url(/warehouse/images/leftnav_midstretch.png) repeat-y top;
	    margin:0px;
	    padding:0px;
	    padding-left: 25px; /* 35px needed to add to align menu options with background image */
	}
	.homePagePanel .panelBtm {
	    background: url(/warehouse/images/leftnav_btm.png) no-repeat top;
	    height:20px;
	    margin:0px;
	}
	.homePagePanel .panelTop {
	    background: url(/warehouse/images/leftnav_top.png) no-repeat top;
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
<body class="yui-skin-sam">
    <div id="doc3" class="yui-t7">
		<!-- Spinner gets displayed when AJAX is invoked -->
		<div id="spinner" class="spinner" style="display:none;">
		    <img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
		</div>
		<!-- Header includes includes logo, global navigation -->
		<div id="hd" role="banner">
		    <!-- Banner -->
		    <div class="yui-b">
				<div class="yui-gf">
				    <div id="logo-info" class="yui-u first">
					<div class="logo">
					    <a class="home" href="${createLink(uri: '/home/index')}">
					    	<img src="${createLinkTo(dir:'images',file:'logo.png')}"  width="200" height="61.5" alt="Your Warehouse. You're Welcome." />
					    </a>
					</div>
				    </div>
				    <div id="login-info" class="yui-u">
						<ul>
						    <g:if test="${session.user}">
								<li>Welcome, ${session.user.username}!</li>
								<li><g:link class="list" controller="user" action="preferences"><g:message code="default.preferences.label"  default="Preferences"/></g:link></li> | 
								<li><g:link class="list" controller="user" action="logout"><g:message code="default.logout.label"  default="Logout"/></g:link></li>
						    </g:if>
						    <g:elseif test="${!session.user}">
								<li><g:link class="list" controller="user" action="login"><g:message code="default.login.label" default="Login"/></g:link></li> |
								<li><g:link class="list" controller="user" action="register"><g:message code="default.register.label" default="Register"/></g:link></li> |
								<li><g:link class="list" controller="user" action="help"><g:message code="default.help.label" default="Get Help"/></g:link></li>
						    </g:elseif>
						</ul>
				    </div>
				</div>
		    </div>
		    <div class="yui-b">
				<!-- Global Navigation menu -->
				<div class="nav">
				    <span class="menuButton"><a class="home" href="${createLink(uri: '/home/index')}">Home</a></span>				    
				    <g:if test="${session.user}">
					    <span class="menuButton"><a class="shipment" href="${createLink(uri: '/shipment/index')}">Shipments</a></span>
					    <span class="menuButton"><a class="warehouse" href="${createLink(uri: '/warehouse/index')}">Warehouses</a></span>
					    <span class="menuButton"><a class="inventory" href="${createLink(uri: '/warehouse/showInventory/1')}">Inventory</a></span>
					    <span class="menuButton"><a class="settings" href="${createLink(uri: '/admin/index')}">Settings</a></span>
				    	<g:pageProperty name="page.globalLinks" /><!-- Populated using the 'globalLinks' property defined in the GSP file -->
				    </g:if>
				</div>
		    </div>
		</div>
    </div>
    <div id="doc3" class="yui-t3">
	    <div class="breadcrumb">
			<g:render template="breadcrumb" model=""/>			
		</div>
		<!-- Body includes the divs for the main body content and left navigation menu -->
		<div id="bd" role="main">
	    	<!-- Main Content Block -->
	      	<div id="yui-main">
		    	<div id="mainBlock" class="yui-b">
					<!-- Populated using the 'pageTitle' property defined in the GSP file -->
					<g:if test="${pageProperty(name:'page.pageTitle')}">
					    <div id="pageTitle">
							<!-- Include page title (use content tag in child GSP) -->
							<h1><g:pageProperty name="page.pageTitle" /></h1>
							<hr/>
							<br/>
					    </div>
					</g:if>
					<%--<nav:render group="tabs"/>--%>
					<!-- Include body defined in the GSP file -->
					<g:layoutBody />
				</div>
	      	</div>
	      	<!-- Left Content Block -->
	      	<div id="leftBlock" role="navigation" class="yui-b">		  		
	      		<!--  Only display the local navigation menu if the user is logged in -->
	      		<g:if test="${session.user}">
			  		<!-- Navigation Menu -->
			  		<div id="navMenu" class="homePagePanel">
			      		<div class="panelTop"><!-- used to dislay the bottom border of the navigation menu --></div>
						<div class="panelBody">							
							<h1><g:pageProperty name="page.menuTitle" /> Menu</h1>
							<ul>
								<!--  Hide the default menu items if the content page is override'ing the localLinks 
								<g:if test="${pageProperty(name:'page.localLinksMode') as String != 'override'}">							
									<li><g:link class="list" controller="warehouse" action="list">Show Warehouses</g:link></li>
									<li><g:link class="list" controller="product" action="list">Show Products</g:link></li>
									<li><g:link class="list" controller="user" action="list">Show Users</g:link></li>
								</g:if>	
								-->
								<g:pageProperty name="page.localLinks" />
							</ul>							
							<br/><!-- this is added in order to get rid of whitespace in menu -->
						</div>
						<div class="panelBtm"><!-- used to dislay the bottom border of the navigation menu --></div>
					</div>
				</g:if>
			</div>
		</div>
		<!-- Footer includes footer information -->
		<div id="ft" role="contentinfo">
			<div id="footer">
				&copy; 2010 <a href="http://www.pih.org">PIH&trade;</a> Warehouse &nbsp;&nbsp; | &nbsp;&nbsp;
				Application Version: <g:meta name="app.version"/>&nbsp;&nbsp; | &nbsp;&nbsp;
				Grails Version: <g:meta name="app.grails.version"></g:meta>
			</div>
		</div>
	</div>
</body>
</html>
