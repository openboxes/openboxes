<div id="footer">
	<div style="line-height: 2em;">
		&copy; 2012 Partners In Health&trade; <b>OpenBoxes</b> &nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="application.environment.label"/>: <b>${grails.util.GrailsUtil.environment}</b> &nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="application.version.label"/>: &nbsp;<b><g:meta name="app.version"/></b>&nbsp;&nbsp; | &nbsp;&nbsp; 
		<warehouse:message code="application.buildNumber.label"/>: <b><g:meta name="app.buildNumber"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="application.buildDate.label"/>: <b><g:meta name="app.buildDate"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="application.revisionNumber.label"/>: <b><g:meta name="app.revisionNumber"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;					
		<warehouse:message code="application.grailsVersion.label"/>: &nbsp; <b><g:meta name="app.grails.version"></g:meta></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<%-- <warehouse:message code="default.date.label"/>: <b>${new Date() }</b>&nbsp;&nbsp; | &nbsp;&nbsp;--%>
		<warehouse:message code="default.locale.label"/>: &nbsp;  	
		
		<!-- show all supported locales -->
		<g:each in="${grailsApplication.config.locale.supportedLocales}" var="l">
			<g:set var="locale" value="${new Locale(l)}"/>
			<a href="${createLink(controller: 'user', action: 'updateAuthUserLocale', params: ['locale':locale])}">
				<!-- fetch the display for locale based on the current locale -->
				${locale?.getDisplayName(session?.user?.locale ?: new Locale(grailsApplication.config.locale.defaultLocale))}
			</a> &nbsp;
		</g:each>
		
		<a href="${createLink(controller: 'user', action: 'updateAuthUserLocale', params: ['locale':'debug'])}">
			debug
		</a>

	</div>
</div>