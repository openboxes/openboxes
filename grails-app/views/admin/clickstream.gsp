<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.User" %>
<%@ page import="org.pih.warehouse.core.Role" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="clickstream.title" default="Clickstream" /></title>
    </head>
    <body>        
		<div id="settings" role="main" class="yui-ga">

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">


                <div class="box">
                    <h2><warehouse:message code="clickstream.label" default="Clickstream"/></h2>
                    <table>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    <warehouse:message code="clickstream.requests.label" default="# of requests"/>
                                </label>
                            </td>
                            <td class="value">
                                ${session?.clickstream?.stream?.size()} requests
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    Initial Referrer
                                </label>
                            </td>
                            <td class="value">
                                <a href="<%= session.clickstream.getInitialReferrer() %>">
                                    <%= session.clickstream.getInitialReferrer() %></a>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    Hostname
                                </label>
                            </td>
                            <td class="value">
                                <%= session.clickstream.getHostname() %>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    Session ID
                                </label>
                            </td>
                            <td class="value">
                                <%= session.id %>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    Bot
                                </label>
                            </td>
                            <td class="value">
                                <%= session.clickstream.isBot() ? "Yes" : "No" %>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    Stream Start
                                </label>
                            </td>
                            <td class="value">
                                <%= session.clickstream.getStart() %>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    Last Request
                                </label>
                            </td>
                            <td class="value">
                                <%= session.clickstream.getLastRequest() %>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label>
                                    Actions
                                </label>
                            </td>
                            <td class="value">
                                <g:link controller="admin" action="clickstream" params="[format:'csv']" class="button icon log">
                                    <warehouse:message code="default.downloadAsCsv" default="Download as CSV"/>
                                </g:link>
                            </td>
                        </tr>



                        <tr class="prop">
                            <td class="name">
                                <label>
                                    <warehouse:message code="clickstream.label" default="Clickstream"/>
                                </label>
                            </td>
                            <td class="value">



                                <div class="box" style="padding: 10px;">
                                    <label>Limit</label>
                                    <a href="?">All</a> |
                                    <a href="?limit=10">10</a> |
                                    <a href="?limit=25">25</a> |
                                    <a href="?limit=50">50</a> |
                                    <a href="?limit=100">100</a> |
                                    <a href="?limit=250">250</a> |
                                    <a href="?limit=500">500</a> |
                                    <a href="?limit=1000">1000</a>
                                </div>

                                <g:set var="clickstream" value="${session?.clickstream?.stream?.reverse()}"/>
                                <g:if test="${params.limit}">
                                    <g:set var="endIndex" value="${params.int('limit')?:10}"/>
                                    <g:set var="clickstream" value="${clickstream[0..endIndex-1]}"/>
                                </g:if>
                                <ul>
                                    <g:each var="entry" in="${clickstream}" status="i">
                                        <li>
                                            ${++i}
                                            ${entry.timestamp}
                                            <a href="http://${entry.toString()}">
                                                ${entry.toString()}
                                            </a>
                                        </li>
                                    </g:each>
                                </ul>
                            </td>
                        </tr>
                    </table>
                </div>


            </div>
		</div>
    </body>
</html>
