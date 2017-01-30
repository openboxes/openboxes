
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
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
            <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[warehouse.message(code:'persons.label').toLowerCase()]"/></g:link></li>
            <li><g:link class="create" action="create"><g:message code="default.create.label" args="[entityName]" /></g:link></li>
        </ul>
    </div>

    <div class="dialog box">
        <h2><warehouse:message code="person.list.label"/></h2>

        <div class="filter center">
            <g:form action="index" method="get">
                <g:textField name="q" size="45" value="${params.q }" class="text middle" placeholder="${g.message(code:"person.search.label")}"/>
                <button type="submit" class="button"><img
                        src="${resource(dir:'images/icons/silk',file:'zoom.png')}"
                        alt="Save" /> ${warehouse.message(code: 'default.button.find.label')}
                </button>

                <g:link controller="person" action="index"><g:message code="default.button.reset.label" default="Reset"/></g:link>
            </g:form>
        </div>
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
            <g:each in="${userInstanceList}" status="i" var="personInstance">
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
            <g:unless test="${userInstanceList}">
                <tr>
                    <td colspan="4" class="center empty fade">
                        <p><g:message code="default.noResults.label"/></p>

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
</body>
</html>
