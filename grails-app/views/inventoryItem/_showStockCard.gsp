<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<g:if test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.SUPPORTED }">
	<div id="transactionLogTabs" class="tabs">												
		<ul>
			<li><a href="#tabs-1" id="current-stock-tab"><warehouse:message code="inventory.currentStockCurrentLocation.label" default="Current Location"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showCurrentStockAllLocations/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.currentStockAllLocations.label" default="All Locations"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showAlternativeProducts/${commandInstance?.productInstance?.id}"><warehouse:message code="product.substitutions.label" default="Substitutions"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showStockHistory/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.stockHistory.label"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showPendingRequisitions/${commandInstance?.productInstance?.id}"><warehouse:message code="request.pendingRequestLog.label" default="Pending requisitions"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showPendingShipments/${commandInstance?.productInstance?.id}"><warehouse:message code="shipment.pendingShipmentLog.label"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showConsumption/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.consumption.label" default="Consumption"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showInventorySnapshot/${commandInstance?.productInstance?.id}"><warehouse:message code="inventory.snapshot.label" default="Snapshot"/></a></li>
		</ul>
		<div id="tabs-1">
			<g:render template="showCurrentStock"/>
		</div>
	</div>
</g:if>
<g:elseif test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.NOT_SUPPORTED }">
	<div class="padded center box">
		<h4 class="fade"><g:message code="enum.InventoryStatus.NOT_SUPPORTED"/></h4>
		<g:link controller="product" action="edit" params="['id': commandInstance?.productInstance?.id]">
			<warehouse:message code="product.edit.label"/>
		</g:link>
	</div>
</g:elseif>								
<g:elseif test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.SUPPORTED_NON_INVENTORY }">
	<div class="padded center box">
		<h4 class="fade"><g:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/></h4>
		<g:link controller="product" action="edit" params="['id': commandInstance?.productInstance?.id]">
			<warehouse:message code="product.edit.label"/>
		</g:link>
	</div>
</g:elseif>