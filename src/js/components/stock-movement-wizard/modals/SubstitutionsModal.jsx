import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { reduxForm, formValueSelector, change, arrayRemove, arrayInsert } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import { renderFormField } from '../../../utils/form-utils';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';

const FIELDS = {
  substitutions: {
    type: ArrayField,
    disableVirtualization: true,
    fields: {
      productCode: {
        type: LabelField,
        label: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'Product',
      },
      minExpirationDate: {
        type: LabelField,
        label: 'Expiry Date',
      },
      quantityAvailable: {
        type: LabelField,
        label: 'Qty Available',
      },
      quantitySelected: {
        type: TextField,
        label: 'Qty Selected',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

/* eslint no-param-reassign: "error" */
class SubstitutionsModal extends Component {
  constructor(props) {
    super(props);
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      attr,
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  onOpen() {
    this.props.change(
      'substitution-form',
      'substitutions',
      this.state.attr.lineItem.substitutionItems,
    );
  }

  onSave() {
    this.props.showSpinner();
    const substitutions = _.filter(this.props.substitutions, sub => sub.quantitySelected > 0);
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}`;
    const payload = {
      lineItems: _.map(substitutions, sub => ({
        id: this.state.attr.lineItem.requisitionItemId,
        substitute: 'true',
        'newProduct.id': sub.productId,
        newQuantity: sub.quantitySelected,
        // TODO: reasonCode field on modal
        reasonCode: 'SUB',
      })),
    };

    return apiClient.post(url, payload).then((resp) => {
      const substitutedItem = _.find(
        resp.data.data.lineItems,
        item => item.id === this.state.attr.lineItem.requisitionItemId,
      );
      const newEditPageItem = {
        ...substitutedItem,
        // hide reason code for crossed out
        reasonCode: '',
        productName: substitutedItem.product.name,
        substitutions: _.map(substitutedItem.substitutionItems, sub => ({
          ...sub,
          productName: sub.product.name,
        })),
      };

      this.props.arrayRemove('stock-movement-wizard', 'editPageItems', this.props.rowIndex);
      this.props.arrayInsert('stock-movement-wizard', 'editPageItems', this.props.rowIndex, newEditPageItem);
      this.props.rewriteTable();

      this.props.hideSpinner();
    }).catch(() => { this.props.hideSpinner(); });
  }

  calculateRemaining() {
    return _.reduce(this.props.substitutions, (sum, val) =>
      (sum + (val.quantitySelected ? _.toInteger(val.quantitySelected) : 0)), 0);
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        btnSaveDisabled={this.props.invalid}
      >
        <form value={this.state.attr.productCode}>
          <div className="font-weight-bold">Product Code: {this.state.attr.lineItem.productCode}</div>
          <div className="font-weight-bold">Product Name: {this.state.attr.lineItem.productName}</div>
          <div className="font-weight-bold">Quantity Requested: {this.state.attr.lineItem.quantityRequested}</div>
          <div className="font-weight-bold pb-2">Quantity Remaining: {this.calculateRemaining()}</div>
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
        </form>
      </ModalWrapper>
    );
  }
}

function validate(values) {
  const errors = {};
  errors.substitutions = [];

  _.forEach(values.substitutions, (item, key) => {
    if (item.quantitySelected > item.quantityAvailable) {
      errors.substitutions[key] = { quantitySelected: 'Selected quantity is higher than available' };
    }
  });
  return errors;
}

const substitutionSelector = formValueSelector('substitution-form');

const mapStateToProps = state => ({
  substitutions: substitutionSelector(state, 'substitutions'),
});

export default reduxForm({
  form: 'substitution-form',
  validate,
})(connect(mapStateToProps, {
  change, showSpinner, hideSpinner, arrayRemove, arrayInsert,
})(SubstitutionsModal));

SubstitutionsModal.propTypes = {
  initialize: PropTypes.func.isRequired,
  invalid: PropTypes.bool.isRequired,
  change: PropTypes.func.isRequired,
  substitutions: PropTypes.arrayOf(PropTypes.shape({})),
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  stockMovementId: PropTypes.string.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  arrayRemove: PropTypes.func.isRequired,
  arrayInsert: PropTypes.func.isRequired,
  rewriteTable: PropTypes.func.isRequired,
  rowIndex: PropTypes.number.isRequired,
};

SubstitutionsModal.defaultProps = {
  substitutions: [],
};
