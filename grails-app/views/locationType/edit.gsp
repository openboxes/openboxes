<%@ page import="org.pih.warehouse.core.LocationTypeCode; org.pih.warehouse.core.LocationType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="custom"/>
    <g:set var="entityName"
           value="${warehouse.message(code: 'locationType.label', default: 'LocationType')}"/>
    <title><warehouse:message code="default.edit.label" args="[entityName]"/></title>
    <!-- Specify content to overload like global navigation links, page titles, etc. -->
    <content tag="pageTitle"><warehouse:message code="default.edit.label"
                                                args="[entityName]"/></content>
</head>

<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${locationTypeInstance}">
        <div class="errors">
            <g:renderErrors bean="${locationTypeInstance}" as="list"/>
        </div>
    </g:hasErrors>

    <div class="button-bar">
        <g:link class="button icon search" action="list" controller="locationType">
            <warehouse:message code="default.list.label"
                               args="[g.message(code: 'locationTypes.label')]"/>
        </g:link>
    </div>

    <g:form method="post">
        <div class="box">
            <h2><warehouse:message code="default.edit.label" args="[entityName]"/></h2>
        <g:hiddenField name="id" value="${locationTypeInstance?.id}"/>
        <g:hiddenField name="version" value="${locationTypeInstance?.version}"/>
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="locationTypeCode"><warehouse:message
                                code="locationTypeCode.label" default="Location Type Code"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: locationTypeInstance, field: 'description', 'errors')}">
                        <g:select name="locationTypeCode" class="chzn-select-deselect"
                                  from="${org.pih.warehouse.core.LocationTypeCode.list()}"
                                  noSelection="['': '']"
                                  value="${locationTypeInstance?.locationTypeCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><warehouse:message code="default.name.label"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: locationTypeInstance, field: 'name', 'errors')}">
                        <g:textField name="name" value="${locationTypeInstance?.name}" class="text"
                                     style="width:100%"/>
                    </td>
                </tr>



                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><warehouse:message
                                code="default.description.label"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: locationTypeInstance, field: 'description', 'errors')}">
                        <g:textArea name="description" value="${locationTypeInstance?.description}"
                                    cols="80" rows="5" style="width:100%"/>
                    </td>
                </tr>


                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="supportedActivities"><warehouse:message
                                code="locationType.supportedActivities.label"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: locationTypeInstance, field: 'supportedActivities', 'errors')}">
                        <g:set var="activityList"
                               value="${org.pih.warehouse.core.ActivityCode.list()}"/>
                        <g:select name="supportedActivities" multiple="true" from="${activityList}"
                                  size="${activityList.size()}" class="chzn-select-deselect"
                                  optionKey="id" optionValue="${{ format.metadata(obj: it) }}"
                                  value="${locationTypeInstance?.supportedActivities}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="sortOrder"><warehouse:message
                                code="default.sortOrder.label"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: locationTypeInstance, field: 'sortOrder', 'errors')}">
                        <g:textField name="sortOrder"
                                     value="${fieldValue(bean: locationTypeInstance, field: 'sortOrder')}"
                                     class="text" style="width:100%"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top"></td>
                    <td valign="top">
                        <div class="buttons left">
                            <g:actionSubmit class="button" action="update"
                                            value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}"/>
                            <g:actionSubmit class="button" action="delete"
                                            value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}"
                                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
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
