<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:layoutTitle default="OpenBoxes" /></title>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css">
    <g:set var="translationModeLocale" value="${new Locale(grailsApplication.config.openboxes.locale.translationModeLocale)}" />
    <g:set var="localizationModeEnabled" value="${(session?.locale ?: session?.user?.locale) == translationModeLocale}" />
    <g:if test="${localizationModeEnabled}">
        <script type="text/javascript">
            var _jipt = [];
            _jipt.push(['project', 'openboxes']);
        </script>
        <script type="text/javascript" src="//cdn.crowdin.com/jipt/jipt.js"></script>
    </g:if>
</head>
<body class="d-flex flex-column">
    <div style="flex: 1">
        <g:layoutBody />
    </div>
</body>
</html>
