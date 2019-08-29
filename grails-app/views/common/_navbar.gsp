<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>

            <a class="navbar-brand" href="#"><i class="glyphicon glyphicon-th"></i> OpenBoxes Analytics</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li><a href="${createLink(uri: '/dashboard/index')}">Dashboard</a></li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Analytics <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="${createLink(uri: '/inventoryBrowser/index')}">Inventory Browser</a></li>
                        <li><a href="${createLink(uri: '/inventorySnapshot/index')}">Inventory Periodic Snapshots</a></li>
                    </ul>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">${session.user.name} <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li>
                            <g:link controller="user" action="show" id="${session.user.id }">
                                <warehouse:message code="profile.label" default="Profile"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link  controller="dashboard" action="index">
                                <warehouse:message code="dashboard.label" default="Dashboard"/>
                            </g:link>
                        </li>
                        <g:isUserAdmin>
                            <g:if test="${session._showTime}">
                                <li class="action-menu-item">
                                    <g:link controller="dashboard" action="index" params="[showTime:'off']">
                                        <warehouse:message code="dashboard.disableShowTime.label" default="Disable show time"/>
                                    </g:link>
                                </li>
                            </g:if>
                            <g:else>
                                <li>
                                    <g:link controller="dashboard" action="index" params="[showTime:'on']">
                                        <warehouse:message code="dashboard.enableShowTime.label" default="Enable show time"/>
                                    </g:link>
                                </li>
                            </g:else>
                            <g:if test="${session.useDebugLocale }">
                                <li>
                                    <g:link controller="user" action="disableDebugMode">
                                        ${warehouse.message(code:'debug.disable.label', default: 'Disable debug mode')}
                                    </g:link>
                                </li>
                            </g:if>
                            <g:else>
                                <li>
                                    <g:link controller="user" action="enableDebugMode">
                                        ${warehouse.message(code:'debug.enable.label', default: 'Enable debug mode')}
                                    </g:link>
                                </li>
                            </g:else>
                            <li>
                                <g:link controller="dashboard" action="flushCache">
                                    ${warehouse.message(code:'cache.flush.label', default: 'Flush cache')}
                                </g:link>
                            </li>
                        </g:isUserAdmin>
                        <li class="divider"></li>
                        <li>
                            <g:link class="list" controller="auth" action="logout">
                                <warehouse:message code="default.logout.label"/>
                            </g:link>
                        </li>


                    </ul>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">${session.warehouse.name} <b class="caret"></b></a>
                    <ul class="dropdown-menu scrollable-menu">
                        <g:each var="entry" in="${session.loginLocationsMap}" status="i">
                            <li class="dropdown-header">
                                <g:if test="${!entry?.key }">
                                    ${warehouse.message(code: 'default.others.label', default: 'Others')}
                                </g:if>
                                <g:else>
                                    ${entry.key }
                                </g:else>
                            </li>
                            <g:each var="warehouse" in="${entry.value.sort() }">
                                <li>
                                    <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
                                    <a class="button" href='${createLink(controller: "dashboard", action:"chooseLocation", id: warehouse.id, params:['targetUri':targetUri])}'>
                                        ${warehouse.name}
                                    </a>
                                </li>
                            </g:each>
                        </g:each>


                    </ul>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Help <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li>
                            <g:link url="http://openboxes.atlassian.net/wiki/questions" target="_blank">
                                <warehouse:message code="docs.faq.label" default="Questions"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link url="https://www.dropbox.com/sh/okkhdne14rju65d/JD9TpTUOt6" target="_blank">
                                <warehouse:message code="docs.userGuide.label" default="User Guide"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link url="https://groups.google.com/forum/#!forum/openboxes" target="_blank">
                                <warehouse:message code="docs.forum.label" default="Forum"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link url="https://github.com/openboxes/openboxes/releases/tag/v${g.meta(name:'app.version')}" target="_blank">
                                <warehouse:message code="docs.releaseNotes.label" default="Release Notes"/> (${g.meta(name:'app.version')})
                            </g:link>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <g:link url="https://openboxes.atlassian.net/secure/CreateIssue!default.jspa" target="_blank">
                                <warehouse:message code="docs.reportBug.label" default="Report a Bug"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link url="mailto:support@openboxes.com" target="_blank">
                                <warehouse:message code="docs.contactSupport.label" default="Contact Support"/>
                            </g:link>
                        </li>
                        <li>
                            <g:link url="mailto:feedback@openboxes.com" data-uv-trigger="contact" target="_blank">
                                <warehouse:message code="docs.provideFeedback.label" default="Provide Feedback"/>
                            </g:link>
                        </li>
                    </ul>
                </li>

            </ul>
        </div><!--/.nav-collapse -->
    </div>

</div>
