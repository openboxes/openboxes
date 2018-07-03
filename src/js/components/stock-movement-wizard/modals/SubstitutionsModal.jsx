import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { reduxForm, formValueSelector, change } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import { renderFormField } from '../../../utils/form-utils';
import { SUBSTITUTIONS_MOCKS } from '../../../mockedData';

const FIELDS = {
  substitutions: {
    type: ArrayField,
    disableVirtualization: true,
    fields: {
      product: {
        type: LabelField,
        label: 'Description',
        attributes: {
          formatValue: value => (value.name),
        },
      },
      substitutionExpiryDate: {
        type: LabelField,
        label: 'Expiry Date',
      },
      maxQuantity: {
        type: LabelField,
        label: 'Qty Available',
      },
      quantity: {
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
    let substituteAvailable;
    if (_.isEmpty(SUBSTITUTIONS_MOCKS[attr.productCode])) {
      substituteAvailable = false;
    } else {
      substituteAvailable = true;
    }

    this.state = {
      attr, substituteAvailable,
    };
    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  onOpen() {
    const substitutions = SUBSTITUTIONS_MOCKS[this.state.attr.productCode];
    this.props.change('substitution-form', 'substitutions', substitutions);
  }

  onSave() {
    const newLineItems = _.cloneDeep(this.props.lineItems);

    newLineItems[this.state.attr.rowIndex].substituted = true;

    _.forEach(
      _.filter(this.props.substitutions, sub => !_.isEmpty(sub.quantity)),
      (sub) => {
        const subCopy = _.cloneDeep(sub);
        subCopy.revisedQuantity = sub.quantity;
        subCopy.rowKey = _.uniqueId('lineItem_');

        newLineItems.splice(
          this.state.attr.rowIndex + 1,
          0, subCopy,
        );
      },
    );
    this.props.change('stock-movement-wizard', 'lineItems', newLineItems);
  }

  render() {
    if (this.state.substituteAvailable === false) {
      return (
        <button
          type="button"
          className="disabled btn btn-outline-secondary"
        >
          No
        </button>
      );
    }

    return (

      <ModalWrapper
        {...this.state.attr}
        btnOpenClassName="btn btn-outline-success"
        onOpen={this.onOpen}
        onSave={this.onSave}
        btnOpenDisabled={this.props.lineItems[this.state.attr.rowIndex].substituted}
        btnSaveDisabled={this.props.invalid}
      >
        <form value={this.state.attr.productCode}>
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
    if (item.quantity > item.maxQuantity) {
      errors.substitutions[key] = { quantity: 'Selected quantity is higher than available' };
    }
  });
  return errors;
}

const stockMovementSelector = formValueSelector('stock-movement-wizard');
const substitutionSelector = formValueSelector('substitution-form');

const mapStateToProps = state => ({
  lineItems: stockMovementSelector(state, 'lineItems'),
  substitutions: substitutionSelector(state, 'substitutions'),
});

export default reduxForm({
  form: 'substitution-form',
  validate,
})(connect(mapStateToProps, { change })(SubstitutionsModal));

SubstitutionsModal.propTypes = {
  initialize: PropTypes.func.isRequired,
  invalid: PropTypes.bool.isRequired,
  change: PropTypes.func.isRequired,
  lineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  substitutions: PropTypes.arrayOf(PropTypes.shape({})),
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};

SubstitutionsModal.defaultProps = {
  substitutions: [],
};
