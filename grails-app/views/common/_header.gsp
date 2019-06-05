<%@ page import="org.pih.warehouse.core.RoleType" %>
<!-- Block which includes the logo and login banner -->
<div id="header" class="yui-b">
	<div id="banner" class="yui-gd">
        <div class="yui-u first" >
            <g:displayLogo location="${session?.warehouse?.id}" includeLink="${true}"/>
        </div>

        <div class="yui-u">
            <div id="loggedIn">
                <ul>
                    <li>
                        <g:globalSearch id="globalSearch" cssClass="globalSearch" name="searchTerms" size="300"
                                        jsonUrl="${request.contextPath }/json/globalSearch"></g:globalSearch>
                    </li>
                    <g:if test="${session.user}">
                        <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>

                        <li>
                            <span class="action-menu" >
                                <button class="action-hover-btn button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'user.png')}"/>
                                    <span id="username">${session?.user?.name}</span>
                                    <span id="userrole">[<g:userRole user="${session.user}"/>]</span>

                                </button>
                                <ul class="actions" style="text-align:left;">
                                    <li class="action-menu-item">
                                        <g:link controller="user" action="edit" id="${session.user.id }" style="color: #666;">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'user.png')}"/>
                                            <g:message code="default.edit.label" args="[g.message(code:'user.profile.label')]"></g:message>
                                        </g:link>
                                    </li>
                                    <li class="action-menu-item">
                                        <g:link  controller="dashboard" action="index" style="color: #666;">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'application_view_tile.png')}"/>
                                            <warehouse:message code="dashboard.label" default="Dashboard"/>
                                        </g:link>
                                    </li>

                                    <g:if test="${session._showTime}">
                                        <li class="action-menu-item">
                                            <g:link controller="dashboard" action="index" params="[showTime:'off']" style="color: #666;">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'clock_delete.png')}"/>
                                                <warehouse:message code="dashboard.disableResponseTime.label" default="Hide response time"/>
                                            </g:link>
                                        </li>
                                    </g:if>
                                    <g:else>
                                        <li class="action-menu-item">
                                            <g:link controller="dashboard" action="index" params="[showTime:'on']" style="color: #666;">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'clock_add.png')}"/>
                                                <warehouse:message code="dashboard.enableResponseTime.label" default="Enable response time"/>
                                            </g:link>
                                        </li>
                                    </g:else>
                                    <g:if test="${session.useDebugLocale }">
                                        <li class="action-menu-item">
                                            <g:link controller="user" action="disableLocalizationMode" style="color: #666;">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'bug_delete.png')}"/>
                                                ${warehouse.message(code:'localization.disable.label', default: 'Disable translation mode')}
                                            </g:link>
                                        </li>
                                    </g:if>
                                    <g:else>
                                        <li class="action-menu-item">
                                            <g:link controller="user" action="enableLocalizationMode" style="color: #666;">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'flag_france.png')}"/>
                                                ${warehouse.message(code:'localization.enable.label', default: 'Enable translation mode')}
                                            </g:link>
                                        </li>
                                    </g:else>
                                    <li class="action-menu-item">
                                        <g:link controller="dashboard" action="flushMegamenu" style="color: #666">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'page_white_refresh.png')}"/>
                                            ${warehouse.message(code:'cache.flush.label', default: 'Refresh megamenu')}
                                        </g:link>
                                    </li>
                                    <li class="action-menu-item">
                                        <g:link controller="dashboard" action="flushCache" style="color: #666">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'database_refresh.png')}"/>
                                            ${warehouse.message(code:'cache.flush.label', default: 'Refresh data caches')}
                                        </g:link>
                                    </li>

                                    <g:if test="${session?.warehouse}">
                                        <%--
                                        <li class="action-menu-item">
                                            <g:link controller="dashboard" action="chooseLocation" style="color: #666">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}"/>
                                                <warehouse:message code="dashboard.changeLocation.label" default="Change location"/>
                                            </g:link>
                                        </li>
                                        --%>
                                        <li class="action-menu-item">
                                            <a href="javascript:void(0);" class="btn-show-dialog"
                                               data-title="${g.message(code:'dashboard.chooseLocation.label')}"
                                               data-url="${request.contextPath}/dashboard/changeLocation?targetUri=${targetUri}"
                                               style="color: #666">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}"/>
                                                <warehouse:message code="dashboard.changeLocation.label" default="Change location"/>
                                            </a>
                                        </li>
                                    </g:if>
                                    <li class="action-menu-item">
                                        <g:link class="list" controller="auth" action="logout" style="color:#666">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'door.png')}" class="middle"/>
                                            <warehouse:message code="default.logout.label"/>
                                        </g:link>
                                    </li>
                                </ul>
                            </span>
                        </li>
                        <g:if test="${session?.warehouse}">
                            <li>
                                <button class="btn-show-dialog button"
                                        data-title="${g.message(code:'dashboard.chooseLocation.label')}"
                                        data-url="${request.contextPath}/dashboard/changeLocation?targetUri=${targetUri}">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}" />
                                    ${session?.warehouse?.name }
                                </button>
                            </li>
                        </g:if>

                    </g:if>

                    <li>
                        <span class="action-menu">
                            <img src="${resource(dir: 'images/icons/silk', file: 'help.png')}" class="action-hover-btn"/>
                            <ul class="actions" style="text-align:left;">

                                <li class="action-menu-item">
                                    <g:link url="http://openboxes.readthedocs.org/en/latest/" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>
                                        <warehouse:message code="docs.userGuide.label" default="User Guide"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="http://openboxes.atlassian.net/wiki/questions" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'help.png')}"/>
                                        <warehouse:message code="docs.faq.label" default="FAQ"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://groups.google.com/forum/#!forum/openboxes" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'comment.png')}"/>
                                        <warehouse:message code="docs.forum.label" default="Forum"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://github.com/openboxes/openboxes/releases/tag/v${g.meta(name:'app.version')}" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}"/>
                                        <warehouse:message code="docs.releaseNotes.label" default="Release Notes"/> (${g.meta(name:'app.version')})
                                    </g:link>
                                </li>
                                <li>
                                    <hr/>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://openboxes.atlassian.net/secure/CreateIssue!default.jspa" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'bug.png')}"/>
                                        <warehouse:message code="docs.reportBug.label" default="Report a Bug"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="mailto:support@openboxes.com" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'email.png')}"/>
                                        <warehouse:message code="docs.contactSupport.label" default="Email Support"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="mailto:feedback@openboxes.com" data-uv-trigger="contact" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'lightbulb.png')}"/>
                                        <warehouse:message code="docs.provideFeedback.label" default="Request a New Feature"/>
                                    </g:link>
                                </li>
                            </ul>
                        </span>
                    </li>
                </ul>
            </div>
        </div>
	</div>
</div>
