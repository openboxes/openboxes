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
    type: ({ prevPage, onSave, saveDisabled }) => (
      <div className="mb-1 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form btn-xs" onClick={prevPage}>
          Back to Edit
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={onSave}
          disabled={saveDisabled}
        >Save
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form btn-xs"
          disabled={saveDisabled}
        >Receive shipment
        </button>
      </div>),
  },
  containers: {
    type: ArrayField,
    maxTableHeight: window.innerWidth > 1440 ? 'calc(100vh - 450px)' : 'calc(100vh - 250px)',
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'shipmentItems',
    fields: {
      'parentContainer.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'Pallet',
        flexWidth: '8',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') || _.get(fieldValue, 'container.name') || 'Unpacked'),
        },
      },
      'container.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'Box',
        flexWidth: '6',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') ? _.get(fieldValue, 'container.name') || '' : ''),
        },
      },
      'product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Code',
      },
      'product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Product',
        flexWidth: '24',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      lotNumber: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Lot/Serial No',
      },
      expirationDate: {
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
          className: fieldValue.cancelRemaining || !fieldValue.quantityRemaining ? 'strike-through' : 'text-danger',
        }),
      },
      cancelRemaining: {
        type: params => (params.subfield ? <CheckboxField {...params} /> : null),
        label: 'Cancel Remaining',
        fixedWidth: '140px',
        getDynamicAttr: ({ saveDisabled }) => ({
          disabled: saveDisabled,
        }),
      },
      comment: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Comment',
        fixedWidth: '110px',
      },
    },
  },
  buttonsBottom: {
    // eslint-disable-next-line react/prop-types
    type: ({ prevPage, onSave, saveDisabled }) => (
      <div className="my-1 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form btn-xs" onClick={prevPage}>
          Back to Edit
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-xs"
          onClick={onSave}
          disabled={saveDisabled}
        >Save
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form btn-xs"
          disabled={saveDisabled}
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
            saveDisabled: this.props.completed || !_.size(this.props.formValues.containers),
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
  formValues: PropTypes.shape({
    containers: PropTypes.arrayOf(PropTypes.shape({})),
  }),
  /** Indicator if partial receiving has been completed */
  completed: PropTypes.bool,
};

ReceivingCheckScreen.defaultProps = {
  formValues: {},
  completed: false,
};
