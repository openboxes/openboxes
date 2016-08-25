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

            <div id="header">
                <div class="right">

                    <warehouse:message code="dashboard.loggedInAs.message" args="[session.user.name]"/>
                    &nbsp;
                    <g:link class="button icon unlock big" controller="auth" action="logout">
                        <warehouse:message code="default.logout.label"/>
                    </g:link>
                </div>
                <div class="clear"></div>

            </div>


            <div id="chooseLocation">

				<g:if test="${flash.message}">
			    	<div class="message">${warehouse.message(code:flash.message,default:flash.message)}</div>  
			    	<!-- we wrap this in a message tag since we can't call it directly from with the SecurityFilter -->
				</g:if>


				<g:form controller="dashboard" action="chooseLocation">
					<div class="box">
                        <h2>
                            <img src="${resource(dir:'images/icons/silk',file:'map.png')}" class="middle"/>
                            ${warehouse.message(code: 'dashboard.chooseLocation.label')}
                        </h2>
                        <%--
                        <div style="padding: 50px;">
                            <div>
                                <g:select name="id" value="${session.user.warehouse}" from="${session?.loginLocations?.sort()}"
                                    optionKey="id" optionValue="name"
                                      noSelection="['null':'']" class="chzn-select-deselect"/>
                            </div>
                            <div>
                                <g:checkBox name="rememberLastLocation" value="${userInstance?.rememberLastLocation}"/>
                                <label for="rememberLastLocation">${warehouse.message(code:'user.rememberLastLocation.label', default: 'Remember my location and log me in automatically.')}</label>
                            </div>

                            <div class="right">
                                <button class="button icon arrowright">${warehouse.message(code:'default.button.go.label', default: 'Go')}</button>
                            </div>

                        </div>
                        --%>
                        <g:if test="${session.loginLocationsMap && !session.loginLocationsMap.isEmpty() }">
                            <div id="chooseLocationSelect">
                                <table>
                                    <tbody>
                                        <g:set var="count" value="${0 }"/>
                                        <g:each var="entry" in="${session.loginLocationsMap}" status="i">
                                            <tr>
                                                <td class="top left" >
                                                    <h3>${entry.key?:warehouse.message(code:'locationGroup.empty.label') }</h3>
                                                    <g:set var="locationGroup" value="${entry.key }"/>
                                                    <g:each var="warehouse" in="${entry.value.sort() }" status="status">
                                                        <div class="left" style="margin: 2px;">
                                                            <a id="warehouse-${warehouse.id}-link" href='${createLink(action:"chooseLocation", id: warehouse.id)}' class="button big">
                                                                <format:metadata obj="${warehouse}"/>
                                                            </a>
                                                        </div>
                                                    </g:each>
                                                </td>
                                            </tr>
                                        </g:each>
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

                                    </tbody>
                                </table>
                            </div>
                        </g:if>

                        <g:unless test="${session.loginLocations }">
                            <div style="padding:10px;">
                                <h3>
                                    <warehouse:message code="dashboard.noWarehouse.message"/>
                                </h3>
                                <div>
                                    <warehouse:message code="dashboard.requiredActivities.message"
                                                       args="[grailsApplication.config.openboxes.chooseLocation.requiredActivities]"/>
                                </div>
                            </div>
                        </g:unless>

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
        <script>
            $(function() {
                $( ".accordion" ).accordion();
            });

        </script>

    </body>
</html>

