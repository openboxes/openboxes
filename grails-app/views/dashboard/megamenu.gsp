<%@page import="org.pih.warehouse.core.ActivityCode"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>
<style>
	.submenuItem {
		padding-left:15px;
	}
	.menu-subheading { 
		font-weight: bold;
	}
</style>
<ul class="megamenu">
	<li>
		<g:link controller="dashboard" action="index">
			<img src="${createLinkTo(dir:'images/icons/silk',file:'house.png')}" class="middle"/>
		</g:link>
	</li>
	<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
		<li>
			<g:link controller="inventory" action="browse">
				<warehouse:message code="inventory.label" />
			</g:link>
			<div>							
				<table>
					<tr>
						<td style="${quickCategories ? 'border-right: 2px solid black;':''}">
							<table>
								<tr>
									<td>
										<g:link controller="inventory" action="browse"><warehouse:message code="inventory.browse.label"/></g:link>
									</td>
								</tr>
								<tr>
									<td>
										<g:link controller="product" action="create">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" class="middle"/>
											<warehouse:message code="product.add.label"/>
										</g:link>
									</td>					
								</tr>
							</table>
						</td>
						<td>
							<g:if test='${quickCategories }'>	
								<table>
									<tr>
										<g:each var="quickCategory" in="${quickCategories}">
											<td>
												<table>
													<tr>
														<td>
															<g:link controller="inventory" action="browse" params="[categoryId:quickCategory.id,resetSearch:true]">
																<b><format:category category="${quickCategory}"/></b>
															</g:link>
														</td>
													</tr>
													<tr>
														<td>
															<div class="menu-section">
																<g:if test="${quickCategory.categories}">
																	<table>							
																		<g:each var="childCategory" in="${quickCategory.categories}">
																			<tr>
																				<td>
																					<g:link controller="inventory" action="browse" params="[categoryId:childCategory.id,resetSearch:true]">
																						<format:category category="${childCategory}"/>
																					</g:link>
																					
																				</td>
																			</tr>																				
																		</g:each>	
																	</table>
																</g:if>
																<g:else>
																	<warehouse:message code="default.none.label"></warehouse:message>
																</g:else>
															</div>
														</td>
													</tr>
												</table>
											</td>
										</g:each>	
									</tr>
								</table>
							</g:if>	
						</td>
					</tr>
				</table>				
			</div>
		</li>
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.PLACE_ORDER,ActivityCode.FULFILL_ORDER]">	
		<li>
			<g:link controller="order" action="list">			
				<warehouse:message code="orders.label"/>
			</g:link>
			<div>
				<table>
					<tr>
						<td>
							<g:link controller="order" action="list" params="[status:'PENDING']"><warehouse:message code="order.list.label"/></g:link>
						</td>
					</tr>
					<tr>
						<td>
							<table>
								<g:each in="${incomingOrders}" var="orderStatusRow">
									<tr>
										<td>
											<g:link controller="order" action="list" params="[status:orderStatusRow[0]]">
												<format:metadata obj="${orderStatusRow[0]}"/> (${orderStatusRow[1]})
											</g:link>
										</td>
									</tr>
								</g:each>
							</table>
						</td>
					</tr>
					<tr>	
						<td>
							<g:link controller="purchaseOrderWorkflow" action="index">
								<warehouse:message code="order.create.label"/>
							</g:link>
						</td>								
					</tr>
					<%-- 
					<li>
						<span>
							<g:link controller="order" action="listOrderItems"><warehouse:message code="orderItem.list.label"  default="List order items"/></g:link>
						</span>
					</li>
					--%>									
								
				</table>										
			</div>
		</li>
	</g:authorize>
	<g:authorize activity="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
		<li>
			<g:link controller="request" action="list">
				<warehouse:message code="requests.label"/>
			</g:link>
			<div>
				<table>
					<tr>
						<td>
							<g:link controller="request" action="list" params="[requestType:'INCOMING']"><warehouse:message code="request.listIncoming.label" /></g:link>
						</td>
					</tr>
					<tr>	
						<td>
							<table>
								<g:each in="${incomingRequests}" var="status">
									<tr>								
										<td>
											<g:link controller="request" action="list" params="[requestType:'INCOMING',status:status.key]">
												<format:metadata obj="${status.key}"/> (${status.value.size()})
											</g:link>
										</td>
									</tr>
								</g:each>
							</table>				
						</td>
					</tr>
					<tr>
						<td>
							<g:link controller="request" action="list" params="[requestType:'OUTGOING']"><warehouse:message code="request.listOutgoing.label" /></g:link>
						</td>
					</tr>
					<tr>
						<td>
							<table>
								<g:each in="${outgoingRequests}" var="status">
									<tr>
										<td>
											<g:link controller="request" action="list" params="[requestType:'OUTGOING',status:status.key]">
												<format:metadata obj="${status.key}"/> (${status.value.size()})
											</g:link>
										</td>
									</tr>
								</g:each>
							</table>	
						</td>
					</tr>
					<tr>
						<td>
							<g:link controller="createRequestWorkflow" action="index"><warehouse:message code="request.create.label" default="Add new request"/></g:link>
						</td>					
					</tr>				
					
				</table>
			</div>			
		</li>
	</g:authorize>		
	</li>
	<g:authorize activity="[ActivityCode.SEND_STOCK]">
		<li>
			<g:link controller="shipment" action="list" params="[type:'outgoing']">
				<warehouse:message code="shipping.label" />
			</g:link>
			<div>
				<table>
					<tr>
						<td>
							<g:link controller="shipment" action="list" params="[type:'outgoing']"><warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/></g:link>
						</td>
					</tr>
					<tr>
						<td>
							<table>
								<g:each in="${outgoingShipments}" var="statusRow">
									<tr>
										<td>
											<g:link controller="shipment" action="list" params="[status:statusRow.key]">
												<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
											</g:link>
										</td>
									</tr>
								</g:each>
							</table>
						
						</td>
					</tr>
					<tr>
						<td>
							<g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']"><warehouse:message code="shipping.createOutgoingShipment.label"/></g:link>
						</td>	
					</tr>
				</table>
			</div>
		</li>
	</g:authorize>		
	<g:authorize activity="[ActivityCode.RECEIVE_STOCK]">		
		<li>
			<g:link controller="shipment" action="list" params="[type: 'incoming']">
				<warehouse:message code="receiving.label" />
			</g:link>
			<div>
				<table>
					<tr>
						<td>
							<g:link controller="shipment" action="list" params="[type: 'incoming']"><warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/></g:link>
						</td>
					</tr>
					<tr>
						<td>
							<table>
								<g:each in="${incomingShipments}" var="statusRow">
									<tr>
										<td>
											<g:link controller="shipment" action="list" params="[type: 'incoming', status:statusRow.key]">
												<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
											</g:link>
										</td>
									</tr>
								</g:each>					
							</table>
						</td>
					</tr>
					<tr>
						<td>
							<g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']"><warehouse:message code="shipping.createIncomingShipment.label"/></g:link>
						</td> 
					</tr>
				</table>
			</div>
		</li>		
	</g:authorize>		
	
	<li>
		<a href="javascript:void(0)"><warehouse:message code="report.label" /></a>
		<div>
			<table>			
				<tr>
					<td>
						<g:link controller="report" action="showTransactionReport"><warehouse:message code="report.showTransactionReport.label"/></g:link>
					</td>
				</tr>
				<tr>
					<td>
						<g:link controller="report" action="showShippingReport"><warehouse:message code="report.showShippingReport.label"/></g:link>
					</td>
				</tr>
				<tr>
					<td>
						<g:link controller="inventory" action="listExpiredStock"><warehouse:message code="inventory.expiredStock.label"/></g:link> 
					</td>
				</tr>
				<tr>
					<td>
						<g:link controller="inventory" action="listExpiringStock"><warehouse:message code="inventory.expiringStock.label"/></g:link> 
					</td>
				</tr>
				<tr>
					<%-- 
					<td>
						<g:link controller="inventory" action="listLowStock"><warehouse:message code="inventory.lowStock.label"/></g:link> 
					</td>
					--%>
				</tr>
				<tr>
					<td>
						<g:link controller="inventory" action="showConsumption"><warehouse:message code="inventory.consumption.label"/></g:link> 
					</td>
				</tr>
				<tr>
					<td>
						<g:link controller="inventory" action="listDailyTransactions"><warehouse:message code="transaction.dailyTransactions.label"/></g:link> 
					</td>
				</tr>
			</table>
		</div>		
		
	</li>
	<li>
		<a href="javascript:void(0)"><warehouse:message code="admin.label" /></a>	
		<div>
			<table>
				<tr>
					<td>
						<span class="menu-subheading"><warehouse:message code="default.general.label"/></span>
						<table>
							<tr>
								<td>
									<g:link controller="admin" action="showSettings"><warehouse:message code="default.manage.label" args="[warehouse.message(code:'default.settings.label')]"/></g:link>
								</td>		
							</tr>
							<tr>
								<td>
									<g:link controller="batch" action="importData" params="[type:'product']"><warehouse:message code="default.import.label" args="[warehouse.message(code:'products.label')]"/></g:link> 				
								</td>			
							</tr>
							<tr>
								<td>
									<g:link controller="batch" action="importData" params="[type:'inventory']"><warehouse:message code="default.import.label" args="[warehouse.message(code:'inventory.label')]"/></g:link> 				
								</td>			
							</tr>
						</table>				
					</td>
					<td>
					
						<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">	
							<span class="menu-subheading"><warehouse:message code="inventory.label"/></span>
							<table>			
								<tr>
									<td>
										<g:link controller="inventory" action="listAllTransactions"><warehouse:message code="transaction.list.label"/></g:link> 
									</td>
								</tr>
								<tr>
									<td>
										<g:link controller="inventory" action="editTransaction"><warehouse:message code="transaction.add.label"/></g:link> 				
									</td>			
								</tr>
							</table>	
						</g:authorize>				
					</td>
					<td>
						<span class="menu-subheading"><warehouse:message code="products.label"/></span>
						<table>			
							<tr>
								<td>
									<g:link controller="product" action="list"><warehouse:message code="products.label"/></g:link>
								</td>
							</tr>
							<tr>
								<td>
									<g:link controller="attribute" action="list"><warehouse:message code="attributes.label"/></g:link>
								</td>
							</tr>
							<tr>
								<td>
									<g:link controller="category" action="tree"><warehouse:message code="categories.label"/></g:link>
								</td>
							</tr>
							<tr>
								<td>
									<g:link controller="product" action="batchEdit"><warehouse:message code="product.batchEdit.label" /></g:link>
								</td>
							</tr>
						</table>				
					</td>
					<td>
						<span class="menu-subheading"><warehouse:message code="locations.label"/></span>
						<table>
							<tr>
								<td>
									<g:link controller="locationGroup" action="list"><warehouse:message code="location.sites.label"/></g:link>
								</td>
							</tr>
							<tr>
								<td>
									<g:link controller="location" action="list"><warehouse:message code="locations.label"/></g:link>
								</td>
							</tr>
							<tr>
								<td>
									<g:link controller="shipper" action="list"><warehouse:message code="location.shippers.label"/></g:link>
								</td>
							</tr>
							<tr>
								<td>
									<g:link controller="locationType" action="list"><warehouse:message code="location.locationTypes.label"/></g:link>
								</td>
							</tr>
						</table>				
					</td>
					<td>
					
						<span class="menu-subheading"><warehouse:message code="persons.label"/></span>
						<table>
							<tr>
								<td>
									<g:link controller="person" action="list"><warehouse:message code="person.list.label"/></g:link>
								</td>		
							</tr>
							<tr>
								<td>
									<g:link controller="user" action="list"><warehouse:message code="users.label"/></g:link>
								</td>	
							</tr>
							<!--  								
							<tr>
								<td>
									<g:link controller="role" action="list"><warehouse:message code="default.manage.label" args="['roles']"/></g:link>
								</td>		
							</tr>
							-->
						</table>				
					</td>
				</tr>
			</table>
		</div>
	</li>
<div>	
</ul>
<!--MegaMenu Ends-->