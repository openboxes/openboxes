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
                                                <img src="${resource(dir: 'images/icons/flags/png', file: 'ht.png')}"/>
                                                ${warehouse.message(code:'localization.enable.label', default: 'Enable translation mode')}
                                            </g:link>
                                        </li>
                                    </g:else>
                                    <li class="action-menu-item">
                                        <g:link controller="dashboard" action="flushCache" style="color: #666">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'database_wrench.png')}"/>
                                            ${warehouse.message(code:'cache.flush.label', default: 'Refresh caches')}
                                        </g:link>
                                    </li>
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
                            <button class="action-hover-btn button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'help.png')}" />
                                <g:message code="default.support.label" default="Support"/>
                            </button>
                            <ul class="actions" style="text-align:left;">
                                <li class="action-menu-item">
                                    <g:link url="https://openboxes.com/community" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'comments.png')}"/>&nbsp;
                                        <warehouse:message code="default.discussionForum.label" default="Discussion Forum"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://docs.openboxes.com/en/develop/" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'book.png')}"/>&nbsp;
                                        <warehouse:message code="docs.userGuide.label" default="User Guide"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://openboxes.com/tutorials" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'film.png')}"/>&nbsp;
                                        <warehouse:message code="default.tutorials.label" default="Tutorials"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://openboxes.com/roadmap" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'date.png')}"/>&nbsp;
                                        <warehouse:message code="default.roadmap.label" default="RoadMap"/>
                                    </g:link>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://github.com/openboxes/openboxes/releases/tag/v${g.meta(name:'app.version')}" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'package.png')}"/>&nbsp;
                                        <warehouse:message code="docs.releaseNotes.label" default="Release Notes"/>
                                    </g:link>
                                </li>
                                <li>
                                    <hr/>
                                </li>
                                <li class="action-menu-item">
                                    <g:link url="https://openboxes.com/support" style="color: #666;" target="_blank">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'help.png')}"/>&nbsp;
                                        <warehouse:message code="default.generalSupport.label" default="General Support"/>
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
