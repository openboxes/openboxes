<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'dashboard.chooseLocation.label')}</title>
		<style>			
			#menu { display: none; } 
			.page-title { display: none; } 
			td.warehouse { padding: 0px; } 
			#hd { display: none; }  
			.breadcrumb { display: none; }
			.warehouse { padding: 10px; width: 175px; background-color: #fcfcfc; color: #333 } 			
			.warehouse a:hover { text-decoration: underline; }
			<%--
			<g:each var="warehouse" in="${session.loginLocations}" status="i">						
				<g:if test="${warehouse?.fgColor && warehouse?.bgColor }">
					#warehouse-${warehouse?.id} { background-color: #${warehouse.bgColor}; color: #${warehouse.fgColor}; } 
					#warehouse-${warehouse?.id} a { color: #${warehouse.fgColor}; }  	
				</g:if>
			</g:each>
			--%>			
		</style>
    </head>
    <body>        
		<div class="body">		
			<div id="chooseLocation">
				<g:if test="${flash.message}">
			    	<div class="message">${warehouse.message(code:flash.message,default:flash.message)}</div>  
			    	<!-- we wrap this in a message tag since we can't call it directly from with the SecurityFilter -->
				</g:if>		
				
				<g:form controller="dashboard" action="chooseLocation">
					<div class="box">
                        <h2>
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'map.png')}" class="middle"/>
                            ${warehouse.message(code: 'dashboard.chooseLocation.label')}
                        </h2>
                        <br/>
						<div id="chooseLocationSelect">
							<table>
								<tbody>
									<g:set var="count" value="${0 }"/>
									<g:set var="nullLocationGroup" value="${session.loginLocationsMap.remove(null) }"/> 
									<g:each var="entry" in="${session.loginLocationsMap}" status="i">
										<tr class="${count++%2?'even':'odd' }">
											<td class="top left" >			
												<h2>${entry.key?:warehouse.message(code:'default.none.label') }</h2>
											</td>
										</tr>
										<tr class="${count++%2?'even':'odd' }">
											<td>	
												<div class="button-group">
													<g:set var="locationGroup" value="${entry.key }"/>
													<g:each var="warehouse" in="${entry.value.sort() }" status="status">
														<a id="warehouse-${warehouse.id}-link" href='${createLink(action:"chooseLocation", id: warehouse.id)}' class="button big">
															${warehouse.name}
														</a>
													</g:each>
												</div>
												
											</td>
										</tr>										
									</g:each>
									<tr class="${count++%2?'even':'odd' }">
										<td class="top left">
											<h2>${warehouse.message(code: 'default.others.label', default: 'Others')}</h2>
										</td>
									</tr>
									<tr class="${count++%2?'even':'odd' }">
										<td>
											<div class="button-group">											
												<g:each var="warehouse" in="${nullLocationGroup }" status="status">
													<a id="warehouse-${warehouse.id}-link" href='${createLink(action:"chooseLocation", id: warehouse.id)}' class="button big">
														${warehouse.name}
													</a>
												</g:each>
											</div>
										</td>										
									</tr>
									<%--
									<tr class="prop">
										<td class="">
										</td>
										<td class="middle">
											<g:checkBox name="rememberLastLocation" value="${session.user.rememberLastLocation}"/> 
											Remember my location and log me in automatically.
											
											${session.user.rememberLastLocation}
											${session.user.warehouse }
										</td>
									</tr>	
									<tr>
										<td>
											<g:if test="${session?.user?.warehouse }">
												<warehouse:message code="dashboard.youLastLoggednHereOn.message" args="[format.datetime(obj:session?.user?.lastLoginDate)]"/> 
											</g:if>												
										
										</td>
									</tr>
									--%>
									<g:unless test="${session.loginLocations }">
										<div class="warehouse">
											<warehouse:message code="dashboard.noWarehouse.message"/>
										</div>
										<div class="warehouse">		
											<warehouse:message code="dashboard.requiredActivities.message" args="[grailsApplication.config.app.loginLocation.requiredActivities]"/>
										</div>
									</g:unless>							
								</tbody>
							</table>
						</div>
						<%-- 	
						<table>
							<tr>
								<td class="left middle" colspan="2">
									<hr/>
								</td>
							</tr>
							<tr class="prop>
								<td class="left middle" colspan="2">
									<g:checkBox name="rememberLastLocation" value="${session.user.rememberLastLocation}"/> Remember my location and log me in automatically.
								</td>
							</tr>	
							<tr>
								<td>
									<g:if test="${session?.user?.warehouse?.id }">
										<warehouse:message code="dashboard.youLastLoggednHereOn.message" args="[format.datetime(obj:session?.user?.lastLoginDate)]"/> 
									</g:if>												
								
								</td>
							</tr>
						</table>
						--%>
					</div>
				</g:form>				
			</div>
		</div>
    </body>
</html>

