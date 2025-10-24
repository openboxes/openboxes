import translate from 'utils/Translate';

export const getExpiredStockOptions = () => ([
  {
    id: '0',
    label: translate({
      id: 'react.report.reorder.removeExpiredStock.label',
      defaultMessage: 'Remove expired stock',
    }),
  },
  {
    id: '1',
    label: translate({
      id: 'react.report.reorder.includeExpiredStock.label',
      defaultMessage: 'Include expired stock',
    }),
  },
  {
    id: '2',
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 30 days',
      data: {
        days: '30',
      },
    }),
  },
  {
    id: '3',
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 90 days',
      data: {
        days: '90',
      },
    }),
  },
  {
    id: '4',
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 180 days',
      data: {
        days: '180',
      },
    }),
  },
  {
    id: '5',
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 365 days',
      data: {
        days: '365',
      },
    }),
  },
]);

export const getFilterProductOptions = () => ([
  {
    id: '0',
    label: translate({
      id: 'react.report.reorder.showAllProducts.label',
      defaultMessage: 'Show all products',
    }),
  },
  {
    id: '1',
    label: translate({
      id: 'react.report.reorder.showProductsBelowReorder.label',
      defaultMessage: 'Show products below reorder',
    }),
  },
  {
    id: '2',
    label: translate({
      id: 'react.report.reorder.showProductsBelowMaximum.label',
      defaultMessage: 'Show products below maximum',
    }),
  },
  {
    id: '3',
    label: translate({
      id: 'react.report.reorder.showProductsBelowMinimum.label',
      defaultMessage: 'Show products below minimum',
    }),
  },
]);
