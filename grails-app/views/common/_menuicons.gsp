<%@page import="org.pih.warehouse.core.ActivityCode;"%>
<div class="d-none d-${breakPoint}-flex">
    <div class="menu-icon position-relative">
        <div class="tooltip2">
            <i class="ri-search-line" id="global-search-button"></i>
            <span class="tooltiptext2">
                ${warehouse.message(code:'default.search.label', default: 'Search')}
            </span>
        </div>
        <g:globalSearch buttonId="global-search-button" jsonUrl="${request.contextPath}/json/globalSearch"/>
    </div>
    <g:if test="${grailsApplication.config.openboxes.helpscout.widget.enabled}">
    <div class="menu-icon">
        <div class="tooltip2">
            <g:if test="${grailsApplication.config.openboxes.helpscout.widget.enabled}">
                <i class="ri-question-line" onclick="Beacon('toggle');"></i>
            </g:if>
            <g:else>
                <i class="ri-question-line"></i>
            </g:else>
            <span class="tooltiptext2">
                ${warehouse.message(code:'default.support.label', default: 'Help')}
            </span>
        </div>
    </div>
    </g:if>
    <g:if test="${configurationSection}">
    <div class="menu-icon">
        <div class="btn-group">
            <div class="tooltip2">
                <i class="ri-settings-5-line dropdown-toggle"
                   data-toggle="dropdown"
                   aria-haspopup="true"
                   aria-expanded="false">
                </i>
                <div class="dropdown-menu dropdown-menu-right nav-item padding-8 margin-top-18">
                    <div class="dropdown-menu-subsections conf-subsections">
                        <g:each in="${configurationSection?.subsections}" var="subsection">
                            <div class="padding-8">
                                <g:if test="${subsection?.label}">
                                    <span class="subsection-title">${subsection?.label}</span>
                                </g:if>
                                <g:each in="${subsection?.menuItems}" var="item">
                                    <a href="${item?.href}" class="dropdown-item">
                                        ${item?.label}
                                    </a>
                                </g:each>
                            </div>
                        </g:each>
                    </div>
                </div>
                <span class="tooltiptext2">${configurationSection?.label}</span>
            </div>
        </div>
    </div>
    </g:if>
    <div class="menu-icon">
        <div class="btn-group">
            <div class="tooltip2">
                <i class="ri-user-3-line dropdown-toggle"
                   data-toggle="dropdown"
                   aria-haspopup="true"
                   aria-expanded="false">
                </i>
                <div class="dropdown-menu dropdown-menu-right nav-item padding-8 margin-top-18">
                    <span class="subsection-title">${session?.user?.username} (<g:userRole user="${session?.user}"/>)</span>
                    <g:link controller="user" action="edit" id="${session?.user?.id }" class="dropdown-item">
                        <span class="icon">
                            <i class="ri-user-3-line"></i>
                        </span>
                        <g:message code="default.edit.label" args="[g.message(code:'user.profile.label')]" />
                    </g:link>
                    <g:if test="${session?.useDebugLocale}">
                        <g:link controller="user" action="disableLocalizationMode" class="dropdown-item">
                            <span class="icon">
                                <i class="ri-map-pin-line"></i>
                            </span>
                            ${warehouse.message(code:'localization.disable.label', default: 'Disable translation mode')}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="user" action="enableLocalizationMode" class="dropdown-item">
                            <span class="icon">
                                <i class="ri-map-pin-line"></i>
                            </span>
                            ${warehouse.message(code:'localization.enable.label', default: 'Enable translation mode')}
                        </g:link>
                    </g:else>
                    <g:link controller="dashboard" action="flushCache" class="dropdown-item">
                        <span class="icon">
                            <i class="ri-refresh-line"></i>
                        </span>
                        ${warehouse.message(code:'cache.flush.label', default: 'Refresh caches')}
                    </g:link>
                    <g:link controller="auth" action="logout" class="dropdown-item">
                        <span class="icon">
                            <i class="ri-login-box-line"></i>
                        </span>
                        ${warehouse.message(code:'default.logout.label', default: 'Logout')}
                    </g:link>
                </div>
                <span class="tooltiptext2">
                    ${warehouse.message(code:'user.profile.label', default: 'Profile')}
                </span>
            </div>
        </div>
    </div>
</div>
<ul class="navbar-nav d-flex d-${breakPoint}-none flex-column px-3 w-100">
    <li class="nav-item collapsable align-items-center">
        <g:globalSearch jsonUrl="${request.contextPath}/json/globalSearch"/>
    </li>
    <li class="nav-item collapsable align-items-center">
        <button
            data-target="#collapse-profile"
            class="nav-link d-flex justify-content-between align-items-center w-100"
            data-toggle="collapse"
            aria-expanded="false"
            aria-controls="collapse-profile"
        >
            <span class="d-flex align-items-center">
                <i class="ri-user-3-line mr-2"></i>
                ${warehouse.message(code:'user.profile.label', default: 'Profile')}
            </span>
            <i class="ri-arrow-drop-down-line"></i>
        </button>
        <div class="collapse w-100" id="collapse-profile">
            <div class="d-flex flex-wrap">
                <span class="subsection-title">${session?.user?.username} (<g:userRole user="${session?.user}"/>)</span>
                <g:link controller="user" action="edit" id="${session?.user?.id }" class="dropdown-item">
                    <span class="icon">
                        <i class="ri-user-3-line"></i>
                    </span>
                    <g:message code="default.edit.label" args="[g.message(code:'user.profile.label')]" />
                </g:link>
                <g:if test="${session?.useDebugLocale}">
                    <g:link controller="user" action="disableLocalizationMode" class="dropdown-item">
                        <span class="icon">
                            <i class="ri-map-pin-line"></i>
                        </span>
                        ${warehouse.message(code:'localization.disable.label', default: 'Disable translation mode')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="user" action="enableLocalizationMode" class="dropdown-item">
                        <span class="icon">
                            <i class="ri-map-pin-line"></i>
                        </span>
                        ${warehouse.message(code:'localization.enable.label', default: 'Enable translation mode')}
                    </g:link>
                </g:else>
                <g:link controller="dashboard" action="flushCache" class="dropdown-item">
                    <span class="icon">
                        <i class="ri-refresh-line"></i>
                    </span>
                    ${warehouse.message(code:'cache.flush.label', default: 'Refresh caches')}
                </g:link>
                <g:link controller="auth" action="logout" class="dropdown-item">
                    <span class="icon">
                        <i class="ri-login-box-line"></i>
                    </span>
                    ${warehouse.message(code:'default.logout.label', default: 'Logout')}
                </g:link>
            </div>
        </div>
    </li>
    <g:if test="${configurationSection}">
    <li class="nav-item collapsable align-items-center">
        <button
            data-target="#collapse-configuration"
            class="nav-link d-flex justify-content-between align-items-center w-100"
            data-toggle="collapse"
            aria-expanded="false"
            aria-controls="collapse-configuration"
        >
            <span class="d-flex align-items-center">
                <i class="ri-settings-5-line mr-2"></i>
                ${configurationSection?.label}
            </span>
            <i class="ri-arrow-drop-down-line"></i>
        </button>
        <div class="collapse w-100" id="collapse-configuration">
            <div class="d-flex flex-wrap">
                <g:each in="${configurationSection?.subsections}" var="subsection">
                    <div class="padding-8">
                        <g:if test="${subsection?.label}">
                            <span class="subsection-title">${subsection?.label}</span>
                        </g:if>
                        <g:each in="${subsection?.menuItems}" var="item">
                            <a href="${item?.href}" class="dropdown-item">
                                ${item?.label}
                            </a>
                        </g:each>
                    </div>
                </g:each>
            </div>
        </div>
    </li>
    </g:if>
    <g:if test="${grailsApplication.config.openboxes.helpscout.widget.enabled}">
        <li class="nav-item mobile-help-scout dropdown align-items-center" onclick="Beacon('toggle');">
            <a class="d-flex">
                <i class="ri-question-line mr-2" ></i>
                ${warehouse.message(code:'default.support.label', default: 'Help')}
            </a>
        </li>
    </g:if>
</ul>

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
