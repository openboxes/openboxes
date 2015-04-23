<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.User" %>
<%@ page import="org.pih.warehouse.core.Role" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="admin.title" default="Settings" /></title>
</head>
<body>
<div id="settings" role="main" class="yui-ga">

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="yui-u first">
        <div class="tabs">
            <ul>
                <li>
                    <a href="#tabs-1" id="send-text-tab"><warehouse:message code="mail.header"/></a>
                </li>
            </ul>
            <div id="tabs-1">
                <g:form controller="admin" action="sendMail" method="POST" enctype="multipart/form-data" useToken="true">
                    <table class="table table-bordered">
                        <tbody>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="mail.to.label" default="To"/></label>
                                </td>
                                <td class="value">
                                    <g:textField name="to" value="${session?.user?.email}" class="text" size="60"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="mail.from.label" default="From"/></label>
                                </td>
                                <td class="value">
                                    <g:textField name="from" value="info@openboxes.com" class="text" size="60"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="mail.subject.label" default="Subject"/></label>
                                </td>
                                <td class="value">
                                    <g:textField name="subject" value="${params.subject?:'Test email'}" class="text" size="60"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="mail.includesHtml.label" default="Includes HTML?"/></label>
                                </td>
                                <td class="value">
                                    <g:checkBox name="includesHtml" value="${params?.includesHtml}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="mail.message.label" default="Message"/></label>
                                </td>
                                <td class="value">
                                    <g:textArea name="message" value="${params?.message}" class="text" cols="60" rows="10"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message code="mail.file.label" default="File"/></label>
                                </td>
                                <td class="value">
                                    <input type="file" name="file" value="${params.file}"/>
                                </td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td colspan="2" class="center">
                                    <g:submitButton name="Send Mail" class="button icon email"></g:submitButton>
                                    <g:link controller="admin" action="sendMail" class="button icon reload"><warehouse:message code="default.button.cancel.label"/></g:link>
                                </td>
                            </tr>
                        </tfoot>

                    </table>
                </g:form>


            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {

        $(".tabs").tabs({
            cookie : {
                expires : 1
            }
        });
    });
</script>

</body>
</html>
