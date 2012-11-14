<%@page import="org.pih.warehouse.core.ActivityCode"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>
<style>
	
	.menu-subheading { 
		font-weight: bold;
		padding-bottom: 5px;
		display: block;
	}
</style>
<ul class="megamenu">
	<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
		<li>
			<g:link controller="inventory" action="browse">
				<warehouse:message code="inventory.label" />&nbsp;
			</g:link>
			<div>							
				<table>
					<tr>
						<td>
							<div class="buttonsBar">
								<div class="linkButton">
									<g:link controller="inventory" action="browse" class="browse"><warehouse:message code="inventory.browse.label"/></g:link>
								</div>
								<div class="linkButton">
									<g:link controller="product" action="create" class="create"><warehouse:message code="product.create.label"/></g:link>
								</div>
								<div class="linkButton">
									<g:link controller="createProductFromTemplate" action="index" class="create"><warehouse:message code="product.createFromTemplate.label"/></g:link>
								</div>
								<div class="linkButton">
									<g:link controller="createProduct" action="index" class="create"><warehouse:message code="product.createFromGoogle.label"/></g:link>
								</div>
								<div class="linkButton">
									<g:link controller="rxNorm" action="lookupProducts" class="create"><warehouse:message code="product.createFromNDC.label"/></g:link>
								</div>
							</div>
						</td>
						<td>
							<g:if test="${session.lastProduct }" >
								<h2>Recently viewed</h2>
								<div class="buttonsBar">
									<div class="linkButton">
										<g:link controller="inventoryItem" action="showStockCard" id="${session.lastProduct.id }" class="product">
											${session.lastProduct.name }</g:link>						
										<span class="fade">edited <g:formatDate date="${session.lastProduct.lastUpdated }" format="MMM dd hh:mma"/></span>					
									</div>
								</div>
							</g:if>
						</td>					
					</tr>
					<tr>
						<g:if test='${quickCategories }'>	
							<td colspan="${session.lastProduct ? '2':'1' }">
								<h2>Quick categories</h2>
								<table>
									<tr>
										<g:each var="quickCategory" in="${quickCategories}">
											<td>
												<table>
													<tr>
														<td>
															<g:link controller="inventory" action="browse" params="[categoryId:quickCategory.id,resetSearch:true]">
																<label><format:category category="${quickCategory}"/></label>
															</g:link>
														</td>
													</tr>
													<tr>
														<td>
															<div class="menu-section">
																<ul>							
																	<g:if test="${quickCategory.categories}">
																		<g:each var="childCategory" in="${quickCategory.categories}">
																			<li>
																				<g:link controller="inventory" action="browse" params="[categoryId:childCategory.id,resetSearch:true]">
																					<format:category category="${childCategory}"/>
																				</g:link>
																			</li>																				
																		</g:each>	
																	</g:if>
																	<g:else>
																		<li>
																			<warehouse:message code="default.none.label"/>
																		</li>
																	</g:else>
																</ul>
															</div>
														</td>
													</tr>
												</table>
											</td>
										</g:each>	
									</tr>
								</table>
							</td>
						</g:if>						
					</tr>
				</table>				
			</div>
		</li>
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.PLACE_ORDER,ActivityCode.FULFILL_ORDER]">	
		<li>
			<g:link controller="order" action="list" class="list">			
				<warehouse:message code="orders.label"/>
			</g:link>
			<div class="buttonsBar">
				<div class="linkButton">
					<g:link controller="order" action="list" params="[status:'PENDING']" class="list"><warehouse:message code="order.list.label"/></g:link>
				</div>
				<g:each in="${incomingOrders}" var="orderStatusRow">
					<div class="linkButton">
						<g:link controller="order" action="list" params="[status:orderStatusRow[0]]" class="order-status-${orderStatusRow[0] }">
							<format:metadata obj="${orderStatusRow[0]}"/> (${orderStatusRow[1]})
						</g:link>
					</div>					
				</g:each>
				<div class="linkButton">							
					<g:link controller="purchaseOrderWorkflow" action="index" class="create">
						<warehouse:message code="order.create.label"/>
					</g:link>
				</div>										
			</div>
		</li>
	</g:authorize>
	<g:authorize activity="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
		<li>
			<g:link controller="requisition" action="list">
				<warehouse:message code="requests.label"/>
			</g:link>
			<div>
							<div class="buttonsBar">
								<div class="linkButton">
									<g:link controller="requisition" action="list" params="[requestType:'INCOMING']" class="list">
										<warehouse:message code="requisition.listIncoming.label" /> (${incomingRequests?.values()?.flatten()?.size()?:0 })
									</g:link>
								</div>
								%{--<g:each in="${org.pih.warehouse.requisition.RequisitionStatus.list() }" var="status">--}%
									%{--<div class="linkButton">							--}%
										%{--<g:set var="statusName" value="${warehouse.message(code:'enum.RequisitionStatus.' + status)}"/>--}%
										%{--<g:link controller="requisition" action="list" class="request-status-${status }" params="[requestType:'INCOMING',status:status]" fragment="${statusName}">--}%
											%{--<format:metadata obj="${status}"/> (${incomingRequests[status]?.size()?:0})--}%
										%{--</g:link>--}%
									%{--</div>--}%
								%{--</g:each>--}%
							</div>
						
							
							<div class="linkButton">
								<g:link controller="requisition" action="list" params="[requestType:'OUTGOING']" class="list">
									<warehouse:message code="requisition.listOutgoing.label" /> (${outgoingRequests?.values()?.flatten()?.size()?:0 })
								</g:link>
							</div>
							
							%{--<g:each in="${org.pih.warehouse.requisition.RequisitionStatus.list() }" var="status">--}%
								%{--<g:set var="statusName" value="${warehouse.message(code:'enum.RequisitionStatus.' + status)}"/>--}%
								%{--<div class="linkButton">							--}%
									%{--<g:link controller="requisition" action="list" class="request-status-${status }" params="[requestType:'OUTGOING',status:status]" fragment="${statusName }>--}%
										%{--<format:metadata obj="${status}"/> (${outgoingRequests[status]?.size()?:0 })--}%
									%{--</g:link>--}%
								%{--</div>--}%
							%{--</g:each>							--}%
							

							<div class="linkButton">
								<g:link controller="requisition" action="create" class="create">
									<warehouse:message code="requisition.create.label" />
								</g:link>
							</div>

			</div>
		</li>
	</g:authorize>		
	
	<g:authorize activity="[ActivityCode.SEND_STOCK]">
		<li>
			<g:link controller="shipment" action="list" params="[type:'outgoing']">
				<warehouse:message code="shipping.label" />
			</g:link>
			<div class="buttonsBar">
				<div class="linkButton">
					<g:link controller="shipment" action="list" params="[type:'outgoing']" class="list"><warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/></g:link>
				</div>					
				<g:each in="${outgoingShipments}" var="statusRow">
					<div class="linkButton">
						<g:link controller="shipment" action="list" params="[status:statusRow.key]" class="shipment-status-${statusRow.key }">
							<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
						</g:link>
					</div>
				</g:each>
				<div class="linkButton">					
					<g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']" class="create"><warehouse:message code="shipping.createOutgoingShipment.label"/></g:link>
				</div>	
			</div>
		</li>
	</g:authorize>		
	<g:authorize activity="[ActivityCode.RECEIVE_STOCK]">		
		<li>
			<g:link controller="shipment" action="list" params="[type: 'incoming']">
				<warehouse:message code="receiving.label" />
			</g:link>

			<div class="buttonsBar">
				<div class="linkButton">
					<g:link controller="shipment" action="list" params="[type: 'incoming']" class="list"><warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/></g:link>
				</div>
				<g:each in="${incomingShipments}" var="statusRow">
					<div class="linkButton">
						<g:link controller="shipment" action="list" params="[type: 'incoming', status:statusRow.key]" class="shipment-status-${statusRow.key }">
							<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
						</g:link>
					</div>
				</g:each>					
				<div class="linkButton">						
					<g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']" class="create"><warehouse:message code="shipping.createIncomingShipment.label"/></g:link>
				</div>							
			</div>
		</li>		
	</g:authorize>			
	<li>
		<a href="javascript:void(0)">
			<warehouse:message code="report.label" />
		</a>
		<div class="buttonsBar">
			<div class="linkButton">
				<g:link controller="report" action="showTransactionReport" class="report_inventory"><warehouse:message code="report.showTransactionReport.label"/></g:link>							
			</div>
			<div class="linkButton">
				<g:link controller="report" action="showShippingReport" class="report_shipping"><warehouse:message code="report.showShippingReport.label"/></g:link>
			</div>
			<div class="linkButton">
				<g:link controller="inventory" action="listExpiredStock" class="report_expired"><warehouse:message code="inventory.expiredStock.label"/></g:link> 
			</div>
			<div class="linkButton">
				<g:link controller="inventory" action="listExpiringStock" class="report_expiring"><warehouse:message code="inventory.expiringStock.label"/></g:link> 
			</div>
			<%-- 
				<div class="linkButton">
					<g:link controller="inventory" action="listLowStock" class="report"><warehouse:message code="inventory.lowStock.label"/></g:link> 
				</div>
			--%>				
			<div class="linkButton">
				<g:link controller="inventory" action="showConsumption" class="report_consumption"><warehouse:message code="inventory.consumption.label"/></g:link> 
			</div>
			<div class="linkButton">
				<g:link controller="inventory" action="listDailyTransactions" class="report_transactions"><warehouse:message code="transaction.dailyTransactions.label"/></g:link> 
			</div>
		</div>					
	</li>
	<li>
		<a href="javascript:void(0)">
			<warehouse:message code="admin.label" />
		</a>	
		<div>
			<table>
				<tr>
					<td class="top">
						<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">	
							<span class="menu-subheading"><warehouse:message code="default.general.label"/></span>
							<div class="buttonBar">
								<div class="linkButton">
									<g:link controller="admin" action="showSettings" class="list"><warehouse:message code="default.manage.label" args="[warehouse.message(code:'default.settings.label')]"/></g:link>
								</div>
								<div class="linkButton">
									<g:link controller="batch" action="importData" params="[type:'product']" class="product"><warehouse:message code="default.import.label" args="[warehouse.message(code:'products.label')]"/></g:link> 
								</div>										
								<div class="linkButton">
									<g:link controller="batch" action="importData" params="[type:'inventory']" class="inventory"><warehouse:message code="default.import.label" args="[warehouse.message(code:'inventory.label')]"/></g:link> 				
								</div>
								<div class="linkButton">									
									<g:link controller="product" action="batchEdit" class="edit"><warehouse:message code="product.batchEdit.label" /></g:link>
								</div>
							</div>
						</g:authorize>	
					
					</td>
					<td class="top">
						<span class="menu-subheading"><warehouse:message code="locations.label"/></span>
						<div class="buttonBar">
							<div class="linkButton">									
								<g:link controller="locationGroup" action="list" class="site"><warehouse:message code="location.sites.label"/></g:link>
							</div>
							<div class="linkButton">									
								<g:link controller="location" action="list" class="location"><warehouse:message code="locations.label"/></g:link>
							</div>
							<div class="linkButton">									
								<g:link controller="shipper" action="list" class="shipper"><warehouse:message code="location.shippers.label"/></g:link>
							</div>
							<div class="linkButton">									
								<g:link controller="locationType" action="list" class="locationType"><warehouse:message code="location.locationTypes.label"/></g:link>
							</div>
						</div>
					</td>
					<td class="top" rowspan="2">
						<span class="menu-subheading"><warehouse:message code="products.label"/></span>
						<div class="buttonBar">
							<div class="linkButton">									
								<g:link controller="product" action="list" class="list"><warehouse:message code="products.label"/></g:link>
							</div>
							<div class="linkButton">									
								<g:link controller="productGroup" action="list" class="list"><warehouse:message code="productGroups.label"/></g:link>
							</div>
							<div class="linkButton">									
								<g:link controller="attribute" action="list" class="list"><warehouse:message code="attributes.label"/></g:link>
							</div>
							<div class="linkButton">									
								<g:link controller="category" action="tree" class="list"><warehouse:message code="categories.label"/></g:link>
							</div>
							<div class="linkButton">
								<g:link controller="unitOfMeasure" action="list" class="list"><warehouse:message code="unitOfMeasure.label"/></g:link> 				
							</div>										
							<div class="linkButton">
								<g:link controller="unitOfMeasureClass" action="list" class="list"><warehouse:message code="unitOfMeasureClass.label"/></g:link> 				
							</div>										
						</div>
						
					</td>
				</tr>
				<tr>
					<td class="top">
						<span class="menu-subheading"><warehouse:message code="persons.label"/></span>
						<div class="buttonBar">
							<div class="linkButton">									
								<g:link controller="person" action="list" class="people"><warehouse:message code="person.list.label"/></g:link>
							</div>
							<div class="linkButton">									
								<g:link controller="user" action="list" class="user"><warehouse:message code="users.label"/></g:link>
							</div>
						</div>		
					</td>
					<td class="top">
						<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">	
							<span class="menu-subheading"><warehouse:message code="inventory.label"/></span>
							<div class="buttonBar">
								<div class="linkButton">
									<g:link controller="inventory" action="listAllTransactions" class="list"><warehouse:message code="transactions.label"/></g:link> 
								</div>										
								<div class="linkButton">
									<g:link controller="inventory" action="editTransaction" class="create"><warehouse:message code="transaction.add.label"/></g:link> 				
								</div>										
							</div>
						</g:authorize>				
					</td>
				</tr>
			</table>
		</div>
	</li>
</ul>
<!--MegaMenu Ends-->
