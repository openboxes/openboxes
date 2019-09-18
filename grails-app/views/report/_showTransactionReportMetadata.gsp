<%@ page import="org.pih.warehouse.core.Constants" %>
<table>
    <tr class="prop">
        <td class="name">
            <label><g:message code="transactions.label"/></label>
        </td>
        <td class="value">
            <g:formatNumber number="${transactionCount}" maxFractionDigits="0"/>
        </td>
    </tr>
    <tr class="prop">
        <td class="name">
            <label><g:message code="products.label"/></label>
        </td>
        <td class="value">
            <g:formatNumber number="${productCount}" maxFractionDigits="0"/>
        </td>
    </tr>
    <tr class="prop">
        <td class="name">
            <label><g:message code="transactionFact.minTransactionDate.label" default="Earliest"/></label>
        </td>
        <td>
            <g:formatDate date="${minTransactionDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}"/>
        </td>
    </tr>
    <tr class="prop">
        <td class="name">
            <label><g:message code="transactionFact.maxTransactionDate.label" default="Latest"/></label>
        </td>
        <td>
            <g:formatDate date="${maxTransactionDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_FORMAT}"/>
        </td>
    </tr>
    <tr class="prop">
        <td class="name">
            <label>Next Refresh</label>
        </td>
        <td class="value">
            <span title="${nextFireTime}"><g:prettyDateFormat date="${nextFireTime}"/></span>
        </td>
    </tr>
</table>
<g:isSuperuser>
    <div class="buttons center">
        <a href="javascript:void(0);"
           id="refreshTransactionFact" class="button"
           data-url="${request.contextPath}/report/refreshTransactionFact"
            data-confirmation-prompt="${warehouse.message(code: 'default.button.delay.confirm.message', default: 'Are you sure?')}">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'reload.png')}" />
            ${message(code:"default.button.refresh.label")} ${message(code:"default.data.label")}
        </a>
    </div>
</g:isSuperuser>
