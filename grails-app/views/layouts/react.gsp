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
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>

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
