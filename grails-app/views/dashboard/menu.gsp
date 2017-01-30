<%@page import="org.pih.warehouse.core.ActivityCode"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>
<style>
	.submenuItem {
		padding-left:15px;
	}
	
	.menu-subheading { 
		padding: 15px;
	}
</style>
<div id="leftnav-accordion-menu" class="accordion menu">	
	<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
		<h6>
			<a href="#inventory" name="inventory" class="menu-heading">
				<warehouse:message code="inventory.label"  default="Inventory"/>
			</a>
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
						<g:link controller="product" action="list"><warehouse:message code="products.label"/></g:link>
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
		<h6>
			<a href="#orders" name="orders" class="menu-heading">
				<warehouse:message code="orders.label"  default="Orders"/>
			</a>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="order" action="list" params="[status:'PENDING']"><warehouse:message code="order.list.label" default="List orders"/></g:link>
					</span>
				</li>
				<g:each in="${incomingOrders}" var="orderStatusRow">
					<li>
						<span class="menuButton submenuItem">
							<g:link controller="order" action="list" params="[status:orderStatusRow[0]]">
								<format:metadata obj="${orderStatusRow[0]}"/> (${orderStatusRow[1]})
							</g:link>
						</span>
					</li>			
				</g:each>	
				<%-- 
				<li>
					<span class="menuButton">
						<g:link controller="order" action="listOrderItems"><warehouse:message code="orderItem.list.label"  default="List order items"/></g:link>
					</span>
				</li>
				--%>									
				<li>
					<span class="menuButton">
						<g:link controller="purchaseOrderWorkflow" action="index">
							<warehouse:message code="order.create.label" default="Add incoming order"/>
						</g:link>
					</span>
				</li>					
			</ul>										
		</div>
	</g:authorize>
	
	
	<g:authorize activity="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
		<h6>
			<a href="#requests" name="requests" class="menu-heading">
				<warehouse:message code="requests.label"  default="Requests"/>
			</a>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="requisition" action="list" params="[requestType:'INCOMING']"><warehouse:message code="request.listIncoming.label" /></g:link>
					</span>
				</li>				
				<g:each in="${incomingRequests}" var="status">
					<li>
						<span class="menuButton submenuItem">
							<g:link controller="requisition" action="list" params="[requestType:'INCOMING',status:status.key]">
								<format:metadata obj="${status.key}"/> (${status.value.size()})
							</g:link>
						</span>
					</li>
				</g:each>				
				<li>
					<span class="menuButton">
						<g:link controller="requisition" action="list" params="[requestType:'OUTGOING']"><warehouse:message code="request.listOutgoing.label" /></g:link>
					</span>
				</li>						
				<g:each in="${outgoingRequests}" var="status">
					<li>
						<span class="menuButton submenuItem">
							<g:link controller="requisition" action="list" params="[requestType:'OUTGOING',status:status.key]">
								<format:metadata obj="${status.key}"/> (${status.value.size()})
							</g:link>
						</span>
					</li>
				</g:each>				
				
				
				<li>
					<span class="menuButton">
						<g:link controller="createRequestWorkflow" action="index"><warehouse:message code="request.create.label" default="Add new request"/></g:link>
					</span>
				</li>					
			</ul>
		</div>			
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.SEND_STOCK]">
		<h6>
			<a href="#shipping" name="shipping" class="menu-heading">
				<warehouse:message code="shipping.label" />
			</a>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="shipment" action="list" params="[type:'outgoing']"><warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/></g:link>
					</span>
				</li>
				<g:each in="${outgoingShipments}" var="statusRow">
					<li>
						<span class="menuButton submenuItem">
							<g:link controller="shipment" action="list" params="[status:statusRow.key]">
								<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
							</g:link>
						</span>
					</li>
				</g:each>
				<li>
					<span class="menuButton">
						<g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']"><warehouse:message code="shipping.createOutgoingShipment.label"/></g:link>
					</span>
				</li>	
			</ul>
		</div>
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.RECEIVE_STOCK]">		
		<h6>
			<a href="#receiving" name="receiving" class="menu-heading" >
				<warehouse:message code="receiving.label" />
			</a>
		</h6>
		<div class="menu-section">
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="shipment" action="list" params="[type: 'incoming']"><warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/></g:link>
					</span>
				</li>
				<g:each in="${incomingShipments}" var="statusRow">
					<li>
						<span class="menuButton submenuItem">
							<g:link controller="shipment" action="list" params="[type: 'incoming', status:statusRow.key]">
								<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
							</g:link>
						</span>
					</li>
				</g:each>					
				<li>
					<span class="menuButton">
						<g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']"><warehouse:message code="shipping.createIncomingShipment.label"/></g:link>
					</span> 
				</li>	
			</ul>										
		</div>
	</g:authorize>
	<h6>	
		<a href="#reporting" name="reporting" class="menu-heading" >
			<warehouse:message code="report.label" />
		</a>
	</h6>
	<div class="menu-section">
		<ul>			
			<li>
				<span class="menuButton">
					<g:link controller="report" action="showTransactionReport"><warehouse:message code="report.showTransactionReport.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="report" action="showShippingReport"><warehouse:message code="report.showShippingReport.label"/></g:link>
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
			<%-- 
			<li>
				<span class="menuButton">
					<g:link controller="inventory" action="listLowStock"><warehouse:message code="inventory.lowStock.label"/></g:link> 
				</span>
			</li>
			--%>
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
		</ul>
	</div>
	<h6>
		<a href="#administration" name="administration" class="menu-heading">
			<warehouse:message code="administration.label"  default="Administration"/>
		</a>
	</h6>			
	<div class="menu-section">
		<span class="menu-subheading"><warehouse:message code="default.general.label"/></span>
		<ul>
			<li>
				<span class="menuButton">
					<g:link controller="admin" action="showSettings"><warehouse:message code="default.manage.label" args="[warehouse.message(code:'default.settings.label')]"/></g:link>
				</span>		
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="batch" action="importData" params="[type:'product']"><warehouse:message code="default.import.label" args="[warehouse.message(code:'products.label')]"/></g:link> 				
				</span>			
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="batch" action="importData" params="[type:'inventory']"><warehouse:message code="default.import.label" args="[warehouse.message(code:'inventory.label')]"/></g:link> 				
				</span>			
			</li>
		</ul>
	
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
		<span class="menu-subheading"><warehouse:message code="products.label"/></span>
		<ul>			
			<li>
				<span class="menuButton">
					<g:link controller="product" action="list"><warehouse:message code="products.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="attribute" action="list"><warehouse:message code="attributes.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="category" action="tree"><warehouse:message code="categories.label"/></g:link>
				</span>
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="product" action="batchEdit"><warehouse:message code="product.batchEdit.label" /></g:link>
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
					<g:link controller="person" action="index"><warehouse:message code="person.list.label"/></g:link>
				</span>		
			</li>
			<li>
				<span class="menuButton">
					<g:link controller="user" action="list"><warehouse:message code="users.label"/></g:link>
				</span>	
			</li>
			<!--  								
			<li>
				<span class="menuButton">
					<g:link controller="role" action="list"><warehouse:message code="default.manage.label" args="['roles']"/></g:link>
				</span>		
			</li>
			-->
		</ul>
	</div>
</div>
