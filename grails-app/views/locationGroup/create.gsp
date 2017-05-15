<%@ page import="org.pih.warehouse.core.LocationGroup" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="custom"/>
    <g:set var="entityName" value="${warehouse.message(code: 'locationGroup.label', default: 'LocationGroup')}"/>
    <title><warehouse:message code="default.create.label" args="[entityName]"/></title>
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

    <g:render template="summary"/>

    <div class="button-bar">
        <g:link class="button" action="list"><warehouse:message code="default.list.label" args="[g.message(code:'locationGroups.label')]"/></g:link>
        <g:link class="button" action="create"><warehouse:message code="default.add.label" args="[g.message(code:'locationGroup.label')]"/></g:link>
    </div>

    <g:form action="save" method="post">

        <div class="dialog box">
            <h2><warehouse:message code="default.create.label" args="[entityName]"/></h2>
            <table>
                <tbody>

                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="name"><warehouse:message code="locationGroup.name.label"
                                                                 default="Name"/></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean: locationGroupInstance, field: 'name', 'errors')}">
                            <g:textField class="text" name="name" size="100" value="${locationGroupInstance?.name}"/>
                        </td>
                    </tr>


                    <tr class="prop">
                        <td valign="top"></td>
                        <td valign="top">
                            <div class="buttons left">
                                <g:submitButton name="create" class="button"
                                                value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}"/>

                                <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>

                            </div>
                        </td>
                    </tr>

                </tbody>
            </table>
        </div>
    </g:form>
</div>
</body>
</html>
