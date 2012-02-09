<!-- Block which includes the logo and login banner -->
<div class="yui-b">
	<div class="yui-gf">				
		<div id="banner">
		    <div id="bannerLeft" class="yui-u first" >
				<div class="logo" >
				    <a class="home" href="${createLink(uri: '/dashboard/index')}" style="text-decoration: none">						    	
			    		<img src="${createLinkTo(dir:'images/icons/',file:'logo.gif')}" alt="Your Boxes. You're Welcome." 
			    			style="vertical-align: absmiddle"/>
			    			<span style="font-size: 2em; vertical-align: middle;">openboxes</span>
				    </a>
				</div>
		    </div>
		    <div id="bannerRight" class="yui-u" >
		    	<div id="loggedIn">
					<ul>
					    <g:if test="${session.user}">
							<li>
								<warehouse:message code="layout.welcome.label"/>, <b>${session?.user?.name}</b> 
							</li>
							<!-- 																	
							<li>
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cart.png')}" style="vertical-align: middle" />
								<g:link controller="cart" action="list">Cart <span style="color: orange; font-weight: bold;">${session?.cart ? session?.cart?.items?.size() : '0'}</span></g:link>
								
							</li>
							-->
							<g:if test="${session?.warehouse}">
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
								</li>					
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'user.png')}" style="vertical-align: middle" />
									<g:link class="home" controller="user" action="show" id="${session.user.id}">
										<warehouse:message code="layout.myAccount.label"/>
									</g:link>	
								</li>
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
								</li>
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'application_view_tile.png')}" style="vertical-align: middle" />
									<g:link class="home" controller="dashboard" action="index">
										<warehouse:message code="dashboard.label"/>
									</g:link>	
								</li>
														
								<li>
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
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
										<div style="height: 200px; overflow: auto;">
											<table>
												<tbody>
													<g:each var="warehouse" in="${session.loginLocations}" status="i">	
														<tr class="prop">
															<td nowrap="nowrap" class="center" style="padding: 0">
																<g:if test="${warehouse?.fgColor && warehouse?.bgColor }">
																	<style>
																		#warehouse-${warehouse?.id} { background-color: #${warehouse.bgColor}; color: #${warehouse.fgColor}; } 
																		#warehouse-${warehouse?.id} a { color: #${warehouse.fgColor}; }  	
																	</style>				
																</g:if>					
																<div id="warehouse-${warehouse.id }" class="warehouse button">												
																	<a href='${createLink(controller: "dashboard", action:"chooseLocation", id: warehouse.id, params: ['returnUrl':request.forwardURI])}' style="display: block; padding: 0px;">
																		${warehouse.name}
																	</a> 
																</div>												
															</td>											
														</tr>
													</g:each>																	
													<g:unless test="${session.loginLocations }">
														<tr class="prop">
															<td nowrap="nowrap">
																<div style="color: black; background-color: white;">
																	<warehouse:message code="dashboard.noWarehouse.message"/>
																</div>
															</td>
														</tr>
													</g:unless>
												</tbody>					
											</table>	
										</div>												
									</span>
								</li>
							</g:if>
							<li>
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png')}" style="vertical-align: middle" />
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
							<li><g:link class="list" controller="auth" action="signup"><warehouse:message code="default.signup.label"/></g:link></li>
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