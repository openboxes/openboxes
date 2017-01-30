
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><warehouse:message code="person.list.label"/></title>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="nav" role="navigation">
        <ul>
            <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
            <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[entityName]"/></g:link></li>
            <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
        </ul>
    </div>


    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2><g:message code="default.filters.label"/></h2>
                <g:form action="index" method="get">
                    <div class="filter-list-item">
                        <g:textField name="q" size="45" value="${params.q }" class="text middle" placeholder="${g.message(code:"person.search.label")}"/>
                    </div>
                    <hr/>
                    <div class="buttons center">
                        <button type="submit" class="button">${warehouse.message(code: 'default.button.search.label')}</button>
                        &nbsp;
                        <g:link controller="person" action="index"><g:message code="default.button.reset.label" default="Reset"/></g:link>
                    </div>
                </g:form>
            </div>

        </div>
        <div class="yui-u">
            <div class="dialog box">
                <h2><warehouse:message code="person.list.label"/> <small><g:message code="default.searchResults.label" args="[personInstanceCount]"/></small></h2>

                <table>
                    <thead>
                    <tr>

                        <th><g:message code="default.actions.label"/> </th>

                        <g:sortableColumn property="type" title="${warehouse.message(code: 'person.type.label')}" />

                        <g:sortableColumn property="lastName" title="${warehouse.message(code: 'person.name.label')}" />

                        <g:sortableColumn property="email" title="${warehouse.message(code: 'person.email.label')}" />

                        <g:sortableColumn property="phoneNumber" title="${warehouse.message(code: 'person.phoneNumber.label')}" />

                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${userInstanceList?:personInstanceList}" status="i" var="personInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td>
                                <g:link action="edit" id="${personInstance?.id}" class="button"><g:message code="default.button.edit.label"/></g:link>
                            </td>

                            <td>
                                ${warehouse.message(code: (personInstance.class.simpleName.toLowerCase() + '.label'))}
                            </td>

                            <td>
                                <g:link action="edit" id="${personInstance.id}">
                                    ${fieldValue(bean: personInstance, field: "firstName")}
                                    ${fieldValue(bean: personInstance, field: "lastName")}
                                </g:link>
                            </td>

                            <td>${fieldValue(bean: personInstance, field: "email")}</td>

                            <td>${fieldValue(bean: personInstance, field: "phoneNumber")}</td>

                        </tr>
                    </g:each>
                    <g:unless test="${userInstanceList?:personInstanceList}">
                        <tr>
                            <td colspan="5" class="center empty fade">
                                <g:message code="default.noResults.label"/>

                            </td>
                        </tr>
                    </g:unless>
                    </tbody>
                </table>
                <div class="pagination">
                    <g:paginate total="${personInstanceCount}" />
                </div>
            </div>
        </div>
    </div>


</div>
</body>
</html>
