<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
	<div id="transactionLogTabs" class="tabs">
		<ul>
			<li><a href="#tabs-1" id="current-stock-tab"><warehouse:message code="inventory.currentStockCurrentLocation.label" default="Current Location"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showAlternativeProducts/${commandInstance?.productInstance?.id}"><warehouse:message code="product.alternativeProducts.label" default="Alternative products"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showCurrentStockAllLocations/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.currentStockAllLocations.label" default="All Locations"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showStockHistory/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.stockHistory.label"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showPendingRequisitions/${commandInstance?.productInstance?.id}"><warehouse:message code="request.pendingRequestLog.label" default="Pending requisitions"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showPendingShipments/${commandInstance?.productInstance?.id}"><warehouse:message code="shipment.pendingShipmentLog.label"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showConsumption/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.consumption.label" default="Consumption"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showInventorySnapshot/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.snapshot.label" default="Snapshot"/></a></li>

            <%--
			<li><a href="#tabs-2"><warehouse:message code="inventory.stockHistory.label"/></a></li>
			<li><a href="#tabs-3"><warehouse:message code="request.pendingRequestLog.label" default="Pending requisitions"/></a></li>
			<li><a href="#tabs-4" id="pending-shipment-tab"><warehouse:message code="shipment.pendingShipmentLog.label"/></a></li>
            <li><a href="#tabs-5"><warehouse:message code="inventory.showConsumption.label"/></a></li>
            --%>
		</ul>
		<div id="tabs-1">
			<g:render template="showCurrentStock"/>
		</div>

        <%--
		<div id="tabs-2">
			<g:render template="showStockHistory" />
		</div>
		<div id="tabs-3">
			<g:render template="showPendingRequestLog"/>
		</div>
		<div id="tabs-4">
			<g:render template="showPendingShipmentLog" model="[shipmentMap:commandInstance?.shipmentMap]"/>
		</div>
        <div id="tabs-5">
            <g:render template="showConsumption"/>
        </div>
        --%>
        <%--
		<div id="tabs-3">
			<g:render template="showPendingOrderLog"/>
		</div>
		--%>
		<%--
		<div id="tabs-5" style="padding: 0px;">
			<g:render template="showLotNumbers"/>
		</div>
		--%>	
	</div>
