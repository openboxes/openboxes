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
<body class="d-flex flex-column">
    <div style="flex: 1">
        <g:layoutBody />
    </div>
<%= NewRelic.getBrowserTimingFooter() %>
</body>
</html>
