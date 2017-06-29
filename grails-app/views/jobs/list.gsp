<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="users.label" /></title>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="buttonBar">
        <g:link class="button icon settings" controller="admin" action="showSettings" fragment="tab-5">${g.message(code:'admin.backTo.label', default: 'Back to Admin')}</g:link>
        <g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="[g.message(code:'jobKeys.label', default: 'Background Jobs')]"/></g:link>
    </div>

    <div class="yui-gf">
        <div class="yui-u first">
            <div class="dialog box">
                <h2><warehouse:message code="default.filters.label"/></h2>
                <g:form action="list" method="get">
                    <ul class="filter-list">
                        <li class="filter-list-item">
                            <p>
                                <label><warehouse:message code="user.search.label"/></label>
                            </p>
                            <g:textField name="q" size="40" value="${params.q }" class="text"/>
                        </li>
                        <li class="filter-list-item">
                            <p>
                                <label>${warehouse.message(code:'default.status.label', default: "Status")}</label>
                            </p>
                            <g:select name="status" from="['':'All users', 'true':'Active users only', 'false':'Inactive users only']"
                                      value="${params.status}"
                                      class="chzn-select-deselect"
                                      optionKey="key" optionValue="value"/>

                        </li>
                        <li>
                            <hr/>
                        </li>
                        <li class="filter-list-item right">
                            <button type="submit" class="button icon search">${warehouse.message(code: 'default.button.find.label')}</button>
                        </li>
                    </ul>
                </g:form>
            </div>


        </div>
        <div class="yui-u">

            <div class="list box dialog">
                <h2><warehouse:message code="users.label"/> (${jobKeys.size()} results)</h2>
                <table>
                    <thead>
                    <tr>
                        <th>job key</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${jobKeys}" status="i" var="jobKey">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link controller="jobs" action="show" id="${jobKey.name}" >${jobKey.name}</g:link></td>
                            <td>${jobKey.properties}</td>
                        </tr>
                    </g:each>
                    <g:unless test="${jobKeys}">
                        <tr>
                            <td colspan="7">
                                <p class="empty center">
                                    <warehouse:message code="users.empty.label" default="No jobs returned"/>
                                </p>
                            </td>
                        </tr>
                    </g:unless>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${jobKeys}" params="${params}"/>
            </div>
        </div>
    </div>
</div>

</body>
</html>
