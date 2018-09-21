<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'stockMovement.label', default: 'Stock Movement')}" />
    <title>
        <warehouse:message code="stockMovement.label"/>
    </title>
</head>
<body>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="summary">
        <div class="title">${entityName} &rsaquo; ${stockMovement?.identifier} ${stockMovement?.name}</div>
    </div>


    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2><g:message code="default.header.label" default="Header"/></h2>
                <div></div>
            </div>
        </div>
        <div class="yui-u">

            <div class="box">
                <h2>${stockMovement?.identifier} ${stockMovement?.name}</h2>

                <div class="empty center">
                    To be coming ...

                </div>
            </div>
        </div>
    </div>

</div>