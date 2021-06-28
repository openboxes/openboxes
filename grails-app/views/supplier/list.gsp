<%@ page import="org.pih.warehouse.order.OrderType; org.pih.warehouse.shipping.ShipmentStatusCode; org.pih.warehouse.order.OrderTypeCode" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="location.suppliers.label" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
           	<div>
                <div class="yui-gf">
                    <div class="yui-u first">
                        <div class="dialog box">
                            <h2>Filters</h2>
                            <g:form action="list" method="get">
                                <div>
                                    <div class="filter-list-item">
                                        <label>${warehouse.message(code: 'default.search.label')}</label>
                                        <g:textField class="text" id="q" name="q" value="${params.q}" style="width:100%" placeholder="Search by organization name or location name"/>
                                    </div>
                                    <div class="filter-list-item center">
                                        <button type="submit" class="button icon search">
                                            ${warehouse.message(code: 'default.button.search.label')}
                                        </button>
                                    </div>
                                </div>
                            </g:form>
                        </div>
                    </div>
                    <div class="yui-u">
                        <div class="box">
                            <h2>
                                ${warehouse.message(code: 'default.searchResults.label', args: [suppliersTotal]) }
                            </h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th><warehouse:message code="location.supplierOrganization.label" default="Supplier Organization"/></th>
                                        <th><warehouse:message code="location.supplierLocation.label" default="Supplier Location"/></th>
                                        <th><warehouse:message code="location.supplierOpenPOs.label" default="No. of Open POs"/></th>
                                        <th><warehouse:message code="location.supplierOpenShipments.label" default="No. of Open Shipments"/></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <g:each in="${suppliers}" status="i" var="supplier">
                                        <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                                            <td>
                                                <g:link controller="supplier" action="show" id="${supplier?.organization?.id }">
                                                    ${supplier?.organization?.name}
                                                </g:link>
                                            </td>
                                            <td>
                                                <g:link controller="location" action="show" id="${supplier?.id }">
                                                    ${supplier?.name}
                                                </g:link>
                                            </td>
                                            <td>
                                                <g:link controller="order" action="list" params="[orderType:OrderTypeCode.PURCHASE_ORDER.name(), origin: supplier.id, destination: null]" class="list">
                                                    ${supplier?.pendingOrdersCount}
                                                </g:link>
                                            </td>
                                            <td>
                                                <g:link controller="stockMovement" action="list" params="[direction:'INBOUND', 'origin.id': supplier?.id, 'destination.id': null, receiptStatusCode: ShipmentStatusCode.listPending()]" class="list">
                                                    ${supplier?.pendingShipmentsCount}
                                                </g:link>
                                            </td>
                                        </tr>
                                    </g:each>
                                </tbody>
                            </table>
                            <g:set var="pageParams" value="${[q: params.q]}"/>
                            <g:if test="${suppliersTotal >= params.max }">
                                <div class="paginateButtons">
                                    <g:paginate total="${suppliersTotal}" max="${params.max}" offset="${params.offset}" params="${pageParams}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
