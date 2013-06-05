<%@ page import="org.pih.warehouse.core.LocationGroup" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="custom"/>
    <g:set var="entityName" value="${warehouse.message(code: 'locationGroup.label', default: 'LocationGroup')}"/>
    <title><warehouse:message code="default.create.label" args="[entityName]"/></title>
    <!-- Specify content to overload like global navigation links, page titles, etc. -->
    <content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]"/></content>
</head>

<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${locationGroupInstance}">
        <div class="errors">
            <g:renderErrors bean="${locationGroupInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <fieldset>
            <div class="dialog">
                <table>
                    <tbody>

                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="name"><warehouse:message code="locationGroup.name.label"
                                                                 default="Name"/></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean: locationGroupInstance, field: 'name', 'errors')}">
                            <g:textArea name="name" cols="40" rows="5" value="${locationGroupInstance?.name}"/>
                        </td>
                    </tr>


                    <tr class="prop">
                        <td valign="top"></td>
                        <td valign="top">
                            <div class="buttons">
                                <g:submitButton name="create" class="save"
                                                value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}"/>

                                <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>

                            </div>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </div>
        </fieldset>
    </g:form>
</div>
</body>
</html>
