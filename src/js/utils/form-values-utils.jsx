import React from 'react';

import _ from 'lodash';
import { Tooltip } from 'react-tippy';

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

export const formatProductDisplayName = rowValue => (
  <div className="d-flex">
    <span className="text-truncate">
      {rowValue?.displayName || rowValue?.displayNames?.default || rowValue?.name}
    </span>
    {renderHandlingIcons(rowValue?.handlingIcons)}
  </div>);


export const getReceivingPayloadContainers = formValues =>
  _.map(formValues.containers, container => ({
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

/** Expects to have filterValue already in lower case */
export const matchesProductCodeOrName = ({
  productCode, productName, displayName, filterValue,
}) =>
  (productCode?.toLowerCase()?.includes(filterValue) ||
    productName?.toLowerCase()?.includes(filterValue) ||
      displayName?.toLowerCase()?.includes(filterValue)
  );
