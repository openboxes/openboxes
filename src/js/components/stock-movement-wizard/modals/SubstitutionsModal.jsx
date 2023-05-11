import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import ProductSelectField from 'components/form-elements/ProductSelectField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import apiClient from 'utils/apiClient';
import { debounceAvailableItemsFetch } from 'utils/option-utils';
import Translate from 'utils/Translate';


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
    getDynamicRowAttr: ({
      rowValues,
      originalItem,
      isLoading,
      isEmptyData,
    }) => {
      let className = '';
      const rowDate = new Date(rowValues.minExpirationDate);
      const origDate = originalItem && originalItem.minExpirationDate ?
        new Date(originalItem.minExpirationDate) : null;
      if (!rowValues.originalItem) {
        className = (origDate && rowDate && rowDate < origDate) || (!origDate && rowDate) ? 'text-danger' : '';
      } else {
        className = 'font-weight-bold';
      }
      return {
        className,
        isLoading,
        isEmptyData,
      };
    },
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({}, null, false)}
      ><Translate
        id="react.default.button.addCustomSubstitution.label"
        defaultMessage="Add custom substitution"
      />
      </button>
    ),
    fields: {
      product: {
        fieldKey: 'disabled',
        type: ProductSelectField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        getDynamicAttr: ({ debouncedAvailableItemsFetch }) => ({
          loadOptions: debouncedAvailableItemsFetch,
        }),
      },
      'product.minExpirationDate': {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '2',
        attributes: {
          showValueTooltip: true,
        },
      },
      'product.quantityAvailable': {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Qty Available',
        flexWidth: '2',
        fieldKey: '',
        attributes: {
          // eslint-disable-next-line no-nested-ternary
          formatValue: fieldValue => (_.get(fieldValue, 'quantityAvailable') ? _.get(fieldValue, 'quantityAvailable')
            .toLocaleString('en-US') :
            _.get(fieldValue, 'product.quantityAvailable') ? _.get(fieldValue, 'product.quantityAvailable')
              .toLocaleString('en-US') : null),
          showValueTooltip: true,
        },
        getDynamicAttr: ({ fieldValue }) => ({
          tooltipValue: _.map(fieldValue && fieldValue.availableItems, availableItem =>
            (
              <p>{fieldValue.productCode} {fieldValue.productName}, {availableItem.expirationDate ? availableItem.expirationDate : '---'},
                Qty {availableItem.quantityAvailable}
              </p>
            )),
        }),
      },
      quantitySelected: {
        type: TextField,
        label: 'react.stockMovement.quantitySelected.label',
        defaultMessage: 'Quantity selected',
        flexWidth: '2',
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
      fieldConfig: {
        attributes,
        getDynamicAttr,
      },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      isEmptyData: true,
      isLoading: false,
      attr,
      formValues: {},
      originalItem: null,
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.validate = this.validate.bind(this);

    this.debouncedAvailableItemsFetch = debounceAvailableItemsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
    );
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: {
        attributes,
        getDynamicAttr,
      },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  /** Loads available substitutions for chosen item into modal's form.
   * @public
   */
  onOpen() {
    this.state.attr.onOpen()
      .then(() => this.fetchSubstitutions());
  }

  /** Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const substitutions = _.filter(values.substitutions, sub =>
      sub.quantitySelected > 0);
    const originalItem = _.find(values.substitutions, sub => sub.originalItem)
      || this.state.attr.lineItem;
    const substitutionReasonCode = _.get(values, 'reasonCode.value');

    const url = `/openboxes/api/stockMovementItems/${originalItem.requisitionItemId}/substituteItem`;
    const payload = {
      substitutionItems: _.map(substitutions, (sub, key) => ({
        'newProduct.id': sub.product.id,
        newQuantity: sub.quantitySelected,
        reasonCode: substitutionReasonCode === 'SUBSTITUTION'
          ? substitutionReasonCode
          : `SUBSTITUTION${substitutionReasonCode ? ` (${substitutionReasonCode})` : ''}`,
        // Sort order of substitution items should be different for each of them so it is increased
        sortOrder: originalItem.sortOrder + key,
      })),
    };

    apiClient.post(url, payload)
      .then(() => {
        this.props.onResponse();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  fetchSubstitutions() {
    this.setState({ isLoading: true });
    const url = `/openboxes/api/stockMovements/${this.state.attr.lineItem.requisitionItemId}/substitutionItems`;
    return apiClient.get(url)
      .then((resp) => {
        let substitutions = _.map(
          resp.data.data,
          val => ({
            ...val,
            disabled: true,
            product: {
              id: `${val.productId}`,
              productCode: `${val.productCode}`,
              name: `${val.productName}`,
              displayName: val.product.displayNames?.default,
              minExpirationDate: `${val.minExpirationDate}`,
              quantityAvailable: `${val.quantityAvailable}`,
              handlingIcons: val.product.handlingIcons,
            },
          }),
        );
        let originalItem = null;

        if (_.toInteger(this.state.attr.lineItem.quantityAvailable) > 0) {
          const {
            productCode, product, productName, minExpirationDate, quantityAvailable,
          } = this.state.attr.lineItem;

          originalItem = {
            ...this.state.attr.lineItem,
            originalItem: true,
            product: {
              id: `${product.id}`,
              productCode: `${productCode}`,
              name: `${productName}`,
              displayName: product.displayNames?.default,
              minExpirationDate,
              quantityAvailable,
            },
          };
          substitutions = [
            originalItem,
            ...substitutions,
          ];
        }

        this.setState({
          isEmptyData: !substitutions.length,
          formValues: {
            substitutions,
          },
          originalItem,
        });
      })
      .catch(err => err)
      .finally(() => this.setState({
        isLoading: false,
      }));
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
          <Translate
            id="react.stockMovement.quantitySelected.label"
            defaultMessage="Quantity selected"
          />: {_.reduce(values.substitutions, (sum, val) =>
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
          isEmptyData: this.state.isEmptyData,
          isLoading: this.state.isLoading,
          emptyDataMessageId: 'react.stockMovement.noSubstitutionsAvailable.message',
          defaultEmptyDataMessage: 'There are no substitutions available',
          reasonCodes: this.state.attr.reasonCodes,
          originalItem: this.state.originalItem,
          debouncedAvailableItemsFetch: this.debouncedAvailableItemsFetch,
        }}
        renderBodyWithValues={this.calculateSelected}
      >
        <div>
          <div className="font-weight-bold">
            <Translate
              id="react.stockMovement.productCode.label"
              defaultMessage="Product code"
            />: {this.state.attr.lineItem.productCode}
          </div>
          <div className="font-weight-bold">
            <Tooltip
              html={<div className="text-truncate">{this.state.attr.lineItem.product.name}</div>}
              theme="dark"
              disabled={!this.state.attr.lineItem.product.displayNames?.default}
              position="top-start"
            >
              <Translate
                id="react.stockMovement.productName.label"
                defaultMessage="Product name"
              />: {
              this.state.attr.lineItem.product.displayNames?.default ??
              this.state.attr.lineItem.product.name
              }
            </Tooltip>
          </div>
          <div className="font-weight-bold">
            <Translate
              id="react.stockMovement.quantityRequested.label"
              defaultMessage="Qty Requested"
            />: {this.state.attr.lineItem.quantityRequested}
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

export default connect(mapStateToProps, {
  showSpinner,
  hideSpinner,
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
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};
