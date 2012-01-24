<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="admin.title" default="Settings" /></title>
    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">

				<table>
					<tr class="">
						<td colspan="2">
							<h1><warehouse:message code="tomcat.manager.label"/></h1>
						</td>
					</tr>
					<tr class="prop">						
						<td class="name">
							<label><warehouse:message code="tomcat.applications.label"/></label>
						</td>
						<td class="value">
							<table>
								<g:each in="${applications }" var="application">
									<tr class="prop">
										<td>
											${application[0] }
										</td>
										<td>							
											${application[1] }
										</td>
										<td>							
											${application[2] }
										</td>
										<td>							
											${application[3] }
										</td>
										<td>							
											${application[4] }
										</td>
									</tr>
								</g:each>
							</table>
						</td>
					</tr>
					<tr class="">
						<td colspan="2">
							<h1><warehouse:message code="admin.generalSettings.header"/></h1>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">					
							<label>
								<warehouse:message code="application.environment.label"/>
							</label>
						</td>
						<td class="value">						
							${grails.util.GrailsUtil.environment}
						</td>
					</tr>													
					<tr class="prop">							
						<td class="name">		
							<label>			
								<warehouse:message code="application.version.label"/>
							</label>
						</td>
						<td class="value">						
							<g:meta name="app.version"/> &nbsp;			
							
							<g:link controller="admin" action="reloadWar">[Reload]</g:link>
										
							<g:if test='${!session.future || session.future.isDone() || session.future.isCancelled()}'>
								<g:link controller="admin" action="updateWar">[Download]</g:link>
							</g:if>				
							<g:else>
								[Update] [<g:link controller="admin" action="cancelUpdateWar">Cancel</g:link>]
							</g:else>
							${warLastModifiedDate }
							<script>
								$(function() {
									$( "#progressbar" ).progressbar({
										value: ${(warFile.size() / warSize) * 100}
									});
								});
							</script>
							<div>
								<div id="progressbar"></div>
								${(warFile.size() / warSize) * 100}%
								<g:if test="${session?.future?.isCancelled() }">
									Download was canceled.
								</g:if>
								<g:elseif test="${session?.future?.isDone() }">
									Download has been completed!!!				
									[<g:link controller="admin" action="deployWar">Deploy</g:link>]
								</g:elseif>
							</div>
							
						</td>
					</tr>													
					<tr class="prop">							
						<td class="name">		
							<label>			
								<warehouse:message code="application.buildNumber.label"/>
							</label>
						</td>
						<td class="value">						
							<g:meta name="app.buildNumber"/>
						</td>
					</tr>													
					<tr class="prop">							
						<td class="name">					
							<label>			
								<warehouse:message code="application.buildDate.label"/>
							</label>
						</td>
						<td class="value">						
							<g:meta name="app.buildDate"/>
						</td>
					</tr>													
					<tr class="prop">
						<td class="name">					
							<label>			
								<warehouse:message code="application.revisionNumber.label"/>
							</label>
						</td>
						<td class="value">						
							<g:meta name="app.revisionNumber"/>	
						</td>
					</tr>													
					<tr class="prop">							
						<td class="name">					
							<label>			
								<warehouse:message code="application.grailsVersion.label"/>
							</label>
						</td>
						<td class="value">						
							<g:meta name="app.grails.version"></g:meta>
						</td>
					</tr>													
					<tr class="prop">							
						<td class="name">					
							<label>			
								<warehouse:message code="default.date.label"/> 
							</label>
						</td>
						<td class="value">						
							${new Date() }
						</td>
					</tr>													
					<tr class="prop">
						<td class="name">					
							<label>			
								<warehouse:message code="default.locale.label"/>
							</label>
						</td>
						<td class="value">						
							${session?.user?.locale}
						</td>					
					<tr>
					<tr class="prop">
						<td colspan="2">
							<h1><warehouse:message code="admin.emailSettings.header"/></h1>	
						</td>
					</tr>			
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="admin.emailEnabled.label"/></label>
						</td>
						<td>
							${enabled }
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="admin.hostname.label"/> </label>
						</td>
						<td>
							${host }
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="admin.port.label"/> </label>
						</td>
						<td>
							${port}
						</td>
					</tr>						
					<tr class="">
						<td colspan="2">
							<h1><warehouse:message code="admin.externalAppConfig.header"/></h1>
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							<label><warehouse:message code="admin.externalConfigFile.label"/></label>
						</td>
						<td>
							${grailsApplication.config.grails.config.locations }
						</td>
					</tr>
					<g:each in="${externalConfigProperties }" var="externalProperty">
						<g:each var="property" in="${externalProperty }">
							<tr class="prop">
								<td class="name">				
									<label>${property.key }</label>
								</td>
								<td class="value">
									<g:if test="${property.key == 'dataSource.password' }">																			
										${util.StringUtil.mask(property.value, "*")}
									</g:if>
									<g:else>
										${property.value }												
									</g:else>
								</td>
							</tr>
						</g:each>
					</g:each>								
					<tr class="">
						<td colspan="2">
							<h1><warehouse:message code="admin.systemProperties.header"/></h1>
						</td>
					</tr>
					<g:each in="${systemProperties}" var="prop"> 
						<tr class="prop">
							<td class="name">
								${prop.key }
							</td>
							<td class="value">
								${prop.value }
							</td>
						</tr>
					</g:each>
				</table>
			</div>	
		</div>
    </body>
</html>
