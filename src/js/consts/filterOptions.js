import translate from 'utils/Translate';

export const EXPIRATION_FILTER = {
  REMOVE_EXPIRED_STOCK: 'REMOVE_EXPIRED_STOCK',
  INCLUDE_EXPIRED_STOCK: 'INCLUDE_EXPIRED_STOCK',
  EXPIRING_WITHIN_MONTH: 'EXPIRING_WITHIN_MONTH',
  EXPIRING_WITHIN_QUARTER: 'EXPIRING_WITHIN_QUARTER',
  EXPIRING_WITHIN_HALF_YEAR: 'EXPIRING_WITHIN_HALF_YEAR',
  EXPIRING_WITHIN_YEAR: 'EXPIRING_WITHIN_YEAR',
};

export const INVENTORY_LEVEL_STATUS = {
  IN_STOCK: 'IN_STOCK',
  BELOW_REORDER: 'BELOW_REORDER',
  BELOW_MAXIMUM: 'BELOW_MAXIMUM',
  BELOW_MINIMUM: 'BELOW_MINIMUM',
};

export const getExpiredStockOptions = () => ([
  {
    id: EXPIRATION_FILTER.REMOVE_EXPIRED_STOCK,
    label: translate({
      id: 'react.report.reorder.removeExpiredStock.label',
      defaultMessage: 'Remove expired stock',
    }),
  },
  {
    id: EXPIRATION_FILTER.INCLUDE_EXPIRED_STOCK,
    label: translate({
      id: 'react.report.reorder.includeExpiredStock.label',
      defaultMessage: 'Include expired stock',
    }),
  },
  {
    id: EXPIRATION_FILTER.EXPIRING_WITHIN_MONTH,
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 30 days',
      data: {
        days: '30',
      },
    }),
  },
  {
    id: EXPIRATION_FILTER.EXPIRING_WITHIN_QUARTER,
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 90 days',
      data: {
        days: '90',
      },
    }),
  },
  {
    id: EXPIRATION_FILTER.EXPIRING_WITHIN_HALF_YEAR,
    label: translate({
      id: 'react.report.reorder.removeExpiringWithin.label',
      defaultMessage: 'Remove expiring within 180 days',
      data: {
        days: '180',
      },
    }),
  },
  {
    id: EXPIRATION_FILTER.EXPIRING_WITHIN_YEAR,
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
    id: INVENTORY_LEVEL_STATUS.IN_STOCK,
    label: translate({
      id: 'react.report.reorder.showAllProducts.label',
      defaultMessage: 'Show all products',
    }),
  },
  {
    id: INVENTORY_LEVEL_STATUS.BELOW_REORDER,
    label: translate({
      id: 'react.report.reorder.showProductsBelowReorder.label',
      defaultMessage: 'Show products below reorder',
    }),
  },
  {
    id: INVENTORY_LEVEL_STATUS.BELOW_MAXIMUM,
    label: translate({
      id: 'react.report.reorder.showProductsBelowMaximum.label',
      defaultMessage: 'Show products below maximum',
    }),
  },
  {
    id: INVENTORY_LEVEL_STATUS.BELOW_MINIMUM,
    label: translate({
      id: 'react.report.reorder.showProductsBelowMinimum.label',
      defaultMessage: 'Show products below minimum',
    }),
  },
]);
