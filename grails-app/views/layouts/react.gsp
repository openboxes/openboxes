<%@ page import="com.newrelic.api.agent.NewRelic" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <%= NewRelic.getBrowserTimingHeader() %>
    <title><g:layoutTitle default="OpenBoxes" /></title>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css">
    <g:render template="/localization/crowdin"/>
    <g:googleSiteTag />
    <g:render template="/common/hotjar"/>
    %{-- The unified design ships a blue mark; keep the original for instances
         that have not opted in, so the browser tab does not change under them.
         default.gsp needs no such guard — it only renders when opted in. --}%
    <g:if test="${grailsApplication.config.getProperty('openboxes.layout.unified.enabled', Boolean, false)}">
        <link rel="shortcut icon" href="${request.contextPath}/static/images/favicon-unified.ico?v=1" type="image/x-icon"/>
    </g:if>
    <g:else>
        <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>
    </g:else>

    <g:layoutHead/>
</head>
<body class="d-flex flex-column ${grailsApplication.config.getProperty('openboxes.layout.unified.enabled', Boolean, false) ? 'unified-layout' : ''}">
    %{-- The React bundle is one file for both states, so the JSX reads this
         rather than being built twice. Kept in the layout because
         common/react.gsp carries a generated bundle hash. --}%
    <script>window.UNIFIED_LAYOUT = ${grailsApplication.config.getProperty('openboxes.layout.unified.enabled', Boolean, false)};</script>
    <div style="flex: 1">
        <g:layoutBody />
    </div>
<%= NewRelic.getBrowserTimingFooter() %>
</body>
</html>
