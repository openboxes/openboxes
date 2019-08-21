<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationGroup.label', default: 'Location Group')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationGroupInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationGroupInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <g:render template="summary"/>

            <div class="button-bar">
                <g:link class="button" action="list"><warehouse:message code="default.list.label" args="[g.message(code:'locationGroups.label')]"/></g:link>
                <g:link class="button" action="create"><warehouse:message code="default.add.label" args="[g.message(code:'locationGroup.label')]"/></g:link>
            </div>



            <div class="dialog box">
                <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                <table>
                    <tbody>
                        <tr class="prop">
                            <td class="name">
                                ${warehouse.message(code: 'locationGroup.name.label') }
                            </td>
                            <td class="value">
                                ${locationGroupInstance?.name }
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                ${warehouse.message(code: 'locationGroup.locations.label') }
                            </td>
                            <td class="value">
                                <table>
                                    <g:each in="${locationGroupInstance?.locations }" var="location">
                                        <tr>
                                            <td>
                                                ${location?.name }
                                            </td>
                                        </tr>
                                    </g:each>
                                </table>
                            </td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr class="prop">
                            <td valign="top"></td>
                            <td valign="top">
                                <div class="buttons left">
                                    <g:form>
                                        <g:hiddenField name="id" value="${locationGroupInstance?.id}" />
                                        <g:actionSubmit class="button" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
                                        <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                    </g:form>
                                </div>
                            </td>
                        </tr>
                    </tfoot>

                </table>
            </div>
    </body>
</html>
