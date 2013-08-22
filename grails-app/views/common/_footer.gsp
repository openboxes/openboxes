<%@ page import="org.pih.warehouse.core.RoleType" %>
<div id="footer">
	<div style="line-height: 3em; " class="middle">
		&copy; 2013 <b>OpenBoxes</b> &nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="application.version.label"/>: &nbsp;<b><g:meta name="app.version"/></b>&nbsp;&nbsp; | &nbsp;&nbsp; 
		<warehouse:message code="application.buildNumber.label"/>: <b><g:meta name="app.revisionNumber"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;					
		<warehouse:message code="application.environment.label"/>: <b>${grails.util.GrailsUtil.environment}</b> &nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="application.buildDate.label"/>: <b><g:meta name="app.buildDate"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="application.grailsVersion.label"/>: &nbsp; <b><g:meta name="app.grails.version"></g:meta></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<%-- <warehouse:message code="default.date.label"/>: <b>${new Date() }</b>&nbsp;&nbsp; | &nbsp;&nbsp;--%>
		<warehouse:message code="default.locale.label"/>: &nbsp;  	
		<!-- show all supported locales -->
		<g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
		<g:each in="${grailsApplication.config.locale.supportedLocales}" var="l">
			<g:set var="locale" value="${new Locale(l)}"/>
			<g:set var="selected" value="${locale == session?.user?.locale || locale == session?.locale }"/>
			<a class="${selected?'selected':''}" href="${createLink(controller: 'user', action: 'updateAuthUserLocale',
				params: ['locale':locale,'targetUri':targetUri])}">
				<!-- fetch the display for locale based on the current locale -->
				${locale?.getDisplayName(session?.user?.locale ?: new Locale(grailsApplication.config.locale.defaultLocale))}
			</a>
		</g:each>
		<g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
			&nbsp;&nbsp; | &nbsp;&nbsp;
			<a href="${createLink(controller: 'user', action: 'updateAuthUserLocale', 
				params: ['locale':'debug','targetUri':targetUri])}">
				<warehouse:message code="admin.debug.label"/>:
			</a>
			${session.useDebugLocale?"on":"off" }
		</g:isUserInRole>
        &nbsp;&nbsp; | &nbsp;&nbsp;
        <span>
            <warehouse:message code="default.ipAddress.label" default="IP Address"/>: &nbsp;
            ${request.getRemoteAddr()}
		</span>
		<%-- 
		&nbsp;&nbsp; | &nbsp;&nbsp;
		<warehouse:message code="default.layout.label"/>: &nbsp; 
		<g:link controller="dashboard" action="chooseLayout" params="['layout':'custom']">custom</g:link>&nbsp;
		<g:link controller="dashboard" action="chooseLayout" params="['layout':'mega']">mobile</g:link>		
		--%>		
	</div>
</div>
