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
	
	
	<!-- Include javascript files -->
	<g:javascript library="application"/>

	<g:javascript library="jquery" plugin="jquery" />
	<%--<jqui:resources theme="smoothness" /> --%> 
	<link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery-ui-1.8.7.js')}" type="text/javascript" ></script>
	
	<!-- Include other plugins -->
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery.ui.autocomplete.selectFirst.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.cookies.2.2.0.min.js')}" type="text/javascript" ></script>

	<%--
	<!-- Not using yet -->
	<link href="${createLinkTo(dir:'js/jquery.jqGrid/css', file:'ui.jqgrid.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.jqGrid/js', file:'jquery.jqGrid.min.js')}" type="text/javascript" ></script>
	 --%>
	<%--
	<!-- Broken --> 
    <script type="text/javascript" src="${createLinkTo(dir:'js/jquery/', file:'fg.menu.js')}"></script>
    <link type="text/css" href="${createLinkTo(dir:'js/jquery/', file:'fg.menu.css')}" media="screen" rel="stylesheet" />	
	--%>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'custom.css')}" type="text/css" media="screen, projection" />
	
	<!-- Custom styles to be applied to all pages -->
	<style type="text/css" media="screen"></style>
	
	<!-- Grails Layout : write head element for page-->
	<g:layoutHead />
	
	<%--  
	<script>
		$(document).ready(function() {
	  		$('table.dataTable tr').hover(function() {
				//$(this).css('background-color', '#FFFF99');
				$(this).contents('td').css({'border': '1px solid red', 'border-left': 'none', 'border-right': 'none'});
				$(this).contents('td:first').css('border-left', '1px solid red');
				$(this).contents('td:last').css('border-right', '1px solid red');
			},
			function() {
				//$(this).css('background-color', '#FFFFFF');
				$(this).contents('td').css('border', 'none');
			});
		});
	</script>
	--%>
	
	
</head>
<body class="yui-skin-sam">

	<div class="notification-container"></div>
	<%-- 
	<g:if test="${flash.message}">	
		<div id="notify-container" style="display: hidden;">
			<div id="notify-message" class="message">${flash.message}</div>	
		</div>
	</g:if>
 	--%>

    <div id="doc3" class="yui-t7">
		<!-- Spinner gets displayed when AJAX is invoked -->
		<div id="spinner" class="spinner" style="display:none;">
		    <img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
		</div>
		<!-- Header "hd" includes includes logo, global navigation -->
		<div id="hd" role="banner">
		    
		    <!-- Block which includes the logo and login banner -->
		    <div class="yui-b">
				<div class="yui-gf">				
					<div id="banner">
					    <div id="bannerLeft" class="yui-u first" >
							<div class="logo" >
							    <a class="home" href="${createLink(uri: '/dashboard/index')}" style="text-decoration: none">						    	
						    		<img src="${createLinkTo(dir:'images/icons/',file:'logo.gif')}" alt="Your Boxes. You're Welcome." 
						    			style="vertical-align: absmiddle"/>
						    			<span style="font-size: 2em; vertical-align: top;">openboxes</span>
							    </a>
							</div>
					    </div>
					    <div id="bannerRight" class="yui-u" >
					    	<div id="loggedIn">
					    		
								<ul>
								    <g:if test="${session.user}">
										<li>
											Welcome, <b>${session.user.username}</b>
										</li>
										<li>
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
										</li>																		
										<li>
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cart.png')}" style="vertical-align: middle" />
											<g:link controller="shoppingCart" action="list">Cart <span style="color: orange; font-weight: bold;">${session?.cart ? session?.cart?.items?.size() : '0'}</span></g:link>
											
										</li>
										<li>
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
										</li>					
										<li>
											<img src="${createLinkTo(dir: 'images/icons', file: 'profile.png')}" style="vertical-align: top" />
											<g:link style="vertical-align: middle" class="home" controller="user" action="show" id="${session.user.id}">
												Profile
											</g:link>	
										</li>
										<li>
											<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
										</li>												
																	
										<g:if test="${session?.warehouse}">
											<li>
												<img src="${createLinkTo(dir: 'images/icons/silk', file: 'building.png')}" style="vertical-align: middle" />
												<a style="vertical-align: middle" class="home" href='${createLink(controller: "dashboard", action:"chooseWarehouse")}'>
													${session?.warehouse?.name }
												</a>	
											</li>
											<li>
												<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
											</li>																		
										</g:if>
										<li>
											<g:link class="list" controller="auth" action="logout"><g:message code="default.logout.label"  default="Sign Out"/></g:link>
										</li>
										<!-- 
										 <li><g:link class="list" controller="user" action="preferences"><g:message code="default.preferences.label"  default="Preferences"/></g:link></li>
										 -->										 
										<!-- 
										 <li><input type="text" value="search" name="q" style="color: #aaa; font-weight: bold;" disabled=disabled /></li>
										 -->
								    </g:if>
								    <g:else test="${!session.user}">
										<li>Not logged in</li>
										<li><g:link class="list" controller="auth" action="signup"><g:message code="default.signup.label" default="Signup"/></g:link></li>
										<li><g:link class="list" controller="auth" action="login"><g:message code="default.login.label" default="Login"/></g:link></li>
										<!-- 
										  <li><g:link class="list" controller="user" action="register"><g:message code="default.register.label" default="Register"/></g:link></li>
										  <li><g:link class="list" controller="user" action="help"><g:message code="default.help.label" default="Help"/></g:link></li>
										 -->
										 
								    </g:else>
								</ul>
							</div>					
					    </div>
					</div>
					
				</div>
		    </div>		    
		</div>
    </div>
    <div id="doc3" class="yui-t7" style="clear: both;">
		<!-- Populated using the 'pageTitle' property defined in the GSP file -->
		<g:if test="${session.user}">
			<h3>
			    <div id="pageTitle">							    
			    	<g:link controller="dashboard" action="index">
				    	<img src="${createLinkTo(dir: 'images/icons/silk', file: 'house.png')}"/>
			    	</g:link>
				    &nbsp;&rsaquo;&nbsp;								
					<g:if test="${session?.warehouse}">									
						<g:if test="${session.warehouse.logo }">
							<img class="photo" width="25" height="25" 
								src="${createLink(controller:'warehouse', action:'viewLogo', id:session.warehouse.id)}" style="vertical-align: middle" />
						</g:if>
						${session?.warehouse?.name} &nbsp;&rsaquo;&nbsp;
					</g:if>
				    <g:message code="${params.controller }.label"/>
					&nbsp;&rsaquo;&nbsp;								
					<g:if test="${pageProperty(name:'page.pageTitle')}"><b>${pageProperty(name:'page.pageTitle')}</b></g:if>
					<g:else><b><g:layoutTitle /></b></g:else>
				</div>
			</h3>
		</g:if>		
	</div>
    <div id="doc3" class="yui-t2">	    
		<!-- 
				Body includes the divs for the main body content and left navigation menu 
			----------------------------------------------------------------------------------->
		<!-- YUI "body" block that includes the main content for the page -->
		<div id="bd" role="main">

	    	<!-- YUI main Block including page title and content -->
	      	<div id="yui-main">
		    	<div id="content" class="yui-b">
					<g:layoutBody />
				</div>
	      	</div>
	      		      	
	      	<!-- YUI nav block that includes the local navigation menu -->
	      	<div id="menu" role="navigation" class="yui-b">
		  		<g:if test="${session?.user}">
					<!-- Navigation Menu -->
					<g:render template="/common/menu"/>
					<%-- 			  		
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
					--%>
				</g:if>
			</div>			 
		</div>

		<!-- YUI "footer" block that includes footer information -->
		<div id="ft" role="contentinfo">
			<div id="footer">
				<div style="line-height: 2em;">
					&copy; 2010 Partners In Health&trade; <b>OpenBoxes</b> &nbsp;&nbsp; | &nbsp;&nbsp;
					Application Version: &nbsp;<b><g:meta name="app.version"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;
					Grails Version: &nbsp; <b><g:meta name="app.grails.version"></g:meta></b>&nbsp;&nbsp; | &nbsp;&nbsp;
					Locale: &nbsp;  	
					
					<img src="${createLinkTo(dir: 'images/flags', file: 'us.png') }" style="vertical-align: middle;">
					<g:if test="${session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'].toString() != 'en_US'}">			
						<a href="${createLink(controller: 'dashboard', action: 'index', params: ['lang':'en_US'])}">English (US)</a> &nbsp;
					</g:if>
					<g:else>
						<span>English (US)</span>					
					</g:else>
					&nbsp;					
					<img src="${createLinkTo(dir: 'images/flags', file: 'fr.png') }" style="vertical-align: middle;">
					<g:if test="${session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'].toString() != 'fr'}">			
						<a href="${createLink(controller: 'dashboard', action: 'index', params: ['lang':'fr'])}">French</a> &nbsp;
					</g:if>
					<g:else>
						<span>French</span>					
					</g:else>
					&nbsp;
					<img src="${createLinkTo(dir: 'images/flags', file: 'es.png') }" style="vertical-align: middle;">
					<g:if test="${session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'].toString() != 'es'}">			
						<a href="${createLink(controller: 'dashboard', action: 'index', params: ['lang':'es'])}">Spanish</a> &nbsp;
					</g:if>
					<g:else>
						<span>Spanish</span>					
					</g:else>

<!--  

					<img src="${createLinkTo(dir: 'images/flags', file: 'do.png') }" style="vertical-align: middle;">
					<g:if test="${org.springframework.context.i18n.LocaleContextHolder.locale != 'do'}">			
						<a href="${createLink(controller: 'dashboard', action: 'index', params: ['lang':'do'])}">Spanish (Dominican Republic)</a> &nbsp;
					</g:if>
					<g:else>
						<span>French (Rwanda)</span>					
					</g:else>
-->
				</div>
			</div>
		</div>
	</div>
</body>
</html>
