<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<!-- Include default page title -->
	<title><g:layoutTitle default="Your Warehouse App" /></title>
	<%--<link rel="stylesheet" href="http://yui.yahooapis.com/2.7.0/build/reset-fonts-grids/reset-fonts-grids.css" type="text/css"> --%>
	<link rel="stylesheet" href="${createLinkTo(dir:'js/yui/2.7.0/reset-fonts-grids',file:'reset-fonts-grids.css')}" type="text/css">
	
	<!-- Include Favicon -->
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	
	<!-- Include Main CSS -->
	<!-- TODO Apparently there's a slight distinction between these two ... need to figure out what that distinction is -->
	<%--<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />--%>
	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'form.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footer.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'custom.css')}" type="text/css" media="screen, projection" />
	
	<%--
	<!-- Include Blueprint CSS --> 
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'reset.css')}" type="text/css" media="screen, projection">
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint',file:'typography.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint',file:'grid.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint',file:'forms.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'print.css')}" type="text/css" media="print">
	<!--[if IE]><link rel="stylesheet" href="${createLinkTo(dir:'css/blueprint', file:'ie.css')}" type="text/css" media="screen, projection"><![endif]-->
	<!-- TODO Would like to use the bp:blueprintCss <bp:blueprintCss/> -->
	--%>

	
	<!-- Grails Layout : write head element for page-->
	<g:layoutHead />
	
	<!-- Include javascript files -->
	<g:javascript library="application"/>
	<g:javascript library="jquery"/>
	<script type="text/javascript">
		$.noConflict();
		// Code that uses other library's $ can follow here.
	</script>

	<!-- Manually include jquery-ui resources -->
	<link href="${createLinkTo(dir:'js/jquery.ui/css/cupertino', file:'jquery-ui-1.8.2.custom.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery-ui-1.8.2.custom.min.js')}" type="text/javascript" ></script>

	
	<!-- Dynamically include jquery-ui resources :  NOT WORKING CORRECTLY -->
	<!-- <jqui:resources components="dialog, datepicker"/> -->	
	<!-- <jqui:resources components="datepicker" mode="normal" theme="cupertino" /> -->

	<!-- Dynamically include Grails UI components -->
	<gui:resources components="richEditor, dialog, tabView, autoComplete"/>

	<!-- Custom styles to be applied to all pages -->
	<style type="text/css" media="screen"></style>
</head>
<body class="yui-skin-sam">
    <div id="doc3" class="yui-t7">
		<!-- Spinner gets displayed when AJAX is invoked -->
		<div id="spinner" class="spinner" style="display:none;">
		    <img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
		</div>
		<!-- 
			Header "hd" includes includes logo, global navigation 
		------------------------------------------------------------------->
		<div id="hd" role="banner">
		    
		    <!-- Block which includes the logo and login banner -->
		    <div class="yui-b">
				<div class="yui-gf">
				    <div id="bannerLeft" class="yui-u first" >
						<div class="logo">
						    <a class="home" href="${createLink(uri: '/home/index')}" style="text-decoration: none">						    	
					    		<img src="${createLinkTo(dir:'images',file:'openboxes_logo3.png')}" alt="Your Boxes. You're Welcome." 
					    			style="vertical-align: absmiddle"/>
						    </a>
						</div>
				    </div>
				    <div id="bannerRight" class="yui-u" >
				    	<div id="loggedIn">
							<ul>
							    <g:if test="${session.user}">
									<li>Logged in as <b>${session.user.username}</b>
										(<g:link class="list" controller="auth" action="logout"><g:message code="default.notuser.label"  default="not you?"/></g:link>)
									</li>
									<!-- 
									| <li><g:link class="list" controller="user" action="preferences"><g:message code="default.preferences.label"  default="Preferences"/></g:link></li>
									 -->
									| <li><g:link class="list" controller="auth" action="logout"><g:message code="default.logout.label"  default="Logout"/></g:link></li>
									| <li><input type="text" value="search" name="q" style="color: #aaa; font-weight: bold;" disabled=disabled /></li>
									
							    </g:if>
							    <g:else test="${!session.user}">
									<li>Not logged in</li>  | <li><g:link class="list" controller="auth" action="login"><g:message code="default.login.label" default="Login"/></g:link></li>
									<!-- 
									 | <li><g:link class="list" controller="user" action="register"><g:message code="default.register.label" default="Register"/></g:link></li>
									 | <li><g:link class="list" controller="user" action="help"><g:message code="default.help.label" default="Help"/></g:link></li>
									 -->
									 
							    </g:else>
							</ul>
						</div>
						<div>
						</div>
				    </div>
				</div>
		    </div>
		    
		    <!-- Block which includes global navigation and breadcrumb -->
		    <div class="yui-b">
				<!-- Global Navigation menu -->
				<div class="nav">
					<%-- <g:render template="../common/breadcrumb" />--%>
					<g:breadcrumb />
					
					
				    <g:if test="${session.user}">				    
						<%-- 
				    	<g:render template="../common/global"/>
				    	<g:pageProperty name="page.globalLinks" /><!-- Populated using the 'globalLinks' property defined in the GSP file -->
					    --%>				    
						<!-- TODO Implemented hack to move the settings menu over to the right -->
						<span class="menuButton" style="position:absolute; right: 15px;"><a class="settings" href="${createLink(uri: '/admin/index')}">Settings</a></span>
				    </g:if>
				</div>
		    </div>
		    
		</div>
    </div>
    
    <div id="doc3" class="yui-t3">
    
	    
		<br/>
		<!-- 
			Body includes the divs for the main body content and left navigation menu 
		----------------------------------------------------------------------------------->
		<!-- YUI "body" block that includes the main content for the page -->
		<div id="bd" role="main">

	    	<!-- YUI main Block including page title and content -->
	      	<div id="yui-main">
		    	<div id="content" class="yui-b">
					<!-- Populated using the 'pageTitle' property defined in the GSP file -->
					<g:if test="${pageProperty(name:'page.pageTitle')}">
					    <div id="pageTitle">
							<h1><g:pageProperty name="page.pageTitle" /></h1>
							<hr/>
					    </div>
					</g:if>
					<g:layoutBody />
				</div>
	      	</div>
	      		      	
	      	<!-- YUI nav block that includes the local navigation menu -->
	      	
	      	<div id="menu" role="navigation" class="yui-b">
		  		<g:if test="${session.user}">
			  		<!-- Navigation Menu -->
			  		<div id="navMenu" class="homePagePanel">
			      		<div class="panelTop"><!-- used to dislay the bottom border of the navigation menu --></div>
						<div class="panelBody">							
							<h1><g:pageProperty name="page.menuTitle" /> Menu</h1>
							<ul>
								<li><span class="menuButton"><a class="dashboard" href="${createLink(uri: '/home/index')}">Dashboard</a></span></li>
								<g:pageProperty name="page.localLinks" />
							</ul>							
							<br/><!-- this is added in order to get rid of whitespace in menu -->
						</div>
						<div class="panelBtm"><!-- used to dislay the bottom border of the navigation menu --></div>
					</div>
				</g:if>
			</div>
		</div>
		
		<!-- YUI "footer" block that includes footer information -->
		<div id="ft" role="contentinfo">
			<div id="footer">
				&copy; 2010 <a href="http://www.pih.org">PIH</a>&trade; Warehouse &nbsp;&nbsp; | &nbsp;&nbsp;
				Application Version: <g:meta name="app.version"/>&nbsp;&nbsp; | &nbsp;&nbsp;
				Grails Version: <g:meta name="app.grails.version"></g:meta>
			</div>
		</div>
	</div>
</body>
</html>
