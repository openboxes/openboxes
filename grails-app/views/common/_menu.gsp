<%@page import="org.pih.warehouse.core.ActivityCode"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>
<style>
	.submenuItem {
		padding-left:15px;
	}
</style>
<div id="leftnav-accordion-menu" class="accordion menu">	
	<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
		<h6 class="menu-heading">
			<warehouse:message code="inventory.label"  default="Inventory"/>
		</h6>
		<div class="menu-section">									
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="browse"><warehouse:message code="inventory.browse.label"/></g:link>
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="listExpiredStock"><warehouse:message code="inventory.expiredStock.label"/></g:link> 
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="listExpiringStock"><warehouse:message code="inventory.expiringStock.label"/></g:link> 
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="listLowStock"><warehouse:message code="inventory.lowStock.label"/></g:link> 
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="showConsumption"><warehouse:message code="inventory.consumption.label"/></g:link> 
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="listDailyTransactions"><warehouse:message code="transaction.dailyTransactions.label"/></g:link> 
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="product" action="create"><warehouse:message code="product.add.label"/></g:link>
					</span>
				</li>
			</ul>
		</div>
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.PLACE_ORDER,ActivityCode.FULFILL_ORDER]">	
		<h6 class="menu-heading" >
			<warehouse:message code="orders.label"  default="Orders"/>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="order" action="list" params="[status:'PENDING']"><warehouse:message code="order.list.label" default="List orders"/></g:link>
					</span>
				</li>					
				<li>
					<span class="menuButton">
						<g:link controller="order" action="listOrderItems"><warehouse:message code="orderItem.list.label"  default="List order items"/></g:link>
					</span>
				</li>									
				<li>
					<span class="menuButton">
						<g:link controller="purchaseOrderWorkflow" action="index"><warehouse:message code="order.create.label" default="Add incoming order"/></g:link>
					</span>
				</li>					
			</ul>										
		</div>
	</g:authorize>
	
	
	<g:authorize activity="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
		<h6 class="menu-heading" >
			<warehouse:message code="requests.label"  default="Requests"/>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="requisition" action="list" params="[requestType:'INCOMING']"><warehouse:message code="request.listIncoming.label" /></g:link>
					</span>
				</li>						
				<li>
					<span class="menuButton">
						<g:link controller="requisition" action="list" params="[requestType:'OUTGOING']"><warehouse:message code="request.listOutgoing.label" /></g:link>
					</span>
				</li>						
				
				<li>
					<span class="menuButton">
						<g:link controller="createRequestWorkflow" action="index"><warehouse:message code="request.create.label" default="Add new request"/></g:link>
					</span>
				</li>					
			</ul>
		</div>			
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.SEND_STOCK]">
		<h6 class="menu-heading" >
			<warehouse:message code="shipping.label"  default="Shipping"/>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="shipment" action="list" params="[status:'PENDING']"><warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/></g:link>
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="createShipmentWorkflow" action="index"><warehouse:message code="shipping.add.label" default="Add a shipment"/></g:link>
					</span>
				</li>	
			</ul>
		</div>
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.RECEIVE_STOCK]">		
		<h6 class="menu-heading" >
			<warehouse:message code="receiving.label"  default="Receiving"/>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="shipment" action="list" params="[type: 'incoming', status: 'PENDING']"><warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/></g:link>
					</span>
				</li>								
			</ul>										
		</div>
	</g:authorize>	
		
	<h6 class="menu-heading">
		<warehouse:message code="administration.label"  default="Administration"/>
	</h6>			
	<div class="menu-section">
		<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">	
			<span class="menu-subheading"><warehouse:message code="inventory.label"/></span>
			<ul>			
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="listAllTransactions"><warehouse:message code="transaction.list.label"/></g:link> 
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="editTransaction"><warehouse:message code="transaction.add.label"/></g:link> 				
					</span>			
				</li>
			</ul>	
		</g:authorize>
		<span class="menu-subheading"><warehouse:message code="report.label"/></span>
		<ul>			
			<li>
				<span class="menuButton">
					<g:link controller="report" action="showTransactionReport"><warehouse:message code="report.showTransactionReport.label"/></g:link>
				</span>
			</li>
		</ul>
		<span class="menu-subheading"><warehouse:message code="products.label"/></span>
		<ul>			
			<li>
				<span class="menuButton">
					<g:link controller="product" action="list"><warehouse:message code="products.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="attribute" action="list"><warehouse:message code="attribute.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="category" action="tree"><warehouse:message code="category.productCategories.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="inventoryItem" action="importInventoryItems"><warehouse:message code="inventory.import.label"/></g:link> 				
				</span>			
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="product" action="batchEdit"><warehouse:message code="default.batchEdit.label" args="[warehouse.message(code:'products.label')]" default="Batch Edit Products" /></g:link>
				</span>
			</li>
		</ul>
		<span class="menu-subheading"><warehouse:message code="locations.label"/></span>
		<ul>
			<li>
				<span class="menuButton">
					<g:link controller="locationGroup" action="list"><warehouse:message code="location.sites.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="location" action="list"><warehouse:message code="locations.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="shipper" action="list"><warehouse:message code="location.shippers.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="locationType" action="list"><warehouse:message code="location.locationTypes.label"/></g:link>
				</span>
			</li>
		</ul>
		<span class="menu-subheading"><warehouse:message code="persons.label"/></span>
		<ul>
			<li>
				<span class="menuButton">
					<g:link controller="person" action="list"><warehouse:message code="person.list.label"/></g:link>
				</span>		
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="user" action="list"><warehouse:message code="users.label"/></g:link>
				</span>	
			</li>
		</ul>
		<span class="menu-subheading"><warehouse:message code="default.general.label"/></span>
		<ul>
			<li>
				<span class="menuButton">
					<g:link controller="admin" action="showSettings"><warehouse:message code="default.manage.label" args="[warehouse.message(code:'default.settings.label')]"/></g:link>
				</span>		
			</li>
		</ul>
	</div>
</div>
