import _ from 'lodash';
import React, { Component } from 'react';
import { reduxForm, initialize, getFormValues } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';

import ArrayField from '../form-elements/ArrayField';
import CheckboxField from '../form-elements/CheckboxField';
import LabelField from '../form-elements/LabelField';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { renderFormField } from '../../utils/form-utils';
import apiClient, { flattenRequest, parseResponse } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

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
    type: ({ prevPage, onSave, completed }) => (
      <div className="mb-3 d-flex justify-content-center">
        <button type="button" className="btn btn-outline-primary mr-3" onClick={prevPage}>
          Back to Edit
        </button>
        <button
          type="button"
          className="btn btn-outline-primary mr-3"
          onClick={onSave}
          disabled={completed}
        >Save
        </button>
        <button
          type="submit"
          className="btn btn-outline-primary"
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
      },
      quantityRemaining: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Remaining',
        fixedWidth: '95px',
        getDynamicAttr: ({ fieldValue }) => ({
          className: !fieldValue ? '' : 'text-danger',
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
};

class ReceivingCheckScreen extends Component {
  constructor(props) {
    super(props);

    this.state = {
      completed: false,
    };

    this.onComplete = this.onComplete.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  onComplete(formValues) {
    this.save({ ...formValues, receiptStatus: 'COMPLETE' }, () => {
      this.setState({ completed: true });
      Alert.success('Shipment was received successfully!');
    });
  }

  onSave() {
    this.save(this.props.formValues);
  }

  save(formValues, callback) {
    this.props.showSpinner();
    const url = `/openboxes/api/partialReceiving/${this.props.shipmentId}`;

    return apiClient.post(url, flattenRequest(formValues))
      .then((response) => {
        this.props.hideSpinner();

        this.props.initialize('partial-receiving-wizard', {}, false);
        this.props.initialize('partial-receiving-wizard', parseResponse(response.data.data), false);
        if (callback) {
          callback();
        }
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const { handleSubmit } = this.props;
    return (
      <form onSubmit={handleSubmit(values => this.onComplete(values))}>
        {_.map(FIELDS, (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, {
            prevPage: this.props.prevPage,
            onSave: this.onSave,
            completed: this.state.completed,
          }))}
      </form>
    );
  }
}

const mapStateToProps = state => ({
  formValues: getFormValues('partial-receiving-wizard')(state),
});

export default reduxForm({
  form: 'partial-receiving-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(connect(mapStateToProps, { showSpinner, hideSpinner, initialize })(ReceivingCheckScreen));

ReceivingCheckScreen.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
  prevPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  initialize: PropTypes.func.isRequired,
  formValues: PropTypes.shape({}),
  shipmentId: PropTypes.string,
};

ReceivingCheckScreen.defaultProps = {
  formValues: {},
  shipmentId: '',
};
