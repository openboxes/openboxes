import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import SelectField from '../../form-elements/SelectField';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner, fetchReasonCodes } from '../../../actions';

const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: 'Reason code',
    attributes: {
      required: true,
    },
    getDynamicAttr: props => ({
      options: props.reasonCodes,
    }),
  },
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
        fixedWidth: '150px',
        attributes: {
          formatValue: value => (value.toLocaleString('en-US')),
        },
      },
      quantitySelected: {
        type: TextField,
        label: 'Qty Selected',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.substitutions = [];

  _.forEach(values.substitutions, (item, key) => {
    if (item.quantitySelected > item.quantityAvailable) {
      errors.substitutions[key] = { quantitySelected: 'Selected quantity is higher than available' };
    }
  });

  if (!values.reasonCode) {
    errors.reasonCode = 'This field is required';
  }
  return errors;
}

/**
 * Modal window where user can choose substitution and it's quantity.
 * It is available only when there is a substitution for an item.
 */
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
      formValues: {},
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  /** Loads available substitutions for chosen item into modal's form.
   * @public
   */
  onOpen() {
    this.setState({
      formValues: {
        substitutions: this.state.attr.lineItem.availableSubstitutions,
        reasonCode: '',
      },
    });
  }

  /** Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();
    const substitutions = _.filter(values.substitutions, sub => sub.quantitySelected > 0);
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}?stepNumber=3`;
    const payload = {
      lineItems: _.map(substitutions, sub => ({
        id: this.state.attr.lineItem.requisitionItemId,
        substitute: 'true',
        'newProduct.id': sub.productId,
        newQuantity: sub.quantitySelected,
        reasonCode: values.reasonCode,
      })),
    };

    return apiClient.post(url, payload).then((resp) => {
      const { editPageItems } = resp.data.data.editPage;
      this.props.onResponse(editPageItems);
      this.props.hideSpinner();
    }).catch(() => { this.props.hideSpinner(); });
  }

  /** Sums up quantity selected from all available substitutions.
   * @param {object} values
   * @public
   */
  /* eslint-disable-next-line class-methods-use-this */
  calculateSelected(values) {
    return (
      <div>
        <div className="font-weight-bold pb-2">Quantity Selected: {_.reduce(values.substitutions, (sum, val) =>
          (sum + (val.quantitySelected ? _.toInteger(val.quantitySelected) : 0)), 0)
        }
        </div>
        <hr />
      </div>
    );
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        validate={validate}
        initialValues={this.state.formValues}
        formProps={{ reasonCodes: this.props.reasonCodes }}
        renderBodyWithValues={this.calculateSelected}
      >
        <div>
          <div className="font-weight-bold">Product Code: {this.state.attr.lineItem.productCode}</div>
          <div className="font-weight-bold">Product Name: {this.state.attr.lineItem.productName}</div>
          <div className="font-weight-bold">Quantity Requested: {this.state.attr.lineItem.quantityRequested}</div>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  reasonCodesFetched: state.reasonCodes.fetched,
  reasonCodes: state.reasonCodes.data,
});

export default connect(mapStateToProps, {
  fetchReasonCodes, showSpinner, hideSpinner,
})(SubstitutionsModal);

SubstitutionsModal.propTypes = {
  /** Name of the field */
  fieldName: PropTypes.string.isRequired,
  /** Configuration of the field */
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  /** Stock movement's ID */
  stockMovementId: PropTypes.string.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching reason codes */
  fetchReasonCodes: PropTypes.func.isRequired,
  /** Indicator if reason codes' data is fetched */
  reasonCodesFetched: PropTypes.bool.isRequired,
  /** Array of available reason codes */
  reasonCodes: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
};
