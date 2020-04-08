const es = {
  navbar: {
    dashboard: {
      label: 'Dashboard',
      link: '/openboxes/dashboard/index',
    },
    analytics: {
      label: 'Analytics',
      link: '#',
      adminOnly: true,
      subsections: {
        inventoryBrowser: {
          label: 'Revisa el inventario',
          link: '/openboxes/inventoryBrowser/index',
        },
        snapshot: {
          label: 'Inventory Snapshots',
          link: '/openboxes/snapshot/list',
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
          link: '/openboxes/inventory/browse?resetSearch=true',
        },
        manage: {
          label: 'Administrar inventario',
          link: '/openboxes/inventory/manage',
        },
        browseByCategory: {
          label: 'Browse by Category',
          link: '/openboxes/inventory/browse?resetSearch=true',
        },
        chemicals: {
          label: 'Chemicals',
          link: '/openboxes/inventory/browse?subcategoryId=C0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        drugs: {
          label: 'Drugs',
          link: '/openboxes/inventory/browse?subcategoryId=D0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        facilities: {
          label: 'Facilities',
          link: '/openboxes/inventory/browse?subcategoryId=F0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        itAndCommunicationsEq: {
          label: 'IT & Communications Equipment',
          link: '/openboxes/inventory/browse?subcategoryId=I0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        lab: {
          label: 'Lab',
          link: '/openboxes/inventory/browse?subcategoryId=L0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        medEquipSupply: {
          label: 'MedEquipSupply',
          link: '/openboxes/inventory/browse?subcategoryId=M0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        vehiclesAndParts: {
          label: 'Vehicles and Parts',
          link: '/openboxes/inventory/browse?subcategoryId=V0000&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
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
          link: '/openboxes/purchaseOrderWorkflow/index',
        },
        orders: {
          label: 'List Purchase Orders',
          link: '/openboxes/order/list?orderTypeCode=PURCHASE_ORDER',
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
          link: '/openboxes/requisition/chooseTemplate?type=STOCK',
        },
        nonStockRequisition: {
          label: 'Create non-stock requisition',
          link: '/openboxes/requisition/create?type=NON_STOCK',
        },
        adHocRequisition: {
          label: 'Create adhoc stock requisition',
          link: '/openboxes/requisition/create?type=ADHOC',
        },
        requisitionList: {
          label: 'Requisitions',
          link: '/openboxes/requisition/list',
        },
        requisitionsAll: {
          label: 'All',
          link: '/openboxes/requisition/list',
        },
        requisitionsCreated: {
          label: 'Created',
          link: '/openboxes/requisition/list?status=CREATED',
        },
        requisitionsChecking: {
          label: 'Checking',
          link: '/openboxes/requisition/list?status=CHECKING',
        },
        requisitionsIssued: {
          label: 'Issued',
          link: '/openboxes/requisition/list?status=ISSUED',
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
          link: '/openboxes/stockMovement/createInbound?direction=INBOUND',
        },
        list: {
          configName: 'stockMovement',
          label: 'Lista de Inbound Movements',
          link: '/openboxes/stockMovement/list?direction=INBOUND',
        },
        request: {
          configName: 'stockMovement',
          label: 'Request Stock',
          link: '/openboxes/stockMovement/createRequest',
        },
        createPutAway: {
          configName: 'stockMovement',
          label: 'Crear Putaway',
          link: '/openboxes/putAway/index',
        },
        listPutAways: {
          configName: 'stockMovement',
          label: 'Lista de Putaways',
          link: '/openboxes/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING',
        },
        createShipment: {
          configName: 'receiving',
          label: 'Crear Inbound Shipment',
          link: '/openboxes/createShipmentWorkflow/createShipment?type=INCOMING',
        },
        listShipments: {
          configName: 'receiving',
          label: 'Inbound Shipments',
          link: '/openboxes/shipment/list?type=incoming',
        },
        all: {
          configName: 'receiving',
          label: 'All',
          link: '/openboxes/shipment/list?type=incoming',
        },
        receiving: {
          configName: 'receiving',
          label: 'Receiving',
          link: '/openboxes/shipment/list?type=incoming&status=PARTIALLY_RECEIVED',
        },
        pending: {
          configName: 'receiving',
          label: 'Pendiente',
          link: '/openboxes/shipment/list?type=incoming&status=PENDING',
        },
        received: {
          configName: 'receiving',
          label: 'Recibido',
          link: '/openboxes/shipment/list?type=incoming&status=RECEIVED',
        },
        shipped: {
          configName: 'receiving',
          label: 'Enviado',
          link: '/openboxes/shipment/list?type=incoming&status=SHIPPED',
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
          link: '/openboxes/stockMovement/createOutbound?direction=OUTBOUND',
        },
        list: {
          configName: 'stockMovement',
          label: 'Lista de Outbound Movements',
          link: '/openboxes/stockMovement/list?direction=OUTBOUND',
        },
        createShipment: {
          configName: 'shipping',
          label: 'Crear Outbound Shipment',
          link: '/openboxes/createShipmentWorkflow/createShipment?type=OUTGOING',
        },
        listShipments: {
          configName: 'shipping',
          label: 'Outbound Shipments',
          link: '/openboxes/shipment/list?type=outgoing',
        },
        all: {
          configName: 'shipping',
          label: 'All',
          link: '/openboxes/shipment/list?type=outgoing',
        },
        pending: {
          configName: 'shipping',
          label: 'Pendiente',
          link: '/openboxes/shipment/list?status=PENDING',
        },
        received: {
          configName: 'shipping',
          label: 'Recibido',
          link: '/openboxes/shipment/list?status=RECEIVED',
        },
        shipped: {
          configName: 'shipping',
          label: 'Enviado',
          link: '/openboxes/shipment/list?status=SHIPPED',
        },
      },
    },
    reporting: {
      label: 'Informe',
      link: '#',
      subsections: {
        listInStock: {
          label: 'In stock ',
          link: '/openboxes/inventory/listInStock',
        },
        showBinLocationReport: {
          label: 'Bin Location Report',
          link: '/openboxes/report/showBinLocationReport',
        },
        listExpiredStock: {
          label: 'Expired stock',
          link: '/openboxes/inventory/listExpiredStock',
        },
        listExpiringStock: {
          label: 'Expiring stock',
          link: '/openboxes/inventory/listExpiringStock',
        },
        showInventoryByLocationReport: {
          label: 'Inventory By Location Report',
          link: '/openboxes/report/showInventoryByLocationReport',
        },
        cycleCountReport: {
          label: 'Cycle Count Report',
          link: '/openboxes/cycleCount/exportAsCsv',
        },
        inventory: {
          label: 'Baseline QoH Report',
          link: '/openboxes/inventory/show',
        },
        showTransactionReport: {
          label: 'Transaction Report',
          link: '/openboxes/report/showTransactionReport',
        },
        consumption: {
          label: 'Consumption Report',
          link: '/openboxes/consumption/show',
        },
        exportAsCsv: {
          label: 'Export products',
          link: '/openboxes/product/exportAsCsv',
        },
        exportProductSources: {
          label: 'Export product sources',
          link: '/openboxes/productSupplier/export',
        },
        exportLatestInventoryDate: {
          label: 'Export latest inventory date',
          link: '/openboxes/inventory/exportLatestInventoryDate',
        },
        inventoryLevelExport: {
          label: 'Export inventory levels',
          link: '/openboxes/inventoryLevel/export',
        },
        requisitionExport: {
          label: 'Export requisitions',
          link: '/openboxes/requisition/export',
        },
        requisitionItem: {
          label: 'Export requisition items',
          link: '/openboxes/requisitionItem/listCanceled',
        },
        exportBinLocation: {
          label: 'Export bin locations',
          link: '/openboxes/report/exportBinLocation?downloadFormat=csv',
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
          link: '/openboxes/product/list',
        },
        productGroup: {
          label: 'Producto Grupos',
          link: '/openboxes/productGroup/list',
        },
        productSupplier: {
          label: 'Products Suppliers',
          link: '/openboxes/productSupplier/list',
        },
        productAssociation: {
          label: 'Associations',
          link: '/openboxes/productAssociation/list',
        },
        productCatalog: {
          label: 'Catalogs',
          link: '/openboxes/productCatalog/list',
        },
        productComponent: {
          label: 'Components',
          link: '/openboxes/productComponent/list',
        },
        attribute: {
          label: 'Atributos',
          link: '/openboxes/attribute/list',
        },
        category: {
          label: 'Categorias',
          link: '/openboxes/category/tree',
        },
        tag: {
          label: 'Tags',
          link: '/openboxes/tag/list',
        },
        unitOfMeasure: {
          label: 'Unidad de Medida',
          link: '/openboxes/unitOfMeasure/list',
        },
        unitOfMeasureClass: {
          label: 'Unidad de Medida de la clase',
          link: '/openboxes/unitOfMeasureClass/list',
        },
        inventoryLevel: {
          label: 'Inventory Levels',
          link: '/openboxes/inventoryLevel/list',
        },
        productCreate: {
          label: 'Crear nuevo producto',
          link: '/openboxes/product/create',
          adminOnly: true,
        },
        productBatchEdit: {
          label: 'Edición por lotes de productos',
          link: '/openboxes/product/batchEdit',
          adminOnly: true,
        },
        productImportAsCsv: {
          label: 'Import products',
          link: '/openboxes/product/importAsCsv',
          adminOnly: true,
        },
        productExportAsCsv: {
          label: 'Export products',
          link: '/openboxes/product/exportAsCsv',
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
          link: '/openboxes/requisitionTemplate/list',
        },
        createStockList: {
          label: 'Create stock list',
          link: '/openboxes/requisitionTemplate/create',
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
          link: '/openboxes/admin/showSettings',
        },
        migration: {
          label: 'Data Migration',
          link: '/openboxes/migration/index',
        },
        console: {
          label: 'Console',
          link: '/openboxes/console/index',
        },
        cache: {
          label: 'Cache',
          link: '/openboxes/admin/cache',
        },
        sendMail: {
          label: 'Email',
          link: '/openboxes/admin/sendMail',
        },
        localization: {
          label: 'Localization',
          link: '/openboxes/localization/list',
        },
        documentType: {
          label: 'Document Types',
          link: '/openboxes/documentType/list',
        },
        eventType: {
          label: 'Event Types',
          link: '/openboxes/eventType/list',
        },
        locationGroup: {
          label: 'Location groups',
          link: '/openboxes/locationGroup/list',
        },
        locationType: {
          label: 'Tipos Ubicación',
          link: '/openboxes/locationType/list',
        },
        partyType: {
          label: 'Party types',
          link: '/openboxes/partyType/list',
        },
        partyRole: {
          label: 'Party roles',
          link: '/openboxes/partyRole/list',
        },
        location: {
          label: 'Ubicaciones',
          link: '/openboxes/location/list',
        },
        shipper: {
          label: 'Remitentes',
          link: '/openboxes/shipper/list',
        },
        organization: {
          label: 'Organizations',
          link: '/openboxes/organization/list',
        },
        shipmentWorkflow: {
          label: 'Shipment Workflows',
          link: '/openboxes/shipmentWorkflow/list',
        },
        document: {
          label: 'Documentos',
          link: '/openboxes/document/list',
        },
        person: {
          label: 'Personas',
          link: '/openboxes/person/list',
        },
        listAllTransactions: {
          label: 'Transacciones',
          link: '/openboxes/inventory/listAllTransactions',
        },
        user: {
          label: 'Usuarios',
          link: '/openboxes/user/list',
        },
        editTransaction: {
          label: 'Añadir transacción',
          link: '/openboxes/inventory/editTransaction',
        },
        importInventory: {
          label: 'Importar Inventario',
          link: '/openboxes/batch/importData?type=inventory',
        },
        importInventoryLevel: {
          label: 'Importar nivel de inventario',
          link: '/openboxes/batch/importData?type=inventoryLevel',
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
