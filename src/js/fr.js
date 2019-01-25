const fr = {
  navbar: {
    dashboard: {
      label: 'Tableau de bord',
      link: '/openboxes/dashboard/index',
    },
    stockMovement: {
      label: 'Stock Movement',
      link: '#',
      subsections: {
        listStockMovements: {
          label: 'List Stock Movements',
          link: '/openboxes/stockMovement/list',
        },
        creteStockMovement: {
          label: 'Create Stock Movement',
          link: '/openboxes/stockMovement/index',
        },
      },
    },
    receiving: {
      label: 'Reception',
      link: '#',
      subsections: {
        receiveStockMovement: {
          label: 'Receive Stock Movement',
          link: '/openboxes/stockMovement/list?status=ISSUED&direction=INBOUND',
        },
        listPutAways: {
          label: 'List Putaways',
          link: '/openboxes/putAway/list',
        },
        createPutAway: {
          label: 'Create Putaway',
          link: '/openboxes/putAway/index',
        },
      },
    },
    products: {
      label: 'Produits',
      link: '#',
      subsections: {
        product: {
          label: 'Produits',
          link: '/openboxes/product/list',
        },
      },
    },
    configuration: {
      label: 'Configuration',
      link: '#',
      subsections: {
        person: {
          label: 'Tous les individus',
          link: '/openboxes/person/list',
        },
      },
    },
  },
};

export default fr;
