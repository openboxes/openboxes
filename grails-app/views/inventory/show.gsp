<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
</head>
<body>
    <div class="body">




        <%--
                    <h1>${location}</h1>
                    ${quantityMap.keySet().size()} results (${elapsedTime/1000} seconds)
                    --%>
        <%--
        <div class="action-menu">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
                     style="vertical-align: middle" />
            </button>
            <div class="actions left">
                <div class="action-menu-item">
                    <g:link controller="inventory" action="show" params="['format':'csv']"><img
                            src="${createLinkTo(dir:'images/icons/silk',file:'disk_download.png')}"
                    /> &nbsp;<warehouse:message code="default.export.label" args="['CSV']"/>
                    </g:link>
                </div>
            </div>
        </div>
        --%>
        <div class="yui-gf">
            <div class="yui-u first">

                <div class="box" style="height:100%;">
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
                                    <g:selectTag name="tag" class="chzn-select-deselect" value="${command?.tag?.id}" noSelection="['':'']"/>
                                </div>
                            </div>
                            <div class="prop">
                                <label><warehouse:message code="reporting.startDate.label" default="Start date"/></label>
                                <div>
                                    <g:jqueryDatePicker id="startDate" name="startDate" value="${command?.startDate}" format="MM/dd/yyyy" size="30"/>
                                </div>
                            </div>
                            <div class="prop">
                                <label><warehouse:message code="reporting.endDate.label" default="End date"/></label>
                                <div>
                                    <g:jqueryDatePicker id="endDate" name="endDate" value="${command?.endDate}" format="MM/dd/yyyy" size="30"/>
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
                    <h2 class="middle">
                        Quantity On Hand Report
                        <%--
                        <g:if test="${command?.locations}">
                            <g:each var="location" in="${command?.locations}">
                                &rsaquo; ${location?.name}
                            </g:each>
                        </g:if>
                        <g:if test="${command?.tag}">
                            &rsaquo; ${command?.tag?.tag}
                        </g:if>
                        <g:if test="${command?.startDate}">
                            &rsaquo; ${command?.startDate.format("MMM dd yyyy")}
                        </g:if>
                        --%>
                        <g:if test="${command?.products}">
                            (${command?.products.size()} results)
                        </g:if>
                    </h2>
                    <div class="right" style="padding: 15px;">
                        <%--
                        <g:if test="${quantityMap}">
                            <g:link class="button icon log" controller="inventory" action="download" params="[startDate:command.startDate.format('MM/dd/yyyy'), location: command?.location?.id, tag: command?.tag?.id]">
                                Download CSV
                            </g:link>
                        </g:if>
                        --%>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th width="25px;"><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <th><warehouse:message code="product.uom.label"/></th>
                                <th><warehouse:message code="product.manufacturer.label"/></th>
                                <th><warehouse:message code="product.vendor.label"/></th>
                                <g:each var="date" in="${command?.dates}">
                                    <th class="right"><g:formatDate date="${date}" format="MMM dd"/></th>
                                </g:each>
                            </tr>
                        </thead>
                        <g:each var="product" in="${command?.products}" status="i">

                            <tr class="${i%2?'even':'odd'} prop">
                                <td>
                                    ${product?.productCode}
                                </td>
                                <td>
                                    <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">${product?.name}</g:link>
                                </td>
                                <td>
                                    ${product?.unitOfMeasure}
                                </td>
                                <td>
                                    ${product?.manufacturer}
                                    ${product?.manufacturerCode}
                                </td>
                                <td>
                                    ${product?.vendor}
                                    ${product?.vendorCode}
                                </td>
                                <g:each var="date" in="${command.dates}">
                                    <td class="right">
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