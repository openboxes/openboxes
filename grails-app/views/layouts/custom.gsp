<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<!-- Include default page title -->
	<title><g:layoutTitle default="Your Warehouse App" /></title>
	<%--<link rel="stylesheet" href="http://yui.yahooapis.com/2.7.0/build/reset-fonts-grids/reset-fonts-grids.css" type="text/css"> --%>
	<link rel="stylesheet" href="${createLinkTo(dir:'js/yui/2.7.0/reset-fonts-grids',file:'reset-fonts-grids.css')}" type="text/css">

	<!-- Include Blueprint CSS -->
	<%--<bp:blueprintCss/>--%><!-- TODO Would like to use the bp:blueprintCss -->
	<!-- 	
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'screen.css')}" type="text/css" media="screen, projection">
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'print.css')}" type="text/css" media="print">
	 -->
	<!--[if IE]><link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'ie.css')}" type="text/css" media="screen, projection"><![endif]-->
	
	<!-- Include Favicon -->
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	
	<!-- Include Main CSS -->
	<!-- TODO Apparently there's a slight distinction between these two ... need to figure out what that distinction is -->
	<%--<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />--%>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="screen" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="screen" />
	
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
								<li>Welcome, ${session.user.username}!</li>  | 
								<li><g:link class="list" controller="user" action="preferences"><g:message code="default.preferences.label"  default="Preferences"/></g:link></li> | 
								<li><g:link class="list" controller="auth" action="logout"><g:message code="default.logout.label"  default="Logout"/></g:link></li>
						    </g:if>
						    <g:else test="${!session.user}">
								<li><g:link class="list" controller="auth" action="login"><g:message code="default.login.label" default="Login"/></g:link></li> |
								<li><g:link class="list" controller="user" action="register"><g:message code="default.register.label" default="Register"/></g:link></li> |
								<li><g:link class="list" controller="user" action="help"><g:message code="default.help.label" default="Get Help"/></g:link></li>
						    </g:else>
						</ul>
				    </div>
				</div>
		    </div>
		    <div class="yui-b">
				<!-- Global Navigation menu -->
				<div class="nav">
				    				    
					<g:render template="../common/breadcrumb" />

				    <g:if test="${session.user}">
					    <span class="menuButton"><a class="shipment" href="${createLink(uri: '/shipment/index')}">Shipments</a></span>
					    <span class="menuButton"><a class="inventory" href="${createLink(uri: '/warehouse/showInventory/' + session.warehouse.id)}">Inventory</a></span>
					    <span class="menuButton"><a class="settings" href="${createLink(uri: '/admin/index')}">Settings</a></span>
				    	<g:pageProperty name="page.globalLinks" /><!-- Populated using the 'globalLinks' property defined in the GSP file -->
				    </g:if>
				</div>
		    </div>
		</div>
    </div>
    <div id="doc3" class="yui-t3">
	    <div class="breadcrumb">
			
		</div>
		<!-- Body includes the divs for the main body content and left navigation menu -->
		<div id="bd" role="main">
	    	<!-- Main Content Block -->
	      	<div id="yui-main">
		    	<div id="mainBlock" class="yui-b">
					<!-- Populated using the 'pageTitle' property defined in the GSP file -->
					<%--
					<g:if test="${pageProperty(name:'page.pageTitle')}">
					    <div id="pageTitle">
							<!-- Include page title (use content tag in child GSP) -->
							<h1><g:pageProperty name="page.pageTitle" /></h1>
							<hr/>
							<br/>
					    </div>
					</g:if>
					--%>
					
					<g:layoutBody />
				</div>
	      	</div>
	      	<!-- Left Content Block -->
	      	<div id="leftBlock" role="navigation" class="yui-b">		  		
	      		
		  		<!-- Navigation Menu -->
		  		<div id="navMenu" class="homePagePanel">
		      		<div class="panelTop"><!-- used to dislay the bottom border of the navigation menu --></div>
					<div class="panelBody">							
						<h1><g:pageProperty name="page.menuTitle" /> Menu</h1>
						<ul>
							<!--  Only display the local navigation menu if the user is logged in -->
      						<g:if test="${session.user}">
								<g:pageProperty name="page.localLinks" />
							</g:if>
							<g:else>
								<li><span class="menuButton"><g:link class="create" controller="auth" action="login">Login</g:link></span></li>
							</g:else>
						</ul>							
						<br/><!-- this is added in order to get rid of whitespace in menu -->
					</div>
					<div class="panelBtm"><!-- used to dislay the bottom border of the navigation menu --></div>
				</div>
				
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
