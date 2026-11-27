<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="\${message(code: '${propertyName}.label', default: '${className}')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="\${flash.message}">
                <div class="message" role="status" aria-label="message">\${flash.message}</div>
            </g:if>
            <g:hasErrors bean="\${this.${propertyName}}">
                <div class="errors" role="alert" aria-label="error-message">
                    <g:renderErrors bean="\${this.${propertyName}}" as="list" />
                </div>
            </g:hasErrors>
            <g:render template="/common/scaffoldButtonBar" model="[entityName: entityName]" />
            <g:form resource="\${this.${propertyName}}" method="PUT">
                <g:hiddenField name="version" value="\${this.${propertyName}?.version}" />
                <div id="edit-${propertyName}" class="box" role="main">
                    <h2><g:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <f:all bean="${propertyName}"/>
                            <tr class="prop">
                                <td></td>
                                <td class="value">
                                    <g:link class="button" action="index">
                                        <g:message code="default.button.cancel.label" default="Cancel" />
                                    </g:link>
                                    <button type="submit" class="button">
                                        <g:message code="default.button.update.label" default="Update" />
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </g:form>
        </div>
    </body>
</html>
