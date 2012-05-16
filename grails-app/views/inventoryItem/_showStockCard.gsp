<%@ page import="org.pih.warehouse.product.Product"%>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<g:if test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.SUPPORTED }">
	<table>
		<tr>
			<td style="padding: 0px;">
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
					<div id="tabs-1" style="padding: 0px;">										
						<g:render template="showCurrentStock"/>
					</div>
					<%-- 
					<div id="tabs-2" style="padding: 0px;">
						<g:render template="showPendingRequestLog"/>
					</div>
					<div id="tabs-3" style="padding: 0px;">
						<g:render template="showPendingOrderLog"/>
					</div>
					--%>
					<div id="tabs-4" style="padding: 0px;">
						<g:render template="showPendingShipmentLog"/>
					</div>
					<%-- 
					<div id="tabs-5" style="padding: 0px;">
						<g:render template="showLotNumbers"/>
					</div>
					--%>

				</div>
			</td>
		</tr>
	</table>
</g:if>
<g:elseif test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.NOT_SUPPORTED }">
	<div> 	
		<h4 class="fade"><warehouse:message code="inventory.currentAndPendingStock.label"/></h4>
		<div class="padded center box">
			<span class="fade"><g:message code="enum.InventoryStatus.NOT_SUPPORTED"/></span>
			<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': commandInstance?.productInstance?.id, 'inventory.id':commandInstance?.inventoryInstance?.id]">
				<warehouse:message code="default.change.label"/>
			</g:link>
		</div>
	</div>									
</g:elseif>								
<g:elseif test="${commandInstance?.inventoryLevelInstance?.status == InventoryStatus.SUPPORTED_NON_INVENTORY }">
	<div> 	
		<h4 class="fade"><warehouse:message code="inventory.currentAndPendingStock.label"/></h4>
		<div class="padded center box">
			<span class="fade"><g:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/></span>
			<g:link controller="inventoryItem" action="editInventoryLevel" params="['product.id': commandInstance?.productInstance?.id, 'inventory.id':commandInstance?.inventoryInstance?.id]">
				<warehouse:message code="default.change.label"/>
			</g:link>
		</div>
	</div>
</g:elseif>