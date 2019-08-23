<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.User" %>
<%@ page import="org.pih.warehouse.core.Role" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="cache.title" default="Cache" /></title>
    </head>
    <body>        
		<div id="settings" role="main" class="yui-ga">

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">

            
                <div class="box">
                    <h2><warehouse:message code="admin.cacheSettings.header" default="Cache settings"/></h2>
                    <table>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="admin.cacheEnabled.label" default="Cached enabled?"/></label>
                            </td>
                            <td>
                                ${grailsApplication.config.hibernate }
                            </td>
                        </tr>

                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="admin.cacheStatistics.label" default="Cache statistics"/></label>

                            </td>
                            <td class="value">
                                ${cacheStatistics}
                            </td>

                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="admin.secondLevelCacheStatistics.label" default="Second-level cache statistics"/></label>
                            </td>
                            <td class="value">
                                <table style="width:auto;">
                                    <thead>
                                        <tr>
                                            <th><warehouse:message code="cache.regionName.label" default="Region name"/></th>
                                            <th><warehouse:message code="cache.hitCount.label" default="Hit count"/></th>
                                            <th><warehouse:message code="cache.missCount.label" default="Miss count"/></th>
                                            <th><warehouse:message code="cache.putCount.label" default="Put count"/></th>
                                            <th><warehouse:message code="cache.elementCountInMemory.label" default="Element Count In Memory"/></th>
                                            <th><warehouse:message code="cache.elementCountOnDisk.label" default="Element Count On Disk"/></th>
                                            <th><warehouse:message code="cache.sizeInMemory.label" default="Size In Memory"/></th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:each var="regionName" in="${cacheStatistics.secondLevelCacheRegionNames}" status="status">
                                            <g:set var="statistics" value="${cacheStatistics.getSecondLevelCacheStatistics(regionName)}"/>
                                            <tr class="${status%2?'odd':'even'}">
                                                <td>
                                                    ${regionName}
                                                </td>
                                                <td class="center">
                                                    ${statistics.hitCount}
                                                </td>
                                                <td class="center">
                                                    ${statistics.missCount}
                                                </td>
                                                <td class="center">
                                                    ${statistics.putCount}
                                                </td>
                                                <td class="center">
                                                    ${statistics.elementCountInMemory}
                                                </td>
                                                <td class="center">
                                                    ${statistics.elementCountOnDisk}
                                                </td>
                                                <td class="center">
                                                    ${statistics.sizeInMemory}
                                                </td>
                                                <td>
                                                    <g:link controller="admin" action="evictDomainCache" params="[name:regionName]" class="button">
                                                        ${warehouse.message(code:'default.evict.label', default: 'Evict')}
                                                    </g:link>

                                                </td>
                                            </tr>
                                        </g:each>
                                    </tbody>
                                </table>
                            </td>
                        </tr>

                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="admin.queryCacheStatistics.label" default="Query cache statistics"/></label>
                            </td>
                            <td class="value">

                                <g:link controller="admin" action="evictQueryCache" class="button">
                                    ${warehouse.message(code:'default.evictAll.label', default: 'Evict all')}
                                </g:link>

                                <table style="width:auto;">
                                    <thead>
                                    <tr>
                                        <th><warehouse:message code="cache.queryName.label" default="Region"/></th>
                                        <th><warehouse:message code="cache.hitCount.label" default="Hits"/></th>
                                        <th><warehouse:message code="cache.missCount.label" default="Misses"/></th>
                                        <th><warehouse:message code="cache.putCount.label" default="Puts"/></th>
                                        <th><warehouse:message code="cache.executionCount.label" default="Count"/></th>
                                        <th><warehouse:message code="cache.executionMinTime.label" default="Min Time"/></th>
                                        <th><warehouse:message code="cache.executionMaxTime.label" default="Max Time"/></th>
                                        <th><warehouse:message code="cache.executionAvgTime.label" default="Avg Time"/></th>
                                        <th><warehouse:message code="cache.executionRowCount.label" default="Row Count"/></th>
                                        <th></th>

                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:each var="queryName" in="${cacheStatistics.queries}" status="status">
                                        <g:set var="statistics" value="${cacheStatistics.getQueryStatistics(queryName)}"/>
                                        <tr class="${status%2?'odd':'even'}">
                                            <td>
                                                ${queryName}
                                            </td>
                                            <td class="center">
                                                ${statistics.cacheHitCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.cacheMissCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.cachePutCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.executionCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.executionMinTime}
                                            </td>
                                            <td class="center">
                                                ${statistics.executionMaxTime}
                                            </td>
                                            <td class="center">
                                                ${statistics.executionAvgTime}
                                            </td>
                                            <td class="center">
                                                ${statistics.executionRowCount}
                                            </td>
                                            <td>
                                                <g:link controller="admin" action="evictQueryCache" params="[name:queryName]" class="button">
                                                    ${warehouse.message(code:'default.evict.label', default: 'Evict')}
                                                </g:link>

                                            </td>

                                        </tr>

                                    </g:each>

                                    </tbody>
                                </table>

                            </td>
                        </tr>

                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="admin.entityCacheStatistics.label" default="Entity cache statistics"/></label>
                            </td>
                            <td>
                                <table style="width:auto;">
                                    <thead>
                                    <tr>
                                        <th><warehouse:message code="cache.entityName.label" default="Entity"/></th>
                                        <th><warehouse:message code="cache.loadCount.label" default="Loads"/></th>
                                        <th><warehouse:message code="cache.deleteCount.label" default="Deletes"/></th>
                                        <th><warehouse:message code="cache.fetchCount.label" default="Fetches"/></th>
                                        <th><warehouse:message code="cache.insertCount.label" default="Inserts"/></th>
                                        <th><warehouse:message code="cache.updateCount.label" default="Updates"/></th>
                                        <th><warehouse:message code="cache.optimisticFailureCount.label" default="Optimistic failures"/></th>

                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:each var="entityName" in="${grailsApplication.domainClasses}" status="status">
                                        <g:set var="statistics" value="${cacheStatistics.getEntityStatistics(entityName.fullName)}"/>
                                        <tr class="${status%2?'odd':'even'}">
                                            <td>
                                                ${entityName}
                                            </td>
                                            <td class="center">
                                                ${statistics.loadCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.deleteCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.fetchCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.insertCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.optimisticFailureCount}
                                            </td>
                                            <td class="center">
                                                ${statistics.updateCount}
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
		</div>
    </body>
</html>
