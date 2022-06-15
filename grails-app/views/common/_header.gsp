<%@ page import="org.pih.warehouse.api.LocalizationApiController; org.pih.warehouse.core.RoleType" %>
<!-- Block which includes the logo and login banner -->
<g:if test="${grailsApplication.config.openboxes.helpscout.widget.enabled}">
    <!--
      -- Place magical HelpScout incantations early to make
      -- window.Beacon accessible to any included divs, etc.
      -->
    <script type="text/javascript">
      !function (e, t, n) {
        function a() {
          var e = t.getElementsByTagName("script")[0],
            n = t.createElement("script");
          n.type = "text/javascript", n.async = !0, n.src = "https://beacon-v2.helpscout.net", e.parentNode.insertBefore(n, e)
        }

        if (e.Beacon = n = function (t, n, a) {
          e.Beacon.readyQueue.push({
            method: t,
            options: n,
            data: a
          })
        }, n.readyQueue = [], "complete" === t.readyState) return a();
        e.attachEvent ? e.attachEvent("onload", a) : e.addEventListener("load", a, !1)
      }(window, document, window.Beacon || function () {
      });
    </script>
    <!-- end magical HelpScout incantations -->

    <!-- now, configure the widget the preceding code created for us -->
    <script type="text/javascript">
      let configUrl = new URL('/openboxes/api/helpscout/configuration/', window.location.href)
      fetch(configUrl.toString(), { credentials: 'same-origin' })
        .then(response => response.json())
        .then(configJson => {
          window.Beacon('init', configJson.localizedHelpScoutKey);
          window.Beacon('config', configJson)
        })
    </script>
</g:if>

<div id="header" class="yui-b">
	<div id="banner" class="yui-gd">
        <div class="yui-u first" >
            <g:displayLogo location="${session?.warehouse?.id}" includeLink="${true}"/>
        </div>

        <div class="yui-u">
            <div id="loggedIn">
                <ul class="banner-menu">
                    <li>
                        <g:globalSearch id="globalSearch" cssClass="globalSearch" name="searchTerms" size="300"
                                        jsonUrl="${request.contextPath }/json/globalSearch"></g:globalSearch>
                    </li>
                    <g:if test="${session.user}">
                        <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>

                        <li>
                            <span class="action-menu">
                                <button class="action-hover-btn button top">
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
                                <button class="btn-show-dialog button top"
                                        data-title="${g.message(code:'dashboard.chooseLocation.label')}" data-height="300"
                                        data-url="${request.contextPath}/dashboard/changeLocation?targetUri=${targetUri}">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}" />
                                    ${session?.warehouse?.name }
                                </button>
                            </li>
                        </g:if>
                    </g:if>
                    <g:if test="${grailsApplication.config.openboxes.helpscout.widget.enabled}">
                        <li>
                            <button class="action-hover-btn button helpscout top" onclick="Beacon('toggle');">
                                <i class="far fa-life-ring"></i>
                                <warehouse:message code="default.support.label" default="Support" />
                            </button>
                        </li>
                    </g:if>
                </ul>
            </div>
        </div>
	</div>
</div>
