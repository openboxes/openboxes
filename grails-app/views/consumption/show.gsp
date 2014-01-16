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


                <div class="box" id="parameters-box" style="display: none;">
                    <h2><warehouse:message code="consumption.parameters.label" default="Parameters"/></h2>
                    <g:if test="${command?.toDate && command?.fromDate && command.fromLocations}">
                        <div class="left" style="width:50%">
                            <table border="1">
                                <tr>
                                    <td class="right" width="33%">
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
                                            <span><format:metadata obj="${transactionType}"/></span>
                                        </g:each>

                                    </td>
                                </tr>
                                <tr>
                                    <td class="right">
                                        <label><warehouse:message code="consumption.fromLocations.label" default="Transferred from"/></label>
                                    </td>
                                    <td>
                                        <g:each var="fromLocation" in="${command.fromLocations}">
                                            <span>${fromLocation.name}</span>
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
                                                <span>${toLocation.name}</span>
                                            </g:each>
                                        </g:if>
                                        <g:else>
                                            <div>
                                                <div class="action-menu">
                                                    ${command.selectedLocations?.size()} locations
                                                    <a href="javascript:void(-1);" class="action-btn">view all</a>
                                                    <div class="actions" style="padding:10px; border: 1px solid lightgrey; max-width: 600px; max-height:300px; overflow-y: scroll">
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
                        <div class="right" style="width:50%;">
                            <table border="1">
                                <tr>
                                    <td class="right" width="25%">
                                        <label><warehouse:message code="consumption.products.label" default="Products"/></label>
                                    </td>
                                    <td>
                                        ${command.rows.keySet().size()} products
                                    </td>
                                </tr>
                                <tr>
                                    <td class="right">
                                        <label><warehouse:message code="consumption.tags.label" default="Tags"/></label>
                                    </td>
                                    <td>
                                        <g:each var="tag" in="${command.selectedTags}">
                                            <span>${tag.tag}</span>
                                        </g:each>
                                        <g:unless test="${command?.selectedTags}">
                                            <span><warehouse:message code="default.none.label"/></span>
                                        </g:unless>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="right">
                                        <label><warehouse:message code="consumption.categories.label" default="Categories"/></label>
                                    </td>
                                    <td>
                                        <g:each var="category" in="${command.selectedCategories}">
                                            <format:metadata obj="${category}"/>
                                        </g:each>
                                        <g:unless test="${command?.selectedCategories}">
                                            <span><warehouse:message code="default.none.label"/></span>
                                        </g:unless>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="right">
                                        <label><warehouse:message code="consumption.columns.label" default="Additional columns"/></label>
                                    </td>
                                    <td>
                                        <g:each var="property" in="${command.selectedProperties}">
                                            ${property}
                                        </g:each>
                                        <g:unless test="${command?.selectedProperties}">
                                            <span><warehouse:message code="default.none.label"/></span>
                                        </g:unless>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="right">
                                        <label><warehouse:message code="consumption.includeLocationBreakdown.label" default="Include location breakdown"/></label>
                                    </td>
                                    <td>
                                        ${command.includeLocationBreakdown?'yes':'no'}
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="clear"></div>
                    </g:if>
                    <g:else>
                        <table>
                            <tr>
                                <td class="center">
                                    <div class="empty fade">
                                        <warehouse:message code="default.parameters.empty.label" default="No parameters selected"/>
                                    </div>
                                </td>
                            </tr>
                       </table>

                    </g:else>
                </div>

                <div class="box">
                    <h2>
                        Returned ${command.rows?.keySet()?.size()} results
                    </h2>

                    <div>
                        <table>
                            <thead>

                                <tr>
                                    <th class="center border-right"><warehouse:message code="product.productCode.label"/></th>
                                    <th class="border-right"><warehouse:message code="product.name.label"/></th>

                                    <%--
                                    <th class="border-right"><warehouse:message code="category.label"/></th>
                                    <th class="center border-right"><warehouse:message code="product.unitOfMeasure.label"/></th>
                                    <th class="center border-right"><warehouse:message code="inventoryLevel.binLocation.label"/></th>
                                    <th class="center" colspan="2"><warehouse:message code="consumption.expired.label" default="Expired"/></th>
                                    <th class="centert" colspan="2"><warehouse:message code="consumption.damaged.label" default="Damaged"/></th>
                                    <th class="center border-right" colspan="2"><warehouse:message code="consumption.issued.label" default="Issued"/></th>
                                    --%>

                                    <th class="border-right center"><warehouse:message code="consumption.issued.label" default="Issued"/></th>
                                    <th class="border-right center"><warehouse:message code="consumption.expired.label" default="Expired"/></th>
                                    <th class="border-right center"><warehouse:message code="consumption.damaged.label" default="Damaged"/></th>
                                    <th class="border-right center"><warehouse:message code="consumption.returns.label" default="Returns"/></th>
                                    <%--
                                    <th class="center border-right" colspan="2"><warehouse:message code="consumption.returns.label" default="Returns"/></th>
                                    --%>
                                    <th class="center border-right"><warehouse:message code="consumption.balance.label" default="Balance"/></th>

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

                                    <tr class="prop ${i%2?'odd':'even'}">
                                        <%--
                                            ${(numberOfMonthsLeft<3&&numberOfMonthsLeft>0)?'error':} ${(numberOfMonthsLeft<0)?'notice':''}"
                                        --%>

                                        <td class="center border-right">
                                            ${product?.productCode}
                                        </td>
                                        <td class="border-right">
                                            <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">
                                                ${product?.name}
                                            </g:link>
                                        </td>

                                        <%--
                                        <td class="border-right">
                                            <span class="fade"><format:metadata obj="${product.category}"/></span>
                                        </td>
                                        <td class="center border-right">
                                            ${product?.unitOfMeasure}
                                        </td>
                                        <td class="center border-right">
                                            ${product?.getInventoryLevel(session.warehouse.id)?.binLocation}
                                        </td>
                                        --%>

                                        <%--
                                        <td class="center">
                                            ${row.expiredQuantity}
                                        </td>
                                        <td class="center">
                                            <span class="fade">(${row.expiredTransactions?.size()})</span>

                                        </td>
                                        <td class="center">
                                            ${row.damagedQuantity}
                                        </td>
                                        <td class="center">
                                            <span class="fade">(${row.damagedTransactions?.size()})</span>
                                        </td>
                                        <td class="center">
                                            ${row.transferOutQuantity}
                                        </td>
                                        <td class="center border-right">
                                            <span class="fade">(${row.transferOutTransactions?.size()})</span>
                                        </td>
                                        --%>

                                        <td class="middle center border-right">
                                            <div class="debit">${row.transferOutQuantity}</div>
                                        </td>
                                        <td class="middle center border-right">
                                            <div class="debit">${row.expiredQuantity}</div>
                                        </td>
                                        <td class="middle center border-right">
                                            <div class="debit">${row.damagedQuantity}</div>
                                        </td>
                                        <td class="center border-right">
                                            N/A
                                        </td>


                                        <td class="center border-right">
                                            ${row.transferBalance}
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
                                        <td colspan="20" class="empty center" style="display: inline-block; vertical-align: middle; line-height: normal;">
                                            <warehouse:message code="default.data.empty.label" default="No data to be displayed"/>
                                        </td>
                                    </tr>
                                </g:unless>

                            </tbody>
                        </table>
                    </div>
                </div>

            <%--
                <div class="right">
                    <table style="width:auto;" class="box">
                        <tr>
                            <th>Key</th>
                        </tr>
                        <tr class="notice">
                            <td>Negative consumption rate due to mismatch between tranfer out and transfer in</td>
                        </tr>
                        <tr class="error">
                            <td>Low stock alert</td>
                        </tr>
                    </table>
                </div>
                <div class="clear"></div>
                --%>
            </div>
        </div>
     </div>
    <script type="text/javascript">
        $(document).ready(function() {

            $("#parameters-toggle").click(function() {
                $("#parameters-box").toggle();
            });
        });
    </script>

</body>
</html>