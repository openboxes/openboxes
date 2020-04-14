<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.User" %>
<%@ page import="org.pih.warehouse.core.Role" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="admin.title" default="Settings" /></title>
    </head>
    <body>
		<div id="settings" role="main" class="yui-ga">

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
			<div class="yui-u first">
                <div class="tabs">
                    <ul>
                        <li>
                            <a href="#tabs-1" id="general-settings-tab">
                                <warehouse:message code="admin.generalSettings.header"/></a>
                        </li>
                        <li>
                            <a href="#tabs-2" id="email-settings-tab">
                                <warehouse:message code="admin.emailSettings.header"/></a>
                        </li>
                        <li>
                            <a href="#tabs-3" id="external-config-tab">
                                <warehouse:message code="admin.externalAppConfig.header"/></a>
                        </li>
                        <li>
                            <a href="#tabs-4" id="system-properties-tab">
                                <warehouse:message code="admin.systemProperties.header"/></a>
                        </li>
                        <li>
                            <a href="#tabs-5" id="printers-tab">
                                <warehouse:message code="admin.printers.header" default="Printers"/></a>
                        </li>
                        <li>
                            <a href="#tabs-6" id="background-jobs-tab">
                                <warehouse:message code="admin.backgroundJobs.header"/></a>
                        </li>
                        <li>
                            <a href="#tabs-7" id="cache-tab">
                                <warehouse:message code="admin.cache.header" default="Caches"/></a>
                        </li>
                        <li>
                            <a href="${request.contextPath}/admin/showDatabaseStatus">
                                <warehouse:message code="admin.database.status.label" default="Database Status"/></a></li>
                        </li>
                        <li>
                            <a href="${request.contextPath}/admin/showDatabaseProcessList">
                                <warehouse:message code="admin.database.processListlabel" default="Database Process List"/></a></li>
                        </li>
                    </ul>
                    <div id="tabs-1">
                        <table>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="application.environment.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    ${grails.util.GrailsUtil.environment}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="application.version.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:meta name="app.version"/> &nbsp;
                                    <g:if test="${User.get(session?.user?.id)?.roles?.contains(Role.findByRoleType('ROLE_ADMIN'))}">
                                        <g:link controller="admin" action="showUpgrade"><warehouse:message code="application.upgrade.label"/></g:link>
                                    </g:if>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="application.buildNumber.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:meta name="app.buildNumber"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="application.buildDate.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:meta name="app.buildDate"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="application.revisionNumber.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:meta name="app.revisionNumber"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="application.grailsVersion.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:meta name="app.grails.version"></g:meta>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="default.date.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    ${new Date() }
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label>
                                        <warehouse:message code="default.locale.label"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:each in="${grailsApplication.config.openboxes.locale.supportedLocales}" var="l">
                                        <g:set var="locale" value="${new Locale(l)}"/>
                                        <g:if test="${session?.user?.locale==locale}">
                                            ${locale?.getDisplayName(session?.user?.locale ?: new Locale(grailsApplication.config.openboxes.locale.defaultLocale))}
                                        </g:if>
                                        <g:else>
                                            <a href="${createLink(controller: 'user', action: 'updateAuthUserLocale', params: ['locale':locale,'returnUrl':request.forwardURI])}">
                                                <!-- fetch the display for locale based on the current locale -->
                                                ${locale?.getDisplayName(session?.user?.locale ?: new Locale(grailsApplication.config.openboxes.locale.defaultLocale))}
                                            </a>
                                        </g:else>
                                        &nbsp;|&nbsp;
                                    </g:each>
                                    <g:isUserInRole roles="[RoleType.ROLE_ADMIN,RoleType.ROLE_BROWSER]">
                                        <g:if test="${session?.user?.locale==new Locale('debug')}">
                                            Debug
                                        </g:if>
                                        <g:else>
                                            <a href="${createLink(controller: 'user', action: 'updateAuthUserLocale', params: ['locale':'debug','returnUrl':request.forwardURI])}">
                                                Debug
                                            </a>
                                        </g:else>
                                    </g:isUserInRole>
                                </td>
                            <tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="application.defaultCharset.label"/></label>
                                </td>
                                <td>
                                    ${java.nio.charset.Charset.defaultCharset()}
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div id="tabs-2">
                        <table>
                            <g:each in="${grailsApplication.config.grails.mail}" var="property">
                                <tr class="prop">
                                    <td class="name">
                                        <label>${property.key}</label>
                                    </td>
                                    <td>
                                        <g:if test="${property.key?.contains('password') && property.value }">
                                            ${util.StringUtil.mask(property.value, "*")}
                                        </g:if>
                                        <g:else>
                                            ${property.value }
                                        </g:else>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                    <div id="tabs-3">
                        <table>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="admin.externalConfigFile.label"/></label>
                                </td>
                                <td>
                                    ${grailsApplication.config.grails.config.locations }
                                </td>
                            </tr>
                            <g:each in="${externalConfigProperties }" var="externalProperty">
                                <g:each var="property" in="${externalProperty }">
                                    <tr class="prop">
                                        <td class="name">
                                            <label>${property.key }</label>
                                        </td>
                                        <td class="value">
                                            <g:if test="${property?.key?.contains('password') && property.value}">
                                                ${util.StringUtil.mask(property?.value, "*")}
                                            </g:if>
                                            <g:else>
                                                ${property.value }
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:each>
                            </g:each>
                        </table>
                    </div>
                    <div id="tabs-4">
                        <table>
                            <g:each in="${systemProperties}" var="prop">
                                <tr class="prop">
                                    <td class="name">
                                        ${prop.key }
                                    </td>
                                    <td class="value">
                                        ${prop.value }
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                    <div id="tabs-5">
                        <div class="box">
                            <h2><g:message code="default.printers.label" default="Printers"/></h2>
                            <table>
                                <tr>
                                    <th><g:message code="default.name.label"/></th>
                                    <th><g:message code="printer.attributes.label" default="Attributes"/></th>
                                    <th><g:message code="printer.supportedDocFlavors.label" default="Doc Flavors"/></th>
                                    <th><g:message code="printer.supportedAttributeCategories.label" default="Attribute Categories"/></th>
                                </tr>
                                <g:each in="${printServices}" var="printService">
                                    <tr class="prop">
                                        <td>${printService.name}</td>
                                        <td>
                                            <table>
                                                <g:each var="attribute" in="${printService.attributes.toArray()}">
                                                    <tr>
                                                        <td>
                                                            <label>${attribute.name}</label>
                                                        </td>
                                                        <td>
                                                            ${attribute}
                                                        </td>
                                                    </tr>
                                                </g:each>
                                            </table>
                                        </td>
                                        <td>
                                            <div style="overflow: auto; max-height: 200px;">
                                                <table>
                                                    <g:each var="supportedDocFlavor" in="${printService.supportedDocFlavors}">
                                                        <tr>
                                                            <td>${supportedDocFlavor.mimeType}</td>
                                                        </tr>
                                                    </g:each>
                                                </table>
                                            </div>
                                        </td>
                                        <td>
                                            <div style="overflow: auto; max-height: 200px;">
                                                <table>
                                                <g:each var="supportedAttributeCategory" in="${printService.supportedAttributeCategories}">
                                                    <tr>
                                                        <td>
                                                            ${supportedAttributeCategory.name}
                                                        </td>
                                                        <td>
                                                            ${printService.getDefaultAttributeValue(supportedAttributeCategory)}
                                                        </td>
                                                    </tr>
                                                </g:each>
                                                </table>
                                            </div>
                                        </td>
                                    </tr>
                                </g:each>
                            </table>
                        </div>
                    </div>
                    <div id="tabs-6">
                        <div class="box">
                            <h2> ${quartzScheduler.schedulerName} ${quartzScheduler.schedulerInstanceId}</h2>
                            <table>
                                <tr class="prop">
                                    <td class="name">
                                        <label><warehouse:message code="jobs.scheduler.metadata.label" default="Scheduler Metadata"/></label>
                                    </td>
                                    <td class="value">
                                        <pre>${quartzScheduler.metaData}</pre>
                                    </td>
                                </tr>

                                <g:each var="externalProperty" in="${externalConfigProperties}" >
                                    <g:each var="property" in="${externalProperty}">
                                        <g:if test="${property?.key?.contains('jobs')}">
                                            <tr class="prop">
                                                <td class="name">
                                                    <label>${property.key }</label>
                                                </td>
                                                <td class="value">
                                                    <g:if test="${property?.key?.contains('password') && property.value}">
                                                        ${util.StringUtil.mask(property?.value, "*")}
                                                    </g:if>
                                                    <g:else>
                                                        ${property.value }
                                                    </g:else>
                                                </td>
                                            </tr>
                                        </g:if>
                                    </g:each>
                                </g:each>
                                <tr class="prop">
                                    <td class="name">
                                        <label>
                                            <warehouse:message code="admin.calculateHistoricalQuantityJob.status"
                                                               default="Calculate Historical Quantity Job Status">
                                            </warehouse:message>
                                        </label>
                                    </td>
                                    <td class="value">
                                        <span id="jobStatus"></span>
                                        <g:remoteLink class="button" controller="json" action="statusCalculateHistoricalQuantityJob" update="jobStatus">Show Status</g:remoteLink>
                                        <g:remoteLink class="button" controller="json" action="enableCalculateHistoricalQuantityJob">Enable</g:remoteLink>
                                        <g:remoteLink class="button" controller="json" action="disableCalculateHistoricalQuantityJob">Disable</g:remoteLink>

                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="name">
                                        <label>
                                            <warehouse:message code="jobs.sendStockAlertsJob.label"
                                                               default="Send Stock Alerts Job">
                                            </warehouse:message>
                                        </label>
                                    </td>
                                    <td class="value">
                                        <g:link class="button" controller="admin" action="triggerStockAlerts">
                                            <warehouse:message code="default.button.trigger.label" default="Trigger"/>
                                        </g:link>
                                    </td>
                                </tr>
                                <tfoot>
                                    <tr>
                                        <td colspan="2">
                                            <div class="buttons">
                                                <g:link controller="jobs" action="index" class="button">
                                                    ${g.message(code:'jobs.backgroundJobs.label', default: 'Background Jobs')}
                                                </g:link>
                                            </div>
                                        </td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>
                    </div>
                    <div id="tabs-7">
                        <div class="box">
                            <table>
                                <tr>
                                    <th>name</th>
                                    <th>status</th>
                                    <th>eternal</th>
                                    <th>overflowToDisk</th>
                                    <th>maxElementsInMemory</th>
                                    <th>maxElementsOnDisk</th>
                                    <th>memoryStoreEvictionPolicy</th>
                                    <th>timeToLiveSeconds</th>
                                    <th>timeToIdleSeconds</th>
                                    <th>diskPersistent</th>
                                    <th>diskExpiryThreadIntervalSeconds</th>
                                </tr>
                            <g:each in="${caches}" var="cache">
                                <tr>
                                    <td>${cache?.name}</td>
                                    <td>${(cache?.disabled)?'disabled':'enabled'}</td>
                                    <td>${cache.cacheConfiguration.eternal}</td>
                                    <td>${cache.cacheConfiguration.overflowToDisk}</td>
                                    <td>${cache.cacheConfiguration.maxElementsInMemory}</td>
                                    <td>${cache.cacheConfiguration.maxElementsOnDisk}</td>
                                    <td>${cache.cacheConfiguration.memoryStoreEvictionPolicy}</td>
                                    <td>${cache.cacheConfiguration.timeToLiveSeconds}</td>
                                    <td>${cache.cacheConfiguration.timeToIdleSeconds}</td>
                                    <td>${cache.cacheConfiguration.diskPersistent}</td>
                                    <td>${cache.cacheConfiguration.diskExpiryThreadIntervalSeconds}</td>
                                </tr>

                            </g:each>
                            </table>
                        </div>

                    </div>
                </div>
            </div>
        </div>


        <script>
            $(document).ready(function() {

                $(".tabs").tabs({
                    cookie : {
                        expires : 1
                    }
                });
            });
        </script>

    </body>
</html>
