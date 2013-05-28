<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'consumption.label', default: 'Consumption').toLowerCase()}" />
    <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${command}">
            <div class="errors">
                <g:renderErrors bean="${command}" as="list" />
            </div>
        </g:hasErrors>
        <div class="yui-gf">
            <div class="yui-u first">
                <g:render template="filters" model="[command:command]"/>
            </div>
            <div class="yui-u">
                <div class="box">
                    <h2><warehouse:message code="consumption.label" default="Consumption"/></h2>

                    <table style="width:auto;">
                        <tr>
                            <td class="right" width="25%">
                                <label><warehouse:message code="consumption.reportingPeriod.label" default="Reporting period"/></label>
                            </td>
                            <td>
                                <g:formatDate date="${command.fromDate}" format="dd MMM yyyy"/> - <g:formatDate date="${command.toDate}" format="dd MMM yyyy"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="right">
                                <label><warehouse:message code="consumption.reportingPeriodDays.label" default="Reporting period days"/></label>
                            </td>
                            <td>
                                <g:if test="${command?.toDate && command?.fromDate}">
                                    <g:set var="numberOfDays" value="${command.toDate - command.fromDate}"/>
                                    <g:set var="numberOfWeeks" value="${numberOfDays / 7}"/>
                                    <g:set var="numberOfMonths" value="${numberOfDays / 30}"/>
                                    ${numberOfDays}
                                     days
                                </g:if>
                            </td>
                        </tr>
                        <tr>
                            <td class="right">
                                <label><warehouse:message code="consumption.transactionTypes.label" default="Transaction types"/></label>
                            </td>
                            <td>
                                <g:each var="transactionType" in="${command.transactionTypes}">
                                    <div>
                                        <format:metadata obj="${transactionType}"/>
                                    </div>
                                </g:each>

                            </td>
                        </tr>
                        <tr>
                            <td class="right">
                                <label><warehouse:message code="consumption.fromLocations.label" default="Transferred from"/></label>
                            </td>
                            <td>
                                <g:each var="fromLocation" in="${command.fromLocations}">
                                    ${fromLocation.name}
                                </g:each>
                            </td>
                        </tr>
                        <tr>
                            <td class="right">
                                <label><warehouse:message code="consumption.toLocations.label" default="Transferred to"/></label>
                            </td>
                            <td>
                                <g:each var="toLocation" in="${command.selectedLocations}">
                                    ${toLocation.name}
                                </g:each>
                            </td>
                        </tr>

                    </table>

                    <hr/>

                    <table>
                        <thead>
                            <tr>
                                <td colspan="3" class="border-right"></td>
                                <td colspan="4" class="center border-right">
                                    <label>Consumption</label>
                                </td>
                                <td colspan="2" class="center">
                                    <label>Stock remaining</label>
                                </td>
                            </tr>
                            <tr>
                                <th class="center"><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.name.label"/></th>
                                <th class="center border-right"><warehouse:message code="product.unitOfMeasure.label"/></th>
                                <th class="center"><warehouse:message code="consumption.total.label" default="Total"/></th>
                                <th class="center"><warehouse:message code="consumption.monthly.label" default="Monthly"/></th>
                                <th class="center"><warehouse:message code="consumption.weekly.label" default="Weekly"/></th>
                                <th class="center border-right"><warehouse:message code="consumption.daily.label" default="Daily"/></th>
                                <th class="center"><warehouse:message code="consumption.onHand.label" default="On hand"/></th>
                                <th class="center"><warehouse:message code="consumption.monthsLeft.label" default="# Months left"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:unless test="${command?.productMap}">
                                <tr class="prop">
                                    <td colspan="9" class="empty center">
                                        <warehouse:message code="default.empty.label"/>
                                    </td>
                                </tr>
                            </g:unless>

                            <g:each var="entry" in="${command.productMap}" status="i">
                                <g:set var="total" value="${entry.value}"/>
                                <g:set var="monthly" value="${entry.value/numberOfMonths}"/>
                                <g:set var="weekly" value="${entry.value/numberOfWeeks}"/>
                                <g:set var="daily" value="${entry.value/numberOfDays}"/>
                                <g:set var="onHandQuantity" value="${command.onHandQuantityMap[entry.key]}"/>
                                <g:set var="numberOfMonthsLeft" value="${onHandQuantity / monthly}"/>

                                <tr class="prop ${i%2?'odd':'even'} ${(numberOfMonthsLeft<3)?'error':''}" >
                                    <td class="center">
                                        ${entry?.key?.productCode}
                                    </td>
                                    <td>
                                        <g:link controller="inventoryItem" action="showStockCard" id="${entry?.key?.id}">
                                            ${entry?.key?.name}
                                        </g:link>
                                    </td>
                                    <td class="center border-right">
                                        ${entry?.key?.unitOfMeasure}
                                    </td>
                                    <td class="center">
                                        ${total}
                                    </td>
                                    <td class="center">
                                        ${monthly}
                                    </td>
                                    <td class="center">
                                        <g:formatNumber number="${total / numberOfWeeks}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                    <td class="center border-right">
                                        <g:formatNumber number="${total / numberOfDays}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                    <td class="center">
                                        <g:formatNumber number="${onHandQuantity}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                    <td class="center">
                                        <g:formatNumber number="${onHandQuantity / monthly}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td colspan="9">
                                    <span class="fade">Returned ${command.productMap?.keySet()?.size()} items</span>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
     </div>
</body>
</html>