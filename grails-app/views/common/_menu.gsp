<div class="menu">
	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'truck.png')}" alt="Edit Contents" style="vertical-align: middle"/> <g:message code="shipment.manage.label"  default="Manage Shipments"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="shipment" action="listOutgoing"><g:message code="shipment.list.outgoing.label"  default="Browse Outgoing"/></g:link></span></li>										
			<li><span class="menuButton"><g:link class="nobullet" controller="shipment" action="listIncoming"><g:message code="shipment.list.incoming.label"  default="Browse Incoming"/></g:link></span></li>										
			<li>
				<span class="menuButton">
					<g:link class="nobullet" controller="shipment" action="create" params="['type':'outgoing']"><g:message code="shipment.create.outgoing.label" default="New Outgoing" /></g:link>
				</span>
			</li>			
			<li>
				<span class="menuButton">
					<g:link class="nobullet" controller="shipment" action="create" params="['type':'incoming']"><g:message code="shipment.create.incoming.label" default="New Incoming" /></g:link>
				</span>
			</li>						
		</ul>										
	</div>
	<div class="menuSection">									
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'product.png')}" alt="Manage Products" style="vertical-align: middle"/> <g:message code="product.manage.label"  default="Manage Products"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="product" action="browse"><g:message code="product.browse.label"  default="Browse Products"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="product" action="importProducts"><g:message code="product.import.label" default="Import Products" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="nobullet" controller="product" action="create"><g:message code="product.create.label" default="New Product" /></g:link></span></li>						

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
			<img src="${createLinkTo(dir:'images/icons/',file:'stockbook.png')}" alt="Manage Inventory" style="vertical-align: middle"/>
			<g:message code="inventory.manage.label"  default="Manage Inventory"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="inventory" action="browse"><g:message code="inventory.browse.label"  default="Browse Inventory"/></g:link></span></li>		
		
			<!--  
			<li><span class="menuButton"><g:link class="browse" controller="product" action="browse"><g:message code="default.browse.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="create" controller="product" action="create"><g:message code="default.create.label" args="['Product']" default="Create a new Product" /></g:link></span></li>						
			-->
		
		</ul>	
	</div>							
	<div class="menuSection">									
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'order.png')}" alt="Manage Orders" style="vertical-align: middle"/>
			<g:message code="order.manage.label"  default="Manage Orders"/>

		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="order" action="search"><g:message code="order.search.label" default="Search Orders" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="nobullet" controller="order" action="create"><g:message code="order.create.label" default="New Order" /></g:link></span></li>						
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
			<img src="${createLinkTo(dir:'images/icons/',file:'settings.png')}" alt="Manage Settings" style="vertical-align: middle"/>
			<g:message code="settings.manage.label" args="['Settings']" default="Manage Settings"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>

		</ul>
	</div>								
	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'wrench.png')}" alt="Manage Metadata" style="vertical-align: middle"/>
			<g:message code="metadata.manage.label"  default="Manage Metadata"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="containerType" action="list"><g:message code="default.manage.label" args="['Metadata']"/></g:link></span></li>		
<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="referenceNumberType" action="list"><g:message code="default.manage.label" args="['Reference # Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="genericType" action="list"><g:message code="default.manage.label" args="['Generic Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="productType" action="list"><g:message code="default.manage.label" args="['Product Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="category" action="list"><g:message code="default.manage.label" args="['Categories']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="conditionType" action="list"><g:message code="default.manage.label" args="['Medical Conditions']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="drugRouteType" action="list"><g:message code="default.manage.label" args="['Admin Routes']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="packageType" action="list"><g:message code="default.manage.label" args="['Drug Package']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="drugClass" action="list"><g:message code="default.manage.label" args="['Drug Classes']"/></g:link></span></li>
 -->
		</ul>
	</div>
</div>