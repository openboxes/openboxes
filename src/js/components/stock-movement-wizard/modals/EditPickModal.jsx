import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { change, reduxForm, formValueSelector } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import ValueSelectorField from '../../form-elements/ValueSelectorField';
import LabelField from '../../form-elements/LabelField';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import { renderFormField } from '../../../utils/form-utils';
import SelectField from '../../form-elements/SelectField';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner, fetchReasonCodes } from '../../../actions';

const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: 'Reason code',
    getDynamicAttr: props => ({
      options: props.reasonCodes,
    }),
  },
  availableItems: {
    type: ArrayField,
    disableVirtualization: true,
    fields: {
      lotNumber: {
        type: LabelField,
        label: 'Lot #',
      },
      expirationDate: {
        type: LabelField,
        label: 'Expiry Date',
      },
      'binLocation.name': {
        type: LabelField,
        label: 'Bin',
      },
      recipient: {
        type: ValueSelectorField,
        label: 'Includes recipient',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `availableItems[${rowIndex}].recipient`,
        }),
        component: LabelField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue }) => ({
            className: selectedValue ? 'fa fa-user' : '',
          }),
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'Qty available',
        fixedWidth: '150px',
      },
      quantityPicked: {
        type: TextField,
        label: 'Qty picked',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

/* eslint no-param-reassign: "error" */
class EditPickModal extends Component {
  constructor(props) {
    super(props);

    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = { attr };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  onOpen() {
    this.props.change(
      'stock-movement-wizard', 'availableItems',
      this.state.attr.fieldValue.availableItems,
    );
    this.props.change('stock-movement-wizard', 'reasonCode', '');
    // for validation purposes
    this.props.change('stock-movement-wizard', 'quantityRequired', this.state.attr.fieldValue.quantityRequired);
  }

  onSave(values) {
    this.props.showSpinner();

    const url = `/openboxes/api/stockMovementItems/${this.state.attr.fieldValue['requisitionItem.id']}`;
    const payload = {
      picklistItems: _.map(values.availableItems, (avItem) => {
        // check if this picklist item already exists
        const picklistItem = _.find(
          _.filter(this.state.attr.fieldValue.picklistItems, listItem => !listItem.initial),
          item => item['inventoryItem.id'] === avItem['inventoryItem.id'],
        );
        if (picklistItem) {
          return {
            id: picklistItem.id,
            'inventoryItem.id': avItem['inventoryItem.id'],
            'binLocation.id': avItem['binLocation.id'] || '',
            quantityPicked: avItem.quantityPicked,
            reasonCode: values.reasonCode || '',
          };
        }
        return {
          'inventoryItem.id': avItem['inventoryItem.id'],
          'binLocation.id': avItem['binLocation.id'] || '',
          quantityPicked: avItem.quantityPicked,
          reasonCode: values.reasonCode || '',
        };
      }),
    };

    return apiClient.post(url, payload).then(() => {
      apiClient.get(`/openboxes/api/stockMovements/${this.state.attr.stockMovementId}?stepNumber=4`)
        .then((resp) => {
          const { pickPageItems } = resp.data.data.pickPage;
          this.props.change('stock-movement-wizard', 'pickPageItems', []);
          this.props.change('stock-movement-wizard', 'pickPageItems', this.state.attr.checkForInitialPicksChanges(pickPageItems));

          this.props.hideSpinner();
        })
        .catch(() => { this.props.hideSpinner(); });
    }).catch(() => { this.props.hideSpinner(); });
  }

  calculatePicked() {
    return _.reduce(this.props.availableItems, (sum, val) =>
      (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)), 0);
  }

  render() {
    if (this.state.attr.subfield) {
      return null;
    }

    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.props.handleSubmit(values => this.onSave(values))}
        btnSaveDisabled={this.props.invalid}
      >
        <form className="print-mt">
          <div className="font-weight-bold">Product Code: {this.state.attr.fieldValue.productCode}</div>
          <div className="font-weight-bold">Product Name: {this.state.attr.fieldValue['product.name']}</div>
          <div className="font-weight-bold">Quantity Required: {this.state.attr.fieldValue.quantityRequired}</div>
          <div className="font-weight-bold pb-2">Quantity Picked: {this.calculatePicked()}</div>
          <hr />
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
            reasonCodes: this.props.reasonCodes,
          }))}
        </form>
      </ModalWrapper>
    );
  }
}

function validate(values) {
  const errors = {};
  errors.availableItems = [];
  _.forEach(values.availableItems, (item, key) => {
    if (item.quantityPicked > item.quantityAvailable) {
      errors.availableItems[key] = { quantityPicked: 'Picked quantity is higher than available' };
    }
  });

  const pickedSum = _.reduce(
    values.availableItems, (sum, val) =>
      (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)),
    0,
  );


  if (_.some(values.availableItems, val => !_.isNil(val.quantityPicked)) &&
    !values.reasonCode && pickedSum !== values.quantityRequired) {
    errors.reasonCode = 'Total quantity picked is different than required! Add reason code!';
  }

  return errors;
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  availableItems: selector(state, 'availableItems'),
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(mapStateToProps, {
  change, fetchReasonCodes, showSpinner, hideSpinner,
})(EditPickModal));

EditPickModal.propTypes = {
  change: PropTypes.func.isRequired,
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  availableItems: PropTypes.arrayOf(PropTypes.shape({})),
  rowIndex: PropTypes.number.isRequired,
  invalid: PropTypes.bool.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func.isRequired,
  fetchReasonCodes: PropTypes.func.isRequired,
  reasonCodesFetched: PropTypes.bool.isRequired,
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};

EditPickModal.defaultProps = {
  availableItems: [],
};
