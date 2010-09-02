<div class="menu">
	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'truck.png')}"  alt="Shipping" style="vertical-align: middle"/> <g:message code="shipment.manage.label"  default="Shipping"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="shipment" action="listOutgoing"><g:message code="shipment.list.outgoing.label"  default="browse shipping"/></g:link></span></li>										
			<li>
				<span class="menuButton">
					<g:link class="nobullet" controller="shipment" action="create" params="['type':'outgoing']"><g:message code="shipment.create.outgoing.label" default="add new shipping" /></g:link>
				</span>
			</li>			
		</ul>										
	</div>
	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'handtruck.png')}" width="16" height="16" alt="Receiving" style="vertical-align: middle"/> <g:message code="shipment.manage.label"  default="Receiving"/>
		</span>
		<ul>
			<li>
				<span class="menuButton">
					<g:link class="nobullet" controller="shipment" action="listIncoming"><g:message code="shipment.list.incoming.label"  default="browse receiving"/>
					</g:link>
				</span>		
			</li>										
			<li>
				<span class="menuButton">
					<g:link class="nobullet" controller="shipment" action="create" params="['type':'incoming']"><g:message code="shipment.create.incoming.label" default="add new receiving" /></g:link>
				</span>
			</li>						
		</ul>										
	</div>
	<div class="menuSection">									
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'stockbook.png')}" alt="Inventory" style="vertical-align: middle"/>
			<g:message code="inventory.manage.label"  default="Inventory"/>
		</span>
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="inventory" action="browse"><g:message code="inventory.browse.label"  default="Browse Inventory"/></g:link></span></li>		
		 	-->
			<!--  
			<li><span class="menuButton"><g:link class="browse" controller="product" action="browse"><g:message code="default.browse.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="create" controller="product" action="create"><g:message code="default.create.label" args="['Product']" default="Create a new Product" /></g:link></span></li>						
			-->
		
		</ul>	
	</div>							
	<div class="menuSection">									
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'product.png')}" alt="Products" style="vertical-align: middle"/> <g:message code="product.manage.label"  default="Products"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="product" action="browse"><g:message code="product.browse.label"  default="browse products"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="product" action="importProducts"><g:message code="product.import.label" default="import products" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="nobullet" controller="product" action="create"><g:message code="product.create.label" default="add new product" /></g:link></span></li>
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
			<img src="${createLinkTo(dir:'images/icons/',file:'order.png')}" alt="Orders" style="vertical-align: middle"/>
			<g:message code="order.manage.label"  default="Orders"/>

		</span>
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="order" action="search"><g:message code="order.search.label" default="Search Orders" /></g:link></span></li>						
			<li><span class="menuButton"><g:link class="nobullet" controller="order" action="create"><g:message code="order.create.label" default="New Order" /></g:link></span></li>						
			 -->
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
			<img src="${createLinkTo(dir:'images/icons/',file:'reports.png')}" alt="Reports" style="vertical-align: middle"/>
			<g:message code="settings.manage.label" args="['Reports']" default="Reports"/>
		</span>
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>
			-->
		</ul>
	</div>					
	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'settings.png')}" alt="Settings" style="vertical-align: middle"/>
			<g:message code="settings.manage.label" args="['Settings']" default="Settings"/>
		</span>
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>
			-->
		</ul>
	</div>								
	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'users.png')}" alt="User" style="vertical-align: middle"/>
			<g:message code="user.manage.label"  default="Users"/>
		</span>
		<ul>

			<li>
				<span class="menuButton">
					<g:link class="nobullet" controller="user" action="list"><g:message code="user.list.label"  default="browse users"/></g:link>
				</span>		
			</li>										
		</ul>
	</div>
	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'building.png')}" alt="Warehouse" style="vertical-align: middle"/>
			<g:message code="warehouse.manage.label" default="Warehouses"/>
		</span>
		<ul>

			<li>
				<span class="menuButton">
					<g:link class="nobullet" controller="warehouse" action="list"><g:message code="warehouse.list.label"  default="browse warehouses"/>
					</g:link>
				</span>		
			</li>										
		</ul>
	</div>


	<div class="menuSection">
		<span class="heading">
			<img src="${createLinkTo(dir:'images/icons/',file:'wrench.png')}" alt="Metadata" style="vertical-align: middle"/>
			<g:message code="metadata.manage.label"  default="Metadata"/>
		</span>
		<ul>
			<li><span class="menuButton"><g:link class="nobullet" controller="eventType" action="list"><g:message code="eventType.browse.label"  default="browse event types"/></g:link></span></li>		

			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="containerType" action="list"><g:message code="default.manage.label" args="['Metadata']"/></g:link></span></li>		
			-->
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
