const en = {
  navbar: {
    dashboard: {
      label: 'Dashboard',
      link: '/openboxes/dashboard/index',
    },
    analytics: {
      label: 'Analytics',
      link: '#',
      subsections: {
        inventoryBrowser: {
          label: 'Browse Inventory',
          link: '/openboxes/inventoryBrowser/index',
        },
        snapshot: {
          label: 'Inventory Snapshots',
          link: '/openboxes/snapshot/index',
        },
      },
    },
    inventory: {
      label: 'Inventory',
      link: '#',
      subsections: {
        browse: {
          label: 'Browse Inventory',
          link: '/openboxes/inventory/browse?resetSearch=true',
        },
        manage: {
          label: 'Manage Inventory',
          link: '/openboxes/inventory/manage',
        },
        browseByCategory: {
          label: 'Browse by Category',
          link: '/openboxes/inventory/browse?resetSearch=true',
        },
        equipment: {
          label: 'Equipment',
          link: '/openboxes/inventory/browse?subcategoryId=3&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        medicines: {
          label: 'Medicines',
          link: '/openboxes/inventory/browse?subcategoryId=1&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        other: {
          label: 'Other',
          link: '/openboxes/inventory/browse?subcategoryId=5&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        perishables: {
          label: 'Perishables',
          link: '/openboxes/inventory/browse?subcategoryId=4&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
        supplies: {
          label: 'Supplies',
          link: '/openboxes/inventory/browse?subcategoryId=2&resetSearch=true&searchPerformed=true&showOutOfStockProducts=on',
        },
      },
    },
    purchaseOrders: {
      label: 'Purchase Orders',
      link: '#',
      subsections: {
        purchaseOrdersWorkflow: {
          label: 'Create purchase order',
          link: '/openboxes/purchaseOrderWorkflow/index',
        },
        order: {
          label: 'Purchase Order',
          link: '/openboxes/order/list?status=PENDING',
        },
      },
    },
    requisitions: {
      label: 'Requisitions',
      link: '#',
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
      },
    },
    shipping: {
      label: 'Shipping',
      link: '#',
      subsections: {
        createShipment: {
          label: 'Create outbound shipment',
          link: '/openboxes/createShipmentWorkflow/createShipment?type=OUTGOING',
        },
        shipmentList: {
          label: 'Outbound Shipments',
          link: '/openboxes/shipment/list?type=outgoing',
        },
        shipmentAll: {
          label: 'All',
          link: '/openboxes/shipment/list?type=outgoing',
        },
      },
    },
    stockMovement: {
      label: 'Stock Movement',
      link: '#',
      subsections: {
        creteStockMovement: {
          label: 'Create Stock Movement',
          link: '/openboxes/stockMovement/index',
        },
      },
    },
    receiving: {
      label: 'Receiving',
      link: '#',
      subsections: {
        createShipment: {
          label: 'Create inbound shipment',
          link: '/openboxes/createShipmentWorkflow/createShipment?type=INCOMING',
        },
        shipmentList: {
          label: 'Inbound Shipments',
          link: '/openboxes/shipment/list?type=incoming',
        },
        shipmentAll: {
          label: 'All',
          link: '/openboxes/shipment/list?type=incoming',
        },
        pending: {
          label: 'Pending',
          link: '/openboxes/shipment/list?type=incoming&status=PENDING',
        },
      },
    },
    reporting: {
      label: 'Reporting',
      link: '#',
      subsections: {
        showBinLocationReport: {
          label: 'Bin Location Report',
          link: '/openboxes/report/showBinLocationReport',
        },
        inventory: {
          label: 'Baseline QoH Report',
          link: '/openboxes/inventory/show',
        },
        showTransactionReport: {
          label: 'Transcription Report',
          link: '/openboxes/report/showTransactionReport',
        },
        consumption: {
          label: 'Consumption Report',
          link: '/openboxes/consumption/show',
        },
        listDailyTransactions: {
          label: 'Daily Transactions Report',
          link: '/openboxes/inventory/listDailyTransactions',
        },
        showShippingReport: {
          label: 'Shipping Report',
          link: '/openboxes/report/showShippingReport',
        },
        showInventorySamplingReport: {
          label: 'Inventory Sampling Report',
          link: '/openboxes/report/showInventorySamplingReport',
        },
        listExpiredStock: {
          label: 'Expired stock',
          link: '/openboxes/inventory/listExpiredStock',
        },
        listExpiringStock: {
          label: 'Expiring stock',
          link: '/openboxes/inventory/listExpiringStock',
        },
        listLowStock: {
          label: 'Low stock',
          link: '/openboxes/inventory/listLowStock',
        },
        listReorderStock: {
          label: 'Reorder stock',
          link: '/openboxes/inventory/listReorderStock',
        },
        exportBinLocation: {
          label: 'Export bin locations',
          link: '/openboxes/report/exportBinLocation?downloadFormat=csv',
        },
        exportAsCsv: {
          label: 'Export products',
          link: '/openboxes/product/exportAsCsv',
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
          label: 'Export requisition intems',
          link: '/openboxes/requisitionItem/listCanceled',
        },
      },
    },
    products: {
      label: 'Products',
      link: '#',
      subsections: {
        product: {
          label: 'Products',
          link: '/openboxes/product/list',
        },
        productGroup: {
          label: 'Generic Products',
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
          label: 'Attributes',
          link: '/openboxes/attribute/list',
        },
        category: {
          label: 'Categories',
          link: '/openboxes/category/tree',
        },
        tag: {
          label: 'Tags',
          link: '/openboxes/tag/list',
        },
        unitOfMeasure: {
          label: 'Unit of Measure',
          link: '/openboxes/unitOfMeasure/list',
        },
        unitOfMeasureClass: {
          label: 'UoM Class',
          link: '/openboxes/unitOfMeasureClass/list',
        },
        inventoryLevel: {
          label: 'Inventory Levels',
          link: '/openboxes/inventoryLevel/list',
        },
        productCreate: {
          label: 'Create new product',
          link: '/openboxes/product/create',
        },
        productBatchEdit: {
          label: 'Batch edit product',
          link: '/openboxes/product/batchEdit',
        },
        productImportAsCsv: {
          label: 'import products',
          link: '/openboxes/product/importAsCsv',
        },
        productExportAsCsv: {
          label: 'Export products',
          link: '/openboxes/product/exportAsCsv',
        },
      },
    },
    configuration: {
      label: 'Configuration',
      link: '#',
      subsections: {
        showSettings: {
          label: 'Settings',
          link: '/openboxes/admin/showSettings',
        },
        migration: {
          label: 'Migrate Data',
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
        clickstream: {
          label: 'Clickstream',
          link: '/openboxes/admin/clickstream',
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
          label: 'Location types',
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
          label: 'Locations',
          link: '/openboxes/location/list',
        },
        shipper: {
          label: 'Shippers',
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
          label: 'Documents',
          link: '/openboxes/document/list',
        },
        person: {
          label: 'People',
          link: '/openboxes/person/list',
        },
        requisitionTemplate: {
          label: 'Stock lists',
          link: '/openboxes/requisitionTemplate/list',
        },
        listAllTransactions: {
          label: 'Transactions',
          link: '/openboxes/inventory/listAllTransactions',
        },
        user: {
          label: 'Users',
          link: '/openboxes/user/list',
        },
        editTransaction: {
          label: 'Add transaction',
          link: '/openboxes/inventory/editTransaction',
        },
        importInventory: {
          label: 'Import Inventory',
          link: '/openboxes/batch/importData?type=inventory',
        },
        importInventoryLevel: {
          label: 'Import Inventory Level',
          link: '/openboxes/batch/importData?type=inventoryLevel',
        },
      },
    },
  },
};

export default en;
