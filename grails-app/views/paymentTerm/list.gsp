<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'paymentTerms.label', default: 'Payment Terms')}" />
    <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
    <style>
        .paginateButtons {
            border: 0;
            border-top: 2px solid lightgrey;
        }
    </style>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <div class="list">

            <div class="button-bar">
                <g:link class="button" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[entityName]"/>
                </g:link>
                <g:isUserAdmin>
                    <g:link class="button" action="create">
                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" args="[entityName]"/>
                    </g:link>
                </g:isUserAdmin>
            </div>

            <div class="box">
                <h2><warehouse:message code="default.list.label" args="[entityName]" /></h2>
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="id" title="${warehouse.message(code: 'default.id.label')}" />
                            <g:sortableColumn property="code" title="${warehouse.message(code: 'default.code.label')}" />
                            <g:sortableColumn property="name" title="${warehouse.message(code: 'default.name.label')}" />
                            <g:sortableColumn property="description" title="${warehouse.message(code: 'default.description.label')}" />
                            <g:sortableColumn property="prepaymentPercent" title="${warehouse.message(code: 'paymentTerm.prepaymentPercent.label')}" />
                            <g:sortableColumn property="daysToPayment" title="${warehouse.message(code: 'paymentTerm.daysToPayment.label')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${paymentTerms}" status="i" var="paymentTerm">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="edit" id="${paymentTerm.id}">${fieldValue(bean: paymentTerm, field: "id")}</g:link></td>
                            <td>${fieldValue(bean: paymentTerm, field: "code")}</td>
                            <td>${fieldValue(bean: paymentTerm, field: "name")}</td>
                            <td>${fieldValue(bean: paymentTerm, field: "description")}</td>
                            <td>${fieldValue(bean: paymentTerm, field: "prepaymentPercent")}${paymentTerm.prepaymentPercent ? '%' : ''}</td>
                            <td>${fieldValue(bean: paymentTerm, field: "daysToPayment")}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <div class="paginateButtons">
                    <g:paginate total="${paymentTermsTotal}" />
                </div>
            </div>
        </div>
    </div>
</body>
</html>
