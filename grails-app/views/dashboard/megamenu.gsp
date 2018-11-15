<%@page import="org.pih.warehouse.requisition.RequisitionStatus; org.pih.warehouse.core.ActivityCode"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>

<ul class="megamenu">

    <g:if test="${megamenuConfig.dashboard.enabled || isSuperuser}">
        <li>
            <g:link controller="dashboard" action="index">
                <warehouse:message code="dashboard.label" />&nbsp;
            </g:link>
        </li>
    </g:if>

    <g:if test="${megamenuConfig.analytics.enabled || isSuperuser}">
        <g:isUserAdmin>
            <li>
                <a href="javascript:void(0)"> <warehouse:message code="analytics.label" default="Analytics"/></a>
                <div style="min-width: 200px;">
                    <div class="buttonsBar">
                        <div class="megaButton">
                            <g:link controller="inventoryBrowser" action="index" class="list">
                                <warehouse:message code="inventory.browse.label" default="Inventory browser" />
                                <span class="beta">Beta</span>
                            </g:link>

                        </div>
                        <div class="megaButton">
                            <g:link controller="inventorySnapshot" action="list" class="list">
                                <warehouse:message code="inventory.snapshot.label" default="Inventory snapshots" />
                                <span class="beta">Beta</span>
                            </g:link>

                        </div>
                    </div>
                </div>
            </li>
        </g:isUserAdmin>
    </g:if>

    <g:if test="${megamenuConfig.inventory.enabled || isSuperuser}">

        <g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
            <li>
                <a href="javascript:void(0)"> <warehouse:message code="inventory.label" /></a>
                <div>
                    <div class="buttonsBar" style="min-width: 200px;">

                        <div>
                            <div class="megaButton">
                                <g:link controller="inventory" action="browse" params="[resetSearch:true]">
                                    <warehouse:message code="inventory.browse.label" />
                                </g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="inventory" action="manage">
                                    <warehouse:message code="inventory.manage.label" />
                                </g:link>
                            </div>
                            <hr/>
                            <div class="megaButton">
                                <g:link controller="inventory" action="browse" class="browse" params="[resetSearch:true]">
                                    <warehouse:message code="inventory.browseByCategory.label"/>
                                </g:link>
                            </div>
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
    </g:if>

    <g:if test="${megamenuConfig.orders.enabled || isSuperuser}">
        <g:authorize activity="[ActivityCode.PLACE_ORDER,ActivityCode.FULFILL_ORDER]">
            <li>

                <a href="javascript:void(0)"><warehouse:message code="orders.label"/></a>

                <div class="buttonsBar" style="min-width: 200px;">
                    <div class="megaButton">
                        <g:link controller="purchaseOrderWorkflow" action="index" class="create">
                            <warehouse:message code="order.create.label"/>
                        </g:link>
                    </div>
                    <hr/>
                    <div class="megaButton">
                        <g:link controller="order" action="list" params="[orderTypeCode:'PURCHASE_ORDER']" class="list">
                            <warehouse:message code="orders.label"/>
                        </g:link>
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
    </g:if>


    <g:if test="${megamenuConfig.requisitions.enabled || isSuperuser}">
        <g:authorize activity="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
            <li>

                <a href="javascript:void(0)"><warehouse:message code="requests.label"/></a>
                <div>
                    <div class="megaButton">
                        <g:link controller="requisition" action="chooseTemplate" class="create" params="[type:'STOCK']">
                            <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardStock.label')]" />
                        </g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="requisition" action="create" class="create" params="[type:'NON_STOCK']">
                            <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardNonStock.label')]" />
                        </g:link>
                    </div>
                    <div class="megaButton">
                        <g:link controller="requisition" action="create" class="create" params="[type:'ADHOC']">
                            <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardAdhoc.label')]" />
                        </g:link>
                    </div>
                    <hr/>
                    <div class="buttonsBar" style="min-width: 200px;">

                        <div class="megaButton">
                            <g:link controller="requisition" action="list">
                                <warehouse:message code="requests.label"/>
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="requisition" action="list" class="list">
                                <warehouse:message code="default.all.label" default="All" />
                                (${requisitionStatistics["ALL"]?:0})
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

                    </div>
                </div>
            </li>
        </g:authorize>
    </g:if>



    <g:if test="${megamenuConfig.inbound.enabled || isSuperuser}">
        <g:authorize activity="[ActivityCode.RECEIVE_STOCK]">
            <li>
                <a href="javascript:void(0)"><warehouse:message code="default.inbound.label" /></a>
                    <div class="buttonsBar" style="min-width: 200px;">
                        <g:if test="${megamenuConfig.stockMovement.enabled || isSuperuser}">
                            <div class="megaButton">
                                <g:link controller="stockMovement" action="create" params="[direction:'INBOUND']">
                                    <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.inbound.label')]"/>
                                </g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="stockMovement" action="list" params="[direction:'INBOUND']">
                                    <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.inbound.label')]"/>
                                </g:link>
                            </div>
                            <hr/>
                            <div class="megaButton">
                                <g:link controller="putAway" action="index">
                                    <warehouse:message code="default.create.label" args="[g.message(code:'putAway.label')]"/>
                                </g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="order" action="list" params="[orderTypeCode: 'TRANSFER_ORDER']">
                                    <warehouse:message code="default.list.label" args="[g.message(code:'putAways.label')]"/>
                                </g:link>
                            </div>
                        </g:if>
                        <g:if test="${megamenuConfig.receiving.enabled || isSuperuser}">
                            <hr/>
                            <div class="megaButton">
                                <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']" class="create"><warehouse:message code="shipping.createIncomingShipment.label"/></g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="shipment" action="list" params="[type: 'incoming']" class="list">
                                    <warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/>
                                </g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="shipment" action="list" params="[type:'incoming']" class="list">
                                    <warehouse:message code="default.all.label"/> (${inboundShipmentsTotal})
                                </g:link>
                            </div>
                            <g:each in="${inboundShipmentsCount}" var="statusRow">
                                <div class="megaButton">
                                    <g:link controller="shipment" action="list" params="[type: 'incoming', status:statusRow.status]" class="shipment-status-${statusRow.status }">
                                        <format:metadata obj="${statusRow.status}"/> (${statusRow.count})
                                    </g:link>
                                </div>
                            </g:each>
                        </g:if>
                    </div>
                </a>
            </li>

        </g:authorize>
    </g:if>

    <g:if test="${megamenuConfig.outbound.enabled}">
        <g:authorize activity="[ActivityCode.SEND_STOCK]">
            <li>
                <a href="javascript:void(0)"><warehouse:message code="default.outbound.label" /></a>
                <div class="buttonsBar" style="min-width: 200px;">
                    <g:if test="${megamenuConfig.stockMovement.enabled || isSuperuser}">
                        <div class="megaButton">
                            <g:link controller="stockMovement" action="create" params="[direction:'OUTBOUND']">
                                <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.outbound.label')]"/>
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="stockMovement" action="list" params="[direction:'OUTBOUND']">
                                <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.outbound.label')]"/>
                            </g:link>
                        </div>
                    </g:if>
                    <g:if test="${megamenuConfig.shipping.enabled || isSuperuser}">
                        <hr/>
                        <div class="megaButton">
                            <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']" class="create"><warehouse:message code="shipping.createOutgoingShipment.label"/></g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list">
                                <warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/>
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list">
                                <warehouse:message code="default.all.label"/> (${outboundShipmentsTotal})
                            </g:link>
                        </div>
                        <g:each in="${outboundShipmentsCount}" var="statusRow">
                            <div class="megaButton">
                                <g:link controller="shipment" action="list" params="[status:statusRow.status]" class="shipment-status-${statusRow.status }">
                                    <format:metadata obj="${statusRow.status}"/> (${statusRow.count})
                                </g:link>
                            </div>
                        </g:each>
                    </g:if>
                </div>
            </li>
        </g:authorize>
    </g:if>

    <g:if test="${megamenuConfig.reporting.enabled || isSuperuser}">
        <li>
            <a href="javascript:void(0)"><warehouse:message code="report.label" /></a>
            <div class="buttonsBar" style="min-width: 200px;">
                <%--
                <div class="megaButton">
                    <g:link controller="report" action="list"><warehouse:message code="report.list.label" default="List reports"/></g:link>
                </div>
                --%>
                <div class="megaButton">
                    <g:link controller="cycleCount" action="exportAsCsv">
                        <warehouse:message code="report.cycleCount.label" default="Cycle Count Report"/>
                    </g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="report" action="showBinLocationReport">
                        <warehouse:message code="report.binLocationReport.label" default="Bin Location Report"/>
                    </g:link>
                </div>
                <div class="megaButton">
                    <g:link controller="inventory" action="show">
                        <warehouse:message code="report.baselineQohReport.label" default="Baseline QoH Report"/>
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
                <div class="megaButton">
                    <g:link controller="report" action="exportBinLocation" params="[downloadFormat:'csv']" class=""><warehouse:message code="report.exportBinLocations.label" default="Export bin locations"/></g:link>
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
    </g:if>


    <g:if test="${megamenuConfig.products.enabled || isSuperuser}">
        <g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
            <li>
                <a href="javascript:void(0)"><warehouse:message code="products.label" /></a>
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
                            <g:link controller="productSupplier" action="list" class="list"><warehouse:message code="productSuppliers.label"/></g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="productAssociation" action="list" class="list"><warehouse:message code="product.associations.label" default="Associations"/></g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="productCatalog" action="list" class="list"><warehouse:message code="product.catalogs.label" default="Catalogs"/></g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="productComponent" action="list" class="list"><warehouse:message code="product.components.label" default="Components"/></g:link>
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
                        <div class="megaButton">
                            <g:link controller="inventoryLevel" action="list" class="site"><warehouse:message code="inventoryLevels.label" default="Inventory Levels" /></g:link>
                        </div>
                        <g:isUserAdmin>
                            <div>
                                <hr/>
                            </div>
                            <div class="megaButton">
                                <g:link controller="product" action="create" class="create"><warehouse:message code="product.create.label"/></g:link>
                            </div>
                            <%--
                            <div class="megaButton">
                                <g:link controller="createProductFromTemplate" action="index" class="create"><warehouse:message code="product.createFromTemplate.label"/></g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="createProduct" action="index" class="create"><warehouse:message code="product.createFromGoogle.label"/></g:link>
                            </div>
                            --%>
                            <div class="megaButton">
                                <g:link controller="product" action="batchEdit" class="create"><warehouse:message code="product.batchEdit.label"/></g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="product" action="importAsCsv" class="import"><warehouse:message code="product.importAsCsv.label"/></g:link>
                            </div>
                            <div class="megaButton">
                                <g:link controller="product" action="exportAsCsv" class="list"><warehouse:message code="product.exportAsCsv.label"/></g:link>
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
    </g:if>


    <g:if test="${megamenuConfig.configuration.enabled || isSuperuser}">
        <g:isUserAdmin>
            <li><a href="javascript:void(0)"> <warehouse:message code="configuration.label" default="Configuration" /></a>
                <div style="min-width: 200px;">
                    <div class="buttonsBar">
                        <g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
                            <div class="megaButton">
                                <g:link controller="admin" action="showSettings" class="list">
                                    <g:message code="default.settings.label"/>
                                </g:link>
                            </div>
                        </g:authorize>
                        <div class="megaButton">
                            <g:link controller="migration" action="index" class="list">
                                <g:message code="default.migrateData.label" default="Migrate Data" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="console" action="index" class="list">
                                <g:message code="default.console.label" default="Console" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="admin" action="cache" class="list">
                                <g:message code="default.cache.label" default="Cache" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="admin" action="clickstream" class="list">
                                <g:message code="default.clickstream.label" default="Clickstream" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="admin" action="sendMail" class="list">
                                <g:message code="config.sendMail.label" default="Email"/>
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="localization" action="list" class="list">
                                <g:message code="localization.label" default="Localization"/>
                            </g:link>
                        </div>
                        <hr/>
                        <div class="megaButton">
                            <g:link controller="documentType" action="list">
                                <warehouse:message code="documentTypes.label" default="Document Types"/>
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="eventType" action="list"
                                    class="eventType">
                                <warehouse:message code="location.eventTypes.label" default="Event types" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="locationGroup" action="list" class="site">
                                <warehouse:message code="locationGroups.label" default="Location Groups" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="locationType" action="list"
                                    class="locationType">
                                <warehouse:message code="location.locationTypes.label" default="Location Types" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="partyType" action="list">
                                <warehouse:message code="partyTypes.label" default="Party Types" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="partyRole" action="list">
                                <warehouse:message code="partyRoles.label" default="Party Roles" />
                            </g:link>
                        </div>
                        <hr/>
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
                            <g:link controller="organization" action="list">
                                <warehouse:message code="organizations.label" default="Organizations" />
                            </g:link>
                        </div>
                        <hr/>

                        <div class="megaButton">
                            <g:link controller="shipmentWorkflow" action="list" >
                                <warehouse:message code="shipmentWorkflows.label" default="Shipment Workflows" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="document" action="list" >
                                <warehouse:message code="documents.label" default="Documents" />
                            </g:link>
                        </div>

                        <hr/>
                        <div class="megaButton">
                            <g:link controller="person" action="list" class="people">
                                <warehouse:message code="person.list.label" />
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="requisitionTemplate" action="list" class="list">
                                <warehouse:message code="requisitionTemplates.label" default="Stock lists" />
                                <%--(${requisitionTemplates.size()})--%>
                            </g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="inventory" action="listAllTransactions" class="list"><warehouse:message code="transactions.label"/></g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="user" action="list" class="user">
                                <warehouse:message code="users.label" />
                            </g:link>
                        </div>
                        <hr/>
                        <div class="megaButton">
                            <g:link controller="inventory" action="editTransaction" class="create"><warehouse:message code="transaction.add.label"/></g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="batch" action="importData" params="[type:'inventory']" class="inventory"><warehouse:message code="default.import.label" args="[warehouse.message(code:'inventory.label', default: 'Inventory')]"/></g:link>
                        </div>
                        <div class="megaButton">
                            <g:link controller="batch" action="importData" params="[type:'inventoryLevel']" class="inventory"><warehouse:message code="default.import.label" args="[warehouse.message(code:'inventoryLevel.label', default: 'Inventory levels')]"/></g:link>
                        </div>

                    </div>
                </div>
            </li>
        </g:isUserAdmin>
    </g:if>
    <g:if test="${megamenuConfig.customLinks.enabled || isSuperuser}">
        <li><a href="javascript:void(0)"> <warehouse:message code="customLinks.label" default="Custom Links" /></a>
            <div style="min-width: 200px;">
                <div class="buttonsBar">
                    <g:each var="link" in="${megamenuConfig.customLinks.content}">
                        <div class="megaButton">
                            <a href="${link.href}" target="${link.target}">
                                ${link.label}
                            </a>
                        </div>
                    </g:each>
                    <g:unless test="${megamenuConfig.customLinks.content}">
                        <div class="megaButton">
                            <a href="#">There are no custom links</a>
                        </div>
                    </g:unless>
                </div>
            </div>
        </li>
    </g:if>
</ul>
<!--MegaMenu Ends-->
