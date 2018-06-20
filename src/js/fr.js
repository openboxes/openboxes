const fr = {
  navbar: {
    dashboard: {
      label: 'Tableau de bord',
    },
    analytics: {
      label: 'Analytics',
      subsections: {
        inventoryBrowser: {
          label: 'Parcourir l\'inventaire',
        },
        snapshot: {
          label: 'Inventory Snapshots',
        },
      },
    },
    inventory: {
      label: 'Inventaire',
      subsections: {
        browse: {
          label: 'Parcourir l\'inventaire',
        },
        manage: {
          label: 'Gérer línventaire',
        },
        browseByCategory: {
          label: 'Parcourir par catégorie',
        },
        equipment: {
          label: 'Equipment',
        },
        medicines: {
          label: 'Medicines',
        },
        other: {
          label: 'Other',
        },
        perishables: {
          label: 'Perishables',
        },
        supplies: {
          label: 'Supplies',
        },
      },
    },
    purchaseOrders: {
      label: 'Commandes',
      subsections: {
        purchaseOrdersWorkflow: {
          label: 'Ajouter commande entrante',
        },
        order: {
          label: 'Commandes',
        },
      },
    },
    requisitions: {
      label: 'Demandes',
      subsections: {
        stockRequisition: {
          label: 'Create stock requisition',
        },
        nonStockRequisition: {
          label: 'Create non-stock requisition',
        },
        adHocRequisition: {
          label: 'Create adhoc stock requisition',
        },
        requisitionList: {
          label: 'Demandes',
        },
        requisitionsAll: {
          label: 'Tout',
        },
      },
    },
    shipping: {
      label: 'Livraisons',
      subsections: {
        createShipment: {
          label: 'Ajouter une livraison sortante',
        },
        shipmentList: {
          label: 'Livraisons sortantes',
        },
        shipmentAll: {
          label: 'Tout',
        },
      },
    },
    stockMovement: {
      label: 'Stock Movement',
      subsections: {
        creteStockMovement: {
          label: 'Create Stock Movement',
        },
        partialReceiving: {
          label: 'Partial Receiving',
          link: '/openboxes/partialReceiving/index',
        },
      },
    },
    receiving: {
      label: 'Reception',
      subsections: {
        createShipment: {
          label: 'Ajouter une livraison entrante',
        },
        shipmentList: {
          label: 'Livraisons entrantes',
        },
        shipmentAll: {
          label: 'Tout',
        },
        pending: {
          label: 'En cours',
        },
      },
    },
    reporting: {
      label: 'Rapport',
      subsections: {
        showBinLocationReport: {
          label: 'Bin Location Report',
        },
        inventory: {
          label: 'Baseline QoH Report',
        },
        showTransactionReport: {
          label: 'Afficher rappaport d\'inventaire',
        },
        consumption: {
          label: 'Consumption Report',
        },
        listDailyTransactions: {
          label: 'Transactions journalières',
        },
        showShippingReport: {
          label: 'Afficher rappaport de livraisons',
        },
        showInventorySamplingReport: {
          label: 'Inventory Sampling Report',
        },
        listExpiredStock: {
          label: 'Stock expiré',
        },
        listExpiringStock: {
          label: 'Stock proche d\'expiration',
        },
        listLowStock: {
          label: 'Stock bas',
        },
        listReorderStock: {
          label: 'Reorder stock',
        },
        exportBinLocation: {
          label: 'Export bin locations',
        },
        exportAsCsv: {
          label: 'Export products',
        },
        exportLatestInventoryDate: {
          label: 'Export latest inventory date',
        },
        inventoryLevelExport: {
          label: 'Export inventory levels',
        },
        requisitionExport: {
          label: 'Export requisitions',
        },
        requisitionItem: {
          label: 'Export requisition intems',
        },
      },
    },
    products: {
      label: 'Produits',
      subsections: {
        product: {
          label: 'Produits',
        },
        productGroup: {
          label: 'Generic Products',
        },
        productSupplier: {
          label: 'Products Suppliers',
        },
        productAssociation: {
          label: 'Associations',
        },
        productCatalog: {
          label: 'Catalogs',
        },
        productComponent: {
          label: 'Components',
        },
        attribute: {
          label: 'Attributes',
        },
        category: {
          label: 'Categories',
        },
        tag: {
          label: 'Tags',
        },
        unitOfMeasure: {
          label: 'Unit of Measure',
        },
        unitOfMeasureClass: {
          label: 'UoM Class',
        },
        inventoryLevel: {
          label: 'Inventory Levels',
        },
        productCreate: {
          label: 'Create new product',
        },
        productBatchEdit: {
          label: 'Batch edit product',
        },
        productImportAsCsv: {
          label: 'import products',
        },
        productExportAsCsv: {
          label: 'Export products',
        },
      },
    },
    configuration: {
      label: 'Configuration',
      subsections: {
        showSettings: {
          label: 'Settings',
        },
        migration: {
          label: 'Migrate Data',
        },
        console: {
          label: 'Console',
        },
        cache: {
          label: 'Cache',
        },
        clickstream: {
          label: 'Clickstream',
        },
        sendMail: {
          label: 'Email',
        },
        localization: {
          label: 'Localization',
        },
        documentType: {
          label: 'Document Types',
        },
        eventType: {
          label: 'Event Types',
        },
        locationGroup: {
          label: 'Location groups',
        },
        locationType: {
          label: 'Location types',
        },
        partyType: {
          label: 'Party types',
        },
        partyRole: {
          label: 'Party roles',
        },
        location: {
          label: 'Locations',
        },
        shipper: {
          label: 'Expéditeurs',
        },
        organization: {
          label: 'Organizations',
        },
        shipmentWorkflow: {
          label: 'Shipment Workflows',
        },
        document: {
          label: 'Documents',
        },
        person: {
          label: 'Tous les individus',
        },
        requisitionTemplate: {
          label: 'Stock lists',
        },
        listAllTransactions: {
          label: 'Transactions',
        },
        user: {
          label: 'Utilisateurs',
        },
        editTransaction: {
          label: 'Ajouter une transaction',
        },
        importInventory: {
          label: 'Importer Inventaire',
        },
        importInventoryLevel: {
          label: 'Importer Niveau des stocks',
        },
      },
    },
  },
};

export default fr;
