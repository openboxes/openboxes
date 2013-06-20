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
                    <h3><warehouse:message code="clickstream.label" default="Clickstream"/></h3>
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
                                    <warehouse:message code="clickstream.label" default="Clickstream"/>
                                </label>
                            </td>
                            <td class="value">
                                <table style="table-layout: fixed; max-height: 100%; overflow: auto;">
                                    <thead>
                                        <tr>
                                            <th>Link</th>
                                            <th>Timestamp</th>
                                            <th>Request URI</th>
                                            <th>Query string</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:each var="entry" in="${session?.clickstream?.stream?.reverse()}" status="i">
                                            <tr class="${i%2?'odd':'even'}">
                                                <td width="10%">
                                                    <a href="http://${entry.toString()}">
                                                        Go to link
                                                    </a>
                                                </td>
                                                <td width="10%">
                                                    ${entry.timestamp}
                                                </td>
                                                <td style="word-wrap:break-word" width="40%" >
                                                    ${entry.requestURI}
                                                </td>
                                                <td style="word-wrap:break-word" width="40%">
                                                    ${entry.queryString}
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
