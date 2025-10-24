import translate from 'utils/Translate';

export const getExpiredStockOptions = () => ([
  {
    id: 'REMOVE_EXPIRED_STOCK',
    label: translate({
      id: 'react.report.reorder.removeExpiredStock.label',
      defaultMessage: 'Remove expired stock',
    }),
  },
  {
    id: 'INCLUDE_EXPIRED_STOCK',
    label: translate({
      id: 'react.report.reorder.includeExpiredStock.label',
      defaultMessage: 'Include expired stock',
    }),
  },
  {
    id: 'EXPIRING_WITHIN_MONTH',
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 30 days',
      data: {
        days: '30',
      },
    }),
  },
  {
    id: 'EXPIRING_WITHIN_QUARTER',
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 90 days',
      data: {
        days: '90',
      },
    }),
  },
  {
    id: 'EXPIRING_WITHIN_HALF_YEAR',
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 180 days',
      data: {
        days: '180',
      },
    }),
  },
  {
    id: 'EXPIRING_WITHIN_YEAR',
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
    id: 'IN_STOCK',
    label: translate({
      id: 'react.report.reorder.showAllProducts.label',
      defaultMessage: 'Show all products',
    }),
  },
  {
    id: 'BELOW_REORDER',
    label: translate({
      id: 'react.report.reorder.showProductsBelowReorder.label',
      defaultMessage: 'Show products below reorder',
    }),
  },
  {
    id: 'BELOW_MAXIMUM',
    label: translate({
      id: 'react.report.reorder.showProductsBelowMaximum.label',
      defaultMessage: 'Show products below maximum',
    }),
  },
  {
    id: 'BELOW_MINIMUM',
    label: translate({
      id: 'react.report.reorder.showProductsBelowMinimum.label',
      defaultMessage: 'Show products below minimum',
    }),
  },
]);
