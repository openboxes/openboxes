<%@page import="org.pih.warehouse.requisition.RequisitionStatus; org.pih.warehouse.core.ActivityCode"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>
<ul class="megamenu">
    <li>
        <g:link controller="dashboard" action="index">
            <warehouse:message code="dashboard.label" />&nbsp;
        </g:link>
    </li>
	<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
		<li>
			<g:link controller="inventory" action="browse">
				<warehouse:message code="inventory.label" />&nbsp;
			</g:link>
			<div>
				<div class="buttonsBar" style="min-width: 200px;">
					
					<div>
						<div class="megaButton">
							<g:link controller="inventory" action="browse" class="browse" params="[resetSearch:true]">
								<warehouse:message code="inventory.browseByCategory.label"/>
							</g:link>
						</div>
                        <hr/>
						<div style="max-height: 400px; overflow: auto;">
							<g:if test='${quickCategories }'>
								<g:each var="category" in="${quickCategories}">
									<div class="megaButton">
										<g:link class="outline" controller="inventory" action="browse" params="[subcategoryId:category.id,resetSearch:true,searchPerformed:true,showOutOfStockProducts:'on']">
											<format:category category="${category}"/> (${category?.products?.size() })
										</g:link>
									</div>
									<g:if test="${category.categories}">
										<g:each var="childCategory" in="${category.categories}">
											<div class="megaButton">
												<g:link controller="inventory" action="browse" params="[subcategoryId:childCategory.id,resetSearch:true,searchPerformed:true,showOutOfStockProducts:'on']">
													<format:category category="${childCategory}"/> (${childCategory?.products?.size() })
												</g:link>
											</div>
										</g:each>	
									</g:if>
								</g:each>	
							</g:if>	
							<g:elseif test='${categories }'>
								<g:each var="entry" in="${categories}">
									
									<g:each var="category" in="${entry.value }">
										<div class="megaButton">
											<g:link controller="inventory" action="browse" params="[subcategoryId:category?.id,resetSearch:true,searchPerformed:true,showOutOfStockProducts:'on']">
												${category } (${category.products.size() })
											</g:link>
										</div>
									</g:each>
								</g:each>
							</g:elseif>	
						</div>	
					</div>
                    <%--
					<div style="float: left; padding: 10px">
						<div class="megaButton">
							<g:link controller="inventory" action="browse" class="browse" params="[resetSearch:true]">
								<label><warehouse:message code="inventory.browseByTag.label"/></label>
							</g:link>
						</div>
						<div style="max-height: 400px; overflow: auto; width: 600px;">
							<g:if test='${tags }'>
								<g:each var="tag" in="${tags}">
									
									<g:link class="outline" controller="inventory" action="browse" params="[tag:tag.tag,resetSearch:true]">
										<span class="tag">${tag.tag } (${tag?.products?.size() })</span>
									</g:link>
									
								</g:each>
							</g:if>
						</div>	
					</div>				
					--%>
					
					<div class="clear"></div>		
				</div>			
			</div>				
		</li>
	</g:authorize>
	
	<g:authorize activity="[ActivityCode.PLACE_ORDER,ActivityCode.FULFILL_ORDER]">	
		<li>
			<g:link controller="order" action="list" class="list">			
				<warehouse:message code="orders.label"/>
			</g:link>
			<div class="buttonsBar" style="min-width: 200px;">
                <div class="megaButton">
                    <g:link controller="purchaseOrderWorkflow" action="index" class="create">
                        <warehouse:message code="order.create.label"/>
                    </g:link>
                </div>
                <hr/>
                <div class="megaButton">
					<g:link controller="order" action="list" params="[status:'PENDING']" class="list"><warehouse:message code="order.list.label"/></g:link>
				</div>
				<g:each in="${incomingOrders}" var="orderStatusRow">
					<div class="megaButton">
						<g:link controller="order" action="list" params="[status:orderStatusRow[0]]" class="order-status-${orderStatusRow[0] }">
							<format:metadata obj="${orderStatusRow[0]}"/> (${orderStatusRow[1]})
						</g:link>
					</div>					
				</g:each>
			</div>
		</li>
	</g:authorize>
	<g:authorize activity="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
		<li>
                <g:link controller="requisition" action="list">
                    <warehouse:message code="requests.label"/>
                </g:link>
                <div>
                <div class="megaButton">
                    <g:link controller="requisition" action="chooseTemplate" class="create" params="[type:'WARD_STOCK']">
                        <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardStock.label')]" />
                    </g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="requisition" action="createNonStock" class="create" params="[type:'WARD_NON_STOCK']">
                        <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardNonStock.label')]" />
                    </g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="requisition" action="createAdhoc" class="create" params="[type:'WARD_ADHOC']">
                        <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardAdhoc.label')]" />
                    </g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="requisition" action="createDepot" class="create" params="[type:'DEPOT_TO_DEPOT']">
                        <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.depotToDepot.label')]" />
                    </g:link>
                </div>
                <hr/>
                <div class="buttonsBar" style="min-width: 200px;">
                    <div class="megaButton">
                        <g:link controller="requisition" action="list" class="list">
                            <warehouse:message code="requisitions.label" default="Requisitions" />
                            (${requisitionStatistics["ALL"]})
                        </g:link>
                    </div>
                    <g:each var="requisitionStatus" in="${RequisitionStatus.list()}">
                        <g:if test="${requisitionStatistics[requisitionStatus]>0}">
                            <div class="megaButton">
                                <g:link controller="requisition" action="list" params="[status:requisitionStatus]">
                                    <format:metadata obj="${requisitionStatus}"/>
                                    (${requisitionStatistics[requisitionStatus]?:0 })
                                </g:link>
                            </div>
                        </g:if>
                    </g:each>

                <%--
                <div class="megaButton">
                    <g:link controller="requisition" action="list" class="list" >
                        <warehouse:message code="requisition.listPending.label" default="List pending requisitions"/> (${incomingRequests?.values()?.flatten()?.size()?:0 })
                    </g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="requisition" action="list" params="['createdBy.id':session.user.id]" class="list">
                        <warehouse:message code="requisition.listByMe.label" default="List my requisitions" /> (${myRequisitions?.size()?:0 })
                    </g:link>
                </div>
                --%>
					<%--
	                <div class="megaButton">
	                    <g:link controller="requisition" action="create" class="create" params="[type:'DEPOT_STOCK']">
	                        <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.depot-stock.label')]" />
	                    </g:link>
	                </div>
					
	                <div class="megaButton">
	                    <g:link controller="requisition" action="create" class="create" params="[type:'DEPOT_NON_STOCK']">
	                        <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.depot-non-stock.label')]" />
	                    </g:link>
	                </div>
	                --%>
	        	</div>
			</div>
		</li>
	</g:authorize>		
	
	<g:authorize activity="[ActivityCode.SEND_STOCK]">
		<li>
			<g:link controller="shipment" action="list" params="[type:'outgoing']">
				<warehouse:message code="shipping.label" />
			</g:link>
			<div class="buttonsBar" style="min-width: 200px;">
                <div class="megaButton">
                    <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']" class="create"><warehouse:message code="shipping.createOutgoingShipment.label"/></g:link>
                </div>
                <hr/>
                <div class="megaButton">
   					<g:link controller="shipment" action="list" params="[type:'outgoing']" class="list">
                            <warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/>
                    </g:link>
				</div>
                <div class="megaButton">
                    <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list">
                        All (${outgoingShipmentsCount})
                    </g:link>
                </div>
				<g:each in="${outgoingShipments}" var="statusRow">
					<div class="megaButton">
						<g:link controller="shipment" action="list" params="[status:statusRow.key]" class="shipment-status-${statusRow.key }">
							<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
						</g:link>
					</div>
				</g:each>
			</div>

        </li>
	</g:authorize>		
	<g:authorize activity="[ActivityCode.RECEIVE_STOCK]">		
		<li>
			<g:link controller="shipment" action="list" params="[type: 'incoming']">
				<warehouse:message code="receiving.label" />
			</g:link>

			<div class="buttonsBar" style="min-width: 200px;">
                <div class="megaButton">
                    <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']" class="create"><warehouse:message code="shipping.createIncomingShipment.label"/></g:link>
                </div>
                <hr/>
                <div class="megaButton">
					<g:link controller="shipment" action="list" params="[type: 'incoming']" class="list">
                        <warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/>
                    </g:link>
				</div>

                <div class="megaButton">
                    <g:link controller="shipment" action="list" params="[type:'incoming']" class="list">
                        All (${incomingShipmentsCount})
                    </g:link>
                </div>
				<g:each in="${incomingShipments}" var="statusRow">
					<div class="megaButton">
						<g:link controller="shipment" action="list" params="[type: 'incoming', status:statusRow.key]" class="shipment-status-${statusRow.key }">
							<format:metadata obj="${statusRow.key}"/> (${statusRow.value.size()})
						</g:link>
					</div>
				</g:each>					
			</div>
		</li>		
	</g:authorize>			
	<li>
		<a href="javascript:void(0)">
			<warehouse:message code="report.label" />
		</a>
		<div class="buttonsBar" style="min-width: 200px;">
            <div class="megaButton">
                <g:link controller="inventory" action="show">
                    <warehouse:message code="report.baselineQohReport.label" default="Baseline QoH report"/>
                </g:link>
            </div>

			<div class="megaButton">
				<g:link controller="report" action="showTransactionReport"><warehouse:message code="report.showTransactionReport.label"/></g:link>
			</div>
            <div class="megaButton">
                <g:link controller="consumption" action="show"><warehouse:message code="report.consumption.label" default="Consumption report"/></g:link>
            </div>
            <%--
			<div class="megaButton">
				<g:link controller="inventory" action="showConsumption" class="report_consumption"><warehouse:message code="inventory.consumption.label"/></g:link> 
			</div>
			--%>
			<div class="megaButton">
				<g:link controller="inventory" action="listDailyTransactions"><warehouse:message code="transaction.dailyTransactions.label"/></g:link>
			</div>
			<div class="megaButton">
				<g:link controller="report" action="showShippingReport"><warehouse:message code="report.showShippingReport.label"/></g:link>
			</div>
            <div class="megaButton">
                <g:link controller="report" action="showInventorySamplingReport"><warehouse:message code="report.showInventorySamplingReport.label" default="Inventory sampling report"/></g:link>
            </div>
            <hr/>
			<div class="megaButton">
				<g:link controller="inventory" action="listExpiredStock" class="report-expired"><warehouse:message code="inventory.expiredStock.label"/></g:link>
			</div>
			<div class="megaButton">
				<g:link controller="inventory" action="listExpiringStock" class="report-expiring"><warehouse:message code="inventory.expiringStock.label"/></g:link>
			</div>
			<div class="megaButton">
				<g:link controller="inventory" action="listLowStock" class="report-low"><warehouse:message code="inventory.lowStock.label"/></g:link>
			</div>
			<div class="megaButton">
				<g:link controller="inventory" action="listReorderStock" class="report-reorder"><warehouse:message code="inventory.reorderStock.label"/></g:link>
			</div>
            <hr/>
            <div class="megaButton">
                <g:link controller="product" action="exportAsCsv" class="list"><warehouse:message code="product.exportAsCsv.label"/></g:link>
            </div>
            <div class="megaButton">
                <g:link controller="inventory" action="exportLatestInventoryDate" class="list"><warehouse:message code="product.exportLatestInventoryDate.label" default="Export latest inventory date"/></g:link>
            </div>
            <div class="megaButton">
                <g:link controller="inventoryLevel" action="export" class="list"><warehouse:message code="inventoryLevel.export.label" default="Export inventory levels"/></g:link>
            </div>
            <%--
            <div class="megaButton">
                <g:link controller="requisitionItem" action="listPending" class=""><warehouse:message code="requisitionItem.listPending.label" default="List pending items"/></g:link>
            </div>
            --%>
            <div class="megaButton">
                <g:link controller="requisition" action="export" class="list"><warehouse:message code="default.export.label" default="Export {0}" args="${['requisitions'] }"/></g:link>
            </div>
            <div class="megaButton">
                <g:link controller="requisitionItem" action="listCanceled" class=""><warehouse:message code="requisitionItem.listCanceled.label" default="Export requisition items"/></g:link>
            </div>
		</div>
	</li>

<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
    <li>
        <a href="javascript:void(0)">
            <warehouse:message code="products.label" />
        </a>
        <div>
            <div class="buttonsBar" style="min-width: 200px;">
                <div class="megaButton">
                    <g:link controller="product" action="list" class="list"><warehouse:message code="products.label"/></g:link>
                </div>
                <%--
                <g:if test="${session.productsViewed }">
                    <div>
                        <g:each var="product" in="${session?.productsViewed?.values() }">
                            <div class="megaButton">
                                <g:link controller="inventoryItem" action="showStockCard" id="${product.id }" class="product">
                                    ${product.name }
                                </g:link>
                            </div>
                        </g:each>
                    </div>
                </g:if>
                --%>
                <div class="megaButton">
                    <g:link controller="productGroup" action="list" class="list"><warehouse:message code="productGroups.label"/></g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="attribute" action="list" class="list"><warehouse:message code="attributes.label"/></g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="category" action="tree" class="list"><warehouse:message code="categories.label"/></g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="tag" action="list" class="list"><warehouse:message code="product.tags.label"/></g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="unitOfMeasure" action="list" class="list"><warehouse:message code="unitOfMeasure.label"/></g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="unitOfMeasureClass" action="list" class="list"><warehouse:message code="unitOfMeasureClass.label"/></g:link>
                </div>
                <g:isUserAdmin>
                    <div>
                        <hr/>
                    </div>
                    <div class="megaButton">
                        <g:link controller="product" action="create" class="create"><warehouse:message code="product.create.label"/></g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="createProductFromTemplate" action="index" class="create"><warehouse:message code="product.createFromTemplate.label"/></g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="createProduct" action="index" class="create"><warehouse:message code="product.createFromGoogle.label"/></g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="product" action="batchEdit" class="create"><warehouse:message code="product.batchEdit.label"/></g:link>
                    </div>
                    <hr/>
                    <div class="megaButton">
                        <g:link controller="batch" action="importData" params="[type:'']" class="inventory"><warehouse:message code="default.import.label" args="[warehouse.message(code:'data.label', default: 'data')]"/></g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="product" action="importAsCsv" class="import"><warehouse:message code="product.importAsCsv.label"/></g:link>
                    </div>
                <%--
                <div class="megaButton">
                    <g:link controller="batch" action="importData" params="[type:'productPrice']" class="inventory"><warehouse:message code="default.import.label" args="[warehouse.message(code:'product.label')]"/></g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="batch" action="importData" params="[type:'productPrice']" class="inventory"><warehouse:message code="default.import.label" args="[warehouse.message(code:'productPrice.label')]"/></g:link>
                </div>
                --%>

                </g:isUserAdmin>
            </div>
        </div>
    </li>
</g:authorize>
<g:isUserAdmin>
		<li><a href="javascript:void(0)"> <warehouse:message code="admin.label" /></a>
			<div style="min-width: 200px;">
				<div class="buttonsBar">
                    <div class="megaButton">
                        <g:link controller="console" action="index" class="list">
                            <warehouse:message code="default.console.label" default="Open console" />
                        </g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="admin" action="manage" class="list">
                            <warehouse:message code="default.appinfo.label" default="Show app-info" />
                        </g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="admin" action="cache" class="list">
                            <warehouse:message code="default.cache.label" default="Show cache" />
                        </g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="admin" action="clickstream" class="list">
                            <warehouse:message code="default.clickstream.label" default="Show clickstream" />
                        </g:link>
                    </div>
					<g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
						<div class="megaButton">
							<g:link controller="admin" action="showSettings" class="list">
								<warehouse:message code="default.manage.label"
									args="[warehouse.message(code:'default.settings.label')]" />
							</g:link>
						</div>
					</g:authorize>

                    <div>
                        <hr/>
                    </div>

					<div class="megaButton">
						<g:link controller="locationGroup" action="list" class="site">
							<warehouse:message code="location.sites.label" />
						</g:link>
					</div>
					<div class="megaButton">
						<g:link controller="location" action="list" class="location">
							<warehouse:message code="locations.label" />
						</g:link>
					</div>
					<div class="megaButton">
						<g:link controller="shipper" action="list" class="shipper">
							<warehouse:message code="location.shippers.label" />
						</g:link>
					</div>
					<div class="megaButton">
						<g:link controller="locationType" action="list"
							class="locationType">
							<warehouse:message code="location.locationTypes.label" />
						</g:link>
					</div>
                    <div class="megaButton">
                        <g:link controller="requisitionTemplate" action="list" class="list">
                            <warehouse:message code="requisitionTemplates.label" default="Stock lists" />
                            <%--(${requisitionTemplates.size()})--%>
                        </g:link>
                    </div>

					<div>
						<hr/>
					</div>
					<div class="megaButton">
						<g:link controller="person" action="list" class="people">
							<warehouse:message code="person.list.label" />
						</g:link>
					</div>					
					<div class="megaButton">
						<g:link controller="user" action="list" class="user">
							<warehouse:message code="users.label" />
						</g:link>
					</div>
                    <hr/>
					<div class="megaButton">
						<g:link controller="inventory" action="listAllTransactions" class="list"><warehouse:message code="transactions.label"/></g:link> 
					</div>										
					<div class="megaButton">
						<g:link controller="inventory" action="editTransaction" class="create"><warehouse:message code="transaction.add.label"/></g:link> 				
					</div>
                </div>
			</div>
		</li>
	</g:isUserAdmin>



</ul>
<!--MegaMenu Ends-->
