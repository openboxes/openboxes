<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Products</title>
    <meta name="layout" content="main" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/product/css',file:'app.css')}"  />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/product/css',file:'animations.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/product/css',file:'bootstrap-responsive.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'js/product/css',file:'bootstrap.css')}" />
    <g:javascript library="jquery" plugin="jquery" />
    <jqui:resources />
</head>
<body>
    <div ng-app="phonecatApp">
        <div class="view-container">
            <div ng-view class="view-frame"></div>
        </div>
    </div>
    <script src="${createLinkTo(dir:'js/angular/', file:'angular.min.js')}" type="text/javascript"></script>
    <script src="${createLinkTo(dir:'js/angular/', file:'angular-animate.js')}"></script>
    <script src="${createLinkTo(dir:'js/angular/', file:'angular-resource.js')}"></script>
    <script src="${createLinkTo(dir:'js/angular/', file:'angular-route.js')}"></script>
    <script src="${createLinkTo(dir:'js/product/', file:'app.js')}"></script>
    <script src="${createLinkTo(dir:'js/product/', file:'animations.js')}"></script>
    <script src="${createLinkTo(dir:'js/product/', file:'controllers.js')}"></script>
    <script src="${createLinkTo(dir:'js/product/', file:'filters.js')}"></script>
    <script src="${createLinkTo(dir:'js/product/', file:'services.js')}"></script>
</body>
</html>