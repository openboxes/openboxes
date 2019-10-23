const en = {
  navbar: {
    dashboard: {
      label: 'Dashboard',
      link: '/dashboard/index',
    },
    analytics: {
      label: 'Analytics',
      link: '#',
      adminOnly: true,
      subsections: {
        inventoryBrowser: {
          label: 'Browse Inventory',
          link: '/inventoryBrowser/index',
        },
        snapshot: {
          label: 'Inventory Snapshots',
          link: '/snapshot/list',
        },
      },
    },
    inventory: {
      label: 'Inventory',
      link: '#',
      activity: ['MANAGE_INVENTORY'],
      subsections: {
        browse: {
          label: 'Browse Inventory',
          link: '/inventory/browse?resetSearch=true',
        },
        manage: {
          label: 'Manage Inventory',
          link: '/inventory/manage',
        },
        browseByCategory: {
          label: 'Browse by Category',
          link: '/inventory/browse?resetSearch=true',
        },
        chemicals: {
          label: 'Chemicals',
          link: '/inventory/browse?subcategoryId=C0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        drugs: {
          label: 'Drugs',
          link: '/inventory/browse?subcategoryId=D0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        facilities: {
          label: 'Facilities',
          link: '/inventory/browse?subcategoryId=F0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        itAndCommunicationsEq: {
          label: 'IT & Communications Equipment',
          link: '/inventory/browse?subcategoryId=I0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        lab: {
          label: 'Lab',
          link: '/inventory/browse?subcategoryId=L0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        medEquipSupply: {
          label: 'MedEquipSupply',
          link: '/inventory/browse?subcategoryId=M0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        vehiclesAndParts: {
          label: 'Vehicles and Parts',
          link: '/inventory/browse?subcategoryId=V0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
      },
    },
    orders: {
      label: 'Orders',
      link: '#',
      activity: ['PLACE_ORDER', 'FULFILL_ORDER'],
      subsections: {
        purchaseOrdersWorkflow: {
          label: 'Create order',
          link: '/purchaseOrderWorkflow/index',
        },
        orders: {
          label: 'Order',
          link: '/order/list?orderTypeCode=PURCHASE_ORDER',
        },
        completed: {
          label: 'Completed',
          link: '/order/list?status=COMPLETED',
        },
        placed: {
          label: 'Placed',
          link: '/order/list?status=PLACED',
        },
      },
    },
    requisitions: {
      label: 'Requisitions',
      link: '#',
      activity: ['PLACE_REQUEST', 'FULFILL_REQUEST'],
      subsections: {
        stockRequisition: {
          label: 'Create stock requisition',
          link: '/requisition/chooseTemplate?type=STOCK',
        },
        nonStockRequisition: {
          label: 'Create non-stock requisition',
          link: '/requisition/create?type=NON_STOCK',
        },
        adHocRequisition: {
          label: 'Create adhoc stock requisition',
          link: '/requisition/create?type=ADHOC',
        },
        requisitionList: {
          label: 'Requisitions',
          link: '/requisition/list',
        },
        requisitionsAll: {
          label: 'All',
          link: '/requisition/list',
        },
        requisitionsCreated: {
          label: 'Created',
          link: '/requisition/list?status=CREATED',
        },
        requisitionsChecking: {
          label: 'Checking',
          link: '/requisition/list?status=CHECKING',
        },
        requisitionsIssued: {
          label: 'Issued',
          link: '/requisition/list?status=ISSUED',
        },
      },
    },
    inbound: {
      label: 'Inbound',
      link: '#',
      activity: ['RECEIVE_STOCK'],
      subsections: {
        create: {
          configName: 'stockMovement',
          label: 'Create Inbound Stock Movement',
          link: '/stockMovement/create?direction=INBOUND',
        },
        list: {
          configName: 'stockMovement',
          label: 'List Inbound Stock Movements',
          link: '/stockMovement/list?direction=INBOUND',
        },
        request: {
          configName: 'stockMovement',
          label: 'Request Stock',
          link: '/stockMovement/create?type=REQUEST',
        },
        createPutAway: {
          configName: 'stockMovement',
          label: 'Create Putaway',
          link: '/putAway/index',
        },
        listPutAways: {
          configName: 'stockMovement',
          label: 'List Putaways',
          link: '/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING',
        },
        createShipment: {
          configName: 'receiving',
          label: 'Create Inbound Shipment',
          link: '/createShipmentWorkflow/createShipment?type=INCOMING',
        },
        listShipments: {
          configName: 'receiving',
          label: 'Inbound Shipments',
          link: '/shipment/list?type=incoming',
        },
        all: {
          configName: 'receiving',
          label: 'All',
          link: '/shipment/list?type=incoming',
        },
        receiving: {
          configName: 'receiving',
          label: 'Receiving',
          link: '/shipment/list?type=incoming&status=PARTIALLY_RECEIVED',
        },
        pending: {
          configName: 'receiving',
          label: 'Pending',
          link: '/shipment/list?type=incoming&status=PENDING',
        },
        received: {
          configName: 'receiving',
          label: 'Received',
          link: '/shipment/list?type=incoming&status=RECEIVED',
        },
        shipped: {
          configName: 'receiving',
          label: 'Shipped',
          link: '/shipment/list?type=incoming&status=SHIPPED',
        },
      },
    },
    outbound: {
      label: 'Outbound',
      link: '#',
      activity: ['SEND_STOCK'],
      subsections: {
        create: {
          configName: 'stockMovement',
          label: 'Create Outbound Stock Movement',
          link: '/stockMovement/create?direction=OUTBOUND',
        },
        list: {
          configName: 'stockMovement',
          label: 'List Outbound Stock Movements',
          link: '/stockMovement/list?direction=OUTBOUND',
        },
        createShipment: {
          configName: 'shipping',
          label: 'Create Outbound Shipment',
          link: '/createShipmentWorkflow/createShipment?type=OUTGOING',
        },
        listShipments: {
          configName: 'shipping',
          label: 'Outbound Shipments',
          link: '/shipment/list?type=outgoing',
        },
        all: {
          configName: 'shipping',
          label: 'All',
          link: '/shipment/list?type=outgoing',
        },
        pending: {
          configName: 'shipping',
          label: 'Pending',
          link: '/shipment/list?status=PENDING',
        },
        received: {
          configName: 'shipping',
          label: 'Received',
          link: '/shipment/list?status=RECEIVED',
        },
        shipped: {
          configName: 'shipping',
          label: 'Shipped',
          link: '/shipment/list?status=SHIPPED',
        },
      },
    },
    reporting: {
      label: 'Reporting',
      link: '#',
      subsections: {
        listInStock: {
          label: 'In stock ',
          link: '/inventory/listInStock',
        },
        showBinLocationReport: {
          label: 'Bin Location Report',
          link: '/report/showBinLocationReport',
        },
        listExpiredStock: {
          label: 'Expired stock',
          link: '/inventory/listExpiredStock',
        },
        listExpiringStock: {
          label: 'Expiring stock',
          link: '/inventory/listExpiringStock',
        },
        showInventoryByLocationReport: {
          label: 'Inventory By Location Report',
          link: '/report/showInventoryByLocationReport',
        },
        cycleCountReport: {
          label: 'Cycle Count Report',
          link: '/cycleCount/exportAsCsv',
        },
        inventory: {
          label: 'Baseline QoH Report',
          link: '/inventory/show',
        },
        showTransactionReport: {
          label: 'Transaction Report',
          link: '/report/showTransactionReport',
        },
        consumption: {
          label: 'Consumption Report',
          link: '/consumption/show',
        },
        exportAsCsv: {
          label: 'Export products',
          link: '/product/exportAsCsv',
        },
        exportProductSources: {
          label: 'Export product sources',
          link: '/productSupplier/export',
        },
        exportLatestInventoryDate: {
          label: 'Export latest inventory date',
          link: '/inventory/exportLatestInventoryDate',
        },
        inventoryLevelExport: {
          label: 'Export inventory levels',
          link: '/inventoryLevel/export',
        },
        requisitionExport: {
          label: 'Export requisitions',
          link: '/requisition/export',
        },
        requisitionItem: {
          label: 'Export requisition items',
          link: '/requisitionItem/listCanceled',
        },
        exportBinLocation: {
          label: 'Export bin locations',
          link: '/report/exportBinLocation?downloadFormat=csv',
        },
      },
    },
    products: {
      label: 'Products',
      link: '#',
      activity: ['MANAGE_INVENTORY'],
      subsections: {
        product: {
          label: 'Products',
          link: '/product/list',
        },
        productGroup: {
          label: 'Generic Products',
          link: '/productGroup/list',
        },
        productSupplier: {
          label: 'Products Suppliers',
          link: '/productSupplier/list',
        },
        productAssociation: {
          label: 'Associations',
          link: '/productAssociation/list',
        },
        productCatalog: {
          label: 'Catalogs',
          link: '/productCatalog/list',
        },
        productComponent: {
          label: 'Components',
          link: '/productComponent/list',
        },
        attribute: {
          label: 'Attributes',
          link: '/attribute/list',
        },
        category: {
          label: 'Categories',
          link: '/category/tree',
        },
        tag: {
          label: 'Tags',
          link: '/tag/list',
        },
        unitOfMeasure: {
          label: 'Unit of Measure',
          link: '/unitOfMeasure/list',
        },
        unitOfMeasureClass: {
          label: 'UoM Class',
          link: '/unitOfMeasureClass/list',
        },
        inventoryLevel: {
          label: 'Inventory Levels',
          link: '/inventoryLevel/list',
        },
        productCreate: {
          label: 'Create new product',
          link: '/product/create',
          adminOnly: true,
        },
        productBatchEdit: {
          label: 'Batch edit product',
          link: '/product/batchEdit',
          adminOnly: true,
        },
        productImportAsCsv: {
          label: 'import products',
          link: '/product/importAsCsv',
          adminOnly: true,
        },
        productExportAsCsv: {
          label: 'Export products',
          link: '/product/exportAsCsv',
          adminOnly: true,
        },
      },
    },
    requisitionTemplate: {
      label: 'Stocklists',
      link: '#',
      subsections: {
        listStockLists: {
          label: 'List stock lists',
          link: '/requisitionTemplate/list',
        },
        createStockList: {
          label: 'Create stock list',
          link: '/requisitionTemplate/create',
        },
      },
    },
    configuration: {
      label: 'Configuration',
      link: '#',
      adminOnly: true,
      subsections: {
        showSettings: {
          label: 'Settings',
          link: '/admin/showSettings',
        },
        migration: {
          label: 'Migrate Data',
          link: '/migration/index',
        },
        console: {
          label: 'Console',
          link: '/console/index',
        },
        cache: {
          label: 'Cache',
          link: '/admin/cache',
        },
        sendMail: {
          label: 'Email',
          link: '/admin/sendMail',
        },
        localization: {
          label: 'Localization',
          link: '/localization/list',
        },
        documentType: {
          label: 'Document Types',
          link: '/documentType/list',
        },
        eventType: {
          label: 'Event Types',
          link: '/eventType/list',
        },
        locationGroup: {
          label: 'Location groups',
          link: '/locationGroup/list',
        },
        locationType: {
          label: 'Location types',
          link: '/locationType/list',
        },
        partyType: {
          label: 'Party types',
          link: '/partyType/list',
        },
        partyRole: {
          label: 'Party roles',
          link: '/partyRole/list',
        },
        location: {
          label: 'Locations',
          link: '/location/list',
        },
        shipper: {
          label: 'Shippers',
          link: '/shipper/list',
        },
        organization: {
          label: 'Organizations',
          link: '/organization/list',
        },
        shipmentWorkflow: {
          label: 'Shipment Workflows',
          link: '/shipmentWorkflow/list',
        },
        document: {
          label: 'Documents',
          link: '/document/list',
        },
        person: {
          label: 'People',
          link: '/person/list',
        },
        listAllTransactions: {
          label: 'Transactions',
          link: '/inventory/listAllTransactions',
        },
        user: {
          label: 'Users',
          link: '/user/list',
        },
        editTransaction: {
          label: 'Add transaction',
          link: '/inventory/editTransaction',
        },
        importInventory: {
          label: 'Import Inventory',
          link: '/batch/importData?type=inventory',
        },
        importInventoryLevel: {
          label: 'Import Inventory Level',
          link: '/batch/importData?type=inventoryLevel',
        },
      },
    },
    customLinks: {
      label: 'Custom Links',
      link: '#',
      renderedFromConfig: true,
    },
  },
};

export default en;
