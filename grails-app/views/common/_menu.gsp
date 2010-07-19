<div class="menu">
	<div class="menuSection">
		<span class="heading">
			<span class="menuButton">
				<g:link class="shipment" controller="shipment" action="manage"><g:message code="shipment.manage.label"  default="Manage Shipments"/></g:link>
			</span>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="bullet" controller="shipment" action="list"><g:message code="shipment.list.incoming.label"  default="Incoming Shipments"/></g:link></span></li>										
			<li><span class="menuButton"><g:link class="bullet" controller="shipment" action="list"><g:message code="shipment.list.outgoing.label"  default="Outgoing Shipments"/></g:link></span></li>										
			<li><span class="menuButton"><g:link class="bullet" controller="shipment" action="create"><g:message code="shipment.create.incoming.label" default="New Incoming Shipment" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="bullet" controller="shipment" action="create"><g:message code="shipment.create.outgoing.label" default="New Outgoing Shipment" /></g:link></span></li>						
		</ul>										
	</div>
	<div class="menuSection">									
		<span class="heading">
			<span class="menuButton">
				<g:link class="product" controller="product" action="manage"><g:message code="product.manage.label"  default="Manage Products"/></g:link>
			</span>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="bullet" controller="product" action="browse"><g:message code="product.browse.label"  default="Browse Products"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="product" action="create"><g:message code="product.create.label" default="Create Product" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="bullet" controller="product" action="importProducts"><g:message code="product.import.label" default="Import Products" /></g:link></span></li>						

			<%-- 
			<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="create" controller="drugProduct" action="create"><g:message code="default.create.label" args="['Drug Product']" default="Create a new Drug Product" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="create" controller="consumableProduct" action="create"><g:message code="default.create.label" args="['Consumable Product']" default="Create a new Consumable Product" /></g:link></span></li>
			<li><span class="menuButton"><g:link class="create" controller="durableProduct" action="create"><g:message code="default.create.label" args="['Durable Product']" default="Create a new Durable Product" /></g:link></span></li>
			--%>
		</ul>	
	</div>							
	<div class="menuSection">									
		<span class="heading">
			<span class="menuButton">
				<g:link class="inventory" controller="inventory" action="manage"><g:message code="inventory.manage.label"  default="Manage Inventory"/></g:link>
			</span>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="bullet" controller="inventory" action="browse"><g:message code="inventory.browse.label"  default="Browse Inventory"/></g:link></span></li>		
		
			<!--  
			<li><span class="menuButton"><g:link class="browse" controller="product" action="browse"><g:message code="default.browse.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="create" controller="product" action="create"><g:message code="default.create.label" args="['Product']" default="Create a new Product" /></g:link></span></li>						
			-->
		
		</ul>	
	</div>							
	<div class="menuSection">									
		<span class="heading">
			<span class="menuButton">
				<g:link class="order" controller="order" action="manage"><g:message code="order.manage.label"  default="Manage Orders"/></g:link>
			</span>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="bullet" controller="order" action="search"><g:message code="order.search.label" default="Search orders" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="bullet" controller="order" action="create"><g:message code="order.create.label" default="Create an order" /></g:link></span></li>						
			<!--  
			<li><span class="menuButton"><g:link class="list" controller="order" action="search"><g:message code="order.search.label"  default="Search orders"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="list" controller="order" action="listPending"><g:message code="default.list.label"  args="['Pending Order']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="list" controller="order" action="listMine"><g:message code="default.list.label"  args="['My Order']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="list" controller="order" action="list"><g:message code="default.list.label"  args="['Order']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="create" controller="order" action="create"><g:message code="default.create.label" args="['Order']" default="Create Order" /></g:link></span></li>						
			-->
		</ul>	
	</div>
	<div class="menuSection">
		<span class="heading">
			<span class="menuButton">
				<g:link class="settings" controller="settings" action="manage"><g:message code="settings.manage.label" args="['Settings']" default="Manage Settings"/></g:link>
			</span>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="bullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>

		</ul>
	</div>								
	<div class="menuSection">
		<span class="heading">
			<span class="menuButton">
				<g:link class="metadata" controller="metadata" action="manage"><g:message code="metadata.manage.label"  default="Manage Metadata"/></g:link>
			</span>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="bullet" controller="containerType" action="list"><g:message code="default.manage.label" args="['Container Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="referenceNumberType" action="list"><g:message code="default.manage.label" args="['Reference # Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="genericType" action="list"><g:message code="default.manage.label" args="['Generic Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="productType" action="list"><g:message code="default.manage.label" args="['Product Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="category" action="list"><g:message code="default.manage.label" args="['Categories']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="conditionType" action="list"><g:message code="default.manage.label" args="['Medical Conditions']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="drugRouteType" action="list"><g:message code="default.manage.label" args="['Admin Routes']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="packageType" action="list"><g:message code="default.manage.label" args="['Drug Package']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="bullet" controller="drugClass" action="list"><g:message code="default.manage.label" args="['Drug Classes']"/></g:link></span></li>
		</ul>
	</div>
</div>