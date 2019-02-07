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
import Translate from '../../../utils/Translate';

const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: 'stockMovement.reasonFor.label',
    defaultMessage: 'Reason for not fulfilling full qty',
    attributes: {
      required: true,
    },
    getDynamicAttr: props => ({
      options: props.reasonCodes,
      hidden: !props.originalItem,
    }),
  },
  substitutions: {
    type: ArrayField,
    getDynamicRowAttr: ({ rowValues, originalItem }) => {
      let className = '';
      const rowDate = new Date(rowValues.minExpirationDate);
      const origDate = originalItem && originalItem.minExpirationDate ?
        new Date(originalItem.minExpirationDate) : null;
      if (!rowValues.originalItem) {
        className = (origDate && rowDate && rowDate < origDate) || (!origDate && rowDate) ? 'text-danger' : '';
      } else {
        className = 'font-weight-bold';
      }
      return { className };
    },
    fields: {
      productCode: {
        type: LabelField,
        label: 'stockMovement.code.label',
        defaultMessage: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'stockMovement.productName.label',
        defaultMessage: 'Product name',
      },
      minExpirationDate: {
        type: LabelField,
        label: 'stockMovement.expiry.label',
        defaultMessage: 'Expiry',
      },
      quantityAvailable: {
        type: LabelField,
        label: 'stockMovement.quantityAvailable.label',
        defaultMessage: 'Qty Available',
        fixedWidth: '150px',
        fieldKey: '',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'quantityAvailable') ? _.get(fieldValue, 'quantityAvailable').toLocaleString('en-US') : null),
          showValueTooltip: true,
        },
        getDynamicAttr: ({ fieldValue }) => ({
          tooltipValue: _.map(fieldValue.availableItems, availableItem =>
            (
              <p>{fieldValue.productCode} {fieldValue.productName}, {availableItem.expirationDate ? availableItem.expirationDate : '---'}, Qty {availableItem.quantityAvailable}</p>
            )),
        }),
      },
      quantitySelected: {
        type: TextField,
        label: 'stockMovement.quantitySelected.label',
        defaultMessage: 'Quantity selected',
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
  let originalItem = null;
  let subQty = 0;

  _.forEach(values.substitutions, (item, key) => {
    if (item.originalItem) {
      originalItem = item;
    }
    if (item.quantitySelected) {
      subQty += _.toInteger(item.quantitySelected);
    }

    if (item.quantitySelected > item.quantityAvailable) {
      errors.substitutions[key] = { quantitySelected: 'errors.higherQtySelected.label' };
    }
    if (item.quantitySelected < 0) {
      errors.substitutions[key] = { quantitySelected: 'errors.negativeQtySelected.label' };
    }
  });

  if (originalItem && originalItem.quantitySelected && subQty < originalItem.quantityRequested
    && !values.reasonCode) {
    errors.reasonCode = 'error.requiredField.label';
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
      originalItem: null,
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  componentDidMount() {
    if (!this.props.reasonCodesFetched) {
      this.fetchData(this.props.fetchReasonCodes);
    }
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  /** Loads available substitutions for chosen item into modal's form.
   * @public
   */
  onOpen() {
    this.state.attr.onOpen();
    let substitutions = this.state.attr.lineItem.availableSubstitutions;
    let originalItem = null;

    if (_.toInteger(this.state.attr.lineItem.quantityAvailable) > 0) {
      originalItem = { ...this.state.attr.lineItem, originalItem: true };
      substitutions = [
        originalItem,
        ...this.state.attr.lineItem.availableSubstitutions,
      ];
    }

    this.setState({
      formValues: {
        substitutions,
        reasonCode: originalItem ? '' : 'SUBSTITUTION',
      },
      originalItem,
    });
  }

  /** Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();
    const substitutions = _.filter(values.substitutions, sub => sub.quantitySelected > 0);
    const subQty = _.reduce(values.substitutions, (sum, val) =>
      (sum + (!val.originalItem ? _.toInteger(val.quantitySelected) : 0)), 0);
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}?stepNumber=3`;
    const payload = {
      lineItems: _.map(substitutions, sub => ({
        id: this.state.attr.lineItem.requisitionItemId,
        substitute: 'true',
        'newProduct.id': sub.productId,
        newQuantity: sub.originalItem ? sub.quantityRequested - subQty : sub.quantitySelected,
        quantityRevised: sub.originalItem ? sub.quantitySelected : '',
        reasonCode: sub.originalItem ? values.reasonCode : 'SUBSTITUTION',
        sortOrder: this.state.attr.lineItem.sortOrder,
      })),
    };

    return apiClient.post(url, payload).then((resp) => {
      const { editPageItems } = resp.data.data.editPage;
      this.props.onResponse(editPageItems);
      this.props.hideSpinner();
    }).catch(() => { this.props.hideSpinner(); });
  }

  /**
   * Fetches data using function given as an argument(reducers components).
   * @param {function} fetchFunction
   * @public
   */
  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  /** Sums up quantity selected from all available substitutions.
   * @param {object} values
   * @public
   */
  /* eslint-disable-next-line class-methods-use-this */
  calculateSelected(values) {
    return (
      <div>
        <div className="font-weight-bold pb-2">
          <Translate id="stockMovement.quantitySelected.label" defaultMessage="Quantity selected" />: {_.reduce(values.substitutions, (sum, val) =>
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
        formProps={{
          reasonCodes: this.props.reasonCodes,
          originalItem: this.state.originalItem,
        }}
        renderBodyWithValues={this.calculateSelected}
      >
        <div>
          <div className="font-weight-bold">
            <Translate id="stockMovement.productCode.label" defaultMessage="Product code" />: {this.state.attr.lineItem.productCode}
          </div>
          <div className="font-weight-bold">
            <Translate id="stockMovement.productName.label" defaultMessage="Product name" />: {this.state.attr.lineItem.productName}
          </div>
          <div className="font-weight-bold">
            <Translate id="stockMovement.quantityRequested.label" defaultMessage="Qty Requested" />: {this.state.attr.lineItem.quantityRequested}
          </div>
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
