<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title><warehouse:message code="inventory.list.label"/></title>
    </head>    

	<body>
		<div class="body">


            <h3>
                <warehouse:message code="${controllerName}.${actionName}.label"/>
            </h3>

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>

            <div class="yui-gf">
                <div class="yui-u first">
                    <div class="box">
                        <h2>
                            <warehouse:message code="default.filters.label" default="Filters"/>
                        </h2>
                        <div class="filter-list-item">
                            <g:link controller="inventory" action="listTotalStock" class="${'listTotalStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listTotalStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter-list-item">
                            <g:link controller="inventory" action="listInStock" class="${'listInStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listInStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter-list-item">
                            <g:link controller="inventory" action="listOutOfStock" class="${'listOutOfStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listOutOfStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter-list-item">
                            <g:link controller="inventory" action="listLowStock" class="${'listLowStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listLowStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter-list-item">
                            <g:link controller="inventory" action="listReorderStock" class="${'listReorderStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listReorderStock.label"/>
                            </g:link>
                        </div>
                        <div class="filter-list-item">
                            <g:link controller="inventory" action="listOverStock" class="${'listOverStock'.equals(actionName)?'selected':''}">
                                <warehouse:message code="inventory.listOverStock.label"/>
                            </g:link>
                        </div>
                    </div>

                </div>
                <div class="yui-u">



                    <div class="box">
                        <div class="right">
                            <g:link params="[format:'csv']" controller="${controllerName}" action="${actionName}" class="button">Download .csv</g:link>
                        </div>
                        <h2>
                            <warehouse:message code="default.results.label" default="Results"/> -
                            <warehouse:message code="default.showing.message" args="[quantityMap?.keySet()?.size()]"/>
                        </h2>
                        <table>
                            <tr>
                                <th><warehouse:message code="product.label"/></th>
                                <th class="center"><warehouse:message code="default.quantity.label"/></th>

                            </tr>
                            <g:each var="entry" in="${quantityMap}" status="i">
                                <tr class="${i%2?'odd':'even'}">
                                    <td>
                                        <g:link controller="inventoryItem" action="showStockCard" id="${entry.key.id}">
                                            ${entry.key}
                                        </g:link>
                                    </td>
                                    <td class="center">
                                        ${entry.value}
                                        ${entry?.key?.unitOfMeasure}
                                    </td>

                                </tr>


                            </g:each>
                            <g:unless test="${quantityMap}">
                                <tr>
                                    <td colspan="3" class="center">
                                        <warehouse:message code="default.emptyResults.message" default="No results"/>
                                    </td>

                                </tr>

                            </g:unless>
                        </table>
                    </div>
                </div>
            </div>
		</div>
		
	</body>

</html>
