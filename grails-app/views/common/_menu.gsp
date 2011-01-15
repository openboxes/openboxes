<style>
/*.menuButton { font-variant: small-caps; }*/ 
/* remove gaudy background image */	
.ui-state-default, .ui-widget-content .ui-state-default, .ui-widget-header .ui-state-default {
	background-image: none; 
} 	
	
</style>
<div id="leftnavMenu" class="menu">
	<h3 class="heading" >
		
		<g:message code="dashboard.label" default="Dashboard"/>
	</h3>
	 
	<div class="menuSection">
	 	<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="dashboard" action="index"><g:message code="default.show.label" args="['Dashboard']" default="Show Dashboard"/></g:link>
				</span>
			</li>				
		</ul>
	</div>
	<%-- 
	<h3 class="heading">
		<g:message code="order.label"  default="Orders"/>
	</h3>
	<div class="menuSection">									
		<ul>

			<li>
				<span class="menuButton">
					<g:link class="browse" class="bullet" controller="catalog" action="list"><g:message code="catalog.show.label" default="Show Catalog"/></g:link>
				</span>
			</li>		

		</ul>	
	</div>
	--%>
	<h3 class="heading" >
		<g:message code="shipping.label"  default="Shipping"/>
	</h3>
	<div class="menuSection">
		<ul>
			<!-- 
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
			-->
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listShipping"><g:message code="shipment.listShipping.label"  default="Show Shipments "/></g:link>
				</span>
			</li>				
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="createShipment" action="index"><g:message code="suitcase.add.label" default="Add Suitcase" args="['Suitcase']"/></g:link>
				</span>
			</li>					
			<!-- 			
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
			-->
		</ul>										
	</div>
	<h3 class="heading">
		<g:message code="receiving.label"  default="Receiving"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipment" action="listReceiving"><g:message code="shipment.listReceiving.label"  default="Show Receipts"/></g:link>
				</span>		
			</li>										
			<li>
				<span class="menuButton">
					<g:link class="browse" class="bullet" controller="receipt" action="process"><g:message code="receiving.process.label" default="Process Receipts"/></g:link>
				</span>
			</li>		
		</ul>										
	</div>
	<h3 class="heading">
		<g:message code="inventory.label"  default="Inventory"/>
	</h3>
	<div class="menuSection">									
		<ul>
			<li>
				<span class="menuButton">
					<g:link class="browse" controller="inventory" action="browse">Browse Inventory</g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link class="list" controller="inventory" action="listAllTransactions">All Transactions</g:link> 
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link class="new" controller="inventory" action="createTransaction">Add Transaction</g:link> 				
				</span>			
			</li>
			
		</ul>	
	</div>
	<h3 class="heading">
		<g:message code="product.label" default="Products" />
	</h3>
	<div class="menuSection">									
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="browse"><g:message code="default.edit.label" args="['Products']" default="Show Products"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="create"><g:message code="default.add.label" args="['Product']" default="Add New Product" /></g:link>
				</span>
			</li>
			
			
		<!-- 			
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="create"><g:message code="default.add.label" args="['Product']" default="Add New Product" /></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="importProducts"><g:message code="default.import.label" args="['Products']" default="Import New Products" /></g:link>
				</span>
			</li>						
 		-->			
		</ul>	
	</div>
	<h3 class="heading">
		<g:message code="locations.label"  default="Locations"/>
	</h3>
	<div class="menuSection">								
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="warehouse" action="list"><g:message code="default.show.label" args="['Locations']"/></g:link>
				</span>
			</li>
			<!-- 
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipper" action="list"><g:message code="default.manage.label" args="['Shippers']"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="warehouse" action="list"><g:message code="default.manage.label" args="['Warehouses']"/></g:link>
				</span>		
			</li>														

			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="donor" action="list"><g:message code="default.manage.label" args="['Donors']"/></g:link>
				</span>
			</li>
			 -->
		</ul>
	</div>																	
	<%-- 
	<h3 class="heading">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'chart_bar.png')}" alt="Reports" style="vertical-align: middle"/> &nbsp; 
		<g:message code="settings.label" args="['Reports']" default="Reports"/>
	</h3>
	--%>
	<%-- 
	<div class="menuSection">
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>
			-->
		</ul>
	</div>					
	--%>
	<h3 class="heading">
		<g:message code="users.label"  default="Users"/>
	</h3>			
	<div class="menuSection">
		<ul>			
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="person" action="list"><g:message code="default.manage.label" args="['People']"/></g:link>
				</span>		
			</li>										
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="role" action="list"><g:message code="default.manage.label" args="['Roles']"/></g:link>
				</span>		
			</li>										
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="user" action="list"><g:message code="default.manage.label" args="['Users']"/></g:link>
				</span>	
			</li>
		</ul>
	</div>
	<h3 class="heading">
		<g:message code="metadata.label"  default="Settings"/>
	</h3>
	<div class="menuSection">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="admin" action="index"><g:message code="default.manage.label" args="['All Settings']" /></g:link>
				</span>
			</li>
			<%--
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="category" action="tree"><g:message code="default.manage.label"  args="['Categories']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="productType" action="list"><g:message code="default.manage.label"  args="['Product Types']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="attribute" action="list"><g:message code="default.manage.label" args="['Attributes']" /></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="eventType" action="list"><g:message code="default.manage.label"  args="['Event Types']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="locationType" action="list"><g:message code="default.manage.label" args="['Location Types']"/></g:link>
				</span>		
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="budgetCode" action="list"><g:message code="default.manage.label"  args="['Product Budget Codes']"/></g:link>
				</span>
			</li>		
			<li>
				<span class="menuButton">
					<g:link class="bullet" controller="category" action="list"><g:message code="default.manage.label" args="['Product Categories']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="dosageForm" action="list"><g:message code="default.manage.label"  args="['Product Dosage Forms']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="productType" action="list"><g:message code="default.manage.label"  args="['Product Types']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="shipperService" action="list"><g:message code="default.manage.label" args="['Shipper Services']"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="transactionType" action="list"><g:message code="default.manage.label"  args="['Transaction Types']"/></g:link>
				</span>
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="unitOfMeasure" action="list"><g:message code="default.manage.label"  args="['Unit Of Measures']"/></g:link>
				</span>
			</li>
			 --%>		
 			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="containerType" action="list"><g:message code="default.manage.label" args="['Metadata']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="referenceNumberType" action="list"><g:message code="default.manage.label" args="['Reference # Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="genericType" action="list"><g:message code="default.manage.label" args="['Generic Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="productType" action="list"><g:message code="default.manage.label" args="['Product Types']"/></g:link></span></li>		
			<li><span class="menuButton"><g:link class="nobullet" controller="conditionType" action="list"><g:message code="default.manage.label" args="['Medical Conditions']"/></g:link></span></li>							
			<li><span class="menuButton"><g:link class="nobullet" controller="packageType" action="list"><g:message code="default.manage.label" args="['Drug Package']"/></g:link></span></li>		
			-->
		</ul>
	</div>
	
</div>

<script type="text/javascript">
$(function() { 
	$('#leftnavMenu').accordion({
		active: true, 
		navigation: true, 
		autoheight: true, 
		alwaysOpen: true,
		clearStyle: false, 
		animated: false,
		navigation: true,
		event: "click" /*mouseover*/ 
	});
});
</script>
