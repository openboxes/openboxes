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

                    <g:if test="${command?.toDate && command?.fromDate && command.fromLocations}">
                        <div class="yui-g">
                            <div class="yui-u first">
                                <table style="width:auto;">
                                    <tr>
                                        <td class="right">
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
                                            <g:set var="numberOfDays" value="${command.numberOfDays}"/>
                                            <g:set var="numberOfWeeks" value="${command.numberOfWeeks}"/>
                                            <g:set var="numberOfMonths" value="${command.numberOfMonths}"/>
                                            ${numberOfDays} days
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
                                            <g:if test="${command.selectedLocations?.size()<10}">
                                                <g:each var="toLocation" in="${command.selectedLocations}">
                                                    ${toLocation.name}
                                                </g:each>
                                            </g:if>
                                            <g:else>
                                                <div>
                                                    <div class="action-menu">
                                                        ${command.selectedLocations?.size()} locations
                                                        <a href="javascript:void(-1);" class="action-btn">view all</a>
                                                        <div class="actions" style="padding:10px; border: 1px solid lightgrey;">
                                                            <ul>
                                                                <g:each var="toLocation" in="${command.selectedLocations.sort()}">
                                                                    <li>${toLocation.name}</li>
                                                                </g:each>
                                                            </ul>
                                                        </div>
                                                    </div>
                                                </div>
                                            </g:else>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <div class="yui-u">
                                <table style="width: auto;">
                                    <tr class="prop">
                                        <td class="left">
                                            <label><warehouse:message code="consumption.products.label" default="Products"/></label>
                                        </td>
                                        <td>
                                            ${command.rows.keySet().size()} products
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="left">
                                            <label><warehouse:message code="consumption.tags.label" default="Tags"/></label>
                                        </td>
                                        <td>
                                            <g:each var="tag" in="${command.selectedTags}">
                                                <span class="tag">${tag.tag}</span>
                                            </g:each>
                                            <g:unless test="${command?.selectedTags}">
                                                <span class='fade'><warehouse:message code="default.none.label"/></span>
                                            </g:unless>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="left">
                                            <label><warehouse:message code="consumption.categories.label" default="Categories"/></label>
                                        </td>
                                        <td>
                                            <g:each var="category" in="${command.selectedCategories}">
                                                <format:metadata obj="${category}"/>
                                            </g:each>
                                            <g:unless test="${command?.selectedCategories}">
                                                <span class='fade'><warehouse:message code="default.none.label"/></span>
                                            </g:unless>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="left">
                                            <label><warehouse:message code="consumption.columns.label" default="Additional columns"/></label>
                                        </td>
                                        <td>
                                            <g:each var="property" in="${command.selectedProperties}">
                                                ${property}
                                            </g:each>
                                            <g:unless test="${command?.selectedProperties}">
                                                <span class='fade'><warehouse:message code="default.none.label"/></span>
                                            </g:unless>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="left">
                                            <label><warehouse:message code="consumption.includeLocationBreakdown.label" default="Include location breakdown"/></label>
                                        </td>
                                        <td>
                                            ${command.includeLocationBreakdown?'yes':'no'}
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </g:if>
                    <hr/>

                    <table>
                        <thead>
                            <tr>
                                <td colspan="" class="border-right"></td>
                                <td colspan="" class="border-right"></td>
                                <td colspan="" class="border-right"></td>
                                <td colspan="" class="border-right"></td>
                                <td colspan="" class="border-right"></td>
                                <td colspan="2" class="border-right">
                                    <label>Credit breakdown </label><div class="fade">(number of transactions)</div>
                                </td>
                                <td colspan="6" class="center border-right">
                                    <label>Debit breakdown </label><div class="fade">(number of transactions)</div>
                                </td>
                                <td colspan="3" class="center border-right">
                                    <label>Consumption breakdown</label>
                                </td>
                                <td colspan="2" class="center border-right">
                                    <label>Remaining</label>
                                </td>
                                <%--
                                <g:if test="${command.selectedProperties}">
                                    <td colspan="${command.selectedProperties.size()}">
                                        <label>Custom columns</label>
                                    </td>
                                </g:if>
                                --%>
                            </tr>
                            <tr>
                                <th class="center border-right"><warehouse:message code="product.productCode.label"/></th>
                                <th class="border-right"><warehouse:message code="product.name.label"/></th>
                                <th class="border-right"><warehouse:message code="category.label"/></th>
                                <th class="center border-right"><warehouse:message code="product.unitOfMeasure.label"/></th>
                                <th class="center border-right"><warehouse:message code="inventoryLevel.binLocation.label"/></th>
                                <th class="center border-right" colspan="2"><warehouse:message code="consumption.returns.label" default="Returns"/></th>
                                <th class="center" colspan="2"><warehouse:message code="consumption.consumed.label" default="Consumed"/></th>
                                <th class="center" colspan="2"><warehouse:message code="consumption.expired.label" default="Expired"/></th>
                                <th class="center border-right" colspan="2"><warehouse:message code="consumption.damaged.label" default="Damaged"/></th>
                                <th class="center"><warehouse:message code="consumption.monthly.label" default="Monthly"/></th>
                                <th class="center"><warehouse:message code="consumption.weekly.label" default="Weekly"/></th>
                                <th class="center border-right"><warehouse:message code="consumption.daily.label" default="Daily"/></th>
                                <th class="center"><warehouse:message code="consumption.onHand.label" default="On hand"/></th>
                                <th class="center border-right"><warehouse:message code="consumption.monthsLeft.label" default="Months left"/></th>
                                <%--
                                <g:each var="property" in="${command.selectedProperties}">
                                    <th>${property}</th>
                                </g:each>
                                --%>
                            </tr>
                        </thead>
                        <tbody>

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
                                    <td class="border-right">
                                        <span class="fade"><format:metadata obj="${product.category}"/></span>
                                    </td>
                                    <td class="center border-right">
                                        ${product?.unitOfMeasure}
                                    </td>
                                    <td class="center border-right">
                                        ${product?.getInventoryLevel(session.warehouse.id)?.binLocation}
                                    </td>
                                    <td class="center">
                                        ${row.transferInQuantity}
                                    </td>
                                    <td class="center border-right">
                                        <span class="fade">(${row.transferInTransactions?.size()})</span>
                                    </td>
                                    <td class="center">
                                        ${row.transferOutQuantity}
                                    </td>
                                    <td class="center">
                                        <span class="fade">(${row.transferOutTransactions?.size()})</span>
                                    </td>
                                    <td class="center">
                                        ${row.expiredQuantity}
                                    </td>
                                    <td class="center">
                                        <span class="fade">(${row.expiredTransactions?.size()})</span>

                                    </td>
                                    <td class="center">
                                        ${row.damagedQuantity}
                                    </td>
                                    <td class="center border-right">
                                        <span class="fade">(${row.damagedTransactions?.size()})</span>
                                    </td>
                                    <td class="center">
                                        <g:formatNumber number="${row.monthlyQuantity}" format="###,###.#" maxFractionDigits="1"/>
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
                                    <td class="center border-right">
                                        <g:formatNumber number="${row.numberOfMonthsRemaining}" format="###,###.#" maxFractionDigits="1"/>
                                    </td>
                                    <%--
                                    <g:each var="property" in="${params.selectedProperties}">
                                        <td>${row.product."$property"}</td>
                                    </g:each>
                                    --%>
                                </tr>
                            </g:each>
                            <g:unless test="${command?.rows}">
                                <tr class="prop">
                                    <td colspan="15" class="empty center">
                                        <warehouse:message code="default.empty.label"/>
                                    </td>
                                </tr>
                            </g:unless>

                        </tbody>
                        <tfoot>
                            <tr>
                                <td colspan="15">
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