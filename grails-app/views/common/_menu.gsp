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

	.menuButton { font-variant: small-caps; } 
</style>


<div id="leftnavMenu" class="menu">
	<h4 class="heading" >
		<img src="${createLinkTo(dir:'images/icons/',file:'dashboard.png')}"  alt="Dashboard" style="vertical-align: middle"/> &nbsp; <g:message code="dashboard.manage.label"  default="Dashboard"/>
	</h4>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="dashboard" action="index"><g:message code="dashboard.list.label"  default="dashboard"/></g:link>
				</span>
			</li>				
			<li class="prop">
				<span class="menuButton">
					<g:link class="bullet" controller="sync" action="index"><g:message code="sync.list.label"  default="sync dashboard"/></g:link>
				</span>
			</li>				
		</ul>
	</div>
	<h3 class="heading" >
		<img src="${createLinkTo(dir:'images/icons/',file:'truck.png')}"  alt="Shipping" style="vertical-align: middle"/> &nbsp; <g:message code="shipment.manage.label"  default="Shipping"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="prop first">
				<span class="menuButton">
					<g:link class="new" controller="suitcase" action="index"><g:message code="shipment.create.outgoing.label" default="add new suitcase" /></g:link>
				</span>
			</li>					
			<li class="prop">
				<span class="menuButton">
					<g:link class="new" controller="shipment" action="create" params="['type':'outgoing']"><g:message code="shipment.create.outgoing.label" default="add new shipment" /></g:link>
				</span>
			</li>					
			<li class="prop">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listOutgoing"><g:message code="shipment.list.outgoing.label"  default="view all"/></g:link>
				</span>
			</li>				
			
			<g:each in="${org.pih.warehouse.core.EventStatus.list()}" var="eventStatus">
				<li class="prop">
					<span class="menuButton">
						<g:link class="bullet" controller="shipment" action="listOutgoing" params="['activityType':'SHIPPING','eventStatus':eventStatus]"><g:message code="shipment.list.outgoing.label"  default="view ${eventStatus?.name?.toLowerCase()}"/></g:link>
					</span>
				</li>
			</g:each>
			<%-- 
			<g:each in="${org.pih.warehouse.core.EventType.list()}" var="eventType">
				<g:if test="${eventType?.activityType?.name == 'Shipping'}">
					<li class="prop">
						<span class="menuButton">
							<g:link class="bullet" controller="shipment" action="listOutgoing" params="['eventType.id':eventType.id]"><g:message code="shipment.list.outgoing.label"  default="view ${eventType?.name?.toLowerCase()}"/></g:link>
						</span>
					</li>
				</g:if>
			</g:each>
			<li class="prop">
				<span class="menuButton">
					<g:link class="invalid" controller="shipment" action="listOutgoing" params="['eventType.id':0]"><g:message code="shipment.list.outgoing.label"  default="view invalid shipments"/></g:link>
				</span>
			</li>			
			--%>
									
		</ul>										
	</div>
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'handtruck.png')}" width="16" height="16" alt="Receiving" style="vertical-align: middle"/> &nbsp; <g:message code="shipment.manage.label"  default="Receiving"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listIncoming"><g:message code="shipment.list.incoming.label"  default="view all"/></g:link>
				</span>		
			</li>										
			<%-- 
			<g:each in="${org.pih.warehouse.core.EventType.list()}" var="eventType">
				<g:if test="${eventType?.activityType == org.pih.warehouse.core.ActivityType.RECEIVING}">
					<li class="prop">
						<span class="menuButton">
							<g:link class="bullet" controller="shipment" action="listIncoming" params="['eventType.id':eventType.id]"><g:message code="shipment.list.outgoing.label"  default="view ${eventType?.name?.toLowerCase()}"/></g:link>
						</span>
					</li>
				</g:if>
			</g:each>
			--%>
			<g:each in="${org.pih.warehouse.core.EventStatus.list()}" var="eventStatus">
				<li class="prop">
					<span class="menuButton">
						<g:link class="bullet" controller="shipment" action="listOutgoing" params="['activityType':'RECEIVING','eventStatus':eventStatus]"><g:message code="shipment.list.outgoing.label"  default="view ${eventStatus?.name?.toLowerCase()}"/></g:link>
					</span>
				</li>
			</g:each>	
					
			<!-- 
			<li class="prop">
				<span class="menuButton">
					<g:link class="invalid" controller="shipment" action="listIncoming" params="['eventType.id':0]"><g:message code="shipment.list.incoming.label"  default="view invalid shipments"/></g:link>
				</span>
			</li>			
			<li class="prop">
				<span class="menuButton">
					<g:link class="create" controller="shipment" action="create" params="['type':'incoming']"><g:message code="shipment.create.incoming.label" default="new receipt" /></g:link>
				</span>
			</li>
			 -->						
		</ul>										
	</div>
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'stockbook.png')}" alt="Inventory" style="vertical-align: middle"/> &nbsp; 
		<g:message code="inventory.manage.label"  default="Inventory"/>
	</h3>
	<div class="menuSection">									
		<ul>
			<li>
				<span class="menuButton">
					<g:link class="browse" class="bullet" controller="inventory" action="browse"><g:message code="default.browse.label"  args="['inventory']"/></g:link>
				</span>
			</li>		
			<li>
				<span class="menuButton">
					<g:link class="browse" class="bullet" controller="receipt" action="process"><g:message code="default.browse.label"  args="['receipt']"/></g:link>
				</span>
			</li>		
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="inventory" action="browse"><g:message code="inventory.browse.label"  default="View Inventory"/></g:link></span></li>		
		 	-->
			<!--  
			<li><span class="menuButton"><g:link class="list" controller="product" action="list"><g:message code="default.list.label"  args="['Product']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="create" controller="product" action="create"><g:message code="default.create.label" args="['Product']" default="Create a new Product" /></g:link></span></li>						
			-->
		
		</ul>	
	</div>							
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'product.png')}" alt="Products" style="vertical-align: middle"/> &nbsp; <g:message code="product.manage.label"  default="products"/>
	</h3>
	<div class="menuSection">									
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="browse"><g:message code="product.browse.label"  default="view all"/></g:link>
				</span>
			</li>		
			<li class="prop">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="importProducts"><g:message code="product.import.label" default="import products" /></g:link>
				</span>
			</li>						
			<li class="prop">
				<span class="menuButton">
					<g:link class="new" controller="product" action="create"><g:message code="product.create.label" default="add new product" /></g:link>
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
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'order.png')}" alt="Orders" style="vertical-align: middle"/> &nbsp; 
		<g:message code="order.manage.label"  default="Orders"/>
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
		<g:message code="settings.manage.label" args="['Reports']" default="Reports"/>
	</h3>
	<div class="menuSection">
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>
			-->
		</ul>
	</div>					
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/',file:'wrench.png')}" alt="Administration" style="vertical-align: middle"/> &nbsp; 
		<g:message code="metadata.manage.label"  default="Administration"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="eventType" controller="eventType" action="list"><g:message code="default.manage.label"  args="['event types']"/></g:link>
				</span>
			</li>		
			<li class="prop">
				<span class="menuButton">
					<g:link class="drugRoute" controller="drugRouteType" action="list"><g:message code="default.manage.label" args="['drug routes']"/></g:link>
				</span>
			</li>
			<li class="prop">
				<span class="menuButton">
					<g:link class="drugClass" controller="drugClass" action="list"><g:message code="default.manage.label" args="['drug classes']"/></g:link>
				</span>
			</li>
			<li class="prop">
				<span class="menuButton">
					<g:link class="group" controller="person" action="list"><g:message code="default.manage.label" args="['people']"/></g:link>
				</span>		
			</li>										
			<li class="prop">
				<span class="menuButton">
					<g:link class="role" controller="role" action="list"><g:message code="default.manage.label" args="['roles']"/></g:link>
				</span>		
			</li>										
			<li class="prop">
				<span class="menuButton">
					<g:link class="shipper" controller="shipper" action="list"><g:message code="default.manage.label" args="['shippers']"/></g:link>
				</span>
			</li>
			<li class="prop">
				<span class="menuButton">
					<g:link class="shipperService" controller="shipperService" action="list"><g:message code="default.manage.label" args="['shipper services']"/></g:link>
				</span>
			</li>
			<li class="prop">
				<span class="menuButton">
					<g:link class="user" controller="user" action="list"><g:message code="default.manage.label" args="['users']"/></g:link>
				</span>		
			</li>										
			<li class="prop">
				<span class="menuButton">
					<g:link class="warehouse" controller="warehouse" action="list"><g:message code="default.manage.label" args="['warehouses']"/></g:link>
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
