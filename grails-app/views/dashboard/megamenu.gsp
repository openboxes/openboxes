<%@page import="org.pih.warehouse.core.ActivityCode;"%>
<%@page import="org.pih.warehouse.core.Constants;"%>
<%@page import="org.pih.warehouse.core.RoleType" %>
<%@page import="org.pih.warehouse.core.User" %>
<%@page import="org.pih.warehouse.order.OrderTypeCode"%>
<%@page import="org.pih.warehouse.requisition.RequisitionStatus"%>
<%@page import="org.pih.warehouse.shipping.Shipment"%>
<ul class="d-flex align-items-center navbar-nav mr-auto flex-wrap align-items-stretch">
    <g:hideIfIsNonInventoryManagedAndCanSubmitRequest>
        <g:if test="${megamenuConfig.dashboard.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
                <li class="nav-item dropdown d-flex align-items-center align-items-lg-center" id="dashboard">
                    <g:link controller="dashboard" action="index" class="nav-link dropdown-toggle">
                        <warehouse:message code="dashboard.label" />&nbsp;
                    </g:link>
                </li>
            </g:isUserInRole>
        </g:if>

        <g:if test="${megamenuConfig.analytics.enabled}">
            <g:isUserAdmin>
                <li class="nav-item dropdown d-flex align-items-center" id="analytics">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="analytics.label" default="Analytics"/>
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <div class="padding-8">
                                <g:link controller="inventoryBrowser" action="index" class="dropdown-item">
                                    <warehouse:message code="inventory.browse.label" default="Inventory browser" />
                                </g:link>

                                <g:link controller="inventorySnapshot" action="list" class="dropdown-item">
                                    <warehouse:message code="inventory.snapshot.label" default="Inventory Snapshots" />
                                </g:link>

                                <g:link controller="consumption" action="list" class="dropdown-item">
                                    <warehouse:message code="consumption.report.label" default="Consumption Report" />
                                </g:link>
                            </div>
                        </div>
                    </div>
                </li>
            </g:isUserAdmin>
        </g:if>

        <g:if test="${megamenuConfig.inventory.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
            <g:supports activityCode="${ActivityCode.MANAGE_INVENTORY}">
                <li class="nav-item dropdown justify-content-center align-items-center d-none d-lg-flex" id="inventory">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="inventory.label" />
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="inventory.browse.label" /></span>
                                <g:link controller="inventory" action="browse" params="[resetSearch:true]" class="dropdown-item">
                                    <warehouse:message code="inventory.browse.label"/>
                                </g:link>
                            </div>
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="inventory.manage.label"/></span>
                                <g:supports activityCode="${ActivityCode.ADJUST_INVENTORY}">
                                        <g:link controller="inventory" action="manage" class="dropdown-item">
                                            <warehouse:message code="inventory.manage.label" />
                                        </g:link>
                                </g:supports>


                                <g:link controller="batch" action="importData" params="[type:'inventory']" class="dropdown-item inventory">
                                    <g:message code="default.import.label" args="[g.message(code:'inventory.label', default: 'Inventory')]"/>
                                </g:link>

                                <g:supports activitiesAll="[ActivityCode.PICK_STOCK,ActivityCode.PUTAWAY_STOCK]">
                                    <g:link controller="stockTransfer" action="create" class="create dropdown-item">
                                        <warehouse:message code="inventory.createStockTransfer.label" />
                                    </g:link>
                                    <g:link controller="stockTransfer" action="list" class="list dropdown-item">
                                        <warehouse:message code="inventory.listStockTransfers.label" />
                                    </g:link>

                                    <g:link controller="replenishment" action="index" class="create dropdown-item">
                                        <warehouse:message code="inventory.createReplenishment.label" />
                                    </g:link>

                                </g:supports>
                            </div>
                        </div>
                    </div>
                </li>
                <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none">
                    <button
                        data-target="#collapse-inventory"
                        class="nav-link d-flex justify-content-between align-items-center w-100"
                        data-toggle="collapse"
                        aria-expanded="false"
                        aria-controls="collapse-inventory"
                    >
                        <warehouse:message code="inventory.label" />
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse w-100" id="collapse-inventory">
                        <div class="d-flex flex-wrap">
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="inventory.browse.label" /></span>
                                <g:link controller="inventory" action="browse" params="[resetSearch:true]" class="dropdown-item">
                                    <warehouse:message code="inventory.browse.label"/>
                                </g:link>
                            </div>
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="inventory.manage.label"/></span>
                                <g:supports activityCode="${ActivityCode.ADJUST_INVENTORY}">
                                    <g:link controller="inventory" action="manage" class="dropdown-item">
                                        <warehouse:message code="inventory.manage.label" />
                                    </g:link>
                                </g:supports>


                                <g:link controller="batch" action="importData" params="[type:'inventory']" class="dropdown-item inventory">
                                    <g:message code="default.import.label" args="[g.message(code:'inventory.label', default: 'Inventory')]"/>
                                </g:link>

                                <g:supports activitiesAll="[ActivityCode.PICK_STOCK,ActivityCode.PUTAWAY_STOCK]">
                                    <g:link controller="stockTransfer" action="create" class="create dropdown-item">
                                        <warehouse:message code="inventory.createStockTransfer.label" />
                                    </g:link>
                                    <g:link controller="stockTransfer" action="list" class="list dropdown-item">
                                        <warehouse:message code="inventory.listStockTransfers.label" />
                                    </g:link>

                                    <g:link controller="replenishment" action="index" class="create dropdown-item">
                                        <warehouse:message code="inventory.createReplenishment.label" />
                                    </g:link>

                                </g:supports>
                            </div>
                        </div>
                    </div>
                </li>
            </g:supports>
            </g:isUserInRole>
        </g:if>

        <g:if test="${megamenuConfig.requisitions.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
            <g:supports activitiesAny="[ActivityCode.PLACE_REQUEST,ActivityCode.FULFILL_REQUEST]">
                <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="requisitions">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="requests.label"/>
                        <span class="deprecated"
                              onclick="javascript:alert('${g.message(code: "requisition.deprecation.message")}')">
                            <g:message code="default.deprecated.label"/>
                        </span>
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="default.list.label" args="[warehouse.message(code: 'requisitions.label')]" /></span>
                                <g:link controller="requisition" action="list" class="dropdown-item">
                                    <warehouse:message code="default.all.label" default="All" />
                                    (${requisitionStatistics?.ALL ?: 0})
                                </g:link>
                                <g:each var="requisitionStatus" in="${RequisitionStatus.list()}">
                                    <g:if test="${requisitionStatistics?.requisitionStatus?.name()>0}">
                                            <g:link controller="requisition" action="list" params="[status:requisitionStatus]" class="dropdown-item">
                                                <format:metadata obj="${requisitionStatus}"/>
                                                (${requisitionStatistics?.requisitionStatus?.name()?:0 })
                                            </g:link>
                                    </g:if>
                                </g:each>
                            </div>

                            <div class="padding-8">
                                <span class="subsection-title"><warehouse:message code="default.create.label" args="[warehouse.message(code: 'requisitions.label')]" /></span>
                                <g:link controller="requisition" action="chooseTemplate" class="create dropdown-item" params="[type:'STOCK']">
                                    <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardStock.label')]" />
                                </g:link>
                                <g:link controller="requisition" action="create" class="create dropdown-item" params="[type:'NON_STOCK']">
                                    <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardNonStock.label')]" />
                                </g:link>
                                <g:link controller="requisition" action="create" class="create dropdown-item" params="[type:'ADHOC']">
                                    <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardAdhoc.label')]" />
                                </g:link>
                            </div>
                        </div>
                    </div>
                </li>
                <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none">
                    <button
                        data-target="#collapse-requests"
                        class="nav-link d-flex justify-content-between align-items-center w-100"
                        data-toggle="collapse"
                        aria-expanded="false"
                        aria-controls="collapse-requests"
                    >
                        <warehouse:message code="requests.label"/>
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse" id="collapse-requests">
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="default.list.label" args="[warehouse.message(code: 'requisitions.label')]" /></span>
                            <g:link controller="requisition" action="list" class="dropdown-item">
                                <warehouse:message code="default.all.label" default="All" />
                                (${requisitionStatistics?.ALL ?: 0})
                            </g:link>
                            <g:each var="requisitionStatus" in="${RequisitionStatus.list()}">
                                <g:if test="${requisitionStatistics?.requisitionStatus?.name()>0}">
                                        <g:link controller="requisition" action="list" params="[status:requisitionStatus]" class="dropdown-item">
                                            <format:metadata obj="${requisitionStatus}"/>
                                            (${requisitionStatistics?.requisitionStatus?.name()?:0 })
                                        </g:link>
                                </g:if>
                            </g:each>
                        </div>

                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="default.create.label" args="[warehouse.message(code: 'requisitions.label')]" /></span>
                            <g:link controller="requisition" action="chooseTemplate" class="create dropdown-item" params="[type:'STOCK']">
                                <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardStock.label')]" />
                            </g:link>
                            <g:link controller="requisition" action="create" class="create dropdown-item" params="[type:'NON_STOCK']">
                                <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardNonStock.label')]" />
                            </g:link>
                            <g:link controller="requisition" action="create" class="create dropdown-item" params="[type:'ADHOC']">
                                <warehouse:message code="requisition.create.label" args="[warehouse.message(code:'requisitionType.wardAdhoc.label')]" />
                            </g:link>
                        </div>
                    </div>
                </li>
            </g:supports>
            </g:isUserInRole>
        </g:if>

        <g:if test="${megamenuConfig.purchasing.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
            <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="purchasing">
                <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                    <warehouse:message code="order.purchasing.label" />
                </a>
                <div class="dropdown-menu dropdown-menu-wrapper">
                    <div class="dropdown-menu-subsections dropdown-menu-content">
                        <div class="padding-8">
                            <g:supports activityCode="${ActivityCode.PLACE_ORDER}">
                                    <g:link controller="purchaseOrder" action="index" class="create dropdown-item">
                                        <warehouse:message code="default.create.label" args="[warehouse.message(code:'purchaseOrder.label')]"/>
                                    </g:link>
                            </g:supports>

                            <g:link controller="purchaseOrder" action="list" class="list dropdown-item">
                                <warehouse:message code="order.listPurchase.label" default="List Purchase Orders" />
                            </g:link>


                            <g:link controller="supplier" action="list" class="list dropdown-item">
                                <warehouse:message code="location.listSuppliers.label" default="List Suppliers"/>
                            </g:link>


                            <g:link controller="stockMovement" action="createCombinedShipments" params="[direction:'INBOUND']" class="dropdown-item">
                                <warehouse:message code="shipment.shipfromPO.label"/>
                            </g:link>


                            <a href="${createLink(uri: '/dashboard/supplier', absolute:'true')}" class="dropdown-item">
                                <warehouse:message code="dashboard.supplierDashboard.label" default="Supplier Dashboard" />
                            </a>
                        </div>
                    </div>

                </div>
            </li>
            <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none">
                <button
                    data-target="#collapse-purchasing"
                    class="nav-link d-flex justify-content-between align-items-center w-100"
                    data-toggle="collapse"
                    aria-expanded="false"
                    aria-controls="collapse-purchasing"
                >
                    <warehouse:message code="order.purchasing.label" />
                    <i class="ri-arrow-drop-down-line"></i>
                </button>
                <div class="collapse w-100" id="collapse-purchasing">
                    <div class="d-flex flex-wrap">
                        <div class="padding-8">
                            <g:supports activityCode="${ActivityCode.PLACE_ORDER}">
                                    <g:link controller="purchaseOrder" action="index" class="create dropdown-item">
                                        <warehouse:message code="default.create.label" args="[warehouse.message(code:'purchaseOrder.label')]"/>
                                    </g:link>
                            </g:supports>

                            <g:link controller="purchaseOrder" action="list" class="list dropdown-item">
                                <warehouse:message code="order.listPurchase.label" default="List Purchase Orders" />
                            </g:link>


                            <g:link controller="supplier" action="list" class="list dropdown-item">
                                <warehouse:message code="location.listSuppliers.label" default="List Suppliers"/>
                            </g:link>


                            <g:link controller="stockMovement" action="createCombinedShipments" params="[direction:'INBOUND']" class="dropdown-item">
                                <warehouse:message code="shipment.shipfromPO.label"/>
                            </g:link>


                            <a href="${createLink(uri: '/dashboard/supplier', absolute:'true')}" class="dropdown-item">
                                <warehouse:message code="dashboard.supplierDashboard.label" default="Supplier Dashboard" />
                            </a>
                        </div>
                    </div>
                </div>
            </li>
            </g:isUserInRole>
        </g:if>

        <g:if test="${megamenuConfig.invoicing.enabled}">
            <g:hasRoleInvoice>
                <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="invoicing">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="react.invoicing.label" />
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <div class="padding-8">
                                <g:link controller="invoice" action="index" class="create dropdown-item">
                                    <warehouse:message code="default.create.label" args="[warehouse.message(code:'react.invoice.label')]"/>
                                </g:link>
                                <g:link controller="invoice" action="list" class="list dropdown-item">
                                    <warehouse:message code="react.invoice.list.label" default="List Invoices" />
                                </g:link>
                            </div>
                        </div>
                    </div>
                </li>
                <li class="nav-item collapsable flex-column align-items-center d-flex d-lg-none" id="invoicing">
                    <button
                        data-target="#collapse-invoicing"
                        class="nav-link d-flex justify-content-between align-items-center w-100"
                        data-toggle="collapse"
                        aria-expanded="false"
                        aria-controls="collapse-invoicing"
                    >
                        <warehouse:message code="react.invoicing.label" />
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse w-100" id="collapse-invoicing">
                        <div class="d-flex flex-wrap">
                            <div class="padding-8">
                                <g:link controller="invoice" action="index" class="create dropdown-item">
                                    <warehouse:message code="default.create.label" args="[warehouse.message(code:'react.invoice.label')]"/>
                                </g:link>
                                <g:link controller="invoice" action="list" class="list dropdown-item">
                                    <warehouse:message code="react.invoice.list.label" default="List Invoices" />
                                </g:link>
                            </div>
                        </div>
                    </div>
                </li>
            </g:hasRoleInvoice>
        </g:if>

        <g:if test="${megamenuConfig.stockRequest.enabled}">
            <g:hasHighestRoleAuthenticated>
                <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="inbound">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="default.inbound.label" />
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-content padding-8">
                            <g:link controller="stockMovement" action="createRequest" class="dropdown-item">
                                <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockRequest.label', default: 'Stock Request')]"/>
                            </g:link>
                            <g:link controller="stockMovement" action="list" params="[direction:'INBOUND']" class="dropdown-item">
                                <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.inbound.label')]"/>
                            </g:link>
                        </div>
                    </div>
                </li>
                <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none">
                    <button
                        data-target="#collapse-stockRequest"
                        class="nav-link d-flex justify-content-between align-items-center w-100"
                        data-toggle="collapse"
                        aria-expanded="false"
                        aria-controls="collapse-stockRequest"
                    >
                        <warehouse:message code="inbound.label" />
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse w-100" id="collapse-stockRequest">
                        <div class="d-flex flex-wrap">
                            <g:link controller="stockMovement" action="createRequest" class="dropdown-item">
                                <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockRequest.label', default: 'Stock Request')]"/>
                            </g:link>
                            <g:link controller="stockMovement" action="list" params="[direction:'INBOUND']" class="dropdown-item">
                                <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.inbound.label')]"/>
                            </g:link>
                        </div>
                    </div>
                </li>
            </g:hasHighestRoleAuthenticated>
        </g:if>

        <g:if test="${megamenuConfig.inbound.enabled}">
            <g:hasHigherRoleThanAuthenticated>
            <g:supports activityCode="${ActivityCode.RECEIVE_STOCK}">
                <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="inbound">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="default.inbound.label" />
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <g:if test="${megamenuConfig.stockMovement.enabled}">

                                    <div class="padding-8">
                                        <span class="subsection-title"><warehouse:message code="stockMovements.label" default="Stock Movements" /></span>
                                        <g:link controller="stockMovement" action="createInbound" params="[direction:'INBOUND']" class="dropdown-item">
                                            <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.inbound.label')]"/>
                                        </g:link>
                                        <g:if test="${megamenuConfig.stockRequest.enabled}">
                                            <g:link controller="stockMovement" action="createRequest" class="dropdown-item">
                                                <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockRequest.label', default: 'Stock Request')]"/>
                                            </g:link>
                                        </g:if>
                                        <g:link controller="stockMovement" action="list" params="[direction:'INBOUND']" class="dropdown-item">
                                            <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.inbound.label')]"/>
                                        </g:link>
                                        <g:link controller="stockTransfer" action="createInboundReturn" class="dropdown-item">
                                            <warehouse:message code="inboundReturns.create.label" />
                                        </g:link>
                                    </div>

                            </g:if>
                            <g:supports activitiesAll="[ActivityCode.PICK_STOCK,ActivityCode.PUTAWAY_STOCK]">
                                <g:if test="${megamenuConfig.putaways.enabled}">
                                    <div class="padding-8">
                                        <span class="subsection-title"><warehouse:message code="putaways.label" default="Putaways" /></span>
                                        <g:link controller="putAway" action="index" class="dropdown-item">
                                            <warehouse:message code="default.create.label" args="[g.message(code:'putAway.label')]"/>
                                        </g:link>
                                        <g:link controller="order" action="list" params="[orderType: Constants.PUTAWAY_ORDER, status: 'PENDING']" class="dropdown-item">
                                            <warehouse:message code="default.list.label" args="[g.message(code:'putAways.label')]"/>
                                        </g:link>
                                    </div>
                                </g:if>
                            </g:supports>
                            <g:if test="${megamenuConfig.receiving.enabled}">
                                <div class="padding-8">
                                    <span class="subsection-title"><warehouse:message code="receiving.label" default="Receiving" /><span class="deprecated" onclick="javascript:alert('${g.message(code: "receiving.deprecation.message")}')">deprecated</span></span>
                                    <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']" class="create dropdown-item">
                                        <warehouse:message code="shipping.createIncomingShipment.label"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type: 'incoming']" class="list dropdown-item">
                                        <warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type:'incoming']" class="list dropdown-item">
                                        <warehouse:message code="default.all.label"/> (${inboundShipmentsTotal})
                                    </g:link>
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
                    </div>
                </li>
                <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none" >
                    <button
                        data-target="#collapse-inbound"
                        class="nav-link d-flex justify-content-between align-items-center w-100"
                        data-toggle="collapse"
                        aria-expanded="false"
                        aria-controls="collapse-inbound"
                    >
                        <warehouse:message code="default.inbound.label" />
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse w-100" id="collapse-inbound">
                        <div class="d-flex flex-wrap">
                            <g:if test="${megamenuConfig.stockMovement.enabled}">

                                    <div class="padding-8">
                                        <span class="subsection-title"><warehouse:message code="stockMovements.label" default="Stock Movements" /></span>
                                        <g:link controller="stockMovement" action="createInbound" params="[direction:'INBOUND']" class="dropdown-item">
                                            <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.inbound.label')]"/>
                                        </g:link>
                                        <g:if test="${megamenuConfig.stockRequest.enabled}">
                                            <g:link controller="stockMovement" action="createRequest" class="dropdown-item">
                                                <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockRequest.label', default: 'Stock Request')]"/>
                                            </g:link>
                                        </g:if>
                                        <g:link controller="stockMovement" action="list" params="[direction:'INBOUND']" class="dropdown-item">
                                            <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.inbound.label')]"/>
                                        </g:link>
                                        <g:link controller="stockTransfer" action="createInboundReturn" class="dropdown-item">
                                            <warehouse:message code="inboundReturns.create.label" />
                                        </g:link>
                                    </div>

                            </g:if>
                            <g:supports activitiesAll="[ActivityCode.PICK_STOCK,ActivityCode.PUTAWAY_STOCK]">
                                <g:if test="${megamenuConfig.putaways.enabled}">
                                    <div class="padding-8">
                                        <span class="subsection-title"><warehouse:message code="putaways.label" default="Putaways" /></span>
                                        <g:link controller="putAway" action="index" class="dropdown-item">
                                            <warehouse:message code="default.create.label" args="[g.message(code:'putAway.label')]"/>
                                        </g:link>
                                        <g:link controller="order" action="list" params="[orderType: Constants.PUTAWAY_ORDER, status: 'PENDING']" class="dropdown-item">
                                            <warehouse:message code="default.list.label" args="[g.message(code:'putAways.label')]"/>
                                        </g:link>
                                    </div>
                                </g:if>
                            </g:supports>
                            <g:if test="${megamenuConfig.receiving.enabled}">
                                <div class="padding-8">
                                    <span class="subsection-title"><warehouse:message code="receiving.label" default="Receiving" /><span class="deprecated" onclick="javascript:alert('${g.message(code: "receiving.deprecation.message")}')">deprecated</span></span>
                                    <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'INCOMING']" class="create dropdown-item">
                                        <warehouse:message code="shipping.createIncomingShipment.label"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type: 'incoming']" class="list dropdown-item">
                                        <warehouse:message code="shipping.listIncoming.label"  default="List incoming shipments"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type:'incoming']" class="list dropdown-item">
                                        <warehouse:message code="default.all.label"/> (${inboundShipmentsTotal})
                                    </g:link>
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
                    </div>
                </li>

            </g:supports>
            </g:hasHigherRoleThanAuthenticated>
        </g:if>

        <g:if test="${megamenuConfig.outbound.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
            <g:supports activityCode="${ActivityCode.SEND_STOCK}">
                <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="outbound">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="default.outbound.label" />
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <div class="padding-8">
                                <g:if test="${megamenuConfig.stockMovement.enabled}">
                                    <g:link controller="stockMovement" action="createOutbound" params="[direction:'OUTBOUND']" class="dropdown-item">
                                        <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.outbound.label')]"/>
                                    </g:link>
                                    <g:link controller="stockMovement" action="list" params="[direction:'OUTBOUND']" class="dropdown-item">
                                        <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.outbound.label')]"/>
                                    </g:link>
                                    <g:link controller="stockMovement" action="list" params="[direction:'OUTBOUND', sourceType: 'ELECTRONIC']" class="dropdown-item">
                                        <warehouse:message code="requests.list.label" />
                                    </g:link>
                                    <g:link controller="stockTransfer" action="createOutboundReturn" class="dropdown-item">
                                        <warehouse:message code="outboundReturns.create.label" />
                                    </g:link>
                                </g:if>
                            </div>

                            <g:if test="${megamenuConfig.shipping.enabled}">
                                <div class="padding-8">
                                    <span class="subsection-title"><warehouse:message code="shipping.label" default="Shipping" /><span class="deprecated" onclick="javascript:alert('${g.message(code: "shipping.deprecation.message")}')">deprecated</span></span>
                                    <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']" class="create dropdown-item">
                                        <warehouse:message code="shipping.createOutgoingShipment.label"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list dropdown-item">
                                        <warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list dropdown-item">
                                        <warehouse:message code="default.all.label"/> (${outboundShipmentsTotal})
                                    </g:link>
                                    <g:each in="${outboundShipmentsCount}" var="statusRow">
                                        <div class="mm-menu-item">
                                            <g:link controller="shipment" action="list" params="[status:statusRow.status]" class="shipment-status-${statusRow.status } dropdown-item">
                                                <format:metadata obj="${statusRow.status}"/> (${statusRow.count})
                                            </g:link>
                                        </div>
                                    </g:each>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </li>
                <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none" >
                    <button
                        data-target="#collapse-outbound"
                        class="nav-link d-flex justify-content-between align-items-center w-100"
                        data-toggle="collapse"
                        aria-expanded="false"
                        aria-controls="collapse-outbound"
                    >
                        <warehouse:message code="default.outbound.label" />
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse w-100" id="collapse-outbound">
                        <div class="d-flex flex-wrap">
                            <div class="padding-8">
                                <g:if test="${megamenuConfig.stockMovement.enabled}">
                                    <g:link controller="stockMovement" action="createOutbound" params="[direction:'OUTBOUND']" class="dropdown-item">
                                        <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.outbound.label')]"/>
                                    </g:link>
                                    <g:link controller="stockMovement" action="list" params="[direction:'OUTBOUND']" class="dropdown-item">
                                        <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovements.outbound.label')]"/>
                                    </g:link>
                                    <g:link controller="stockMovement" action="list" params="[direction:'OUTBOUND', sourceType: 'ELECTRONIC']" class="dropdown-item">
                                        <warehouse:message code="requests.list.label" />
                                    </g:link>
                                    <g:link controller="stockTransfer" action="createOutboundReturn" class="dropdown-item">
                                        <warehouse:message code="outboundReturns.create.label" />
                                    </g:link>
                                </g:if>
                            </div>

                            <g:if test="${megamenuConfig.shipping.enabled}">
                                <div class="padding-8">
                                    <span class="subsection-title"><warehouse:message code="shipping.label" default="Shipping" /><span class="deprecated" onclick="javascript:alert('${g.message(code: "shipping.deprecation.message")}')">deprecated</span></span>
                                    <g:link controller="createShipmentWorkflow" action="createShipment" params="[type:'OUTGOING']" class="create dropdown-item">
                                        <warehouse:message code="shipping.createOutgoingShipment.label"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list dropdown-item">
                                        <warehouse:message code="shipping.listOutgoing.label"  default="List outgoing shipments"/>
                                    </g:link>
                                    <g:link controller="shipment" action="list" params="[type:'outgoing']" class="list dropdown-item">
                                        <warehouse:message code="default.all.label"/> (${outboundShipmentsTotal})
                                    </g:link>
                                    <g:each in="${outboundShipmentsCount}" var="statusRow">
                                        <div class="mm-menu-item">
                                            <g:link controller="shipment" action="list" params="[status:statusRow.status]" class="shipment-status-${statusRow.status } dropdown-item">
                                                <format:metadata obj="${statusRow.status}"/> (${statusRow.count})
                                            </g:link>
                                        </div>
                                    </g:each>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </li>
            </g:supports>
            </g:isUserInRole>
        </g:if>

        <g:if test="${megamenuConfig.reporting.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
            <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="reporting">
                <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                    <warehouse:message code="report.label" />
                </a>
                <div class="dropdown-menu dropdown-menu-wrapper dropdown-menu-right">
                    <div class="dropdown-menu-subsections dropdown-menu-content">
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="report.inventoryReports.label" default="Inventory Reports" /></span>
                            <g:link controller="inventory" action="listInStock" class="dropdown-item">
                                <warehouse:message code="report.inStockReport.label"/>
                            </g:link>
                            <g:link controller="report" action="showBinLocationReport" class="dropdown-item">
                                <warehouse:message code="report.binLocationReport.label" default="Bin Location Report"/>
                            </g:link>
                            <g:link controller="inventory" action="listExpiredStock" class="report-expired dropdown-item">
                                <warehouse:message code="report.expiredStockReport.label"/>
                            </g:link>
                            <g:link controller="inventory" action="listExpiringStock" class="report-expiring dropdown-item">
                                <warehouse:message code="report.expiringStockReport.label"/>
                            </g:link>
                            <g:link controller="report" action="showInventoryByLocationReport" class="dropdown-item">
                                <warehouse:message code="report.inventoryByLocationReport.label" default="Inventory By Location Report"/>
                            </g:link>
                            <g:link controller="report" action="showCycleCountReport" class="dropdown-item">
                                <warehouse:message code="report.cycleCount.label" default="Cycle Count Report"/>
                            </g:link>
                            <g:link controller="inventory" action="show" class="dropdown-item">
                                <warehouse:message code="report.baselineQohReport.label" default="Baseline QoH Report"/>
                            </g:link>
                            <g:link controller="report" action="showOnOrderReport" class="dropdown-item">
                                <warehouse:message code="report.onOrderReport.label" default="On Order Report"/>
                            </g:link>
                        </div>

                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="report.orderReports.label" default="Order Reports" /></span>
                                <g:link controller="report" action="showForecastReport" class="dropdown-item">
                                    <warehouse:message code="report.forecastReport.label" default="Forecast Report"/>
                                </g:link>
                                <g:link controller="inventory" action="reorderReport" class="dropdown-item">
                                    <warehouse:message code="report.reorderReport.label" default="Reorder Report"/>
                                </g:link>
                            <g:hasRoleFinance>
                                <g:link controller="report" action="amountOutstandingOnOrdersReport" class="dropdown-item">
                                    <warehouse:message code="report.amountOutstandingReport.label" default="Amount Outstandng Report"/>
                                </g:link>
                            </g:hasRoleFinance>
                        </div>

                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="report.transactionReports.label" default="Transaction Reports" /></span>
                            <g:link controller="report" action="showTransactionReport" class="dropdown-item">
                                <warehouse:message code="report.showTransactionReport.label"/>
                            </g:link>
                            <g:link controller="consumption" action="show" class="dropdown-item">
                                <warehouse:message code="report.consumption.label" default="Consumption Report"/>
                            </g:link>
                            <g:link controller="report" action="showRequestDetailReport" class="dropdown-item">
                                <warehouse:message code="report.requestDetailReport.label" default="Request Detail Report"/>
                            </g:link>
                        </div>
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="dataExports.label" default="Data Exports" /></span>
                            <g:link controller="product" action="exportAsCsv" class="dropdown-item">
                                <warehouse:message code="product.exportAsCsv.label"/>
                            </g:link>
                            <g:link controller="productSupplier" action="export" params="[format: 'xls']" class="dropdown-item">
                                <warehouse:message code="default.export.label" args="[g.message(code: 'productSuppliers.label').toLowerCase()]"/>
                            </g:link>
                            <g:link controller="inventory" action="exportLatestInventoryDate" class="list dropdown-item">
                                <warehouse:message code="product.exportLatestInventoryDate.label" default="Export latest inventory date"/>
                            </g:link>
                            <g:link controller="inventoryLevel" action="export" class="list dropdown-item">
                                <warehouse:message code="inventoryLevel.export.label" default="Export inventory levels"/>
                            </g:link>
                            <g:link controller="requisition" action="export" class="list dropdown-item">
                                <warehouse:message code="default.export.label" default="Export {0}" args="${['requisitions'] }"/>
                            </g:link>
                            <g:link controller="report" action="exportBinLocation" params="[downloadFormat:'csv']" class="dropdown-item">
                                <warehouse:message code="report.exportBinLocations.label" default="Export bin locations"/>
                            </g:link>
                            <g:link controller="report" action="exportDemandReport" params="[downloadFormat:'csv']" class="dropdown-item">
                                <warehouse:message code="default.export.label" args="[g.message(code:'product.demand.label', default: 'product demand')]"/>
                            </g:link>
                            <g:link controller="dataExport" action="index" class="dropdown-item">
                                <warehouse:message code="export.custom.label" default="Custom data exports"/>
                            </g:link>
                        </div>
                    </div>
                </div>
            </li>
            <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none">
                <button
                    data-target="#collapse-report"
                    class="nav-link d-flex justify-content-between align-items-center w-100"
                    data-toggle="collapse"
                    aria-expanded="false"
                    aria-controls="collapse-report"
                >
                    <warehouse:message code="report.label" />
                    <i class="ri-arrow-drop-down-line"></i>
                </button>
                <div class="collapse w-100" id="collapse-report">
                    <div class="d-flex flex-wrap">
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="report.inventoryReports.label" default="Inventory Reports" /></span>
                            <g:link controller="inventory" action="listInStock" class="dropdown-item">
                                <warehouse:message code="report.inStockReport.label"/>
                            </g:link>
                            <g:link controller="report" action="showBinLocationReport" class="dropdown-item">
                                <warehouse:message code="report.binLocationReport.label" default="Bin Location Report"/>
                            </g:link>
                            <g:link controller="inventory" action="listExpiredStock" class="report-expired dropdown-item">
                                <warehouse:message code="report.expiredStockReport.label"/>
                            </g:link>
                            <g:link controller="inventory" action="listExpiringStock" class="report-expiring dropdown-item">
                                <warehouse:message code="report.expiringStockReport.label"/>
                            </g:link>
                            <g:link controller="report" action="showInventoryByLocationReport" class="dropdown-item">
                                <warehouse:message code="report.inventoryByLocationReport.label" default="Inventory By Location Report"/>
                            </g:link>
                            <g:link controller="report" action="showCycleCountReport" class="dropdown-item">
                                <warehouse:message code="report.cycleCount.label" default="Cycle Count Report"/>
                            </g:link>
                            <g:link controller="inventory" action="show" class="dropdown-item">
                                <warehouse:message code="report.baselineQohReport.label" default="Baseline QoH Report"/>
                            </g:link>
                            <g:link controller="report" action="showOnOrderReport" class="dropdown-item">
                                <warehouse:message code="report.onOrderReport.label" default="On Order Report"/>
                            </g:link>
                        </div>

                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="report.orderReports.label" default="Order Reports" /></span>
                                <g:link controller="report" action="showForecastReport" class="dropdown-item">
                                    <warehouse:message code="report.forecastReport.label" default="Forecast Report"/>
                                </g:link>
                                <g:link controller="inventory" action="reorderReport" class="dropdown-item">
                                    <warehouse:message code="report.reorderReport.label" default="Reorder Report"/>
                                </g:link>
                            <g:hasRoleFinance>
                                <g:link controller="report" action="amountOutstandingOnOrdersReport" class="dropdown-item">
                                    <warehouse:message code="report.amountOutstandingReport.label" default="Amount Outstandng Report"/>
                                </g:link>
                            </g:hasRoleFinance>
                        </div>

                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="report.transactionReports.label" default="Transaction Reports" /></span>
                            <g:link controller="report" action="showTransactionReport" class="dropdown-item">
                                <warehouse:message code="report.showTransactionReport.label"/>
                            </g:link>
                            <g:link controller="consumption" action="show" class="dropdown-item">
                                <warehouse:message code="report.consumption.label" default="Consumption Report"/>
                            </g:link>
                            <g:link controller="report" action="showRequestDetailReport" class="dropdown-item">
                                <warehouse:message code="report.requestDetailReport.label" default="Request Detail Report"/>
                            </g:link>
                        </div>
                        <div class="padding-8">
                            <span class="subsection-title"><warehouse:message code="dataExports.label" default="Data Exports" /></span>
                            <g:link controller="product" action="exportAsCsv" class="dropdown-item">
                                <warehouse:message code="product.exportAsCsv.label"/>
                            </g:link>
                            <g:link controller="productSupplier" action="export" params="[format: 'xls']" class="dropdown-item">
                                <warehouse:message code="default.export.label" args="[g.message(code: 'productSuppliers.label').toLowerCase()]"/>
                            </g:link>
                            <g:link controller="inventory" action="exportLatestInventoryDate" class="list dropdown-item">
                                <warehouse:message code="product.exportLatestInventoryDate.label" default="Export latest inventory date"/>
                            </g:link>
                            <g:link controller="inventoryLevel" action="export" class="list dropdown-item">
                                <warehouse:message code="inventoryLevel.export.label" default="Export inventory levels"/>
                            </g:link>
                            <g:link controller="requisition" action="export" class="list dropdown-item">
                                <warehouse:message code="default.export.label" default="Export {0}" args="${['requisitions'] }"/>
                            </g:link>
                            <g:link controller="report" action="exportBinLocation" params="[downloadFormat:'csv']" class="dropdown-item">
                                <warehouse:message code="report.exportBinLocations.label" default="Export bin locations"/>
                            </g:link>
                            <g:link controller="report" action="exportDemandReport" params="[downloadFormat:'csv']" class="dropdown-item">
                                <warehouse:message code="default.export.label" args="[g.message(code:'product.demand.label', default: 'product demand')]"/>
                            </g:link>
                            <g:link controller="dataExport" action="index" class="dropdown-item">
                                <warehouse:message code="export.custom.label" default="Custom data exports"/>
                            </g:link>
                        </div>
                    </div>
                </div>
            </li>
            </g:isUserInRole>
        </g:if>


        <g:if test="${megamenuConfig.products.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
            <g:supports activityCode="${ActivityCode.MANAGE_INVENTORY}">
                <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="products">
                    <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                        <warehouse:message code="products.label" />
                    </a>
                    <div class="dropdown-menu dropdown-menu-wrapper">
                        <div class="dropdown-menu-subsections dropdown-menu-content">
                            <div class="padding-8">
                                <g:isUserAdmin>
                                    <g:link controller="product" action="create" class="create dropdown-item">
                                        <warehouse:message code="product.create.label"/>
                                    </g:link>
                                </g:isUserAdmin>
                                <g:link controller="product" action="list" class="list dropdown-item">
                                    <warehouse:message code="products.list.label"/>
                                </g:link>
                                <g:isUserAdmin>
                                    <g:link controller="product" action="batchEdit" class="create dropdown-item">
                                        <warehouse:message code="product.batchEdit.label"/>
                                    </g:link>
                                    <g:link controller="product" action="importAsCsv" class="import dropdown-item">
                                        <warehouse:message code="product.importAsCsv.label"/>
                                    </g:link>
                                    <g:link controller="product" action="exportAsCsv" class="list dropdown-item">
                                        <warehouse:message code="product.exportAsCsv.label"/>
                                    </g:link>
                                </g:isUserAdmin>
                                <g:isSuperuser>
                                    <g:link controller="productType" action="list" class="site dropdown-item">
                                        <warehouse:message code="productType.label" default="ProductType" />
                                    </g:link>
                                </g:isSuperuser>
                            </div>
                            <div class="padding-8">
                                <g:link controller="category" action="tree" class="list dropdown-item">
                                    <warehouse:message code="categories.label"/>
                                </g:link>
                                <g:link controller="productCatalog" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.catalogs.label" default="Catalogs"/>
                                </g:link>
                                <g:link controller="tag" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.tags.label"/>
                                </g:link>
                                <g:link controller="attribute" action="list" class="list dropdown-item">
                                    <warehouse:message code="attributes.label"/>
                                </g:link>
                                <g:link controller="productAssociation" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.associations.label" default="Product Associations"/>
                                </g:link>
                            </div>
                            <div class="padding-8">
                                <g:link controller="productSupplier" action="list" class="list dropdown-item">
                                    <warehouse:message code="productSuppliers.label"/>
                                </g:link>
                                <g:link controller="productComponent" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.components.label" default="Components"/>
                                </g:link>
                                <g:link controller="productGroup" action="list" class="list dropdown-item">
                                    <warehouse:message code="productGroups.label"/>
                                </g:link>
                                <g:link controller="unitOfMeasure" action="list" class="list dropdown-item">
                                    <warehouse:message code="unitOfMeasure.label"/>
                                </g:link>
                                <g:link controller="unitOfMeasureClass" action="list" class="list dropdown-item">
                                    <warehouse:message code="unitOfMeasureClass.label"/>
                                </g:link>
                                <g:link controller="unitOfMeasureConversion" action="list" class="list dropdown-item">
                                    <warehouse:message code="unitOfMeasureConversion.label" default="UoM Conversion"/>
                                </g:link>
                            </div>
                        </div>
                    </div>
                </li>
                <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none">
                    <button
                        data-target="#collapse-products"
                        class="nav-link d-flex justify-content-between align-items-center w-100"
                        data-toggle="collapse"
                        aria-expanded="false"
                        aria-controls="collapse-products"
                    >
                        <warehouse:message code="products.label" />
                        <i class="ri-arrow-drop-down-line"></i>
                    </button>
                    <div class="collapse w-100" id="collapse-products">
                        <div class="d-flex flex-wrap">
                            <div class="padding-8">
                                <g:isUserAdmin>
                                    <g:link controller="product" action="create" class="create dropdown-item">
                                        <warehouse:message code="product.create.label"/>
                                    </g:link>
                                </g:isUserAdmin>
                                <g:link controller="product" action="list" class="list dropdown-item">
                                    <warehouse:message code="products.list.label"/>
                                </g:link>
                                <g:isUserAdmin>
                                    <g:link controller="product" action="batchEdit" class="create dropdown-item">
                                        <warehouse:message code="product.batchEdit.label"/>
                                    </g:link>
                                    <g:link controller="product" action="importAsCsv" class="import dropdown-item">
                                        <warehouse:message code="product.importAsCsv.label"/>
                                    </g:link>
                                    <g:link controller="product" action="exportAsCsv" class="list dropdown-item">
                                        <warehouse:message code="product.exportAsCsv.label"/>
                                    </g:link>
                                </g:isUserAdmin>
                                <g:isSuperuser>
                                    <g:link controller="productType" action="list" class="site dropdown-item">
                                        <warehouse:message code="productType.label" default="ProductType" />
                                    </g:link>
                                </g:isSuperuser>
                            </div>
                            <div class="padding-8">
                                <g:link controller="category" action="tree" class="list dropdown-item">
                                    <warehouse:message code="categories.label"/>
                                </g:link>
                                <g:link controller="productCatalog" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.catalogs.label" default="Catalogs"/>
                                </g:link>
                                <g:link controller="tag" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.tags.label"/>
                                </g:link>
                                <g:link controller="attribute" action="list" class="list dropdown-item">
                                    <warehouse:message code="attributes.label"/>
                                </g:link>
                                <g:link controller="productAssociation" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.associations.label" default="Product Associations"/>
                                </g:link>
                            </div>
                            <div class="padding-8">
                                <g:link controller="productSupplier" action="list" class="list dropdown-item">
                                    <warehouse:message code="productSuppliers.label"/>
                                </g:link>
                                <g:link controller="productComponent" action="list" class="list dropdown-item">
                                    <warehouse:message code="product.components.label" default="Components"/>
                                </g:link>
                                <g:link controller="productGroup" action="list" class="list dropdown-item">
                                    <warehouse:message code="productGroups.label"/>
                                </g:link>
                                <g:link controller="unitOfMeasure" action="list" class="list dropdown-item">
                                    <warehouse:message code="unitOfMeasure.label"/>
                                </g:link>
                                <g:link controller="unitOfMeasureClass" action="list" class="list dropdown-item">
                                    <warehouse:message code="unitOfMeasureClass.label"/>
                                </g:link>
                                <g:link controller="unitOfMeasureConversion" action="list" class="list dropdown-item">
                                    <warehouse:message code="unitOfMeasureConversion.label" default="UoM Conversion"/>
                                </g:link>
                            </div>
                        </div>
                    </div>
                </li>
            </g:supports>
            </g:isUserInRole>
        </g:if>

        <g:if test="${megamenuConfig.requisitionTemplate.enabled}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
            <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="stocklists">
                <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                    <warehouse:message code="requisitionTemplates.label" default="Stock Lists" />
                </a>
                <div class="dropdown-menu dropdown-menu-wrapper">
                    <div class="dropdown-menu-subsections dropdown-menu-content">
                        <div class="padding-8">
                            <g:link controller="requisitionTemplate" action="list" class="dropdown-item">
                                <warehouse:message code="requisitionTemplates.list.label" default="List stock lists"/>
                            </g:link>
                            <g:isUserAdmin>
                                <g:link controller="requisitionTemplate" action="create" class="dropdown-item">
                                    <warehouse:message code="requisitionTemplates.create.label" default="Create stock list" />
                                </g:link>
                            </g:isUserAdmin>
                        </div>
                    </div>
                </div>
            </li>
            <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none">
                <button
                    data-target="#collapse-requisitionTemplates"
                    class="nav-link d-flex justify-content-between align-items-center w-100"
                    data-toggle="collapse"
                    aria-expanded="false"
                    aria-controls="collapse-requisitionTemplates"
                >
                    <warehouse:message code="requisitionTemplates.label" default="Stock Lists" />
                    <i class="ri-arrow-drop-down-line"></i>
                </button>
                <div class="collapse w-100" id="collapse-requisitionTemplates">
                    <div class="d-flex flex-wrap">
                        <div class="padding-8">
                            <g:link controller="requisitionTemplate" action="list" class="dropdown-item">
                                <warehouse:message code="requisitionTemplates.list.label" default="List stock lists"/>
                            </g:link>
                            <g:isUserAdmin>
                                <g:link controller="requisitionTemplate" action="create" class="dropdown-item">
                                    <warehouse:message code="requisitionTemplates.create.label" default="Create stock list" />
                                </g:link>
                            </g:isUserAdmin>
                        </div>
                    </div>
                </div>
            </li>
            </g:isUserInRole>
        </g:if>


        <g:if test="${megamenuConfig.customLinks.enabled && megamenuConfig.customLinks.menuItems}">
            <g:isUserInRole roles="[RoleType.ROLE_BROWSER]">
                <li class="nav-item dropdown align-items-center d-none d-lg-flex" id="customLinks">
                <a href="javascript:void(0)" class="nav-link dropdown-toggle">
                    <warehouse:message code="customLinks.label" default="Custom Links" />
                </a>
                <div class="dropdown-menu dropdown-menu-wrapper">
                    <div class="dropdown-menu-subsections dropdown-menu-content">
                        <div class="padding-8">
                            <g:each var="item" in="${megamenuConfig.customLinks.menuItems}">
                                    <a href="${item.href}" target="${item.target}" class="dropdown-item">
                                        <warehouse:message code="${item.label}" default="${item.defaultLabel}" />
                                    </a>
                            </g:each>
                        </div>
                    </div>
                </div>
            </li>
                <li class="nav-item collapsable align-items-center flex-column d-flex d-lg-none" >
                <button
                    data-target="#collapse-customLinks"
                    class="nav-link d-flex justify-content-between align-items-center w-100"
                    data-toggle="collapse"
                    aria-expanded="false"
                    aria-controls="collapse-customLinks"
                >
                    <warehouse:message code="customLinks.label" default="Custom Links" />
                    <i class="ri-arrow-drop-down-line"></i>
                </button>
                <div class="collapse w-100" id="collapse-customLinks">
                    <div class="d-flex flex-wrap">
                        <div class="padding-8">
                            <g:each var="item" in="${megamenuConfig.customLinks.menuItems}">
                                    <a href="${item.href}" target="${item.target}" class="dropdown-item">
                                        <warehouse:message code="${item.label}" default="${item.defaultLabel}" />
                                    </a>
                            </g:each>
                        </div>
                    </div>
                </div>
            </li>
            </g:isUserInRole>
        </g:if>
     </g:hideIfIsNonInventoryManagedAndCanSubmitRequest>
<g:hiddenField id="megamenu" name="megamenu" value="${megamenuConfig}"/>
<g:hiddenField id="dashboardMenuItems" name="dashboardMenuItems" value="${megamenuConfig.dashboard.href}"/>
<g:hiddenField id="inventoryMenuItems" name="inventoryMenuItems" value="${megamenuConfig.inventory.subsections}"/>
<g:hiddenField id="purchasingMenuItems" name="purchasingMenuItems" value="${megamenuConfig.purchasing.subsections}"/>
<g:hiddenField id="invoicingMenuItems" name="invoicingMenuItems" value="${megamenuConfig.invoicing.subsections}"/>
<g:hiddenField id="inboundMenuItems" name="inboundMenuItems" value="${megamenuConfig.inbound.subsections}"/>
<g:hiddenField id="outboundMenuItems" name="outboundMenuItems" value="${megamenuConfig.outbound.subsections}"/>
<g:hiddenField id="reportingMenuItems" name="reportingMenuItems" value="${megamenuConfig.reporting.subsections}"/>
<g:hiddenField id="productsMenuItems" name="productsMenuItems" value="${megamenuConfig.products.subsections}"/>
<g:hiddenField id="stocklistMenuItems" name="stocklistMenuItems" value="${megamenuConfig.requisitionTemplate.menuItems}"/>
<g:hiddenField id="currentLocation" name="currentLocation" value="${session?.warehouse?.id}"/>
</ul>
<!--MegaMenu Ends-->

%{--Script for checking active state of menu items--}%
<script type="text/javascript">
  $(document).ready(function() {
    const currentLocationId = $("#currentLocation").val();
    const path = window.location.pathname
    const hrefMap = {
      'inventory': $("#inventoryMenuItems").val(),
      'purchasing': $("#purchasingMenuItems").val(),
      'invoicing': $("#invoicingMenuItems").val(),
      'inbound': $("#inboundMenuItems").val(),
      'outbound': $("#outboundMenuItems").val(),
      'reporting': $("#reportingMenuItems").val(),
      'products': $("#productsMenuItems").val(),
      'stocklists': $("#stocklistMenuItems").val(),
    }
    // Find matched path
    const matchPath = Object.keys(hrefMap).find((key) => hrefMap[key].includes(path));

    // Case for inbound/outbound list
    if (matchPath === 'inbound') {
      if (location.search && (location.search.includes("?direction=OUTBOUND") || location.search.includes('origin.id='+currentLocationId))) {
        document.getElementById('outbound').classList.add('active-section');
        return;
      }
      document.getElementById('inbound').classList.add('active-section');
      return;
    }

    // Assign active to matched section
    document.getElementById(matchPath) && document.getElementById(matchPath).classList.add('active-section');

  });
</script>
