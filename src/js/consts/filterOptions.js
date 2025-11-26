import translate from 'utils/Translate';

export const EXPIRATION_FILTER = {
  SUBTRACT_EXPIRED_STOCK: 'SUBTRACT_EXPIRED_STOCK',
  DO_NOT_SUBTRACT_EXPIRED_STOCK: 'DO_NOT_SUBTRACT_EXPIRED_STOCK',
  SUBTRACT_EXPIRING_WITHIN_MONTH: 'SUBTRACT_EXPIRING_WITHIN_MONTH',
  SUBTRACT_EXPIRING_WITHIN_QUARTER: 'SUBTRACT_EXPIRING_WITHIN_QUARTER',
  SUBTRACT_EXPIRING_WITHIN_HALF_YEAR: 'SUBTRACT_EXPIRING_WITHIN_HALF_YEAR',
  SUBTRACT_EXPIRING_WITHIN_YEAR: 'SUBTRACT_EXPIRING_WITHIN_YEAR',
};

export const INVENTORY_LEVEL_STATUS = {
  ALL_PRODUCTS: 'ALL_PRODUCTS',
  BELOW_REORDER: 'BELOW_REORDER',
  BELOW_MAXIMUM: 'BELOW_MAXIMUM',
  BELOW_MINIMUM: 'BELOW_MINIMUM',
};

export const getExpiredStockOptions = () => ([
  {
    id: EXPIRATION_FILTER.SUBTRACT_EXPIRED_STOCK,
    label: translate({
      id: 'react.report.reorder.subtractExpiredStock.label',
      defaultMessage: 'Subtract expired stock',
    }),
  },
  {
    id: EXPIRATION_FILTER.DO_NOT_SUBTRACT_EXPIRED_STOCK,
    label: translate({
      id: 'react.report.reorder.doNotSubtractExpiredStock.label',
      defaultMessage: 'Do not subtract expired stock',
    }),
  },
  {
    id: EXPIRATION_FILTER.SUBTRACT_EXPIRING_WITHIN_MONTH,
    label: translate({
      id: 'react.report.reorder.subtractExpiringWithin.label',
      defaultMessage: 'Remove expiring within 30 days',
      data: {
        days: '30',
      },
    }),
  },
  {
    id: EXPIRATION_FILTER.SUBTRACT_EXPIRING_WITHIN_QUARTER,
    label: translate({
      id: 'react.report.reorder.subtractExpiringWithin.label',
      defaultMessage: 'Remove expiring within 90 days',
      data: {
        days: '90',
      },
    }),
  },
  {
    id: EXPIRATION_FILTER.SUBTRACT_EXPIRING_WITHIN_HALF_YEAR,
    label: translate({
      id: 'react.report.reorder.subtractExpiringWithin.label',
      defaultMessage: 'Remove expiring within 180 days',
      data: {
        days: '180',
      },
    }),
  },
  {
    id: EXPIRATION_FILTER.SUBTRACT_EXPIRING_WITHIN_YEAR,
    label: translate({
      id: 'react.report.reorder.subtractExpiringWithin.label',
      defaultMessage: 'Remove expiring within 365 days',
      data: {
        days: '365',
      },
    }),
  },
]);

export const getFilterProductOptions = () => ([
  {
    id: INVENTORY_LEVEL_STATUS.ALL_PRODUCTS,
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
