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
                            <td class="right" width="15%">
                                <label><warehouse:message code="consumption.reportingPeriod.label" default="Reporting period"/></label>
                            </td>
                            <td>
                                <g:if test="${command?.toDate && command?.fromDate}">
                                    <g:formatDate date="${command.fromDate}" format="dd MMM yyyy"/> - <g:formatDate date="${command.toDate}" format="dd MMM yyyy"/>
                                </g:if>
                            </td>
                        </tr>
                        <tr>
                            <td class="right">
                                <label><warehouse:message code="consumption.reportingPeriodDays.label" default="Reporting period days"/></label>
                            </td>
                            <td>
                                <g:if test="${command?.toDate && command?.fromDate}">
                                    <g:set var="numberOfDays" value="${command.numberOfDays}"/>
                                    <g:set var="numberOfWeeks" value="${command.numberOfWeeks}"/>
                                    <g:set var="numberOfMonths" value="${command.numberOfMonths}"/>
                                    ${numberOfDays} days
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
                                <td colspan="" class="border-right"></td>
                                <td colspan="" class="border-right"></td>
                                <td colspan="" class="border-right"></td>
                                <td colspan="4" class="center border-right">
                                    <label>Consumption</label>
                                </td>
                                <td colspan="2" class="center">
                                    <label>Remaining</label>
                                </td>
                            </tr>
                            <tr>
                                <th class="center border-right"><warehouse:message code="product.productCode.label"/></th>
                                <th class="border-right"><warehouse:message code="product.name.label"/></th>
                                <th class="center border-right"><warehouse:message code="product.unitOfMeasure.label"/></th>
                                <th class="center"><warehouse:message code="consumption.total.label" default="Total"/></th>
                                <th class="center"><warehouse:message code="consumption.monthly.label" default="Monthly"/></th>
                                <th class="center"><warehouse:message code="consumption.weekly.label" default="Weekly"/></th>
                                <th class="center border-right"><warehouse:message code="consumption.daily.label" default="Daily"/></th>
                                <th class="center"><warehouse:message code="consumption.onHand.label" default="On hand"/></th>
                                <th class="center"><warehouse:message code="consumption.monthsLeft.label" default="Months left"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:unless test="${command?.rows}">
                                <tr class="prop">
                                    <td colspan="9" class="empty center">
                                        <warehouse:message code="default.empty.label"/>
                                    </td>
                                </tr>
                            </g:unless>

                            <g:each var="entry" in="${command.rows}" status="i">
                                <g:set var="row" value="${entry.value}"/>
                                <g:set var="product" value="${entry.key}"/>
                                <g:set var="totalQuantity" value="${row.transferOutQuantity}"/>
                                <g:set var="monthlyQuantity" value="${row.monthlyQuantity}"/>
                                <g:set var="weeklyQuantity" value="${row.weeklyQuantity}"/>
                                <g:set var="dailyQuantity" value="${row.dailyQuantity}"/>
                                <g:set var="onHandQuantity" value="${row.onHandQuantity}"/>
                                <g:set var="numberOfMonthsLeft" value="${onHandQuantity / monthlyQuantity}"/>

                                <tr class="prop ${i%2?'odd':'even'} ${(numberOfMonthsLeft<3)?'error':''}" >
                                    <td class="center border-right">
                                        ${product?.productCode}
                                    </td>
                                    <td class="border-right">
                                        <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">
                                            ${product?.name}
                                        </g:link>
                                    </td>
                                    <td class="center border-right">
                                        ${product?.unitOfMeasure}
                                    </td>
                                    <td class="center">
                                        ${row.transferOutQuantity}
                                    </td>
                                    <td class="center">
                                        ${row.monthlyQuantity}
                                    </td>
                                    <td class="center">
                                        <g:formatNumber number="${row.weeklyQuantity}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                    <td class="center border-right">
                                        <g:formatNumber number="${row.dailyQuantity}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                    <td class="center">
                                        <g:formatNumber number="${row.onHandQuantity}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                    <td class="center">
                                        <g:formatNumber number="${row.numberOfMonthsRemaining}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td colspan="9">
                                    <span class="fade">Returned ${command.rows?.keySet()?.size()} items</span>
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