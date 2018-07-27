import _ from 'lodash';
import React from 'react';
import { reduxForm } from 'redux-form';
import PropTypes from 'prop-types';

import ArrayField from '../form-elements/ArrayField';
import CheckboxField from '../form-elements/CheckboxField';
import LabelField from '../form-elements/LabelField';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { renderFormField } from '../../utils/form-utils';

const FIELDS = {
  'origin.name': {
    type: LabelField,
    label: 'Origin',
  },
  'destination.name': {
    type: LabelField,
    label: 'Destination',
  },
  dateShipped: {
    type: LabelField,
    label: 'Shipped On',
  },
  dateDelivered: {
    type: LabelField,
    label: 'Delivered On',
  },
  buttons: {
    // eslint-disable-next-line react/prop-types
    type: ({ prevPage }) => (
      <div className="mb-3 d-flex justify-content-center">
        <button type="button" className="btn btn-outline-primary mr-3" onClick={prevPage}>
          Back to Edit
        </button>
        <button type="button" className="btn btn-outline-primary mr-3">Save</button>
        <button type="submit" className="btn btn-outline-primary">Receive shipment</button>
      </div>),
  },
  containers: {
    type: ArrayField,
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'shipmentItems',
    fields: {
      'container.name': {
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'Packaging Unit',
        attributes: {
          formatValue: value => (value || 'Unpacked'),
        },
      },
      'product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Code',
      },
      'product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Product',
      },
      'inventoryItem.lotNumber': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Lot/Serial No',
      },
      'inventoryItem.expirationDate': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Expiration Date',
      },
      'binLocation.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Bin Location',
      },
      'recipient.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Recipient',
      },
      quantityReceiving: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Receiving Now',
      },
      quantityRemaining: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Unreceived',
        getDynamicAttr: ({ fieldValue }) => ({
          className: !fieldValue ? '' : 'text-danger',
        }),
      },
      cancelRemaining: {
        type: params => (params.subfield ? <CheckboxField {...params} /> : null),
        label: 'Cancel Remaining',
      },
    },
  },
};

const ReceivingCheckScreen = (props) => {
  const { handleSubmit } = props;
  return (
    <form onSubmit={handleSubmit}>
      {_.map(FIELDS, (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, { prevPage: props.prevPage }))}
    </form>
  );
};

export default reduxForm({
  form: 'partial-receiving-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(ReceivingCheckScreen);

ReceivingCheckScreen.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
  prevPage: PropTypes.func.isRequired,
};

ReceivingCheckScreen.defaultProps = {
  containers: [],
};
