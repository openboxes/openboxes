import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import ArrayField from '../form-elements/ArrayField';
import CheckboxField from '../form-elements/CheckboxField';
import LabelField from '../form-elements/LabelField';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { renderFormField } from '../../utils/form-utils';
import Translate from '../../utils/Translate';

const FIELDS = {
  'origin.name': {
    type: LabelField,
    label: 'stockMovement.origin.label',
    defaultMessage: 'Origin',
  },
  'destination.name': {
    type: LabelField,
    label: 'stockMovement.destination.label',
    defaultMessage: 'Destination',
  },
  dateShipped: {
    type: LabelField,
    label: 'partialReceiving.shippedOn.label',
    defaultMessage: 'Shipped on',
  },
  dateDelivered: {
    type: LabelField,
    label: 'partialReceiving.deliveredOn.label',
    defaultMessage: 'Delivered on',
  },
  buttonsTop: {
    type: ({
      // eslint-disable-next-line react/prop-types
      prevPage, onSave, saveDisabled, saveAndExit,
    }) => (
      <div className="mb-1 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form btn-xs" onClick={prevPage}>
          <Translate id="partialReceiving.backToEdit.label" defaultMessage="Back to edit" />
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={saveAndExit}
          disabled={saveDisabled}
        >
          <span><i className="fa fa-sign-out pr-2" /><Translate id="stockMovement.saveAndExit.label" defaultMessage="Save and exit" /></span>
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={onSave}
          disabled={saveDisabled}
        ><Translate id="default.button.save.label" defaultMessage="Save" />
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form btn-xs"
          disabled={saveDisabled}
        ><Translate id="partialReceiving.receiveShipment.label" defaultMessage="Receive shipment" />
        </button>
      </div>),
  },
  containers: {
    type: ArrayField,
    maxTableHeight: 'none',
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'shipmentItems',
    fields: {
      'parentContainer.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.pallet.label',
        defaultMessage: 'Pallet',
        flexWidth: '1',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') || _.get(fieldValue, 'container.name') || 'Unpacked'),
        },
      },
      'container.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.box.label',
        defaultMessage: 'Box',
        flexWidth: '1',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') ? _.get(fieldValue, 'container.name') || '' : ''),
        },
      },
      'product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.code.label',
        defaultMessage: 'Code',
        flexWidth: '1',
      },
      'product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.name.label',
        defaultMessage: 'Name',
        flexWidth: '4',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      lotNumber: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.lotSerialNo.label',
        defaultMessage: 'Lot/Serial No.',
        flexWidth: '1',
      },
      expirationDate: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.expirationDate.label',
        defaultMessage: 'Expiration date',
        flexWidth: '1',
      },
      'binLocation.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.binLocation.label',
        defaultMessage: 'Bin Location',
        flexWidth: '1.5',
      },
      'recipient.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
      },
      quantityReceiving: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'partialReceiving.receivingNow.label',
        defaultMessage: 'Receiving now',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityRemaining: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'partialReceiving.remaining.label',
        defaultMessage: 'Remaining',
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          formatValue: fieldValue => (fieldValue.quantityRemaining ? fieldValue.quantityRemaining.toLocaleString('en-US') : fieldValue.quantityRemaining),
        },
        getDynamicAttr: ({ fieldValue }) => ({
          className: fieldValue.cancelRemaining || !fieldValue.quantityRemaining ? 'strike-through' : 'text-danger',
        }),
      },
      cancelRemaining: {
        fieldKey: 'quantityRemaining',
        type: params => (params.subfield ? <CheckboxField {...params} /> : null),
        label: 'partialReceiving.cancelRemaining.label',
        defaultMessage: 'Cancel remaining',
        flexWidth: '1',
        getDynamicAttr: ({ saveDisabled, fieldValue }) => ({
          disabled: saveDisabled || _.toInteger(fieldValue) <= 0,
        }),
      },
      comment: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'partialReceiving.comment.label',
        defaultMessage: 'Comment',
        flexWidth: '1',
      },
    },
  },
  buttonsBottom: {

    type: ({
    // eslint-disable-next-line react/prop-types
      prevPage, onSave, saveDisabled, saveAndExit,
    }) => (
      <div className="my-1 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form btn-xs" onClick={prevPage}>
          <Translate id="partialReceiving.backToEdit.label" defaultMessage="Back to edit" />
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={saveAndExit}
          disabled={saveDisabled}
        >
          <span><i className="fa fa-sign-out pr-2" /><Translate id="stockMovement.saveAndExit.label" defaultMessage="Save and exit" /></span>
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={onSave}
          disabled={saveDisabled}
        ><Translate id="default.button.save.label" defaultMessage="Save" />
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form btn-xs"
          disabled={saveDisabled}
        ><Translate id="partialReceiving.receiveShipment.label" defaultMessage="Receive shipment" />
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
    this.onExit = this.onExit.bind(this);
  }

  /**
   * Calls save method.
   * @public
   */
  onSave() {
    this.props.save(this.props.formValues);
  }

  /**
   * Calls save and exit method.
   * @public
   */
  onExit() {
    this.props.saveAndExit(this.props.formValues);
  }

  render() {
    return (
      <div>
        {_.map(FIELDS, (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, {
            prevPage: this.props.prevPage,
            onSave: this.onSave,
            saveDisabled: this.props.completed || !_.size(this.props.formValues.containers),
            saveAndExit: this.onExit,
          }))}
      </div>
    );
  }
}

export default ReceivingCheckScreen;

ReceivingCheckScreen.propTypes = {
  /** Function returning user to the previous page */
  prevPage: PropTypes.func.isRequired,
  /** Function sending all changes made by user to API and updating data */
  save: PropTypes.func.isRequired,
  /** Function sending all changes made by user to API and redirect user to shipment page */
  saveAndExit: PropTypes.func.isRequired,
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
