<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:layoutTitle default="OpenBoxes" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css/', file:'bundle.css')}">

    <style>
        #footer > div > a, #footer > div > b > a {
            color: #777;
        }
    </style>

</head>
<body class="d-flex flex-column">
    <div style="flex: 1">
        <g:layoutBody />
    </div>
    <div class="border-top align-self-end text-center py-2" style="width: 100%; font-size: 12px; color: #777;">
        <g:render template="/common/footer" />
    </div>
</body>
</html>
