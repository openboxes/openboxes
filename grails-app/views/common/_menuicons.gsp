<%@page import="org.pih.warehouse.core.ActivityCode;"%>
<div class="d-none d-md-flex">
    <div class="menu-icon position-relative">
        <div class="tooltip2">
            <i class="ri-search-line" id="global-search-button"></i>
            <span class="tooltiptext2">Search</span>
        </div>
        <g:globalSearch buttonId="global-search-button" jsonUrl="${request.contextPath}/json/globalSearch"/>
    </div>
    <div class="menu-icon">
        <div class="tooltip2">
            <g:if test="${grailsApplication.config.openboxes.helpscout.widget.enabled}">
                <i class="ri-question-line" onclick="Beacon('toggle');"></i>
            </g:if>
            <g:else>
                <i class="ri-question-line"></i>
            </g:else>
            <span class="tooltiptext2">Help</span>
        </div>
    </div>
    <g:isUserAdmin>
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
                        <g:if test="${grailsApplication.config.openboxes.megamenu.configuration.enabled}">
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="admin.label" default="Admin" /></span>
                                <g:supports activityCode="${ActivityCode.MANAGE_INVENTORY}">
                                    <g:link controller="admin" action="showSettings" class="list dropdown-item">
                                        <g:message code="default.settings.label"/>
                                    </g:link>
                                </g:supports>

                                <g:link controller="admin" action="cache" class="list dropdown-item">
                                    <g:message code="default.cache.label" default="Cache" />
                                </g:link>

                                <g:link controller="console" action="index" class="list dropdown-item">
                                    <g:message code="default.console.label" default="Console" />
                                </g:link>

                                <g:link controller="batch" action="importData" class="dropdown-item">
                                    <warehouse:message code="default.import.label"
                                                       args="[warehouse.message(code:'default.data.label', default: 'Data')]"
                                    />
                                </g:link>

                                <g:link controller="migration" action="index" class="list dropdown-item">
                                    <g:message code="default.dataMigration.label" default="Data Migration" />
                                </g:link>

                                <g:link controller="admin" action="sendMail" class="list dropdown-item">
                                    <g:message code="config.sendMail.label" default="Email"/>
                                </g:link>

                                <g:link controller="localization" action="list" class="list">
                                    <g:message code="localization.label" default="Localization"/>
                                </g:link>
                            </div>
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="parties.label" default="Parties" /></span>
                                <g:link controller="location" action="list" class="location dropdown-item">
                                    <warehouse:message code="locations.label" />
                                </g:link>
                                <g:link controller="locationGroup" action="list" class="site dropdown-item">
                                    <warehouse:message code="locationGroups.label" default="Location Groups" />
                                </g:link>
                                <g:link controller="locationType" action="list"
                                        class="locationType dropdown-item">
                                    <warehouse:message code="location.locationTypes.label" default="Location Types" />
                                </g:link>
                                <g:link controller="organization" action="list" class="dropdown-item">
                                    <warehouse:message code="organizations.label" default="Organizations" />
                                </g:link>
                                <g:link controller="partyRole" action="list" class="dropdown-item">
                                    <warehouse:message code="partyRoles.label" default="Party Roles" />
                                </g:link>
                                <g:link controller="partyType" action="list" class="dropdown-item">
                                    <warehouse:message code="partyTypes.label" default="Party Types" />
                                </g:link>
                                <g:link controller="person" action="list" class="people dropdown-item">
                                    <warehouse:message code="person.list.label" />
                                </g:link>
                                <g:link controller="role" action="list" class="role dropdown-item">
                                    <warehouse:message code="roles.label" />
                                </g:link>
                                <g:link controller="user" action="list" class="user dropdown-item">
                                    <warehouse:message code="users.label" />
                                </g:link>
                            </div>
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="transactions.label" default="Transasctions" /></span>
                                <g:link controller="transactionType" class="dropdown-item">
                                    <warehouse:message code="transactionTypes.label" default="Transaction Types"/>
                                </g:link>
                                <g:link controller="inventory" action="listAllTransactions" class="dropdown-item">
                                    <warehouse:message code="transactions.label"/>
                                </g:link>
                                <g:link controller="inventory" action="editTransaction" class="create dropdown-item">
                                    <warehouse:message code="transaction.add.label"/>
                                </g:link>
                                <g:link controller="batch" action="importData" params="[type:'inventory']" class="inventory dropdown-item">
                                    <warehouse:message code="default.import.label" args="[warehouse.message(code:'inventory.label', default: 'Inventory')]"/>
                                </g:link>
                                <g:link controller="batch" action="importData" params="[type:'inventoryLevel']" class="inventory dropdown-item">
                                    <warehouse:message code="default.import.label" args="[warehouse.message(code:'inventoryLevel.label', default: 'Inventory levels')]"/>
                                </g:link>
                            </div>


                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="other.label" default="Other" /></span>
                                <g:link controller="budgetCode" action="list" class="dropdown-item">
                                    <warehouse:message code="budgetCode.label" default="Budget Codes"/>
                                </g:link>
                                <g:link controller="containerType" action="list" class="dropdown-item">
                                    <warehouse:message code="containerTypes.label" default="Container Types"/>
                                </g:link>
                                <g:link controller="document" action="list" class="dropdown-item">
                                    <warehouse:message code="documents.label" default="Documents" />
                                </g:link>
                                <g:link controller="documentType" action="list" class="dropdown-item">
                                    <warehouse:message code="documentTypes.label" default="Document Types"/>
                                </g:link>
                                <g:link controller="eventType" action="list"
                                        class="eventType dropdown-item">
                                    <warehouse:message code="location.eventTypes.label" default="Event Types" />
                                </g:link>
                                <g:link controller="glAccountType" action="list" class="dropdown-item">
                                    <warehouse:message code="glAccountType.label" default="GL Account Type"/>
                                </g:link>
                                <g:link controller="glAccount" action="list" class="dropdown-item">
                                    <warehouse:message code="glAccount.label" default="GL Account"/>
                                </g:link>
                                <g:link controller="orderAdjustmentType" action="list" class="dropdown-item">
                                    <warehouse:message code="orderAdjustmentType.label" default="Order Adjustment Type"/>
                                </g:link>
                                <g:link controller="paymentMethodType" action="list" class="dropdown-item">
                                    <warehouse:message code="paymentMethodTypes.label" />
                                </g:link>
                                <g:link controller="paymentTerm" action="list" class="dropdown-item">
                                    <warehouse:message code="paymentTerms.label" />
                                </g:link>
                                <g:link controller="preferenceType" action="list" class="dropdown-item">
                                    <warehouse:message code="preferenceType.label" />
                                </g:link>
                                <g:link controller="shipper" action="list" class="shipper dropdown-item">
                                    <warehouse:message code="location.shippers.label" />
                                </g:link>
                                <g:link controller="shipmentWorkflow" action="list" class="dropdown-item">
                                    <warehouse:message code="shipmentWorkflows.label" default="Shipment Workflows" />
                                </g:link>
                                <g:link controller="productsConfiguration" action="index" class="dropdown-item">
                                    <warehouse:message code="productsConfiguration.label" default="Categories and Products Configuration" />
                                </g:link>
                                <g:link controller="locationsConfiguration" action="index" class="dropdown-item" >
                                    <warehouse:message code="locationsConfiguration.label" default="Locations Configuration" />
                                </g:link>
                                <g:link controller="loadData" action="index" class="dropdown-item">
                                    <warehouse:message code="loadData.label" default="Load Data" />
                                </g:link>
                                <g:link controller="resettingInstanceInfo" action="index" class="dropdown-item">
                                    <warehouse:message code="resetInstance.label" default="Reset your instance" />
                                </g:link>
                            </div>
                        </g:if>
                    </div>
                </div>
                <span class="tooltiptext2">Configuration</span>
            </div>
        </div>

    </div>
    </g:isUserAdmin>
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
                    <g:if test="${session?.useDebugLocale }">
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
                <span class="tooltiptext2">Profile</span>
            </div>
        </div>
    </div>
</div>
<ul class="navbar-nav d-flex d-md-none flex-column px-3 w-100">
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
                Profile
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
                <g:if test="${session?.useDebugLocale }">
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
    <g:isUserAdmin>
        <g:if test="${grailsApplication.config.openboxes.megamenu.configuration.enabled}">
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
                        Configuration
                    </span>
                    <i class="ri-arrow-drop-down-line"></i>
                </button>
                <div class="collapse w-100" id="collapse-configuration">
                    <div class="d-flex flex-wrap">
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="admin.label" default="Admin" /></span>
                            <g:supports activityCode="${ActivityCode.MANAGE_INVENTORY}">
                                <g:link controller="admin" action="showSettings" class="list dropdown-item">
                                    <g:message code="default.settings.label"/>
                                </g:link>
                            </g:supports>

                            <g:link controller="admin" action="cache" class="list dropdown-item">
                                <g:message code="default.cache.label" default="Cache" />
                            </g:link>

                            <g:link controller="console" action="index" class="list dropdown-item">
                                <g:message code="default.console.label" default="Console" />
                            </g:link>

                            <g:link controller="batch" action="importData" class="dropdown-item">
                                <warehouse:message code="default.import.label"
                                                   args="[warehouse.message(code:'default.data.label', default: 'Data')]"
                                />
                            </g:link>

                            <g:link controller="migration" action="index" class="list dropdown-item">
                                <g:message code="default.dataMigration.label" default="Data Migration" />
                            </g:link>

                            <g:link controller="admin" action="sendMail" class="list dropdown-item">
                                <g:message code="config.sendMail.label" default="Email"/>
                            </g:link>

                            <g:link controller="localization" action="list" class="list">
                                <g:message code="localization.label" default="Localization"/>
                            </g:link>
                        </div>
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="parties.label" default="Parties" /></span>
                            <g:link controller="location" action="list" class="location dropdown-item">
                                <warehouse:message code="locations.label" />
                            </g:link>
                            <g:link controller="locationGroup" action="list" class="site dropdown-item">
                                <warehouse:message code="locationGroups.label" default="Location Groups" />
                            </g:link>
                            <g:link controller="locationType" action="list"
                                    class="locationType dropdown-item">
                                <warehouse:message code="location.locationTypes.label" default="Location Types" />
                            </g:link>
                            <g:link controller="organization" action="list" class="dropdown-item">
                                <warehouse:message code="organizations.label" default="Organizations" />
                            </g:link>
                            <g:link controller="partyRole" action="list" class="dropdown-item">
                                <warehouse:message code="partyRoles.label" default="Party Roles" />
                            </g:link>
                            <g:link controller="partyType" action="list" class="dropdown-item">
                                <warehouse:message code="partyTypes.label" default="Party Types" />
                            </g:link>
                            <g:link controller="person" action="list" class="people dropdown-item">
                                <warehouse:message code="person.list.label" />
                            </g:link>
                            <g:link controller="role" action="list" class="role dropdown-item">
                                <warehouse:message code="roles.label" />
                            </g:link>
                            <g:link controller="user" action="list" class="user dropdown-item">
                                <warehouse:message code="users.label" />
                            </g:link>
                        </div>
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="transactions.label" default="Transasctions" /></span>
                            <g:link controller="transactionType" class="dropdown-item">
                                <warehouse:message code="transactionTypes.label" default="Transaction Types"/>
                            </g:link>
                            <g:link controller="inventory" action="listAllTransactions" class="dropdown-item">
                                <warehouse:message code="transactions.label"/>
                            </g:link>
                            <g:link controller="inventory" action="editTransaction" class="create dropdown-item">
                                <warehouse:message code="transaction.add.label"/>
                            </g:link>
                            <g:link controller="batch" action="importData" params="[type:'inventory']" class="inventory dropdown-item">
                                <warehouse:message code="default.import.label" args="[warehouse.message(code:'inventory.label', default: 'Inventory')]"/>
                            </g:link>
                            <g:link controller="batch" action="importData" params="[type:'inventoryLevel']" class="inventory dropdown-item">
                                <warehouse:message code="default.import.label" args="[warehouse.message(code:'inventoryLevel.label', default: 'Inventory levels')]"/>
                            </g:link>
                        </div>
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="other.label" default="Other" /></span>
                            <g:link controller="budgetCode" action="list" class="dropdown-item">
                                <warehouse:message code="budgetCode.label" default="Budget Codes"/>
                            </g:link>
                            <g:link controller="containerType" action="list" class="dropdown-item">
                                <warehouse:message code="containerTypes.label" default="Container Types"/>
                            </g:link>
                            <g:link controller="document" action="list" class="dropdown-item">
                                <warehouse:message code="documents.label" default="Documents" />
                            </g:link>
                            <g:link controller="documentType" action="list" class="dropdown-item">
                                <warehouse:message code="documentTypes.label" default="Document Types"/>
                            </g:link>
                            <g:link controller="eventType" action="list"
                                    class="eventType dropdown-item">
                                <warehouse:message code="location.eventTypes.label" default="Event Types" />
                            </g:link>
                            <g:link controller="glAccountType" action="list" class="dropdown-item">
                                <warehouse:message code="glAccountType.label" default="GL Account Type"/>
                            </g:link>
                            <g:link controller="glAccount" action="list" class="dropdown-item">
                                <warehouse:message code="glAccount.label" default="GL Account"/>
                            </g:link>
                            <g:link controller="orderAdjustmentType" action="list" class="dropdown-item">
                                <warehouse:message code="orderAdjustmentType.label" default="Order Adjustment Type"/>
                            </g:link>
                            <g:link controller="paymentMethodType" action="list" class="dropdown-item">
                                <warehouse:message code="paymentMethodTypes.label" />
                            </g:link>
                            <g:link controller="paymentTerm" action="list" class="dropdown-item">
                                <warehouse:message code="paymentTerms.label" />
                            </g:link>
                            <g:link controller="preferenceType" action="list" class="dropdown-item">
                                <warehouse:message code="preferenceType.label" />
                            </g:link>
                            <g:link controller="shipper" action="list" class="shipper dropdown-item">
                                <warehouse:message code="location.shippers.label" />
                            </g:link>
                            <g:link controller="shipmentWorkflow" action="list" class="dropdown-item">
                                <warehouse:message code="shipmentWorkflows.label" default="Shipment Workflows" />
                            </g:link>
                            <g:link controller="productsConfiguration" action="index" class="dropdown-item">
                                <warehouse:message code="productsConfiguration.label" default="Categories and Products Configuration" />
                            </g:link>
                            <g:link controller="locationsConfiguration" action="index" class="dropdown-item" >
                                <warehouse:message code="locationsConfiguration.label" default="Locations Configuration" />
                            </g:link>
                            <g:link controller="loadData" action="index" class="dropdown-item">
                                <warehouse:message code="loadData.label" default="Load Data" />
                            </g:link>
                            <g:link controller="resettingInstanceInfo" action="index" class="dropdown-item">
                                <warehouse:message code="resetInstance.label" default="Reset your instance" />
                            </g:link>
                        </div>
                    </div>
                </div>
            </li>
        </g:if>
    </g:isUserAdmin>
    <g:if test="${grailsApplication.config.openboxes.helpscout.widget.enabled}">
        <li class="nav-item mobile-help-scout dropdown align-items-center" onclick="Beacon('toggle');">
            <a class="d-flex">
                <i class="ri-question-line mr-2" ></i>Help
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
