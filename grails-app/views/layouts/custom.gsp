<%@ page import="java.util.Locale" %>
<?xml version="1.0" encoding="UTF-8"?>
<html lang="en">
<head>
    <!-- Include default page title -->
    <title><g:layoutTitle default="OpenBoxes" /></title>

    <!-- YUI -->
    <yui:stylesheet dir="reset-fonts-grids" file="reset-fonts-grids.css" />

    <!-- Include Favicon -->
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

    <!-- Include Main CSS -->
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.megaMenu/',file:'jquery.megamenu.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.nailthumb',file:'jquery.nailthumb.1.1.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/chosen',file:'chosen.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/css/jquery.dataTables.min.css" type="text/css">
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footable.css')}" type="text/css" media="all" />

    <%--<link rel="stylesheet" href="${createLinkTo(dir:'js/feedback',file:'feedback.css')}" type="text/css" media="all" />--%>
    <!-- Include javascript files -->

    <!-- Include jQuery UI files -->
    <g:javascript library="jquery" plugin="jquery" />
    <jqui:resources />
    <link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" rel="stylesheet" media="screen, projection" />


    <!-- Include Jquery Validation and Jquery Validation UI plugins -->
    <jqval:resources />
    <jqvalui:resources />

    <link rel="stylesheet" href="${resource(dir:'css',file:'openboxes.css')}" type="text/css" media="all" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'loading.css')}" type="text/css" media="all" />

    <!-- jquery validation messages -->
    <g:if test="${ session?.user?.locale && session?.user?.locale != 'en'}">
        <script src="${createLinkTo(dir:'js/jquery.validation/', file:'messages_'+ session?.user?.locale + '.js')}"  type="text/javascript" ></script>
    </g:if>

    <!-- Grails Layout : write head element for page-->
    <g:layoutHead />

    <g:render template="/common/customCss"/>
    <g:render template="/common/fullstory"/>
    <g:render template="/common/hotjar"/>

    <r:require module="application"/>
    <ga:trackPageview />
    <r:layoutResources/>
</head>
<body class="yui-skin-sam">

<g:render template="/common/customVariables"/>
<div id="doc3">

    <g:if test="${grailsApplication.config.openboxes.system.notification.enabled}">
        <div class="notice">
            ${grailsApplication.config.openboxes.system.notification.message}
        </div>
    </g:if>
    <g:if test="${session.impersonateUserId}">
        <div class="notice">
            <g:message code="user.impersonate.message" args="[session.user.username]" default="You are impersonating user {0}."/>
            <g:link controller="auth" action="logout">
                ${g.message(code:'default.logout.label', default: "Logout")}
            </g:link>
        </div>
    </g:if>
    <g:if test="${session.useDebugLocale}">

        <div id="debug-header" class="notice" style="margin: 10px;">
            <warehouse:message code="localization.custom.message"/>
            <g:link controller="localization" action="list" class="button">
                <warehouse:message code="default.list.label" args="[message(code: 'localizations.label')]"/>
            </g:link>
            <g:link controller="localization" action="create" class="button">
                <warehouse:message code="default.add.label" args="[message(code: 'localization.label')]"/>
            </g:link>
            <div class="right">
                <g:link controller="user" action="disableLocalizationMode" class="button">
                    <warehouse:message code="localization.disable.label"/>
                </g:link>
            </div>
            <div id="localizations">
                <!--
                    At some point we may want to display all translations for the page in a single div.
                    For the time being, flash.localizations is empty.
                 -->
                <g:each var="localization" in="${flash.localizations }">
                    <div>
                        ${localization.code } = ${localization.text }
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>

    <!-- Header "hd" includes includes logo, global navigation -->
    <g:if test="${session?.user && session?.warehouse}">
        <div id="hd" role="banner">
            <g:render template="/common/header"/>
        </div>
        <div id="megamenu">
            <g:include controller="dashboard" action="megamenu" params="[locationId:session?.warehouse?.id,userId:session?.user?.id]"/>
            <div id="loader" style="display:none; position: absolute; right: 0; top: 0" class="right notice">
                ${g.message(code: 'default.loading.label')}
            </div>

        </div>
        <div id="breadcrumb">
            <g:render template="/common/breadcrumb"/>
        </div>
    </g:if>


    <!-- Body includes the divs for the main body content and left navigation menu -->
    <div id="bd" role="main">
        <div id="yui-main">
            <div id="content" class="yui-b">
                <g:layoutBody />
            </div>
        </div>
    </div>

    <g:if test="${session.useDebugLocale}">
        <g:render template="/common/localization"/>
    </g:if>


    <!-- YUI "footer" block that includes footer information -->
    <g:if test="${session?.user && session?.warehouse}">
        <div id="ft" role="contentinfo">
            <g:render template="/common/footer" />
        </div>
    </g:if>
</div>
<div id="dlgShowDialog" class="dialog hidden">
    <div id="dlgShowDialogContent" class="empty center">
        Loading ...
    </div>
</div>
<!-- Include other plugins -->
<g:if test="${System.getenv().get('headless') != 'false'}" env="test">
    <!--headless driver throw error when using watermark-->
</g:if>
<g:else>
    <script src="${createLinkTo(dir:'js/jquery.watermark/', file:'jquery.watermark.min.js')}" type="text/javascript" ></script>
</g:else>

<g:if test="${session.user && Boolean.valueOf(grailsApplication.config.openboxes.jira.issue.collector.enabled)}">
    <!-- JIRA Issue Collector -->
    <script type="text/javascript" src="${grailsApplication.config.openboxes.jira.issue.collector.url}"></script>
</g:if>

<!-- Set context path required for API requests -->
<script>var contextPath = '${request.contextPath}';</script>

<g:if test="${session.useDebugLocale}">
    <!-- Localization -->
    <script type="text/javascript" src="${createLinkTo(dir:'js/localization.js')}"></script>
</g:if>


<script type="text/javascript" src="${createLinkTo(dir:'js/action-menu.js')}" type="text/javascript"></script>

<script type="text/javascript" src="${createLinkTo(dir:'js/chosen.js')}" type="text/javascript"></script>
<script type="text/javascript" src="${createLinkTo(dir:'js/location-chooser.js')}" type="text/javascript"></script>
<script type="text/javascript" src="${createLinkTo(dir:'js/megamenu.js')}" type="text/javascript"></script>

<g:if test="${session.user && Boolean.valueOf(grailsApplication.config.openboxes.scannerDetection.enabled)}">
    <!-- Barcode Scanner library -->
    <script src="${createLinkTo(dir:'js/jquery.scannerdetection', file:'jquery.scannerdetection.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/barcode.js')}" type="text/javascript"></script>
</g:if>

<g:if test="${session.user && Boolean.valueOf(grailsApplication.config.openboxes.uservoice.widget.enabled)}">
    <!-- UserVoice widget -->
    <script>
    var uservoiceSettings = {
        id: '${session?.user?.id}',
        email: '${session?.user?.email}',
        name: '${session?.user?.name}',
        createdAt: '${session?.user?.dateCreated?.time}',
        triggerPosition: '${grailsApplication.config.openboxes.uservoice.widget.position?:"bottom-right"}',
        locale: '${session?.user?.locale?:"en"}'
    };
    </script>
    <script type="text/javascript" src="${createLinkTo(dir:'js/uservoice.js')}"></script>
</g:if>

<g:if test="${grailsApplication.config.openboxes.zopim.widget.enabled}">
    <!-- Live Chat -->
    <script type="text/javascript">
    var zopimSettings = {
        url: '${grailsApplication.config.openboxes.zopim.widget.url}',
        name: '${session?.user?.name}',
        email: '${session?.user?.email}'
    };
    </script>
    <script type="text/javascript" src="${createLinkTo(dir:'js/zopim.js')}"></script>
    <g:if test="${session.user}">
        <script>
            $zopim(function() {
                $zopim.livechat.setName(zopimSettings.name);
                $zopim.livechat.setEmail(zopimSettings.email);
            });
        </script>
    </g:if>
</g:if>
<r:layoutResources/>

</body>
</html>