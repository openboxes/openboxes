import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import update from 'immutability-helper';

import ArrayField from '../form-elements/ArrayField';
import CheckboxField from '../form-elements/CheckboxField';
import LabelField from '../form-elements/LabelField';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { renderFormField } from '../../utils/form-utils';
import Translate from '../../utils/Translate';

const FIELDS = {
  'origin.name': {
    type: LabelField,
    label: 'react.partialReceiving.origin.label',
    defaultMessage: 'Origin',
  },
  'destination.name': {
    type: LabelField,
    label: 'react.partialReceiving.destination.label',
    defaultMessage: 'Destination',
  },
  dateShipped: {
    type: LabelField,
    label: 'react.partialReceiving.shippedOn.label',
    defaultMessage: 'Shipped on',
  },
  dateDelivered: {
    type: LabelField,
    label: 'react.partialReceiving.deliveredOn.label',
    defaultMessage: 'Delivered on',
  },
  buttonsTop: {
    type: ({
      // eslint-disable-next-line react/prop-types
      prevPage, onSave, saveDisabled, saveAndExit, cancelAllRemaining,
    }) => (
      <div className="mb-1 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form btn-xs" onClick={prevPage}>
          <Translate id="react.partialReceiving.backToEdit.label" defaultMessage="Back to edit" />
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={saveAndExit}
          disabled={saveDisabled}
        >
          <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={onSave}
          disabled={saveDisabled}
        ><Translate id="react.default.button.save.label" defaultMessage="Save" />
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form btn-xs"
          disabled={saveDisabled}
        ><Translate id="react.partialReceiving.receiveShipment.label" defaultMessage="Receive shipment" />
        </button>
        <button
          type="button"
          className="btn btn-outline-danger float-right btn-form btn-xs"
          onClick={cancelAllRemaining}
          disabled={saveDisabled}
        ><Translate id="react.partialReceiving.cancelAllRemaining.label" defaultMessage="Cancel all remaining" />
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
        label: 'react.partialReceiving.packLevel1.label',
        defaultMessage: 'Pack level 1',
        flexWidth: '1',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') || _.get(fieldValue, 'container.name') || 'Unpacked'),
        },
      },
      'container.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.packLevel2.label',
        defaultMessage: 'Pack level 2',
        flexWidth: '1',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') ? _.get(fieldValue, 'container.name') || '' : ''),
        },
      },
      'product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.code.label',
        defaultMessage: 'Code',
        headerAlign: 'left',
        flexWidth: '1',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      'product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        flexWidth: '4',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      lotNumber: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.lotSerialNo.label',
        defaultMessage: 'Lot/Serial No.',
        flexWidth: '1',
      },
      expirationDate: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.expirationDate.label',
        defaultMessage: 'Expiration date',
        flexWidth: '1',
      },
      'binLocation.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.binLocation.label',
        defaultMessage: 'Bin Location',
        flexWidth: '1.5',
        getDynamicAttr: ({ hasBinLocationSupport }) => ({
          hide: !hasBinLocationSupport,
        }),
      },
      'recipient.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
      },
      quantityReceiving: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.receivingNow.label',
        defaultMessage: 'Receiving now',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityRemaining: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.remaining.label',
        defaultMessage: 'Remaining',
        flexWidth: '1',
        fieldKey: '',
        attributes: {
          formatValue: fieldValue => (fieldValue && fieldValue.quantityRemaining ? fieldValue.quantityRemaining.toLocaleString('en-US') : fieldValue.quantityRemaining),
        },
        getDynamicAttr: ({ fieldValue }) => ({
          className: fieldValue && (fieldValue.cancelRemaining || !fieldValue.quantityRemaining) ? 'strike-through' : 'text-danger',
        }),
      },
      cancelRemaining: {
        fieldKey: 'quantityRemaining',
        type: params => (params.subfield ? <CheckboxField {...params} /> : null),
        label: 'react.partialReceiving.cancelRemaining.label',
        defaultMessage: 'Cancel remaining',
        flexWidth: '1',
        getDynamicAttr: ({ saveDisabled, fieldValue }) => ({
          disabled: saveDisabled || _.toInteger(fieldValue) <= 0,
        }),
      },
      comment: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.comment.label',
        defaultMessage: 'Comment',
        flexWidth: '1',
      },
    },
  },
  buttonsBottom: {

    type: ({
    // eslint-disable-next-line react/prop-types
      prevPage, onSave, saveDisabled, saveAndExit, cancelAllRemaining,
    }) => (
      <div className="my-1 text-center">
        <button type="button" className="btn btn-outline-primary float-left btn-form btn-xs" onClick={prevPage}>
          <Translate id="react.partialReceiving.backToEdit.label" defaultMessage="Back to edit" />
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={saveAndExit}
          disabled={saveDisabled}
        >
          <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
        </button>
        <button
          type="button"
          className="btn btn-outline-success btn-form btn-xs"
          onClick={onSave}
          disabled={saveDisabled}
        ><Translate id="react.default.button.save.label" defaultMessage="Save" />
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary float-right btn-form btn-xs"
          disabled={saveDisabled}
        ><Translate id="react.partialReceiving.receiveShipment.label" defaultMessage="Receive shipment" />
        </button>
        <button
          type="button"
          className="btn btn-outline-danger float-right btn-form btn-xs"
          onClick={cancelAllRemaining}
          disabled={saveDisabled}
        ><Translate id="react.partialReceiving.cancelAllRemaining.label" defaultMessage="Cancel all remaining" />
        </button>
      </div>),
  },
};

/**
 * The second page of partial receiving where user can view all changes made during the
 * receiving process. The user can cancel quantities not received and finalize the receipt.
 */
class ReceivingCheckScreen extends Component {
  static cancelRemaining(shipmentItem) {
    return {
      ...shipmentItem,
      cancelRemaining: shipmentItem.quantityRemaining > 0,
    };
  }
  constructor(props) {
    super(props);

    this.onSave = this.onSave.bind(this);
    this.onExit = this.onExit.bind(this);
    this.cancelAllRemaining = this.cancelAllRemaining.bind(this);
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

  cancelAllRemaining() {
    const containers = update(this.props.formValues.containers, {
      $apply: items => (!items ? [] : items.map(item => update(item, {
        shipmentItems: {
          $apply: shipmentItems => (!shipmentItems ? [] : shipmentItems.map(shipmentItem =>
            ReceivingCheckScreen.cancelRemaining(shipmentItem))),
        },
      }))),
    });
    this.props.change('containers', containers);
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
            hasBinLocationSupport: this.props.hasBinLocationSupport,
            cancelAllRemaining: this.cancelAllRemaining,
          }))}
      </div>
    );
  }
}

const mapStateToProps = state => ({
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
});

export default connect(mapStateToProps)(ReceivingCheckScreen);

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
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  /** Function changing the value of a field in the Redux store */
  change: PropTypes.func.isRequired,
};

ReceivingCheckScreen.defaultProps = {
  formValues: {},
  completed: false,
};
