<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'dashboard.chooseLocation.label')}</title>
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
                        <div class="message">
                            <g:message code="dashboard.youLastLoggednHereOn.message"
                                       args="[g.prettyDateFormat(date: session?.user?.lastLoginDate), g.formatDate(date: session?.user?.lastLoginDate, format: 'MMM dd yyyy hh:mm:ss a z')]"/>
                        </div>
                        <div id="chooseLocationSelect">
                            <g:render template="loginLocations"/>
                        </div>
                        <div class="prop" style="background-color: #eee; text-align: center">
                            <g:message code="dashboard.loggedInAs.message" args="[session?.user?.name]"/>.
                            <g:link class="button icon unlock" controller="auth" action="logout">
                                <warehouse:message code="default.logout.label"/>
                            </g:link>
                        </div>
					</div>
				</g:form>				
			</div>
		</div>
    </body>
</html>

