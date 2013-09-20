<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
</head>
<body>

        <table>
            <tr>
                <td>
                    <h1>${location}</h1>
                    ${productQuantityMap.keySet().size()} results (${elapsedTime/1000} seconds)

                </td>
                <td style="text-align: right">
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

                </td>
            </tr>

        </table>



    <table>
        <thead>
            <tr>
                <th><warehouse:message code="product.productCode.label"/></th>
                <th><warehouse:message code="product.label"/></th>
                <th class="right"><warehouse:message code="default.quantity.label"/></th>
                <th><warehouse:message code="product.unitOfMeasure.label"/></th>
            </tr>
        </thead>
        <g:each var="entry" in="${productQuantityMap.findAll {it.value > 0}.sort()}" status="i">
            <g:set var="product" value='${entry?.key}'/>
            <tr class="${i%2?'even':'odd'}">
                <td>
                    ${product?.productCode}
                </td>
                <td>
                    <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">${product?.name}</g:link>
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



</body>
</html>