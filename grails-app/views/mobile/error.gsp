<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="mobile"/>
    <g:set var="entityName"
           value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}"/>
    <title><warehouse:message code="inventory.browse.label" default="Browse inventory"/></title>
</head>

<body>
    <div class="container-fluid">
        <h2>Error Details</h2>
        <div class="message">
            <strong>Error ${request?.'javax.servlet.error.status_code'}:</strong>
                ${request?.'javax.servlet.error.message'?.encodeAsHTML()}<br/>
            <strong>Servlet:</strong> ${request?.'javax.servlet.error.servlet_name'}<br/>
            <strong>URI:</strong> ${request?.'javax.servlet.error.request_uri'}<br/>
            <g:if test="${exception}">
                <strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
                <strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
                <strong>Class:</strong> ${exception.className} <br />
            </g:if>
        </div>
        <g:if test="${exception}">
            <h2>Stack Trace</h2>
            <code>
              <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
            </code>
        </g:if>
    </div>
</body>
