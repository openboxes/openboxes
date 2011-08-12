<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'default.chooseWarehouse.label', default: 'Choose a warehouse to manage')}</title>
    </head>
    <body>        
		<style>
			
			#menu { display: none; } 
			.page-title { display: none; } 
			td.warehouse { padding: 0px; } 
			
			.warehouse { border: 0px solid #F5F5F5; padding: 10px; display: block; } 			
			
			<g:each var="warehouse" in="${warehouses}" status="i">								
				<g:if test="${warehouse?.fgColor && warehouse?.bgColor }">
					#warehouse-${warehouse?.id} { background-color: #${warehouse.bgColor}; color: #${warehouse.fgColor}; } 
					#warehouse-${warehouse?.id} a { color: #${warehouse.fgColor}; }  	
				</g:if>					
			</g:each>			
			
		</style>

		<div class="body">		
	    	<div class="list">			
	    		


				<div id="chooseWarehouse">
					
					<g:if test="${flash.message}">
				    	<div class="message">${flash.message}</div>
					</g:if>		
					<h1><warehouse:message code="dashboard.chooseWarehouse.label"/></h1>
		    		<fieldset>
						<table>
							<tbody>						
								<g:each var="warehouse" in="${warehouses}" status="i">								
									<tr class="prop">
										<td class="warehouse" nowrap="nowrap">
											<div id="warehouse-${warehouse.id }" class="warehouse">												
												<g:if test="${warehouse.local}">
													<a class="home" href='${createLink(action:"chooseWarehouse", id: warehouse.id)}' style="display: block;">
														<g:if test="${warehouse.logo}">	
															<img class="logo" width="16" height="16" style="vertical-align: middle;" src="${createLink(controller:'warehouse', action:'viewLogo', id: warehouse.id)}" />
															<%--<img src="${warehouse.logo}" width="24" height="24" style="vertical-align: middle; padding: 5px;"></img>--%>
														</g:if>
														<g:else>
															<img src="${createLinkTo(dir:'images',file:'icons/building.png')}" style="vertical-align: middle"/>
														</g:else>
														${warehouse.name} 
													</a> 
														
													<g:if test="${warehouse?.id == session?.user?.warehouse?.id }">
														<warehouse:message code="dashboard.youLastLoggednHereOn.message" args="[format.datetime(obj:session?.user?.lastLoginDate)]"/> 
													</g:if>
												</g:if>
												<g:else>
													<g:if test="${warehouse.logo}">	
														<img class="logo" width="16" height="16" style="vertical-align: middle;" src="${createLink(controller:'warehouse', action:'viewLogo', id: warehouse.id)}" />															
														<%--<img src="${warehouse.logo}" width="24" height="24" style="vertical-align: middle; padding: 5px;"></img>--%>
													</g:if>
													<g:else>
														<img src="${createLinkTo(dir:'images',file:'icons/building.png')}" style="vertical-align: middle"/>
													</g:else>
													&nbsp;
													<warehouse:message code="dashboard.managedRemotely.message" args="[warehouse.name]"/>/span>
												</g:else>
											</div>												
										</td>											
									</tr>																		
								</g:each>							
								
							</tbody>					
						</table>
					</fieldset>
				</div>
	    	</div>
		</div>
		
		
		
    </body>
</html>

