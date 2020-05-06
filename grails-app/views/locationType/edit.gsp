<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'locationType.label', default: 'LocationType')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#edit-locationType" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="edit-locationType" class="content scaffold-edit" role="main">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${this.locationType}">
            <ul class="errors" role="alert">
                <g:eachError bean="${this.locationType}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                </g:eachError>
            </ul>
            </g:hasErrors>
            <g:form resource="${this.locationType}" method="PUT">
                <g:hiddenField name="version" value="${this.locationType?.version}" />
                <fieldset class="form">
                    <f:all bean="locationType" except="supportedActivities"/>
                    <div class="fieldcontain" >
                        <label for="supportedActivities"><g:message code="locationType.supportedActivities.label"/></label>
                        <g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list()}"/>
                        <g:select name="supportedActivities" multiple="true" from="${activityList}"
                                  size="${activityList.size()}" class="chzn-select-deselect"
                                  optionKey="id" optionValue="${{ format.metadata(obj: it) }}"
                                  value="${this?.locationType?.supportedActivities}"/>
                    </div>
                </fieldset>

                <fieldset class="buttons">
                    <input class="save" type="submit" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
