<style>
/*.menuButton { font-variant: small-caps; }*/ 
/* remove gaudy background image */	
.ui-state-default, .ui-widget-content .ui-state-default, .ui-widget-header .ui-state-default {
	
} 	
	
</style>
<div id="leftnav-accordion-menu" class="accordion menu">
	
	<h6 class="menu-heading">
		<g:message code="inventory.label"  default="Inventory"/>
	</h6>
	<div class="menu-section">									
		<ul>
			<li>
				<span class="menuButton">
					<g:link class="bullet" controller="inventory" action="browse">Browse Inventory</g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link class="bullet" controller="inventory" action="listAllTransactions">List Transactions</g:link> 
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link class="bullet" controller="inventory" action="createTransaction">Add Transaction</g:link> 				
				</span>			
			</li>
			<li>
				<span class="menuButton">
					<g:link class="bullet" controller="inventoryItem" action="importInventoryItems">Import Items</g:link> 				
				</span>			
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="create"><g:message code="default.add.label" args="['Product']" default="Add New Product" /></g:link>
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
					<g:link class="bullet" controller="shipment" action="listShipping"><g:message code="shipment.listShipping.label"  default="List Shipments "/></g:link>
				</span>
			</li>									
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="createShipmentWorkflow" action="index"><g:message code="suitcase.add.label" default="Add Shipment"/></g:link>
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
					<g:link class="bullet" controller="shipment" action="listReceiving"><g:message code="shipment.listReceiving.label"  default="List Receiving"/></g:link>
				</span>		
			</li>										
			<li>
				<span class="menuButton">
				<!--  <g:link class="browse" class="bullet" controller="receipt" action="process"><g:message code="receiving.process.label" default="Process Receipts"/></g:link>  -->
				</span>
			</li>		
		</ul>										
	</div>
	<h6 class="menu-heading">
		<g:message code="location.label"  default="Locations"/>
	</h6>
	<div class="menu-section">								
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="warehouse" action="list"><g:message code="default.show.label" args="['Warehouses']"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="location" action="list"><g:message code="default.show.label" args="['Locations']"/></g:link>
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
	<h6 class="menu-heading">
		<img src="${createLinkTo(dir:'images/icons/silk',file:'chart_bar.png')}" alt="Reports" style="vertical-align: middle"/> &nbsp; 
		<g:message code="settings.label" args="['Reports']" default="Reports"/>
	</h6>
	--%>
	<%-- 
	<div class="menu-section">
		<ul>
			<!-- 
			<li><span class="menuButton"><g:link class="nobullet" controller="admin" action="index"><g:message code="default.manage.label" args="['Settings']" default="Manage Settings" /></g:link></span></li>
			-->
		</ul>
	</div>					
	--%>
	<h6 class="menu-heading">
		<g:message code="users.label"  default="Users"/>
	</h6>			
	<div class="menu-section">
		<ul>			
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="person" action="list"><g:message code="default.manage.label" args="['People']"/></g:link>
				</span>		
			</li>		
			<!--  								
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="role" action="list"><g:message code="default.manage.label" args="['Roles']"/></g:link>
				</span>		
			</li>
			-->										
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="user" action="list"><g:message code="default.manage.label" args="['Users']"/></g:link>
				</span>	
			</li>
		</ul>
	</div>
	
	<h6 class="menu-heading">
		<g:message code="administration.label"  default="Admin"/>
	</h6>			
	<div class="menu-section">
		<ul>			
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="admin" action="checkSettings"><g:message code="default.show.label" args="['Settings']"/></g:link>
				</span>		
			</li>		
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="category" action="tree"><g:message code="default.show.label" args="['Categories']"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="attribute" action="list"><g:message code="default.show.label" args="['Attributes']"/></g:link>
				</span>
			</li>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="product" action="batchEdit"><g:message code="default.batchEdit.label" args="['Products']" default="Batch Edit Products" /></g:link>
				</span>
			</li>
		</ul>
	</div>
	
	<%--
	<h6 class="menu-heading">
		<g:message code="metadata.label"  default="Settings"/>
	</h6>
	<div class="menu-section">
		<ul>
			<li class="">
				<span class="menuButton">
					<g:link class="bullet" controller="admin" action="index"><g:message code="default.manage.label" args="['All Settings']" /></g:link>
				</span>
			</li>
			
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

