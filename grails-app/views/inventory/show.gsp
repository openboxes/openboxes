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
                                <label>Select a location</label>
                                <div>
                                    <g:selectLocation name="location" class="chzn-select-deselect" value="${command?.location?.id}" noSelection="['':'']"/>
                                </div>
                            </div>
                            <div class="prop">
                                <label>Select products by tag</label>
                                <div>
                                    <g:selectTag name="tag" class="chzn-select-deselect" value="${command?.tag?.id}" noSelection="['':'']"/>
                                </div>
                            </div>
                            <div class="prop">
                                <label>Select a date</label>
                                <div>
                                    <g:jqueryDatePicker id="startDate" name="startDate" value="${command?.startDate}" format="MM/dd/yyyy" size="30"/>
                                </div>
                            </div>
                            <div class="prop">
                                <button class="button icon search">Search</button>
                            </div>
                        </div>
                    </g:form>
                </div>

            </div>
            <div class="yui-u">

                <div class="box">
                    <h2 class="middle">
                        Quantity On Hand Report
                        <g:if test="${command?.location}">
                            &rsaquo; ${command?.location?.name}
                        </g:if>
                        <g:if test="${command?.tag}">
                            &rsaquo; ${command?.tag?.tag}
                        </g:if>
                        <g:if test="${command?.startDate}">
                            &rsaquo; ${command?.startDate.format("MMM dd yyyy")}
                        </g:if>
                        <g:if test="${quantityMap}">
                            &rsaquo; ${quantityMap.keySet().size()} results
                        </g:if>
                    </h2>
                    <div class="right" style="padding: 15px;">
                        <g:if test="${quantityMap}">
                            <g:link class="button icon log" controller="inventory" action="download" params="[startDate:command.startDate.format('MM/dd/yyyy'), location: command?.location?.id, tag: command?.tag?.id]">
                                Download CSV
                            </g:link>
                        </g:if>
                    </div>
                    <table>
                        <thead>
                        <tr>
                            <th width="25px;"><warehouse:message code="product.productCode.label"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="product.manufacturer.label"/></th>
                            <th><warehouse:message code="product.vendor.label"/></th>
                            <th class="right"><warehouse:message code="default.quantity.label"/></th>
                            <th><warehouse:message code="product.uom.label"/></th>
                        </tr>
                        </thead>
                        <g:each var="entry" in="${quantityMap.sort()}" status="i">
                            <g:set var="product" value='${entry?.key}'/>
                            <tr class="${i%2?'even':'odd'} prop">
                                <td>
                                    ${product?.productCode}
                                </td>
                                <td>
                                    <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">${product?.name}</g:link>
                                </td>
                                <td>
                                    ${product?.manufacturer}
                                    ${product?.manufacturerCode}
                                </td>
                                <td>
                                    ${product?.vendor}
                                    ${product?.vendorCode}
                                </td>
                                <td class="right">
                                    <g:formatNumber number="${entry.value}"/>
                                </td>
                                <td>
                                    ${product?.unitOfMeasure}
                                </td>
                            </tr>
                        </g:each>
                    </table>
                    <g:unless test="${quantityMap}">
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