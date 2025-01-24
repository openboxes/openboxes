<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.LocalizationUtil" %>
<%@ page import="util.ConfigHelper" %>

<div id="footer">
	<div style="line-height: 2em;" class="center middle">
		&copy; <g:copyrightYear/>
        <a href="https://openboxes.com"><warehouse:message code="default.poweredBy.label" default="Powered by OpenBoxes"/></a> &nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.grailsVersion.label"/>: &nbsp; <b><g:meta name="info.app.grailsVersion"></g:meta></b> &nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.version.label"/>: &nbsp;<b><a href="https://github.com/openboxes/openboxes/releases/tag/v${g.meta(name:'info.app.version')}"><g:meta name="info.app.version"/></a></b>&nbsp;&nbsp; | &nbsp;&nbsp;
        <g:if test="${gitProperties}">
            <g:message code="application.branchName.label"/>: <b>${ConfigHelper.getBranchName(gitProperties)}</b>&nbsp;&nbsp; | &nbsp;&nbsp;
            <g:message code="application.buildNumber.label"/>: <b><a href="https://github.com/openboxes/openboxes/commit/${gitProperties?.commitId}">${gitProperties?.shortCommitId}</a></b>&nbsp;&nbsp; | &nbsp;&nbsp;
        </g:if>
		<g:message code="application.environment.label"/>: <b>${grails.util.Environment?.current?.name}</b> &nbsp;&nbsp; | &nbsp;&nbsp;
		<g:message code="application.buildDate.label"/>: <b>${g.meta(name: "build.time") ?: g.message(code: "application.realTimeBuild.label")}</b>
    </div>
    <div class="center" style="line-height: 2em;">
		<g:message code="default.locale.label"/>: &nbsp;
		<!-- show all supported locales -->
		<g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
		<g:each in="${grailsApplication.config.openboxes.locale.supportedLocales}" var="l">
			<g:set var="locale" value="${LocalizationUtil.getLocale(l)}"/>
			<g:set var="selected" value="${locale == (session?.locale ?: session?.user?.locale)}"/>
            <g:set var="localizationModeLocale" value="${new Locale(grailsApplication.config.openboxes.locale.localizationModeLocale)}" />
            %{-- If the locale is the localizationModeLocale and localizationMode is active, create a link to disableLocalizationMode--}%
            %{-- If the locale is the localizationModeLocale and localizationMode is NOT active, create a link to enableLocalizationMode--}%
            %{--If the locale is not the localizationModeLocale, so the localizationMode is also inactive, create a regular link to change the language--}%
            <g:set var="link" value="${(locale == localizationModeLocale) ?
                    (session.useDebugLocale ? createLink(controller: 'user', action: 'disableLocalizationMode') : createLink(controller: 'user', action: 'enableLocalizationMode'))
                    : createLink(controller: 'user', action: 'updateAuthUserLocale', params: ['locale':locale,'targetUri':targetUri,'lang':locale?.language])}" />
            <g:set var="defaultLocale" value="${new Locale(grailsApplication.config.openboxes.locale.defaultLocale)}"/>
			<a class="${selected?'selected':''}" href="${link}">
				<!-- fetch the display for locale based on the current locale -->
				${warehouse.message(code: "locale." + l + ".label", default: locale?.getDisplayName(locale ?: defaultLocale))}
			</a>
		</g:each>
		<g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
			&nbsp;&nbsp; | &nbsp;&nbsp;
			<a href="${createLink(controller: 'user', action: 'updateAuthUserLocale',
				params: ['locale':'debug','targetUri':targetUri])}">
				<g:message code="admin.debug.label"/>:
			</a>
			<b>${session.useDebugLocale ?"on":"off" }</b>
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
                <g:message code="default.dataLoad.label" default="Data load"/>:
                <b>${(request?.actionDuration?:0)/1000}s</b>
            </span>
            &nbsp;&nbsp; | &nbsp;&nbsp;
            <span>
                <g:message code="default.pageLoad.label" default="Page load"/>:
                <b>${(request?.pageLoadInMilliseconds?:0)/1000}s</b>
            </span>
        </g:if>
	</div>
</div>
