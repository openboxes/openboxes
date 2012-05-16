<%@ page import="org.pih.warehouse.core.RoleType" %>
<!-- Block which includes the logo and login banner -->
<div class="yui-b">
	<div class="yui-gf">				
		<div id="banner">
		    <div id="bannerLeft" class="yui-u first" >
				<div class="logo" >
				    <a class="home" href="${createLink(uri: '/dashboard/index')}" style="text-decoration: none">						    	
			    		<img src="${createLinkTo(dir:'images/icons/',file:'logo.gif')}" title="${warehouse.message(code:'default.tagline.label') }" class="middle"/>
		    			<span style="font-size: 2em; vertical-align: middle;" class="title">
							<warehouse:message code="default.openboxes.label"/>
						</span>
				    </a>
				</div>
		    </div>
		    <div id="bannerRight" class="yui-u">
		    	<div id="loggedIn" >
					<ul>
					    <g:if test="${session.user}">
							<li>
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'user.png')}" style="vertical-align: middle" />
								<g:link class="home" controller="user" action="show" id="${session.user.id}">
									${session?.user?.name} 
								</g:link>
							</li>
							<!-- 																	
							<li>
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cart.png')}" style="vertical-align: middle" />
								<g:link controller="cart" action="list">Cart <span style="color: orange; font-weight: bold;">${session?.cart ? session?.cart?.items?.size() : '0'}</span></g:link>
								
							</li>
							-->
							<g:if test="${session?.warehouse}">
								<%-- 
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
								</li>					
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'user.png')}" style="vertical-align: middle" />
									<g:link class="home" controller="user" action="show" id="${session.user.id}">
										<warehouse:message code="layout.myAccount.label"/>
									</g:link>	
								</li>
								--%>
								
								<li>
									&nbsp;|&nbsp;
								</li>
								<%-- 
								<g:if test="${exception}">
									<li>
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'email.png')}" style="vertical-align: middle" />
							  			<a href="#" class="email open-dialog">
							  				<warehouse:message code="default.reportBug.label"/>
							  			</a>
									</li>				
									<li>
										&nbsp;|&nbsp;
									</li>
								</g:if>				
								<g:else>
									<li>
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'email.png')}" style="vertical-align: middle" />
							  			<a href="#" class="email open-dialog">
							  				<warehouse:message code="default.feedback.label"/>
							  			</a>
									</li>				
									<li>
										&nbsp;|&nbsp;
									</li>
								</g:else>
								--%>
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_view_tile.png')}" style="vertical-align: middle" />
									<g:link class="home" controller="dashboard" action="index">
										<warehouse:message code="dashboard.label"/>
									</g:link>	
								</li>
														
								<li>
								&nbsp;|&nbsp;
								</li>
							
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'building.png')}" style="vertical-align: middle" />
									<%-- 
									<a class="home" href='${createLink(controller: "dashboard", action:"chooseLocation")}'></a>
									--%>
									<a href="javascript:void(0);" id="warehouse-switch">
										${session?.warehouse?.name }
									</a>
									<span id="warehouseMenu" title="${warehouse.message(code:'warehouse.chooseLocationToManage.message')}" style="display: none;">
										<g:isUserNotInRole roles="[RoleType.ROLE_ADMIN]">
											<div class="error">
												${warehouse.message(code:'auth.needAdminRoleToChangeLocation.message')}
											</div>
										</g:isUserNotInRole>
										<g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
											<div style="height: 200px; overflow: auto;">
												<g:each var="warehouse" in="${session.loginLocations}" status="i">	
													<g:if test="${warehouse?.fgColor && warehouse?.bgColor }">
														<style>
															#warehouse-${warehouse?.id} { background-color: #${warehouse.bgColor}; color: #${warehouse.fgColor}; } 
															#warehouse-${warehouse?.id} a { color: #${warehouse.fgColor}; }  	
														</style>				
													</g:if>					
													<div id="warehouse-${warehouse.id }" class="warehouse button">	
														<g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
														<a href='${createLink(controller: "dashboard", action:"chooseLocation", id: warehouse.id, params:['targetUri':targetUri])}' style="display: block; padding: 0px;">
															${warehouse.name}
														</a> 
													</div>												
												</g:each>																	
												<g:unless test="${session.loginLocations }">
													<div style="background-color: black; color: white;" class="warehouse button">
														<warehouse:message code="dashboard.noWarehouse.message"/>
													</div>
												</g:unless>
											</div>												
										</g:isUserInRole>
									</span>
								</li>
							</g:if>
							<li>
								&nbsp;|&nbsp;
							</li>												
							<li>
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'door_out.png')}" style="vertical-align: middle" />
								<g:link class="list" controller="auth" action="logout"><warehouse:message code="default.logout.label"/></g:link>
							</li>					
							
							<!-- 
							 <li><g:link class="list" controller="user" action="preferences"><warehouse:message code="default.preferences.label"  default="Preferences"/></g:link></li>
							 -->										 
							<!-- 
							 <li><input type="text" value="search" name="q" style="color: #aaa; font-weight: bold;" disabled=disabled /></li>
							 -->
					    </g:if>
					    <g:else test="${!session.user}">
							<li><warehouse:message code="layout.notLoggedIn.label"/></li>
							<li>&nbsp;|&nbsp;</li>
							<li><g:link class="list" controller="auth" action="signup"><warehouse:message code="default.signup.label"/></g:link></li>
							<li>&nbsp;|&nbsp;</li>
							<li><g:link class="list" controller="auth" action="login"><warehouse:message code="default.login.label"/></g:link></li>
							<!-- 
							  <li><g:link class="list" controller="user" action="register"><warehouse:message code="default.register.label" default="Register"/></g:link></li>
							  <li><g:link class="list" controller="user" action="help"><warehouse:message code="default.help.label" default="Help"/></g:link></li>
							 -->
							 
					    </g:else>
					</ul>
				</div>					
		    </div>
		</div>
	</div>
</div>		    