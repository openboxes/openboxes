<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.User" %>
<%@ page import="org.pih.warehouse.core.Role" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="admin.sendZpl.title" default="Send ZPL" /></title>
</head>
<body>
<div id="settings" role="main" class="yui-ga">

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="yui-u first">
        <div class="box">
            <h2><warehouse:message code="admin.sendZpl.title" default="Send ZPL" /></h2>
            <g:form controller="admin" action="sendZpl" method="POST" enctype="multipart/form-data" useToken="true">
                <table class="table table-bordered">
                    <tbody>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="zpl.ipAddress.label" default="IP Address"/></label>
                            </td>
                            <td class="value">
                                <g:textField name="ipAddress" value="${params.ipAddress?:grailsApplication.config.openboxes.barcode.printer.ipAddress}" class="text" size="60"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="zpl.port.label" default="Port"/></label>
                            </td>
                            <td class="value">
                                <g:textField name="port" value="${params.port?:grailsApplication.config.openboxes.barcode.printer.port}" class="text" size="60"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="zpl.url.label" default="URL"/></label>
                            </td>
                            <td class="value">
                                <g:textField name="url" value="${params.url?:grailsApplication.config.openboxes.barcode.labelaryApi.url}" class="text" size="60"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="mail.body.label" default="Body"/></label>
                            </td>
                            <td class="value">
                                <g:textArea name="body" value="${params?.body?:grailsApplication.config.openboxes.barcode.printer.zpl}" class="text" cols="60" rows="10"/>
                            </td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="2" class="center">
                                <button name="print" value="print" class="button">
                                    <warehouse:message code="default.button.print.label"/>
                                </button>
                                <button name="render" value="render" class="button" formtarget="_blank">
                                    <warehouse:message code="default.button.render.label" default="Render"/>
                                </button>
                                <g:link controller="admin" action="sendZpl" class="button">
                                    <warehouse:message code="default.button.cancel.label"/></g:link>
                            </td>
                        </tr>
                    </tfoot>

                </table>
            </g:form>
        </div>
    </div>
</div>

</body>
</html>
