<div id="leftnav-accordion-menu" class="accordion menu">
	
	<h6 class="menu-heading">
		<g:message code="inventory.label"  default="Inventory"/>
	</h6>
	<div class="menu-section">									
		<ul>
			<li>
				<span class="menuButton">
					<g:link controller="inventory" action="browse">Browse inventory</g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="inventory" action="listDailyTransactions">Daily transactions</g:link> 
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="inventory" action="listExpiringStock">Expiring stock</g:link> 
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="inventory" action="listAllTransactions">List transactions</g:link> 
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="inventory" action="createTransaction">Add transaction</g:link> 				
				</span>			
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="inventoryItem" action="importInventoryItems">Import items</g:link> 				
				</span>			
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="product" action="create"><g:message code="default.add.label" args="['product']" default="Add New Product" /></g:link>
				</span>
			</li>
		</ul>
	</div>
	<h6 class="menu-heading" >
		<g:message code="orders.label"  default="Orders"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="order" action="list"><g:message code="order.list.label"  default="List orders "/></g:link>
				</span>
			</li>									
			<li class="">
				<span class="menuButton">
					<g:link controller="order" action="listOrderItems"><g:message code="orderItem.list.label"  default="List order items "/></g:link>
				</span>
			</li>									
			<li class="">
				<span class="menuButton">
					<g:link controller="purchaseOrderWorkflow" action="index"><g:message code="order.create.label" default="Add incoming order"/></g:link>
				</span>
			</li>					
		</ul>										
	</div>
	<h6 class="menu-heading" >
		<g:message code="shipping.label"  default="Shipping"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="shipment" action="listShipping"><g:message code="shipment.listShipping.label"  default="List shipments "/></g:link>
				</span>
			</li>									
			<li class="">
				<span class="menuButton">
					<g:link controller="createShipmentWorkflow" action="index"><g:message code="suitcase.add.label" default="Add a shipment"/></g:link>
				</span>
			</li>					
		</ul>										
	</div>
	<h6 class="menu-heading">
		<g:message code="receiving.label"  default="Receiving"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="shipment" action="listReceiving"><g:message code="shipment.listReceiving.label"  default="List receiving"/></g:link>
				</span>		
			</li>										
			<li>
				<span class="menuButton">
				<!--  <g:link class="browse" class="bullet" controller="receipt" action="process"><g:message code="receiving.process.label" default="Process Receipts"/></g:link>  -->
				</span>
			</li>		
		</ul>										
	</div>
	<%-- 
	<h6 class="menu-heading">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'chart_bar.png')}" alt="Reports" style="vertical-align: middle"/> &nbsp; 
		<g:message code="settings.label" args="['Reports']" default="Reports"/>
	</h6>
	--%>
	<h6 class="menu-heading">
		<g:message code="administration.label"  default="Administration"/>
	</h6>			
	<div class="menu-section">
		<span class="menu-subheading">Products</span>
		<ul>			
			<li class="">
				<span class="menuButton">
					<g:link controller="product" action="list">Products</g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="attribute" action="list">Product Attributes</g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="category" action="tree">Product Categories</g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="product" action="batchEdit"><g:message code="default.batchEdit.label" args="['products']" default="Batch Edit Products" /></g:link>
				</span>
			</li>
		</ul>
		<span class="menu-subheading">Locations</span>
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="warehouse" action="list">Warehouses</g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="location" action="list">Suppliers / Customers</g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="shipper" action="list">Shippers</g:link>
				</span>
			</li>
		</ul>
		<span class="menu-subheading">Persons</span>
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="person" action="list">All Persons</g:link>
				</span>		
			</li>
			<li class="">
				<span class="menuButton">
					<g:link controller="user" action="list">Users</g:link>
				</span>	
			</li>
			<!--  								
			<li class="">
				<span class="menuButton">
					<g:link controller="role" action="list"><g:message code="default.manage.label" args="['roles']"/></g:link>
				</span>		
			</li>
			-->
		</ul>
		<span class="menu-subheading">General</span>
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link controller="admin" action="checkSettings"><g:message code="default.manage.label" args="['settings']"/></g:link>
				</span>		
			</li>
		</ul>
	</div>
</div>
