<%@ page import="org.pih.warehouse.core.Constants" %>
<table>
    <tbody>
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
                <label><g:message code="transactions.label"/></label>
            </td>
            <td class="value">
                <g:formatNumber number="${transactionCount}" maxFractionDigits="0"/>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label><g:message code="transactionFact.minTransactionDate.label" default="Earliest Transaction"/></label>
            </td>
            <td>
                <g:formatDate date="${minTransactionDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_TIME_FORMAT}"/>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label><g:message code="transactionFact.maxTransactionDate.label" default="Latest Transaction"/></label>
            </td>
            <td>
                <g:formatDate date="${maxTransactionDate}" format="${org.pih.warehouse.core.Constants.DEFAULT_DATE_TIME_FORMAT}"/>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label>Previous Refresh</label>
            </td>
            <td class="value">
                <span title="${previousFireTime}"><g:prettyDateFormat date="${previousFireTime}"/></span>
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
    </tbody>
    <g:isSuperuser>
        <tfoot>
        <tr>
            <th colspan="2">
                <div class="buttons center">
                    <a href="javascript:void(0);"
                       id="refreshTransactionFact" class="button"
                       data-url="${request.contextPath}/report/refreshTransactionFact"
                        data-confirmation-prompt="${warehouse.message(code: 'default.button.delay.confirm.message', default: 'Are you sure?')}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'reload.png')}" />
                        ${message(code:"default.button.refresh.label")} ${message(code:"default.data.label")}
                    </a>
                </div>
            </th>
        </tr>
        </tfoot>
    </g:isSuperuser>
</table>
