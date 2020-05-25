<%@page import="org.pih.warehouse.core.ActivityCode"%>
<%@page import="org.pih.warehouse.order.OrderTypeCode"%>
<%@page import="org.pih.warehouse.requisition.RequisitionStatus"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>
<ul class="megamenu">

    <g:if test="${megamenuConfig.dashboard.enabled}">
        <li class="mm-item">
            <g:link controller="dashboard" action="index" class="mm-item-link">
                <warehouse:message code="dashboard.label" />&nbsp;
            </g:link>
        </li>
    </g:if>

    <g:if test="${megamenuConfig.analytics.enabled}">
        <g:isUserAdmin>
            <li class="mm-item">
                <a href="javascript:void(0)" class="mm-item-link">
                    <warehouse:message code="analytics.label" default="Analytics"/>
                </a>
                <div class="mm-content-base">
                    <div class="mm-menu-item">
                        <g:link controller="inventoryBrowser" action="index" class="list">
                            <warehouse:message code="inventory.browse.label" default="Inventory browser" />
                            <span class="beta">Beta</span>
                        </g:link>

                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="inventorySnapshot" action="list" class="list">
                            <warehouse:message code="inventory.snapshot.label" default="Inventory Snapshots" />
                            <span class="beta">Beta</span>
                        </g:link>

                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="consumption" action="list" class="list">
                            <warehouse:message code="consumption.report.label" default="Consumption Report" />
                            <span class="beta">Beta</span>
                        </g:link>

                    </div>
                </div>
            </li>
        </g:isUserAdmin>
    </g:if>

    <g:if test="${megamenuConfig.inventory.enabled}">

        <g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
            <li class="mm-item">
                <a href="javascript:void(0)" class="mm-item-link">
                    <warehouse:message code="inventory.label" />
                </a>
                <div class="mm-item-content">
                    <div class="mm-content-section">
                        <h3><warehouse:message code="inventory.browse.label"/></h3>
                        <div style="max-height: 400px; overflow: auto;">
                            <div class="mm-menu-item">
                                <g:link controller="inventory" action="browse" params="[resetSearch:true]">
                                    <warehouse:message code="inventory.browse.label"/>
                                </g:link>
                            </div>
                            <g:if test='${quickCategories }'>
                                <g:each var="category" in="${quickCategories}">
                                    <div class="mm-menu-item">
                                        <g:link class="outline" controller="inventory" action="browse" params="[subcategoryId:category.id,resetSearch:true,searchPerformed:true,showOutOfStockProducts:'on']">
                                            <format:category category="${category}"/> (${category?.products?.size() })
                                        </g:link>
                                    </div>
                                    <g:if test="${category.categories}">
                                        <g:each var="childCategory" in="${category.categories}">
                                            <div class="mm-menu-item">
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
                                        <div class="mm-menu-item">
                                            <g:link controller="inventory" action="browse" params="[subcategoryId:category?.id,resetSearch:true,searchPerformed:true,showOutOfStockProducts:'on']">
                                                ${category } (${category.products.size() })
                                            </g:link>
                                        </div>
                                    </g:each>
                                </g:each>
                            </g:elseif>
                        </div>
                    </div>
                    <div class="mm-content-section">
                        <h3><warehouse:message code="inventory.manage.label"/></h3>
                        <g:supports activityCode="${org.pih.warehouse.core.ActivityCode.ADJUST_INVENTORY}">
                            <div class="mm-menu-item">
                                <g:link controller="inventory" action="manage">
                                    <warehouse:message code="inventory.manage.label" />
                                </g:link>
                            </div>
                        </g:supports>
                        <div class="mm-menu-item">
                            <g:link controller="batch" action="importData" params="[type:'inventory']" class="inventory">
                                <g:message code="default.import.label" args="[g.message(code:'inventory.label', default: 'Inventory')]"/>
                            </g:link>
                        </div>
                    </div>
                </div>
            </li>
        </g:authorize>
    </g:if>

    <g:if test="${megamenuConfig.requisitions.enabled}">
        <g:authorize activity="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
            <li class="mm-item">
                <a href="javascript:void(0)" class="mm-item-link">
                    <warehouse:message code="requests.label"/>
                    <span class="deprecated"
                          onclick="javascript:alert('${g.message(code: "requisition.deprecation.message")}')">deprecated</span>
                </a>
                <div class="mm-item-content">
                    <div class="mm-content-section">
                        <h3><warehouse:message code="default.list.label" args="[warehouse.message(code: 'requisitions.label')]" /></h3>
                        <div class="mm-menu-item">
                            <g:link controller="requisition" action="list" class="list">
                                <warehouse:message code="default.all.label" default="All" />
                                (${requisitionStatistics["ALL"]?:0})
                            </g:link>
                        </div>
                        <g:each var="requisitionStatus" in="${RequisitionStatus.list()}">
                            <g:if test="${requisitionStatistics[requisitionStatus]>0}">
                                <div class="mm-menu-item">
                                    <g:link controller="requisition" action="list" params="[status:requisitionStatus]">
                                        <format:metadata obj="${requisitionStatus}"/>
                                        (${requisitionStatistics[requisitionStatus]?:0 })
                                    </g:link>
                                </div>
                            </g:if>
                        </g:each>
                    </div>
                    <div class="mm-content-section">
                        <h3><warehouse:message code="default.create.label" args="[warehouse.message(code: 'requisitions.label')]" /></h3>
                        <div class="mm-menu-item">
                            <g:link controller="requisition" action="chooseTemplate" class="create" params="[type:'STOCK']">
                                <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardStock.label')]" />
                            </g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="requisition" action="create" class="create" params="[type:'NON_STOCK']">
                                <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardNonStock.label')]" />
                            </g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="requisition" action="create" class="create" params="[type:'ADHOC']">
                                <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardAdhoc.label')]" />
                            </g:link>
                        </div>
                    </div>
                </div>
            </li>
        </g:authorize>
    </g:if>

    <g:if test="${megamenuConfig.inbound.enabled}">
        <g:authorize activity="[ActivityCode.RECEIVE_STOCK]">
            <li class="mm-item">
                <a href="javascript:void(0)" class="mm-item-link">
                    <warehouse:message code="default.inbound.label" />
                </a>
                <div class="mm-item-content">
                    <g:if test="${megamenuConfig.stockMovement.enabled}">
                        <div class="mm-content-section">
                            <h3><warehouse:message code="stockMovements.label" default="Stock Movements" /></h3>
                            <div class="mm-menu-item">
                                <g:link controller="stockMovement" action="createInbound" params="[direction:'INBOUND']">
                                    <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.inbound.label')]"/>
                                </g:link>
                            </div>
                            <g:if test="${megamenuConfig.stockRequest.enabled}">
                                <div class="mm-menu-item">
                                    <g:link controller="stockMovement" action="createRequest">
                                        <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockRequest.label', default: 'Stock Request')]"/>
                                    </g:link>
                                </div>
                            </g:if>
                            <div class="mm-menu-item">
                                <g:link controller="stockMovement" action="list" params="[direction:'INBOUND']">
                                    <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.inbound.label')]"/>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${megamenuConfig.orders.enabled}">
                        <div class="mm-content-section">
                            <h3><warehouse:message code="purchaseOrders.label" default="Purchase Orders"/></h3>
                            <div class="mm-menu-item">
                                <g:link controller="purchaseOrderWorkflow" action="index" class="create">
                                    <warehouse:message code="default.create.label" args="[warehouse.message(code:'purchaseOrder.label')]"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="order" action="list" params="[orderTypeCode:OrderTypeCode.PURCHASE_ORDER]" class="list">
                                    <warehouse:message code="order.listPurchase.label" default="List Purchase Orders" />
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${megamenuConfig.putaways.enabled}">
                        <div class="mm-content-section">
                            <h3><warehouse:message code="putaways.label" default="Putaways" /></h3>
                            <div class="mm-menu-item">
                                <g:link controller="putAway" action="index">
                                    <warehouse:message code="default.create.label" args="[g.message(code:'putAway.label')]"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="order" action="list" params="[orderTypeCode: 'TRANSFER_ORDER', status: 'PENDING']">
                                    <warehouse:message code="default.list.label" args="[g.message(code:'putAways.label')]"/>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${megamenuConfig.receiving.enabled}">
                        <div class="mm-content-section">
                            <h3>
                                <warehouse:message code="receiving.label" default="Receiving" />
                                <span class="deprecated" onclick="javascript:alert('${g.message(code: "receiving.deprecation.message")}')">deprecated</span>
                            </h3>
                            <div class="mm-menu-item">
                                <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']" class="create">
                                    <warehouse:message code="shipping.createIncomingShipment.label"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="shipment" action="list" params="[type: 'incoming']" class="list">
                                    <warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="shipment" action="list" params="[type:'incoming']" class="list">
                                    <warehouse:message code="default.all.label"/> (${inboundShipmentsTotal})
                                </g:link>
                            </div>
                            <g:each in="${inboundShipmentsCount}" var="statusRow">
                                <div class="mm-menu-item">
                                    <g:link controller="shipment" action="list" params="[type: 'incoming', status:statusRow.status]" class="shipment-status-${statusRow.status }">
                                        <format:metadata obj="${statusRow.status}"/> (${statusRow.count})
                                    </g:link>
                                </div>
                            </g:each>
                        </div>
                    </g:if>
                </div>
            </li>
        </a>

        </g:authorize>
    </g:if>

    <g:if test="${megamenuConfig.outbound.enabled}">
        <g:authorize activity="[ActivityCode.SEND_STOCK]">
            <li class="mm-item">
                <a href="javascript:void(0)" class="mm-item-link">
                    <warehouse:message code="default.outbound.label" />
                </a>
                <div class="mm-item-content">
                    <g:if test="${megamenuConfig.stockMovement.enabled}">
                        <div class="mm-content-section">
                            <h3><warehouse:message code="stockMovements.label" default="Stock Movements" /></h3>
                            <div class="mm-menu-item">
                                <g:link controller="stockMovement" action="createOutbound" params="[direction:'OUTBOUND']">
                                    <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.outbound.label')]"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="stockMovement" action="list" params="[direction:'OUTBOUND']">
                                    <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.outbound.label')]"/>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${megamenuConfig.shipping.enabled}">
                        <div class="mm-content-section">
                            <h3>
                                <warehouse:message code="shipping.label" default="Shipping" />
                                <span class="deprecated" onclick="javascript:alert('${g.message(code: "shipping.deprecation.message")}')">deprecated</span>
                            </h3>
                            <div class="mm-menu-item">
                                <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']" class="create">
                                    <warehouse:message code="shipping.createOutgoingShipment.label"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list">
                                    <warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list">
                                    <warehouse:message code="default.all.label"/> (${outboundShipmentsTotal})
                                </g:link>
                            </div>
                            <g:each in="${outboundShipmentsCount}" var="statusRow">
                                <div class="mm-menu-item">
                                    <g:link controller="shipment" action="list" params="[status:statusRow.status]" class="shipment-status-${statusRow.status }">
                                        <format:metadata obj="${statusRow.status}"/> (${statusRow.count})
                                    </g:link>
                                </div>
                            </g:each>
                        </div>
                    </g:if>
                </div>
            </li>
        </g:authorize>
    </g:if>

    <g:if test="${megamenuConfig.reporting.enabled}">
        <li class="mm-item">
            <a href="javascript:void(0)" class="mm-item-link">
                <warehouse:message code="report.label" />
            </a>
            <div class="mm-item-content">

                <div class="mm-content-section">
                    <h3><warehouse:message code="report.inventoryReports.label" default="Inventory Reports" /></h3>
                    <div class="mm-menu-item">
                        <g:link controller="inventory" action="listInStock">
                            <warehouse:message code="report.inStockReport.label"/></g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="report" action="showBinLocationReport">
                            <warehouse:message code="report.binLocationReport.label" default="Bin Location Report"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="inventory" action="listExpiredStock" class="report-expired">
                            <warehouse:message code="report.expiredStockReport.label"/></g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="inventory" action="listExpiringStock" class="report-expiring">
                            <warehouse:message code="report.expiringStockReport.label"/></g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="report" action="showInventoryByLocationReport" class="">
                            <warehouse:message code="report.inventoryByLocationReport.label" default="Inventory By Location Report"/></g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="cycleCount" action="exportAsCsv">
                            <warehouse:message code="report.cycleCount.label" default="Cycle Count Report"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="inventory" action="show">
                            <warehouse:message code="report.baselineQohReport.label" default="Baseline QoH Report"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="report" action="showOrderReport">
                            <warehouse:message code="report.orderReport.label" default="Order Report"/>
                        </g:link>
                    </div>
                </div>
                <div class="mm-content-section">
                    <h3><warehouse:message code="report.transactionReports.label" default="Transaction Reports" /></h3>
                    <div class="mm-menu-item">
                        <g:link controller="report" action="showTransactionReport">
                            <warehouse:message code="report.showTransactionReport.label"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="consumption" action="show">
                            <warehouse:message code="report.consumption.label" default="Consumption Report"/>
                        </g:link>
                    </div>
                </div>
                <div class="mm-content-section">
                    <h3><warehouse:message code="dataExports.label" default="Data Exports" /></h3>
                    <div class="mm-menu-item">
                        <g:link controller="product" action="exportAsCsv">
                            <warehouse:message code="product.exportAsCsv.label"/></g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="productSupplier" action="export">
                            <warehouse:message code="default.export.label" args="[g.message(code: 'productSuppliers.label').toLowerCase()]"/></g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="inventory" action="exportLatestInventoryDate" class="list">
                            <warehouse:message code="product.exportLatestInventoryDate.label" default="Export latest inventory date"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="inventoryLevel" action="export" class="list">
                            <warehouse:message code="inventoryLevel.export.label" default="Export inventory levels"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="requisition" action="export" class="list">
                            <warehouse:message code="default.export.label" default="Export {0}" args="${['requisitions'] }"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="requisitionItem" action="listCanceled" class="">
                            <warehouse:message code="requisitionItem.listCanceled.label" default="Export requisition items"/>
                        </g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="report" action="exportBinLocation" params="[downloadFormat:'csv']" class="">
                            <warehouse:message code="report.exportBinLocations.label" default="Export bin locations"/></g:link>
                    </div>
                    <div class="mm-menu-item">
                        <g:link controller="report" action="exportDemandReport" params="[downloadFormat:'csv']" >
                            <warehouse:message code="default.export.label" args="[g.message(code:'product.demand.label', default: 'product demand')]"/></g:link>
                    </div>

                </div>
            </div>
        </li>
    </g:if>


    <g:if test="${megamenuConfig.products.enabled}">
        <g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
            <li class="mm-item">
                <a href="javascript:void(0)" class="mm-item-link">
                    <warehouse:message code="products.label" />
                </a>
                <div class="mm-item-content" style="min-width: 200px;">

                    <div class="mm-content-section">
                        <div class="mm-menu-item">
                            <g:link controller="attribute" action="list" class="list">
                                <warehouse:message code="attributes.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="productCatalog" action="list" class="list">
                                <warehouse:message code="product.catalogs.label" default="Catalogs"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="category" action="tree" class="list">
                                <warehouse:message code="categories.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="productComponent" action="list" class="list">
                                <warehouse:message code="product.components.label" default="Components"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="productGroup" action="list" class="list">
                                <warehouse:message code="productGroups.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="inventoryLevel" action="list" class="site">
                                <warehouse:message code="inventoryLevels.label" default="Inventory Levels" /></g:link>
                        </div>
                    </div>
                    <div class="mm-content-section">
                        <div class="mm-menu-item">
                            <g:link controller="product" action="list" class="list">
                                <warehouse:message code="products.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="productSupplier" action="list" class="list">
                                <warehouse:message code="productSuppliers.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="productAssociation" action="list" class="list">
                                <warehouse:message code="product.associations.label" default="Product Associations"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="tag" action="list" class="list">
                                <warehouse:message code="product.tags.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="unitOfMeasure" action="list" class="list">
                                <warehouse:message code="unitOfMeasure.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="unitOfMeasureClass" action="list" class="list">
                                <warehouse:message code="unitOfMeasureClass.label"/></g:link>
                        </div>
                        <div class="mm-menu-item">
                            <g:link controller="unitOfMeasureConversion" action="list" class="list">
                                <warehouse:message code="unitOfMeasureConversion.label" default="UoM Conversion"/></g:link>
                        </div>
                    </div>
                    <g:isUserAdmin>
                        <div class="mm-content-section">
                            <div class="mm-menu-item">
                                <g:link controller="product" action="create" class="create">
                                    <warehouse:message code="product.create.label"/></g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="product" action="batchEdit" class="create">
                                    <warehouse:message code="product.batchEdit.label"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="product" action="importAsCsv" class="import">
                                    <warehouse:message code="product.importAsCsv.label"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="product" action="exportAsCsv" class="list">
                                    <warehouse:message code="product.exportAsCsv.label"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="batch" action="importData" params="[type:'inventory']" class="inventory">
                                    <g:message code="default.import.label" args="[g.message(code:'inventory.label', default: 'Inventory')]"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="batch" action="importData" params="[type:'inventoryLevel']" class="inventory">
                                    <g:message code="default.import.label" args="[g.message(code:'inventoryLevel.label', default: 'Inventory levels')]"/>
                                </g:link>
                            </div>
                        </div>
                    </g:isUserAdmin>
                </div>
            </li>
        </g:authorize>
    </g:if>

    <g:if test="${megamenuConfig.requisitionTemplate.enabled}">
        <li class="mm-item">
            <a href="javascript:void(0)" class="mm-item-link">
                <warehouse:message code="requisitionTemplates.label" default="Stock Lists" />
            </a>
            <div class="mm-item-content">
                <div class="mm-menu-item">
                    <g:link controller="requisitionTemplate" action="list">
                        <warehouse:message code="requisitionTemplates.list.label" default="List stock lists"/>
                    </g:link>
                </div>
                <g:isUserAdmin>
                    <div class="mm-menu-item">
                        <g:link controller="requisitionTemplate" action="create">
                            <warehouse:message code="requisitionTemplates.create.label" default="Create stock list" />
                        </g:link>
                    </div>
                </g:isUserAdmin>
            </div>
        </li>
    </g:if>

    <g:if test="${megamenuConfig.configuration.enabled}">
        <g:isUserAdmin>
            <li class="mm-item">
                <a href="javascript:void(0)" class="mm-item-link">
                    <warehouse:message code="configuration.label" default="Configuration" />
                </a>
                <div class="mm-item-content" style="min-width: 200px;">
                    <div class="mm-content-base">
                        <div class="mm-content-section">
                            <h3><warehouse:message code="admin.label" default="Admin" /></h3>
                            <g:authorize activity="[ActivityCode.MANAGE_INVENTORY]">
                                <div class="mm-menu-item">
                                    <g:link controller="admin" action="showSettings" class="list">
                                        <g:message code="default.settings.label"/>
                                    </g:link>
                                </div>
                            </g:authorize>
                            <div class="mm-menu-item">
                                <g:link controller="migration" action="index" class="list">
                                    <g:message code="default.dataMigration.label" default="Data Migration" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="console" action="index" class="list">
                                    <g:message code="default.console.label" default="Console" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="admin" action="cache" class="list">
                                    <g:message code="default.cache.label" default="Cache" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="admin" action="sendMail" class="list">
                                    <g:message code="config.sendMail.label" default="Email"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="batch" action="importData">
                                    <warehouse:message code="default.import.label"
                                                       args="[warehouse.message(code:'default.data.label', default: 'Data')]"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="localization" action="list" class="list">
                                    <g:message code="localization.label" default="Localization"/>
                                </g:link>
                            </div>
                        </div>
                        <div class="mm-content-section">
                            <h3><warehouse:message code="locations.label" default="Locations" /></h3>
                            <div class="mm-menu-item">
                                <g:link controller="location" action="list" class="location">
                                    <warehouse:message code="locations.label" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="locationGroup" action="list" class="site">
                                    <warehouse:message code="locationGroups.label" default="Location Groups" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="locationType" action="list"
                                        class="locationType">
                                    <warehouse:message code="location.locationTypes.label" default="Location Types" />
                                </g:link>
                            </div>
                        </div>

                        <div class="mm-content-section">
                            <h3><warehouse:message code="transactions.label" default="Transasctions" /></h3>
                            <div class="mm-menu-item">
                                <g:link controller="transactionType">
                                    <warehouse:message code="transactionTypes.label" default="Transaction Types"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="inventory" action="listAllTransactions" class="list">
                                    <warehouse:message code="transactions.label"/>
                                </g:link>
                            </div>

                            <div class="mm-menu-item">
                                <g:link controller="inventory" action="editTransaction" class="create">
                                    <warehouse:message code="transaction.add.label"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="batch" action="importData" params="[type:'inventory']" class="inventory">
                                    <warehouse:message code="default.import.label" args="[warehouse.message(code:'inventory.label', default: 'Inventory')]"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="batch" action="importData" params="[type:'inventoryLevel']" class="inventory">
                                    <warehouse:message code="default.import.label" args="[warehouse.message(code:'inventoryLevel.label', default: 'Inventory levels')]"/>
                                </g:link>
                            </div>

                        </div>

                        <div class="mm-content-section">
                            <h3><warehouse:message code="parties.label" default="Parties" /></h3>

                            <div class="mm-menu-item">
                                <g:link controller="partyType" action="list">
                                    <warehouse:message code="partyTypes.label" default="Party Types" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="partyRole" action="list">
                                    <warehouse:message code="partyRoles.label" default="Party Roles" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="organization" action="list">
                                    <warehouse:message code="organizations.label" default="Organizations" />
                                </g:link>
                            </div>

                            <div class="mm-menu-item">
                                <g:link controller="person" action="list" class="people">
                                    <warehouse:message code="person.list.label" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="user" action="list" class="user">
                                    <warehouse:message code="users.label" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="role" action="list" class="role">
                                    <warehouse:message code="roles.label" />
                                </g:link>
                            </div>
                        </div>
                        <div class="mm-content-section">
                            <h3><warehouse:message code="other.label" default="Other" /></h3>
                            <div class="mm-menu-item">
                                <g:link controller="containerType" action="list">
                                    <warehouse:message code="containerTypes.label" default="Container Types"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="document" action="list" >
                                    <warehouse:message code="documents.label" default="Documents" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="documentType" action="list">
                                    <warehouse:message code="documentTypes.label" default="Document Types"/>
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="eventType" action="list"
                                        class="eventType">
                                    <warehouse:message code="location.eventTypes.label" default="Event Types" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="paymentMethodType" action="list">
                                    <warehouse:message code="paymentMethodTypes.label" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="paymentTerm" action="list">
                                    <warehouse:message code="paymentTerms.label" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="shipper" action="list" class="shipper">
                                    <warehouse:message code="location.shippers.label" />
                                </g:link>
                            </div>
                            <div class="mm-menu-item">
                                <g:link controller="shipmentWorkflow" action="list" >
                                    <warehouse:message code="shipmentWorkflows.label" default="Shipment Workflows" />
                                </g:link>
                            </div>
                        </div>

                    </div>
                </div>
            </li>
        </g:isUserAdmin>
    </g:if>

    <g:if test="${megamenuConfig.customLinks.enabled}">
        <li class="mm-item">
            <a href="javascript:void(0)" class="mm-item-link">
                <warehouse:message code="customLinks.label" default="Custom Links" />
            </a>
            <div class="mm-item-content">
                <g:each var="link" in="${megamenuConfig.customLinks.content}">
                    <div class="mm-menu-item">
                        <a href="${link.href}" target="${link.target}">
                            ${link.label}
                        </a>
                    </div>
                </g:each>
                <g:unless test="${megamenuConfig.customLinks.content}">
                    <div class="mm-menu-item">
                        <a href="#">There are no custom links</a>
                    </div>
                </g:unless>
            </div>
        </li>
    </g:if>
</ul>
<!--MegaMenu Ends-->
