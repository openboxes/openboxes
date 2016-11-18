<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title>
            <warehouse:message code="${controllerName}.${actionName}.label"/>
        </title>

        <style>
            .a_normal { background-color: #dff0d8; }
            .b_warning { background-color: #fcf8e3; }
            .c_danger { background-color: #f2dede; }

        </style>
    </head>
	<body>
		<div class="body" style="margin: 0; padding: 0">

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>

            <div class="yui-gf" style="margin: 0; padding: 0">
                <div class="yui-u first filters">
                    <div>
                        <div style="margin: 20px;">
                            <h1><warehouse:message code="inventory.status.label" default="Status"/></h1>

                            <table>

                                <g:each in="${expirationSummary}" var="status">
                                    <%-- exclude subtotal rows --%>
                                    <g:if test="${status.label}">
                                        <tr class="prop ${status.styleClass} ${status.code} ${status.status}">
                                            <td>
                                                <a href="${status.url}">${status.label}</a>
                                            </td>
                                            <td class="right">
                                                <a href="${status.url}">${status.count}</a>
                                            </td>
                                        </tr>
                                    </g:if>

                                </g:each>
                            </table>
                        </div>
                        <hr/>
                    </div>
                                    </div>
                <div class="yui-u">

                    <h1 style="margin-top: 10px;">
                        <warehouse:message code="dashboard.${params.id}.label"/>
                        <small><warehouse:message code="default.showing.message" args="[inventoryItemList?.size()]"/></small>
                        <g:link params="[format:'csv']" controller="${controllerName}" action="${actionName}" id="${params.id}" class="button">Download CSV</g:link>
                    </h1>

                    <div style="margin: 10px; max-height:670px;overflow:auto;" >
                        <table>
                            <tr>
                                <th class="center"><warehouse:message code="inventoryLevel.status.label"/></th>
                                <th><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <%--<th><warehouse:message code="product.genericProduct.label"/></th>--%>
                                <th><warehouse:message code="category.label"/></th>
                                <th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
                                <th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
                                <%--
                                <th><warehouse:message code="product.manufacturer.label"/></th>
                                <th><warehouse:message code="product.vendor.label"/></th>
                                <th class="left"><warehouse:message code="inventoryLevel.binLocation.label"/></th>
                                <th class="left"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Analysis Class"/></th>
                                --%>
                                <%--
                                <th class="center"><warehouse:message code="inventoryLevel.minimumQuantity.label"/></th>
                                <th class="center"><warehouse:message code="inventoryLevel.reorderQuantity.label"/></th>
                                <th class="center"><warehouse:message code="inventoryLevel.maximumQuantity.label"/></th>
                                --%>
                                <th class="center border-right" colspan="2"><warehouse:message code="inventory.quantity.label" default="Quantity"/></th>
                                <th><warehouse:message code="product.pricePerUnit.label" default="Price per unit"/></th>
                                <th class="center"><warehouse:message code="product.totalValue.label" default="Total amount"/></th>
                            </tr>
                            <tbody>
                                    <g:each var="entry" in="${inventoryItemList}" status="i">
                                        <tr class="prop ${i%2?'odd':'even'} ${entry.statusCode}">
                                            <td>
                                                <g:message code="dashboard.${entry.status}.label"/>
                                                <%--
                                                <g:if test="${statusMap}">
                                                    <g:set var="status" value="${statusMap[entry?.key]}"/>
                                                </g:if>
                                                <g:else>
                                                    <g:set var="status" value="${entry?.key?.getStatus(session.warehouse.id, entry?.value?:0 as int)}"/>
                                                </g:else>
                                                ${warehouse.message(code:'enum.InventoryLevelStatus.'+status)}
                                                --%>
                                            </td>
                                            <td>
                                                ${entry.productCode}
                                            </td>
                                            <td>
                                                <g:link controller="inventoryItem" action="showStockCard" id="${entry.id}">
                                                    ${entry?.productName}
                                                </g:link>
                                            </td>
                                            <%--
                                            <td>
                                                ${entry?.genericProductName?:""}
                                            </td>
                                            --%>
                                            <td>
                                                ${entry?.categoryName}

                                            </td>
                                            <td>
                                                ${entry?.lotNumber}

                                            </td>
                                            <td>
                                                ${entry?.expirationDate}

                                            </td>
                                            <%--
                                            <td>
                                                ${entry.manufacturer}

                                            </td>
                                            <td>
                                                ${entry.vendor}
                                            </td>
                                            <td class="left">
                                                ${entry?.binLocation?:""}
                                            </td>
                                            <td class="center">
                                                ${entry?.abcClass?:""}
                                            </td>
                                            --%>

                                    <%--
                                    <td class="center">
                                        ${entry?.minQuantity?:"--"}
                                    </td>
                                    <td class="center">
                                        ${entry?.reorderQuantity?:"--"}
                                    </td>
                                    <td class="center">
                                        ${entry?.maxQuantity?:"--"}
                                    </td>
                                    --%>
                                            <td class="center">
                                                ${entry.quantity}
                                            </td>
                                            <td class="center border-right">
                                                ${entry?.unitOfMeasure}
                                            </td>
                                    <td class="center">
                                        <g:if test="${entry?.pricePerUnit}">
                                            <g:formatNumber number="${entry.pricePerUnit}" minFractionDigits="2"/>
                                        </g:if>
                                        <g:else>
                                            --
                                        </g:else>
                                    </td>
                                    <td class="center">
                                        <g:if test="${entry.totalCost}">
                                            <g:formatNumber number="${entry.totalCost}" minFractionDigits="2"/>
                                        </g:if>
                                        <g:else>
                                            --
                                        </g:else>
                                    </td>
                                </tr>
                                </g:each>
                            </tbody>

                            <g:unless test="${!quantityMap}">
                                <tr>
                                    <td colspan="12" class="center">
                                        <div class="empty fade">
                                            <warehouse:message code="default.emptyResults.message" default="No results found"/>
                                        </div>
                                    </td>

                                </tr>

                            </g:unless>
                        </table>
                    </div>
                    <hr/>
                    <div class="title right middle">
                        <warehouse:message code="inventory.totalValue.label" default="Total value"/>
                        <small><g:formatNumber number="${totalValue?:0}"/>
                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}</small>
                    </div>
                </div>
            </div>
		</div>
		
	</body>

</html>