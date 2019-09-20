<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>

<g:if test="${message}">
	<div class="message">${message}</div>
</g:if>
<g:hasErrors bean="${transaction}">
	<div class="errors">
		<g:renderErrors bean="${transaction}" as="list" />
	</div>
</g:hasErrors>
<g:hasErrors bean="${command}">
	<div class="errors">
		<g:renderErrors bean="${command}" as="list" />
	</div>
</g:hasErrors>
<g:hasErrors bean="${flash.itemInstance}">
	<div class="errors dialog">
		<g:renderErrors bean="${flash.itemInstance}" as="list" />
	</div>
</g:hasErrors>

<g:if test="${commandInstance?.inventoryLevel?.status == InventoryStatus.SUPPORTED }">
    <div id="transactionLogTabs" class="tabs">
		<ul>
            <li><a href="${request.contextPath}/inventoryItem/showCurrentStock/${commandInstance?.product?.id}"
                   id="current-stock-tab"><warehouse:message code="inventory.listInStock.label" default="In stock"/></a>
            </li>
			<li><a href="${request.contextPath}/inventoryItem/showStockHistory/${commandInstance?.product?.id}"><warehouse:message code="inventory.stockHistory.label"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showCurrentStockAllLocations/${commandInstance?.product?.id}"><warehouse:message code="inventory.currentStockAllLocations.label" default="All Locations"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showSuppliers/${commandInstance?.product?.id}"><warehouse:message code="product.sources.label" default="Sources"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showAlternativeProducts/${commandInstance?.product?.id}"><warehouse:message code="product.substitutions.label" default="Substitution"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showPending/${commandInstance?.product?.id}?type=INBOUND"><warehouse:message code="stockCard.pendingInbound.label" default="Pending Inbound"/></a></li>
			<li><a href="${request.contextPath}/inventoryItem/showPending/${commandInstance?.product?.id}?type=OUTBOUND"><warehouse:message code="stockCard.pendingOutbound.label" default="Pending Outbound"/></a></li>
            <li><a href="${request.contextPath}/inventoryItem/showConsumption/${commandInstance?.product?.id}"><warehouse:message code="inventory.consumption.label" default="Consumption"/></a></li>
			<g:if test="${grailsApplication.config.openboxes.forecasting.enabled}">
				<g:isSuperuser>
					<li><a href="${request.contextPath}/inventoryItem/showProductDemand/${commandInstance?.product?.id}"><warehouse:message code="forecasting.demand.label" default="Consumption"/></a></li>
				</g:isSuperuser>
			</g:if>
            <li><a href="${request.contextPath}/inventoryItem/showInventorySnapshot/${commandInstance?.product?.id}"><warehouse:message code="inventory.snapshot.label" default="Snapshot"/></a></li>
		</ul>
	</div>
</g:if>
<g:elseif test="${commandInstance?.inventoryLevel?.status == InventoryStatus.NOT_SUPPORTED }">
	<div class="padded center box">
		<h4 class="fade"><g:message code="enum.InventoryStatus.NOT_SUPPORTED"/></h4>
		<g:link controller="product" action="edit" params="['id': commandInstance?.product?.id]">
			<warehouse:message code="product.edit.label"/>
		</g:link>
	</div>
</g:elseif>
<g:elseif test="${commandInstance?.inventoryLevel?.status == InventoryStatus.SUPPORTED_NON_INVENTORY }">
	<div class="padded center box">
		<h4 class="fade"><g:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/></h4>
		<g:link controller="product" action="edit" params="['id': commandInstance?.product?.id]">
			<warehouse:message code="product.edit.label"/>
		</g:link>
	</div>
</g:elseif>
