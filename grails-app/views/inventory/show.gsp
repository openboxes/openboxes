<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
</head>
<body>
    <div class="body">

        <div class="yui-gf">
            <div class="yui-u first">

                <div class="box" style="height:100%;">
                    <h2 class="middle">
                        Baseline QoH Report
                    </h2>
                    <g:form controller="inventory" action="search">
                        <div class="filters">

                            <div class="prop">
                                <label>${warehouse.message(code:'locations.label')}</label>
                                <div>
                                    <g:selectLocation name="locations" multiple="true" class="chzn-select-deselect" value="${command?.locations?.id}" noSelection="['':'']" data-placeholder=" " />

                                </div>
                            </div>
                            <div class="prop">
                                <label><warehouse:message code="tag.label"/></label>
                                <div>
                                    <g:selectTag name="tags" class="chzn-select-deselect" multiple="multiple" value="${command?.tags}" noSelection="['':'']"/>
                                </div>
                            </div>
                            <div class="prop">
                                <label><warehouse:message code="reporting.startDate.label" default="Start date"/></label>
                                <div>
                                    <g:jqueryDatePicker id="startDate" name="startDate" value="${command?.startDate}" format="MM/dd/yyyy" size="30" autocomplete="off"/>
                                </div>
                            </div>
                            <div class="prop">
                                <label><warehouse:message code="reporting.endDate.label" default="End date"/></label>
                                <div>
                                    <g:jqueryDatePicker id="endDate" name="endDate" value="${command?.endDate}" format="MM/dd/yyyy" size="30" autocomplete="off"/>
                                </div>
                            </div>
                            <div class="prop">
                                <label><warehouse:message code="reporting.frequency.label" default="Frequency"/></label>
                                <div>
                                    <g:select name="frequency" value="${command?.frequency}" from="['','Daily','Weekly','Monthly','Quarterly','Annually']" class="chzn-select-deselect" />
                                </div>
                            </div>
                            <div class="prop">
                                <div class="center">
                                    <button name="button" value="search" class="button icon search">View</button>
                                    <button name="button" value="download" class="button icon log">Download</button>
                                    <g:link controller="inventory" action="show" class="button icon reload">Reset</g:link>
                                </div>
                            </div>
                        </div>
                    </g:form>
                </div>

            </div>
            <div class="yui-u">

                <g:hasErrors bean="${command}">
                    <div class="errors">
                        <g:renderErrors bean="${command}" as="list" />
                    </div>
                </g:hasErrors>

                <g:if test="${session.quantityMapByDate}">
                    <g:link controller="inventory" action="export" class="button icon log">Export as CSV</g:link>

                </g:if>
                <div class="box">
                    <h2>Results <g:if test="${command?.products}"><small>Returned ${command?.products.size()} results in ${elapsedTime/1000} seconds</small></g:if></h2>
                    <table class="dataTable">
                        <thead>
                            <tr>
                                <th><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <th><warehouse:message code="product.genericProduct.label"/></th>
                                <th><warehouse:message code="category.label"/></th>
                                <th><warehouse:message code="product.uom.label"/></th>
                                <th><warehouse:message code="product.manufacturer.label"/></th>
                                <th class="border-right"><warehouse:message code="product.vendor.label"/></th>
                                <g:each var="date" in="${command?.dates}">
                                    <th class="center"><g:formatDate date="${date}" format="MMM dd"/></th>
                                </g:each>
                            </tr>
                        </thead>
                        <g:each var="product" in="${command?.products}" status="i">

                            <tr class="${i%2?'even':'odd'} prop">
                                <td>
                                    ${product?.productCode}
                                </td>
                                <td>
                                    <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">
                                        ${product?.name}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="productGroup" action="edit" id="${product?.genericProduct?.id}">
                                        ${product?.genericProduct?.name?:""}
                                    </g:link>
                                </td>
                                <td>
                                    ${product?.category?.name}
                                </td>
                                <td>
                                    ${product?.unitOfMeasure}
                                </td>
                                <td>
                                    ${product?.manufacturer}
                                    <div class="fade">${product?.manufacturerCode}</div>
                                </td>
                                <td class="border-right">
                                    ${product?.vendor}
                                    <div class="fade">${product?.vendorCode}</div>
                                </td>
                                <g:each var="date" in="${command.dates}">
                                    <td class="center">
                                        <g:formatNumber number="${quantityMapByDate[date][product]}"/>
                                    </td>
                                </g:each>
                            </tr>
                        </g:each>
                    </table>
                    <g:unless test="${quantityMapByDate}">
                        <div class="empty center">
                            <warehouse:message code="default.noresults.label" default="No results"/>
                        </div>
                    </g:unless>
                </div>
            </div>
        </div>

    </div>
    </div>
</body>
</html>
