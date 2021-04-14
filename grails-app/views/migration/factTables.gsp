<%--
  Created by IntelliJ IDEA.
  User: jmiranda
  Date: 4/13/21
  Time: 11:43 AM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<div id="tabs-4">
    <div class="box">
        <h2><g:message code="data.facts.label" default="Facts"/></h2>
        <table>
            <thead>
            <tr>
                <th>Table</th>
                <th>Count</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr class="prop">
                <td class="name">Transaction Facts</td>
                <td class="value">${transactionFactCount}</td>
                <td>
                    <g:remoteLink controller="report" action="refreshTransactionFact" class="button"
                            onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">Consumption Facts</td>
                <td class="value">${consumptionFactCount}</td>
                <td>
                    <g:remoteLink controller="report" action="refreshConsumptionFact" class="button"
                            onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">Stockout Facts</td>
                <td class="value">${stockoutFactCount}</td>
                <td>
                    <g:remoteLink controller="report" action="refreshStockoutFact" class="button"
                            onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                </td>
            </tr>
            </tbody>
            <tfoot>
            <tr>
                <td></td>
                <td>
                    <div class="button-container">
                        <g:link controller="report" action="truncateFacts" class="button">Truncate</g:link>
                        <g:link controller="report" action="buildFacts" class="button">Build</g:link>
                    </div>
                </td>
                <td></td>
            </tr>
            </tfoot>
        </table>
    </div>
</div>
