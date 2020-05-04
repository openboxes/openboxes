<%@ page import="org.pih.warehouse.core.Constants; org.pih.warehouse.core.RoleType" %>
<div id="footer">
	<div style="line-height: 2em;" class="center middle">
		&copy; <g:copyrightYear/>
        <a href="https://openboxes.com">Powered by OpenBoxes</a> &nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.grailsVersion.label"/>: &nbsp; <b><g:meta name="info.app.grailsVersion"></g:meta></b> &nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.version.label"/>: &nbsp;<b><a href="https://github.com/openboxes/openboxes/releases/tag/v${g.meta(name:'info.app.version')}"><g:meta name="info.app.version"/></a></b>&nbsp;&nbsp; | &nbsp;&nbsp;
        <g:if test="${gitProperties}">
            <g:message code="application.branchName.label"/>: <b>${gitProperties?.branch}</b>&nbsp;&nbsp; | &nbsp;&nbsp;
            <g:message code="application.commitSha.label" default="SHA"/>: <b><a href="https://github.com/openboxes/openboxes/commit/${gitProperties?.commitId}">${gitProperties?.shortCommitId}</a></b>&nbsp;&nbsp; | &nbsp;&nbsp;
        </g:if>
		<g:message code="application.environment.label"/>: <b>${grails.util.Environment?.current?.name}</b> &nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.buildNumber.label"/>: <b><g:meta name="git.commit.id.describe"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<g:message code="application.buildDate.label"/>: <b><g:meta name="build.time"/></b>
    </div>
    <div class="center" style="line-height: 2em;">
		<g:message code="default.locale.label"/>: &nbsp;
		<!-- show all supported locales -->
		<g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
		<g:each in="${grailsApplication.config.openboxes.locale.supportedLocales}" var="l">
			<g:set var="locale" value="${new Locale(l)}"/>
			<g:set var="selected" value="${locale == session?.user?.locale || locale == session?.locale }"/>
			<a class="${selected?'selected':''}" href="${createLink(controller: 'user', action: 'updateAuthUserLocale',
				params: ['locale':locale,'targetUri':targetUri,'lang':locale?.language])}">
				<!-- fetch the display for locale based on the current locale -->
				${locale?.getDisplayName(session?.user?.locale ?: new Locale(grailsApplication.config.openboxes.locale.defaultLocale))}
			</a>
		</g:each>
		<g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
			&nbsp;&nbsp; | &nbsp;&nbsp;
			<a href="${createLink(controller: 'user', action: 'updateAuthUserLocale',
				params: ['locale':'debug','targetUri':targetUri])}">
				<g:message code="admin.debug.label"/>:
			</a>
			<b>${session.useDebugLocale?"on":"off" }</b>
		</g:isUserInRole>
        &nbsp;&nbsp; | &nbsp;&nbsp;
        <span>
            <g:message code="default.ipAddress.label" default="IP Address"/>: &nbsp;
            <b>${request.getRemoteAddr()}</b>
		</span>
        &nbsp;&nbsp; | &nbsp;&nbsp;
        <span>
            <g:message code="default.hostname.label" default="Hostname"/>: &nbsp;
            <b>${session.hostname?:"Unknown"}</b>
        </span>
        &nbsp;&nbsp; | &nbsp;&nbsp;
        <span>
            <g:message code="default.timezone.label" default="Timezone"/>: &nbsp;
            <b>${session?.timezone?.ID}</b>
        </span>
        <g:if test="${session.warehouse && session.user && session._showTime}">
        &nbsp;&nbsp; | &nbsp;&nbsp;
            <span>
                Page load:
                <b>${(request?.pageLoadInMilliseconds?:0)/1000}s</b>
            </span>
        </g:if>
	</div>
</div>
