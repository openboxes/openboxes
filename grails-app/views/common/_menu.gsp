<div id="leftnav-accordion-menu" class="accordion menu">
	
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
					<g:link controller="inventory" action="listDailyTransactions"><warehouse:message code="transaction.dailyTransactions.label"/></g:link> 
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="inventory" action="listExpiringStock"><warehouse:message code="inventory.expiringStock.label"/></g:link> 
				</span>
			</li>
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
			<li>
				<span class="menuButton">
					<g:link controller="inventoryItem" action="importInventoryItems"><warehouse:message code="inventory.import.label"/></g:link> 				
				</span>			
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="product" action="create"><warehouse:message code="product.add.label"/></g:link>
				</span>
			</li>
		</ul>
	</div>
	<h6 class="menu-heading" >
		<warehouse:message code="orders.label"  default="Orders"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="order" action="list"><warehouse:message code="order.list.label" default="List orders "/></g:link>
				</span>
			</li>									
			<li class="">
				<span class="menuButton">
					<g:link controller="order" action="listOrderItems"><warehouse:message code="orderItem.list.label"  default="List order items "/></g:link>
				</span>
			</li>									
			<li class="">
				<span class="menuButton">
					<g:link controller="purchaseOrderWorkflow" action="index"><warehouse:message code="order.create.label" default="Add incoming order"/></g:link>
				</span>
			</li>					
		</ul>										
	</div>
	<h6 class="menu-heading" >
		<warehouse:message code="requests.label"  default="Requests"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="request" action="list"><warehouse:message code="request.list.label"  default="List requests "/></g:link>
				</span>
			</li>						
			
			<li class="">
				<span class="menuButton">
					<g:link controller="createRequestWorkflow" action="index"><warehouse:message code="request.create.label" default="Add new request"/></g:link>
				</span>
			</li>					
		</ul>
		
						
	</div>			
	
	
	<h6 class="menu-heading" >
		<warehouse:message code="shipping.label"  default="Shipping"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="shipment" action="listShipping"><warehouse:message code="shipping.list.label"  default="List shipments "/></g:link>
				</span>
			</li>									
			<li class="">
				<span class="menuButton">
					<g:link controller="createShipmentWorkflow" action="index"><warehouse:message code="shipping.add.label" default="Add a shipment"/></g:link>
				</span>
			</li>					
		</ul>										
	</div>
	<h6 class="menu-heading">
		<warehouse:message code="receiving.label"  default="Receiving"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="shipment" action="listReceiving"><warehouse:message code="receiving.list.label"  default="List receiving"/></g:link>
				</span>		
			</li>		
		</ul>										
	</div>
	<%-- 
	<h6 class="menu-heading">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'chart_bar.png')}" alt="Reports" style="vertical-align: middle"/> &nbsp; 
		<warehouse:message code="settings.label" args="['Reports']" default="Reports"/>
	</h6>
	--%>
	<h6 class="menu-heading">
		<warehouse:message code="administration.label"  default="Administration"/>
	</h6>			
	<div class="menu-section">
		<span class="menu-subheading"><warehouse:message code="products.label"/></span>
		<ul>			
			<li class="">
				<span class="menuButton">
					<g:link controller="product" action="list"><warehouse:message code="products.label"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="attribute" action="list"><warehouse:message code="attribute.label"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="category" action="tree"><warehouse:message code="category.productCategories.label"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="product" action="batchEdit"><warehouse:message code="default.batchEdit.label" args="[warehouse.message(code:'products.label')]" default="Batch Edit Products" /></g:link>
				</span>
			</li>
		</ul>
		<span class="menu-subheading"><warehouse:message code="locations.label"/></span>
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="warehouse" action="list"><warehouse:message code="location.warehouses.label"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="location" action="list"><warehouse:message code="location.suppliersCustomers.label"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="shipper" action="list"><warehouse:message code="location.shippers.label"/></g:link>
				</span>
			</li>
		</ul>
		<span class="menu-subheading"><warehouse:message code="persons.label"/></span>
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="person" action="list"><warehouse:message code="person.list.label"/></g:link>
				</span>		
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="user" action="list"><warehouse:message code="users.label"/></g:link>
				</span>	
			</li>
			<!--  								
			<li class="">
				<span class="menuButton">
					<g:link controller="role" action="list"><warehouse:message code="default.manage.label" args="['roles']"/></g:link>
				</span>		
			</li>
			-->
		</ul>
		<span class="menu-subheading"><warehouse:message code="default.general.label"/></span>
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="admin" action="checkSettings"><warehouse:message code="default.manage.label" args="[warehouse.message(code:'default.settings.label')]"/></g:link>
				</span>		
			</li>
		</ul>
	</div>
</div>
