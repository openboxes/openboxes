<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<!-- Include default page title -->
	<title><g:layoutTitle default="Your Location App" /></title>

	<script src="http://cdn.jquerytools.org/1.2.3/jquery.tools.min.js?select=full&debug=true"></script> 
	
	<!-- tab styling -->
	
	<link href="${createLinkTo(dir:'css', file:'tabs.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
		
	<!-- Grails Layout : write head element for page-->
	<g:layoutHead />

</head>
<body >
    <div >
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
										(<g:link class="list" controller="auth" action="logout"><warehouse:message code="default.notuser.label"  default="not you?"/></g:link>)
									</li>
									| <li><g:link class="list" controller="auth" action="logout"><warehouse:message code="default.logout.label"  default="Logout"/></g:link></li>
									| <li><input type="text" value="search" name="q" style="color: #aaa; font-weight: bold;" disabled=disabled /></li>								
							    </g:if>
							    <g:else test="${!session.user}">
									<li>Not logged in</li>  | <li><g:link class="list" controller="auth" action="login"><warehouse:message code="default.login.label" default="Login"/></g:link></li>									 
							    </g:else>
							</ul>
						</div>
				    </div>
				</div>
		    </div>
		    
		    <!-- Block which includes global navigation and breadcrumb -->
		    <div class="yui-b">
				<!-- Global Navigation menu -->
				<div class="nav">
					<g:breadcrumb />								
				    <g:if test="${session.user}">			    
						<!-- TODO Implemented hack to move the settings menu over to the right -->
						<span class="menuButton" style="position:absolute; right: 15px;"><a class="settings" href="${createLink(uri: '/admin/index')}">Settings</a></span>
				    </g:if>
				</div>
		    </div>
		    
		</div>
    </div>
    
    <div id="doc3" class="yui-t0">
    
	    
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
					<g:layoutBody />
				</div>
	      	</div>
	      		      	
	      	<!-- YUI nav block that includes the local navigation menu -->
		</div>
		
		<!-- YUI "footer" block that includes footer information -->
		<div id="ft" role="contentinfo">
			<div id="footer">
				&copy; 2010 <a href="http://www.pih.org">PIH</a>&trade; Location &nbsp;&nbsp; | &nbsp;&nbsp;
				Application Version: <g:meta name="app.version"/>&nbsp;&nbsp; | &nbsp;&nbsp;
				Grails Version: <g:meta name="app.grails.version"></g:meta>
			</div>
		</div>
	</div>
</body>
</html>
