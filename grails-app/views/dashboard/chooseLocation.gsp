<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'dashboard.chooseLocation.label', default: 'Choose a warehouse to manage')}</title>
		<style>			
			#menu { display: none; } 
			.page-title { display: none; } 
			td.warehouse { padding: 0px; } 
			
			.warehouse { padding: 10px; width: 175px; background-color: #fcfcfc; color: #333 } 			
			.warehouse a:hover { text-decoration: underline; }
			<g:each var="warehouse" in="${session.loginLocations}" status="i">						
				<g:if test="${warehouse?.fgColor && warehouse?.bgColor }">
					#warehouse-${warehouse?.id} { background-color: #${warehouse.bgColor}; color: #${warehouse.fgColor}; } 
					#warehouse-${warehouse?.id} a { color: #${warehouse.fgColor}; }  	
				</g:if>
			</g:each>			
		</style>
    </head>
    <body>        
		<div class="body">		
			<div id="chooseLocation">
				<g:if test="${flash.message}">
			    	<div class="message">${warehouse.message(code:flash.message,default:flash.message)}</div>  <!-- we wrap this in a message tag since we can't call it directly from with the SecurityFilter -->
				</g:if>		
				<h1><warehouse:message code="dashboard.chooseLocation.label"/></h1>
    			<div style="height: 300px; width: 600px; overflow: auto;" class="box">
					<g:each var="warehouse" in="${session.loginLocations}" status="i">
						<div id="warehouse-${warehouse.id }" class="warehouse button center" style="float: left; border: 1px solid lightgrey;">													
							<a href='${createLink(action:"chooseLocation", id: warehouse.id)}' style="display: block;">
								<g:if test="${warehouse.logo}">	
									<img class="logo" width="16" height="16" style="vertical-align: middle;" src="${createLink(controller:'location', action:'viewLogo', id: warehouse.id)}" />
									<%--<img src="${warehouse.logo}" width="24" height="24" style="vertical-align: middle; padding: 5px;"></img>--%>
								</g:if>
								<g:else>
									<%-- 
									<img src="${createLinkTo(dir:'images',file:'icons/building.png')}" style="vertical-align: middle"/>
									--%>
								</g:else>
								${warehouse.name} 
							</a> 
							<g:if test="${warehouse?.id == session?.user?.warehouse?.id }">
								<warehouse:message code="dashboard.youLastLoggednHereOn.message" args="[format.datetime(obj:session?.user?.lastLoginDate)]"/> 
							</g:if>
						</div>												
					</g:each>
					<g:unless test="${session.loginLocations }">
						<div class="warehouse">
							<warehouse:message code="dashboard.noWarehouse.message"/>
						</div>
						<div class="warehouse">		
							<warehouse:message code="dashboard.requiredActivities.message" args="[grailsApplication.config.app.loginLocation.requiredActivities]"/>
						</div>
					</g:unless>							
					
				</div>
				
			</div>
		</div>
    </body>
</html>

