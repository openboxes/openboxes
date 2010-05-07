<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <%--
        The following tag is decribed in more detail further down this page.
        --%>
        <title><g:menuTitle default="Main Menu"/></title>
    </head>
    <body>
        <%--
        The following two tags are special to the menu page. They automatically detect
        whether you have the drilldowns and/or criteria plugins installed and, if so,
        reset those systems in acknowldgement that the user has returned to a menu page.
        --%>
        <g:menuDrilldownReset/>
        <g:menuCriteriaReset/>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/', absolute: true)}"><g:message code="home" default="Home" /></a></span>
        </div>
        <div class="body">
            <%--
            The following tag (also used in the <title> region above) is specific
            to the menu page and determines what title to display. The default is
            only used for the main menu, all sub-menus have their title in their
            domain record.
            --%>
            <h1><g:menuTitle default="Main Menu"/></h1>
            <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
            </g:if>
            <div class="crumbs">
                <%--
                The following tag is specific to the menu page and is used to display
                a breadcrumb trail so that the user may move backwords through the
                menu hierarchy. The default is used only for the main menu since all
                sub-menus use the last node in their path as their own crumb. By
                default, when the main menu is displayed, no breadcrumb trail is displayed
                since there is nowhere to go back to. However, you can force a breadcrumb
                trail to be displayed even for the main menu by including an attribute of
                single="true" in the following tag.
                --%>
                <g:menuCrumbs default="Main"/>
            </div>
            <div class="options">
                <ul>
                    <g:each in="${optionList}" var="option">
                        <%--
                        See the menuOptions.properties file in the i18n directory
                        of your application to determine how to internationalize
                        your menus.
                        --%>
                        <li><g:link action="${option.type == 'submenu' ? 'display' : 'execute'}" id="${option.id}"><g:message code="option.${option.path}" default="${option.title}" encodeAs="HTML" /></g:link></li>
                    </g:each>
                </ul>
            </div>
        </div>
    </body>
</html>
