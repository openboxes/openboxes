<script type="text/javascript">
$(function() { 
	//$('#leftnavMenu').accordion({
	//	active: true, 
	//	navigation: true, 
	//	autoheight: false, 
	//	alwaysOpen: true,
	//	clearStyle: true 
	//});
});
</script>

<style>
	/*.menuButton { font-variant: small-caps; }*/ 
</style>


<div id="leftnavMenu" class="menu">
	<h3 class="heading" >
		<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_tile.png')}"  alt="Dashboard" style="vertical-align: middle"/> &nbsp; 
		<g:message code="dashboard.label" default="Dashboard"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="dashboard" action="index"><g:message code="default.show.label" args="['Dashboard']" default="Show Dashboard"/></g:link>
				</span>
			</li>				
			<%-- 
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="sync" action="index"><g:message code="sync.list.label"  default="Show sync dashboard"/></g:link>
				</span>
			</li>
			--%>				
		</ul>
	</div>
	<h3 class="heading" >
		<img src="${createLinkTo(dir:'images/icons/silk',file:'lorry.png')}"  alt="Shipping" style="vertical-align: middle"/> &nbsp; 
		<g:message code="shipping.label"  default="Shipping"/>
	</h3>
	<div class="menuSection">
		<ul>
			<%-- 
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="createShipment" action="shoppingCart"><g:message code="shipment.create.label" default="shopping cart wizard" /></g:link>
				</span>
			</li>					
			<hr/>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="suitcase" action="index"><g:message code="shipment.create.suitcase.label" default="add new suitcase" /></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="create" params="['type':'outgoing']"><g:message code="shipment.create.label" default="Create Shipment" /></g:link>
				</span>
			</li>					
			--%>					
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listShipping"><g:message code="shipment.listShipping.label"  default="Show All "/></g:link>
				</span>
			</li>				
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="createShipment" action="index"><g:message code="suitcase.create.label" default="Create Suitcase" /></g:link>
				</span>
			</li>					
			<%-- 			
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listShipping" params="['activityType':'SHIPPING', 'browseBy':'eventStatus']"><g:message code="shipment.listShippingByStatus.label"  default="Show By Status"/></g:link>
				</span>
			</li>				
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listShipping" params="['activityType':'SHIPPING', 'browseBy':'shipmentType']"><g:message code="shipment.listShippingByType.label"  default="Show By Type"/></g:link>
				</span>
			</li>				
			<g:each in="${org.pih.warehouse.core.EventStatus.list()}" var="eventStatus">
				<li class="">
					<span class="menuButton">
						<g:link class="bullet" controller="shipment" action="listShipping" params="['activityType':'SHIPPING','eventStatus':eventStatus]"><g:message code="shipment.list.outgoing.label"  default="show ${eventStatus?.name?.toLowerCase()}"/></g:link>
					</span>
				</li>
			</g:each>
			--%>
			
			<%-- 
			<g:each in="${org.pih.warehouse.core.EventType.list()}" var="eventType">
				<g:if test="${eventType?.activityType?.name == 'Shipping'}">
					<li class="">
						<span class="menuButton">
							<g:link class="bullet" controller="shipment" action="listShipping" params="['eventType.id':eventType.id]"><g:message code="shipment.list.outgoing.label"  default="show ${eventType?.name?.toLowerCase()}"/></g:link>
						</span>
					</li>
				</g:if>
			</g:each>
			<li class="">
				<span class="menuButton">
					<g:link class="invalid" controller="shipment" action="listShipping" params="['eventType.id':0]"><g:message code="shipment.list.outgoing.label"  default="show invalid shipments"/></g:link>
				</span>
			</li>			
			--%>
									
		</ul>										
	</div>
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'handtruck.png')}" width="16" height="16" alt="Receiving" style="vertical-align: middle"/> &nbsp; 
		<g:message code="receiving.label"  default="Receiving"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listReceiving"><g:message code="shipment.listReceiving.label"  default="Show All"/></g:link>
				</span>		
			</li>										
			<%-- 
			<g:each in="${org.pih.warehouse.core.EventType.list()}" var="eventType">
				<g:if test="${eventType?.activityType == org.pih.warehouse.core.ActivityType.RECEIVING}">
					<li class="">
						<span class="menuButton">
							<g:link class="bullet" controller="shipment" action="listReceiving" params="['eventType.id':eventType.id]"><g:message code="shipment.list.outgoing.label"  default="show ${eventType?.name?.toLowerCase()}"/></g:link>
						</span>
					</li>
				</g:if>
			</g:each>
			--%>
			<%-- 
			<g:each in="${org.pih.warehouse.core.EventStatus.list()}" var="eventStatus">
				<li class="">
					<span class="menuButton">
						<g:link class="bullet" controller="shipment" action="listReceiving" params="['activityType':'RECEIVING','eventStatus':eventStatus]"><g:message code="shipment.list.outgoing.label"  default="show ${eventStatus?.name?.toLowerCase()}"/></g:link>
					</span>
				</li>
			</g:each>	
			--%>	
			<!-- 
			<li class="">
				<span class="menuButton">
					<g:link class="invalid" controller="shipment" action="listReceiving" params="['eventType.id':0]"><g:message code="shipment.list.incoming.label"  default="show invalid shipments"/></g:link>
				</span>
			</li>			
			<li class="">
				<span class="menuButton">
					<g:link class="create" controller="shipment" action="create" params="['type':'incoming']"><g:message code="shipment.create.incoming.label" default="new receipt" /></g:link>
				</span>
			</li>
			 -->						
			<li>
				<span class="menuButton">
					<g:link class="browse" class="bullet" controller="receipt" action="process"><g:message code="receiving.process.label" default="Process Receiving"/></g:link>
				</span>
			</li>		
		</ul>										
	</div>
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'table.png')}" alt="Inventory" style="vertical-align: middle"/> &nbsp; 
		<g:message code="inventory.label"  default="Inventory"/>
	</h3>
	<div class="menuSection">									
		<ul>
			<li>
				<span class="menuButton">
					<g:link class="browse" class="bullet" controller="inventory" action="browse"><g:message code="inventory.manage.label" default="Manage Inventory"/></g:link>
				</span>
			</li>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="inventory" action="browse"><g:message code="inventory.browse.label"  default="show Inventory"/></g:link></span></li>		
		 	-->
			<!--  
			<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="create" controller="product" action="create"><g:message code="default.create.label" args="['Product']" default="Create a new Product" /></g:link></span></li>						
			-->
		
		</ul>	
	</div>							
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Products" style="vertical-align: middle"/> &nbsp; 
		<g:message code="product.label" default="Product" />
	</h3>
	<div class="menuSection">									
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="create"><g:message code="default.create.label" args="['Product']" default="Add New Product" /></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="browse"><g:message code="default.browse.label" args="['Products']" default="Show All Products"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="importProducts"><g:message code="default.import.label" args="['Products']" default="Import New Products" /></g:link>
				</span>
			</li>						
			<%-- 
				<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
				<li><span class="menuButton"><g:link class="create" controller="drugProduct" action="create"><g:message code="default.create.label" args="['Drug Product']" default="Create a new Drug Product" /></g:link></span></li>						
				<li><span class="menuButton"><g:link class="create" controller="consumableProduct" action="create"><g:message code="default.create.label" args="['Consumable Product']" default="Create a new Consumable Product" /></g:link></span></li>
				<li><span class="menuButton"><g:link class="create" controller="durableProduct" action="create"><g:message code="default.create.label" args="['Durable Product']" default="Create a new Durable Product" /></g:link></span></li>
			--%>
		</ul>	
	</div>						
	<%-- 		
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'order.png')}" alt="Orders" style="vertical-align: middle"/> &nbsp; 
		<g:message code="order.label"  default="Orders"/>
	</h3>
	<div class="menuSection">									
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
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'reports.png')}" alt="Reports" style="vertical-align: middle"/> &nbsp; 
		<g:message code="settings.label" args="['Reports']" default="Reports"/>
	</h3>
	<div class="menuSection">
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>
			-->
		</ul>
	</div>					
	--%>
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'wrench.png')}" alt="Administration" style="vertical-align: middle"/> &nbsp; 
		<g:message code="metadata.label"  default="Administration"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="eventType" controller="eventType" action="list"><g:message code="default.manage.label"  args="['Event Types']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="transaction" controller="transactionType" action="list"><g:message code="default.manage.label"  args="['Transaction Types']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="budgetCode" controller="budgetCode" action="list"><g:message code="default.manage.label"  args="['Budget Code']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="productType" action="list"><g:message code="default.manage.label"  args="['Product Types']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="drug" controller="dosageForm" action="list"><g:message code="default.manage.label"  args="['Dosage Forms']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="shipper" controller="shipper" action="list"><g:message code="default.manage.label" args="['Shippers']"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="shipper" controller="shipperService" action="list"><g:message code="default.manage.label" args="['Shipper Services']"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="group" controller="person" action="list"><g:message code="default.manage.label" args="['People']"/></g:link>
				</span>		
			</li>										
			<li class="">
				<span class="menuButton">
					<g:link class="role" controller="role" action="list"><g:message code="default.manage.label" args="['Roles']"/></g:link>
				</span>		
			</li>										
			<li class="">
				<span class="menuButton">
					<g:link class="user" controller="user" action="list"><g:message code="default.manage.label" args="['Users']"/></g:link>
				</span>		
			</li>										
			<li class="">
				<span class="menuButton">
					<g:link class="warehouse" controller="warehouse" action="list"><g:message code="default.manage.label" args="['Warehouses']"/></g:link>
				</span>		
			</li>														


 			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="containerType" action="list"><g:message code="default.manage.label" args="['Metadata']"/></g:link></span></li>		
			-->
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="referenceNumberType" action="list"><g:message code="default.manage.label" args="['Reference # Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="genericType" action="list"><g:message code="default.manage.label" args="['Generic Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="productType" action="list"><g:message code="default.manage.label" args="['Product Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="category" action="list"><g:message code="default.manage.label" args="['Categories']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="conditionType" action="list"><g:message code="default.manage.label" args="['Medical Conditions']"/></g:link></span></li>		
					
			<li><span class="menuButton"><g:link class="nobullet" controller="packageType" action="list"><g:message code="default.manage.label" args="['Drug Package']"/></g:link></span></li>		
			-->
		</ul>
	</div>
</div>
