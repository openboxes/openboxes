const en = {
  navbar: {
    dashboard: {
      label: 'Dashboard',
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
      label: 'Receiving',
      link: '#',
      subsections: {
        listPutAways: {
          label: 'List Put Aways',
          link: '/openboxes/putAway/list',
        },
        createPutAway: {
          label: 'Create Put Away',
          link: '/openboxes/putAway/index',
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
      },
    },
    configuration: {
      label: 'Configuration',
      link: '#',
      subsections: {
        person: {
          label: 'People',
          link: '/openboxes/person/list',
        },
      },
    },
  },
};

export default en;
