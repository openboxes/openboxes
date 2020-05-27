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
import { showSpinner, hideSpinner } from '../../../actions';
import Translate from '../../../utils/Translate';
import { debounceAvailableItemsFetch } from '../../../utils/option-utils';

const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: 'react.stockMovement.reasonFor.label',
    defaultMessage: 'Reason for not fulfilling full qty',
    attributes: {
      required: true,
      className: 'mb-2',
    },
    getDynamicAttr: props => ({
      options: props.reasonCodes,
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
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({})}
      ><Translate id="react.default.button.addCustomSubstitution.label" defaultMessage="Add custom substitution" />
      </button>
    ),
    fields: {
      product: {
        fieldKey: 'disabled',
        type: SelectField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        flexWidth: '9.5',
        attributes: {
          async: true,
          openOnClick: false,
          autoload: false,
          filterOptions: options => options,
          cache: false,
          options: [],
          showValueTooltip: true,
          className: 'text-left',
          optionRenderer: option => <strong style={{ color: option.color ? option.color : 'black' }}>{option.label}</strong>,
        },
        getDynamicAttr: ({
          fieldValue, debouncedProductsFetch,
        }) => ({
          disabled: !!fieldValue,
          loadOptions: debouncedProductsFetch,
        }),
      },
      'product.minExpirationDate': {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        attributes: {
          showValueTooltip: true,
        },
      },
      'product.quantityAvailable': {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Qty Available',
        fixedWidth: '150px',
        fieldKey: '',
        attributes: {
          // eslint-disable-next-line no-nested-ternary
          formatValue: fieldValue => (_.get(fieldValue, 'quantityAvailable') ? _.get(fieldValue, 'quantityAvailable').toLocaleString('en-US') :
            _.get(fieldValue, 'product.quantityAvailable') ? _.get(fieldValue, 'product.quantityAvailable').toLocaleString('en-US') : null),
          showValueTooltip: true,
        },
        getDynamicAttr: ({ fieldValue }) => ({
          tooltipValue: _.map(fieldValue && fieldValue.availableItems, availableItem =>
            (
              <p>{fieldValue.productCode} {fieldValue.productName}, {availableItem.expirationDate ? availableItem.expirationDate : '---'}, Qty {availableItem.quantityAvailable}</p>
            )),
        }),
      },
      quantitySelected: {
        type: TextField,
        label: 'react.stockMovement.quantitySelected.label',
        defaultMessage: 'Quantity selected',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

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
    this.validate = this.validate.bind(this);

    this.debouncedProductsFetch = debounceAvailableItemsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
    );
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

    let substitutions = _.map(
      this.state.attr.lineItem.availableSubstitutions,
      val => ({
        ...val,
        disabled: true,
        product: {
          label: `${val.productCode} - ${val.productName}`,
          id: `${val.productId}`,
          productCode: `${val.productCode}`,
          name: `${val.productName}`,
          minExpirationDate: `${val.minExpirationDate}`,
          quantityAvailable: `${val.quantityAvailable}`,
        },
      }),
    );
    let originalItem = null;

    if (_.toInteger(this.state.attr.lineItem.quantityAvailable) > 0) {
      originalItem = {
        ...this.state.attr.lineItem,
        originalItem: true,
        product: {
          label: `${this.state.attr.lineItem.productCode} - ${this.state.attr.lineItem.productName}`,
          id: `${this.state.attr.lineItem.productId}`,
          productCode: `${this.state.attr.lineItem.productCode}`,
          name: `${this.state.attr.lineItem.productName}`,
          minExpirationDate: this.state.attr.lineItem.minExpirationDate,
          quantityAvailable: this.state.attr.lineItem.quantityAvailable,
        },
      };
      substitutions = [
        originalItem,
        ...substitutions,
      ];
    }

    this.setState({
      formValues: {
        substitutions,
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

    const substitutions = _.filter(values.substitutions, sub =>
      sub.quantitySelected > 0 && !sub.originalItem);
    const subQty = _.reduce(values.substitutions, (sum, val) =>
      (sum + (!val.originalItem ? _.toInteger(val.quantitySelected) : 0)), 0);
    const originalItem = _.find(values.substitutions, sub => sub.originalItem)
      || this.state.attr.lineItem;

    const url = `/openboxes/api/stockMovementItems/${originalItem.requisitionItemId}/substituteItem`;
    const payload = {
      newQuantity: originalItem.quantitySelected && originalItem.quantitySelected !== '0' ? originalItem.quantityRequested - subQty : '',
      quantityRevised: originalItem.quantitySelected,
      // Newly created substitution with the same product should have
      // higher sort order than other substitution items
      sortOrder: substitutions.length > 0 ?
        _.toInteger(originalItem.sortOrder) + substitutions.length :
        _.toInteger(originalItem.sortOrder) + 1,
      reasonCode: values.reasonCode,
      substitutionItems: _.map(substitutions, (sub, key) => ({
        'newProduct.id': sub.product.id,
        newQuantity: sub.quantitySelected,
        reasonCode: values.reasonCode === 'SUBSTITUTION' ? values.reasonCode : `SUBSTITUTION${values.reasonCode ? ` (${values.reasonCode})` : ''}`,
        // Sort order of substitution items should be different for each of them so it is increased
        sortOrder: originalItem.sortOrder + key,
      })),
    };

    apiClient.post(url, payload)
      .then(() => { this.props.onResponse(); })
      .catch(() => { this.props.hideSpinner(); });
  }

  validate(values) {
    const errors = {};
    errors.substitutions = [];
    let subQty = 0;

    _.forEach(values.substitutions, (item, key) => {
      if (item.quantitySelected) {
        subQty += _.toInteger(item.quantitySelected);
      }

      if (item.product && item.quantitySelected > _.toInteger(item.product.quantityAvailable)) {
        errors.substitutions[key] = { quantitySelected: 'react.stockMovement.errors.higherQtySelected.label' };
      }
      if (item.quantitySelected < 0) {
        errors.substitutions[key] = { quantitySelected: 'react.stockMovement.errors.negativeQtySelected.label' };
      }
    });

    if (subQty < this.state.attr.lineItem.quantityRequested && !values.reasonCode) {
      errors.reasonCode = 'react.default.error.requiredField.label';
    }
    return errors;
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
          <Translate id="react.stockMovement.quantitySelected.label" defaultMessage="Quantity selected" />: {_.reduce(values.substitutions, (sum, val) =>
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
        validate={this.validate}
        initialValues={this.state.formValues}
        formProps={{
          reasonCodes: this.state.attr.reasonCodes,
          originalItem: this.state.originalItem,
          debouncedProductsFetch: this.debouncedProductsFetch,
        }}
        renderBodyWithValues={this.calculateSelected}
      >
        <div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productCode.label" defaultMessage="Product code" />: {this.state.attr.lineItem.productCode}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productName.label" defaultMessage="Product name" />: {this.state.attr.lineItem.productName}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.quantityRequested.label" defaultMessage="Qty Requested" />: {this.state.attr.lineItem.quantityRequested}
          </div>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(SubstitutionsModal);

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
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};
