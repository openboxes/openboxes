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
                    <h2>
                        ${warehouse.message(code:'consumption.label')} <small>(${command.rows?.keySet()?.size()} results)</small>
                    </h2>

                    <div >
                        <table class="dataTable">
                            <thead>

                                <tr>
                                    <th><warehouse:message code="product.productCode.label"/></th>
                                    <th><warehouse:message code="product.label"/></th>
                                    <th class="center"><warehouse:message code="consumption.unitPrice.label" default="Unit Price"/></th>
                                    <th class="center"><warehouse:message code="consumption.issued.label" default="Issued"/></th>
                                    <th class="center"><warehouse:message code="consumption.consumed.label" default="Consumed"/></th>
                                    <th class="center"><warehouse:message code="consumption.returned.label" default="Returned"/></th>
                                    <th class="center"><warehouse:message code="consumption.total.label" default="Total Consumption"/></th>
                                    <th class="center"><warehouse:message code="consumption.totalConsumptionValue.label" default="Total Consumption Value"/></th>
                                    <th class="center"><warehouse:message code="consumption.monthly.label" default="Monthly"/></th>
                                    <th class="center"><warehouse:message code="consumption.qoh.label" default="QoH"/></th>
                                    <th class="center"><warehouse:message code="consumption.months.label" default="Months remaining"/></th>
                                </tr>
                            </thead>
                            <tbody>

                                <g:each var="entry" in="${command.rows}" status="i">
                                    <g:set var="row" value="${entry.value}"/>
                                    <g:set var="product" value="${entry.key}"/>
                                    <g:set var="monthlyQuantity" value="${row.monthlyQuantity}"/>
                                    <g:set var="onHandQuantity" value="${row.onHandQuantity}"/>
                                    <g:set var="numberOfMonthsLeft" value="${onHandQuantity / monthlyQuantity}"/>
                                    <g:set var="totalConsumptionValue" value="${(product?.pricePerUnit ?: 0) * row.totalConsumptionQuantity}"/>

                                    <tr>
                                        <td>
                                            <a href="javascript:void(0);" class="btn-show-dialog" data-position="top"
                                               data-width="1200" data-height="600"
                                               data-title="${g.message(code:'product.label')}"
                                               data-url="${request.contextPath}/consumption/product?id=${product?.id}">
                                                ${product?.productCode}
                                            </a>
                                        </td>
                                        <td>
                                            <a href="javascript:void(0);" class="btn-show-dialog" data-position="top"
                                               data-width="1200" data-height="600"
                                               data-title="${g.message(code:'product.label')}"
                                               data-url="${request.contextPath}/consumption/product?id=${product?.id}">
                                                ${product?.name}
                                            </a>
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${product.pricePerUnit}" maxFractionDigits="2"/>
                                        </td>
                                        <td class="center">
                                            <div class="debit">${row.issuedQuantity}</div>
                                        </td>
                                        <td class="center">
                                            <div class="debit">${row.consumedQuantity}</div>
                                        </td>
                                        <td class="center">
                                            ${row.returnedQuantity}
                                        </td>
                                        <td class="center">
                                            ${row.totalConsumptionQuantity}
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${totalConsumptionValue}" maxFractionDigits="2"/>
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${monthlyQuantity}" maxFractionDigits="4"/>
                                        </td>
                                        <td class="center">
                                            <g:formatNumber number="${row.onHandQuantity}" maxFractionDigits="0"/>
                                        </td>

                                        <td class="center">
                                            <g:formatNumber number="${row.numberOfMonthsRemaining}" maxFractionDigits="0"/>
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                            </tfoot>
                        </table>
                    </div>
                </div>
            </div>
        </div>
     </div>
    <script type="text/javascript">
        $(document).ready(function() {

            $(".dataTable").dataTable({
                "bJQueryUI": true,
                "iDisplayLength": 100,
                "bScrollInfinite": true,
                "bScrollCollapse": true,
                "sScrollY": '50vh'
            });

            $(".tabs").livequery(function(){
                $(this).tabs({
                    cookie: {
                        expires: 1 // store cookie for a day, without, it would be a session cookie
                    }
                }
            )});


        });
    </script>
</body>
</html>
