import _ from 'lodash';
import React, { Component } from 'react';
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
  buttonsTop: {
    // eslint-disable-next-line react/prop-types
    type: ({ prevPage, onSave, completed }) => (
      <div className="mb-3 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form" onClick={prevPage}>
          Back to Edit
        </button>
        <button
          type="button"
          className="btn btn-outline-success margin-bottom-lg btn-form"
          onClick={onSave}
          disabled={completed}
        >Save
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form"
          disabled={completed}
        >Receive shipment
        </button>
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
        flexWidth: '18',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      'inventoryItem.lotNumber': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Lot/Serial No',
      },
      'inventoryItem.expirationDate': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Expiration Date',
        fixedWidth: '130px',
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
        fixedWidth: '115px',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityRemaining: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Remaining',
        fixedWidth: '95px',
        fieldKey: '',
        attributes: {
          formatValue: fieldValue => (fieldValue.quantityRemaining ? fieldValue.quantityRemaining.toLocaleString('en-US') : fieldValue.quantityRemaining),
        },
        getDynamicAttr: ({ fieldValue }) => ({
          className: fieldValue.cancelRemaining ? 'strike-through' : 'text-danger',
        }),
      },
      cancelRemaining: {
        type: params => (params.subfield ? <CheckboxField {...params} /> : null),
        label: 'Cancel Remaining',
        fixedWidth: '140px',
        getDynamicAttr: ({ completed }) => ({
          disabled: completed,
        }),
      },
    },
  },
  buttonsBottom: {
    // eslint-disable-next-line react/prop-types
    type: ({ prevPage, onSave, completed }) => (
      <div className="my-3 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form mt-4" onClick={prevPage}>
          Back to Edit
        </button>
        <button
          type="button"
          className="btn btn-outline-success margin-bottom-lg"
          onClick={onSave}
          disabled={completed}
        >Save
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form mt-4 mb-4"
          disabled={completed}
        >Receive shipment
        </button>
      </div>),
  },
};

/**
 * The second page of partial receiving where user can view all changes made during the
 * receiving process. The user can cancel quantities not received and finalize the receipt.
 */
class ReceivingCheckScreen extends Component {
  constructor(props) {
    super(props);

    this.onSave = this.onSave.bind(this);
  }

  /**
   * Calls save method.
   * @public
   */
  onSave() {
    this.props.save(this.props.formValues);
  }

  render() {
    return (
      <div>
        {_.map(FIELDS, (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, {
            prevPage: this.props.prevPage,
            onSave: this.onSave,
            completed: this.props.completed,
          }))}
      </div>
    );
  }
}

export default ReceivingCheckScreen;

ReceivingCheckScreen.propTypes = {
  /** Function returning user to the previous page */
  prevPage: PropTypes.func.isRequired,
  /** Function sending all changes mage by user to API and updating data */
  save: PropTypes.func.isRequired,
  /** All data in the form */
  formValues: PropTypes.shape({}),
  /** Indicator if partial receiving has been completed */
  completed: PropTypes.bool,
};

ReceivingCheckScreen.defaultProps = {
  formValues: {},
  completed: false,
};
