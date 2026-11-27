<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="\${message(code: '${propertyName}.label', default: '${className}')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="\${flash.message}">
                <div class="message" role="status" aria-label="message">\${flash.message}</div>
            </g:if>
            <g:render template="/common/scaffoldButtonBar" model="[entityName: entityName]" />
            <div id="show-${propertyName}" class="box" role="main">
                <h2><g:message code="default.show.label" args="[entityName]" /></h2>
                <f:display bean="${propertyName}" />
                <div class="buttons border-top">
                    <g:form resource="\${this.${propertyName}}" method="DELETE">
                        <g:link class="button" action="edit" resource="\${this.${propertyName}}">
                            <g:message code="default.button.edit.label" default="Edit" />
                        </g:link>
                        <input class="button" type="submit"
                               value="\${message(code: 'default.button.delete.label', default: 'Delete')}"
                               onclick="return confirm('\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                    </g:form>
                </div>
            </div>
        </div>
    </body>
</html>
