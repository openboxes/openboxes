
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'person.label', default: 'Person')}" />
        <title><warehouse:message code="person.list.label"/></title>
        <content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
                <div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>

            <div class="button-bar">
                <g:link class="button" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[warehouse.message(code:'persons.label').toLowerCase()]"/>
                </g:link>
                <g:link class="button" action="create">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.add.label" args="[warehouse.message(code:'person.label').toLowerCase()]"/>
                </g:link>
            </div>

            <div class="box">
                <h2><g:message code="default.list.label" args="[g.message(code:'persons.label')]"/></h2>
                <g:form action="list" method="get">
                    <div class="filter">
                        <label><warehouse:message code="default.search.label"/></label>
                        <g:textField name="q" size="45" value="${params.q}" class="text" data-testid="person-search-field" />
                        <button type="submit" class="button"><img
                            src="${resource(dir:'images/icons/silk',file:'zoom.png')}" style="vertical-align: middle;"
                            alt="Search" /> ${warehouse.message(code: 'default.button.find.label')}
                        </button>
                    </div>
                </g:form>
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="active" title="${warehouse.message(code: 'user.active.label')}" />
                            <g:sortableColumn property="lastName" title="${warehouse.message(code: 'default.name.label')}" />
                            <g:sortableColumn property="type" title="${warehouse.message(code: 'person.type.label')}" />
                            <g:sortableColumn property="email" title="${warehouse.message(code: 'person.email.label')}" />
                            <g:sortableColumn property="phoneNumber" title="${warehouse.message(code: 'person.phoneNumber.label')}" />
                            <g:sortableColumn property="identifier" title="${warehouse.message(code: 'person.identifier.label', default: 'Identifier')}" />
                            <th><g:message code="default.actions.label"/></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personInstanceList}" status="i" var="personInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>
                                <g:if test="${personInstance?.active}">
                                    <span class="tag tag-success"><warehouse:message code="default.active.label" default="Active"/></span>
                                </g:if>
                                <g:else>
                                    <span class="tag tag-danger"><warehouse:message code="default.inactive.label" default="Inactive"/></span>
                                </g:else>
                            </td>
                            <td>
                                <g:link action="edit" id="${personInstance.id}">
                                    ${fieldValue(bean: personInstance, field: "name")}
                                </g:link>
                            </td>
                            <td>
                                <g:if test="${personInstance.class.simpleName == 'User'}">
                                    <span class="tag tag-info"><warehouse:message code="user.label"/></span>
                                </g:if>
                                <g:else>
                                    <span class="tag"><warehouse:message code="person.label"/></span>
                                </g:else>
                            </td>
                            <td>
                                <g:if test="${grailsApplication.config.openboxes.anonymize.enabled}">
                                    ${util.StringUtil.mask(personInstance?.email)}
                                </g:if>
                                <g:else>
                                    ${fieldValue(bean: personInstance, field: "email")}
                                </g:else>
                            </td>
                            <td>${fieldValue(bean: personInstance, field: "phoneNumber")}</td>
                            <td>${fieldValue(bean: personInstance, field: "identifier")}</td>
                            <td>
                                <g:link controller="person" action="delete" id="${personInstance?.id}">
                                    <g:message code="default.button.delete.label"/>
                                </g:link>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <div class="paginateButtons">
                    <g:paginate total="${personInstanceTotal}" params="${params}" />
                </div>
            </div>
        </div>
    </body>
</html>
