import React from 'react';

import _ from 'lodash';
import { Tooltip } from 'react-tippy';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import renderHandlingIcons from 'utils/product-handling-icons';

export const getInvoiceDescription = (rowValue) => {
  if (!rowValue?.orderAdjustment && !rowValue?.isAdjustment && rowValue?.displayNames?.default) {
    return (
      <Tooltip
        html={rowValue?.productName}
        theme="transparent"
        delay="150"
        duration="250"
        hideDelay="50"
      >
        {rowValue.displayNames?.default}
      </Tooltip>
    );
  }
  return rowValue?.description;
};

export const formatProductSupplierSubtext = (productSupplier) => (
  productSupplier?.name ? `(source: ${productSupplier?.name})` : null
);

export const formatProductDisplayName = (rowValue) => (
  <div className="d-flex">
    <span className="text-truncate">
      {rowValue?.displayName || rowValue?.displayNames?.default || rowValue?.name}
    </span>
    {renderHandlingIcons(rowValue?.handlingIcons)}
  </div>
);

export const getReceivingPayloadContainers = (formValues) =>
  _.map(formValues.containers, (container) => ({
    ...container,
    shipmentItems: _.map(container.shipmentItems, (item) => {
      if (!_.get(item, 'recipient.id')) {
        return _.omit({
          ...item, recipient: '',
        }, 'product.displayNames');
      }
      /** We have to omit product.displayNames, due to an error
       *  while binding bindData(partialReceiptItem, shipmentItemMap)
       *  it expects product.displayNames to have a setter, as we pass
       *  product.displayNames.default: XYZ, to the update method, but it's not a
       *  writable property.
       *  With deprecated product.translatedName it was not the case, because
       *  it was recognizing the transient, and we didn't access product.translatedName.something
       *  but product.translatedName directly
       * */
      return _.omit(item, 'product.displayNames');
    }),
  }));

export const matchesProductCodeOrName = ({
  product, filterValue,
}) => {
  if (!product?.id) {
    return false;
  }
  const { productCode, name, displayNames } = product;
  const value = filterValue?.toLowerCase();
  return (productCode?.toLowerCase()?.includes(value)
    || name?.toLowerCase()?.includes(value)
    || displayNames?.default?.toLowerCase()?.includes(value)
  );
};

export const showOutboundEditValidationErrors = ({ translate, errors }) => {
  const errorMessage = `${translate('react.stockMovement.errors.errorInLine.label', 'Error occurred in line')}:`;
  const errorDetails = errors.reduce((acc, message, key) => [
    ...acc,
    `${message && `${key + 1} - ${_.map(message, (val) => translate(`${val}`))}`}`,
  ], []);

  notification(NotificationType.ERROR_OUTLINED)({
    message: errorMessage,
    detailsArray: errorDetails,
  });
};

export const omitEmptyValues = (values) => _.omitBy(values, (val) => {
  // Do not omit boolean, numbers and date values
  if (typeof val === 'boolean' || typeof val === 'number' || val instanceof Date) {
    return false;
  }
  return _.isEmpty(val);
});

export const mapStringToLimitedList = (value, elementsSeparator, lengthLimit) =>
  (value?.length > lengthLimit ? `${_.take(value, lengthLimit).join('')}...` : value)?.split(elementsSeparator);

/**
 * Checks whether any of the provided values matches the filter value (case-insensitive).
 * Used to determine row visibility when a text filter is applied to a list.
 *
 * @param {Object} params
 * @param {string} params.filterValue - The text to search for.
 * @param {Array<string|function>} [params.matchers=[]] - The list of matchers to apply.
 * Each entry can be a string (e.g. lot number, bin location) or a matcher function
 * that receives filterValue and returns a boolean.
 * @returns {boolean} True if at least one value contains the filter text, false otherwise.
 */
export const matchesItemFilter = ({ filterValue, matchers = [] }) => {
  const trimmedFilterValue = filterValue?.trim();
  return matchers.some((value) => {
    if (typeof value === 'function') {
      return value(trimmedFilterValue);
    }
    return value?.toLowerCase()?.includes(trimmedFilterValue?.toLowerCase());
  });
};

export const formatBinLocation = (binLocation) => (
  binLocation?.zoneName
    ? `${binLocation.zoneName}: ${binLocation.name}`
    : binLocation?.name
);
