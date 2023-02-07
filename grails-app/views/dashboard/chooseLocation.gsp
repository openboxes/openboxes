<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title>${warehouse.message(code: 'dashboard.chooseLocation.label')}</title>
    </head>
    <body>
        <div class="d-flex justify-content-center align-items-center h-100">
            <div class="location-chooser location-chooser__login">
                <div class="location-chooser__header">
                    <h2>${warehouse.message(code: 'dashboard.chooseLocation.label')}</h2>
                    <g:if test="${flash.message}">
                        <div class="message">${warehouse.message(code:flash.message,default:flash.message)}</div>
                        <!-- we wrap this in a message tag since we can't call it directly from with the SecurityFilter -->
                    </g:if>
                </div>
                <g:form controller="dashboard" action="chooseLocation">
                    <g:render template="loginLocations"/>
                    <div class="d-flex justify-content-between location-chooser__footer">
                        <div class="location-chooser__footer__last-signin d-flex align-items-center justify-content-center">
                            <g:message code="dashboard.youLastLoggedInHereOn.message"
                                       args="[g.prettyDateFormat(date: session?.user?.lastLoginDate), g.formatDate(date: session?.user?.lastLoginDate, format: 'MMM dd yyyy hh:mm:ss a z')]"/>
                        </div>
                        <div class="d-flex justify-content-center align-items-center">
                            <span class="location-chooser__footer__logout-user mr-2">
                                <g:message code="dashboard.loggedInAs.message" args="[session?.user?.name]"/>.
                            </span>
                            <g:link class="location-chooser__footer__logout-btn" controller="auth" action="logout">
                                <i class="ri-logout-box-r-line"></i>
                                <warehouse:message code="default.logout.label"/>
                            </g:link>
                        </div>
                    </div>
                </g:form>
            </div>
        </div>
    </body>
</html>
