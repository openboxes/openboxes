<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<g:if test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.SUPPORTED }">
<div id="transactionLogTabs" class="tabs">												
	<ul>
		<li><a href="#tabs-1"><warehouse:message code="inventory.currentStock.label"/></a></li>
		<%-- 
		<li><a href="#tabs-2"><warehouse:message code="request.pendingRequestLog.label"/></a></li>
		<li><a href="#tabs-3"><warehouse:message code="order.pendingOrderLog.label"/></a></li>
		--%>
		<li><a href="#tabs-4"><warehouse:message code="shipment.pendingShipmentLog.label"/></a></li>
		<%-- 
		<li><a href="#tabs-5"><warehouse:message code="inventory.currentLotNumbers.label"/></a></li>
		--%>
	</ul>		
	<div id="tabs-1">										
		<g:render template="showCurrentStock"/>
	</div>
	<%-- 
	<div id="tabs-2">
		<g:render template="showPendingRequestLog"/>
	</div>
	<div id="tabs-3">
		<g:render template="showPendingOrderLog"/>
	</div>
	--%>
	<div id="tabs-4">
		<g:render template="showPendingShipmentLog"/>
	</div>
	<%-- 
	<div id="tabs-5" style="padding: 0px;">
		<g:render template="showLotNumbers"/>
	</div>
	--%>

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