const es = {
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
          label: 'Revisa el inventario',
          link: '/inventoryBrowser/index',
        },
        snapshot: {
          label: 'Inventory Snapshots',
          link: '/snapshot/list',
        },
      },
    },
    inventory: {
      label: 'Inventario',
      link: '#',
      activity: ['MANAGE_INVENTORY'],
      subsections: {
        browse: {
          label: 'Revisa el inventario',
          link: '/inventory/browse?resetSearch=true',
        },
        manage: {
          label: 'Administrar inventario',
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
      label: 'Órdenes',
      link: '#',
      activity: ['PLACE_ORDER', 'FULFILL_ORDER'],
      subsections: {
        purchaseOrdersWorkflow: {
          label: 'Crear Purchase Order',
          link: '/purchaseOrderWorkflow/index',
        },
        orders: {
          label: 'List Purchase Orders',
          link: '/order/list?orderTypeCode=PURCHASE_ORDER',
        },
      },
    },
    requisitions: {
      label: 'Peticiones',
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
          label: 'Crear Inbound Movement',
          link: '/stockMovement/createInbound?direction=INBOUND',
        },
        list: {
          configName: 'stockMovement',
          label: 'Lista de Inbound Movements',
          link: '/stockMovement/list?direction=INBOUND',
        },
        request: {
          configName: 'stockMovement',
          label: 'Request Stock',
          link: '/stockMovement/createRequest',
        },
        createPutAway: {
          configName: 'stockMovement',
          label: 'Crear Putaway',
          link: '/putAway/index',
        },
        listPutAways: {
          configName: 'stockMovement',
          label: 'Lista de Putaways',
          link: '/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING',
        },
        createShipment: {
          configName: 'receiving',
          label: 'Crear Inbound Shipment',
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
          label: 'Pendiente',
          link: '/shipment/list?type=incoming&status=PENDING',
        },
        received: {
          configName: 'receiving',
          label: 'Recibido',
          link: '/shipment/list?type=incoming&status=RECEIVED',
        },
        shipped: {
          configName: 'receiving',
          label: 'Enviado',
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
          label: 'Crear Outbound Movement',
          link: '/stockMovement/createOutbound?direction=OUTBOUND',
        },
        list: {
          configName: 'stockMovement',
          label: 'Lista de Outbound Movements',
          link: '/stockMovement/list?direction=OUTBOUND',
        },
        createShipment: {
          configName: 'shipping',
          label: 'Crear Outbound Shipment',
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
          label: 'Pendiente',
          link: '/shipment/list?status=PENDING',
        },
        received: {
          configName: 'shipping',
          label: 'Recibido',
          link: '/shipment/list?status=RECEIVED',
        },
        shipped: {
          configName: 'shipping',
          label: 'Enviado',
          link: '/shipment/list?status=SHIPPED',
        },
      },
    },
    reporting: {
      label: 'Informe',
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
      label: 'Productos',
      link: '#',
      activity: ['MANAGE_INVENTORY'],
      subsections: {
        product: {
          label: 'Productos',
          link: '/product/list',
        },
        productGroup: {
          label: 'Producto Grupos',
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
          label: 'Atributos',
          link: '/attribute/list',
        },
        category: {
          label: 'Categorias',
          link: '/category/tree',
        },
        tag: {
          label: 'Tags',
          link: '/tag/list',
        },
        unitOfMeasure: {
          label: 'Unidad de Medida',
          link: '/unitOfMeasure/list',
        },
        unitOfMeasureClass: {
          label: 'Unidad de Medida de la clase',
          link: '/unitOfMeasureClass/list',
        },
        inventoryLevel: {
          label: 'Inventory Levels',
          link: '/inventoryLevel/list',
        },
        productCreate: {
          label: 'Crear nuevo producto',
          link: '/product/create',
          adminOnly: true,
        },
        productBatchEdit: {
          label: 'Edición por lotes de productos',
          link: '/product/batchEdit',
          adminOnly: true,
        },
        productImportAsCsv: {
          label: 'Import products',
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
          label: 'Ajustes',
          link: '/admin/showSettings',
        },
        migration: {
          label: 'Data Migration',
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
          label: 'Tipos Ubicación',
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
          label: 'Ubicaciones',
          link: '/location/list',
        },
        shipper: {
          label: 'Remitentes',
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
          label: 'Documentos',
          link: '/document/list',
        },
        person: {
          label: 'Personas',
          link: '/person/list',
        },
        listAllTransactions: {
          label: 'Transacciones',
          link: '/inventory/listAllTransactions',
        },
        user: {
          label: 'Usuarios',
          link: '/user/list',
        },
        editTransaction: {
          label: 'Añadir transacción',
          link: '/inventory/editTransaction',
        },
        importInventory: {
          label: 'Importar Inventario',
          link: '/batch/importData?type=inventory',
        },
        importInventoryLevel: {
          label: 'Importar nivel de inventario',
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

export default es;
