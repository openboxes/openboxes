import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';


const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: () => (
      <label htmlFor="reasonCode" className="ml-3 text-center">
        <Translate id="react.stockMovement.reasonCode.label" defaultMessage="Reason code" />
      </label>
    ),
    defaultMessage: 'Reason code',
    attributes: {
      className: 'mb-2',
    },
    getDynamicAttr: props => ({
      options: props.reasonCodes,
    }),
  },
  availableItems: {
    type: ArrayField,
    getDynamicRowAttr: ({ rowValues }) => {
      let className = '';
      if (!rowValues.quantityAvailable) {
        className = 'text-disabled';
      }
      return { className };
    },
    fields: {
      status: {
        type: LabelField,
        fieldKey: '',
        fixedWidth: '120px',
        getDynamicAttr: ({ translate }) => ({
          showValueTooltip: true,
          formatValue: (fieldValue) => {
            if (fieldValue.status === 'AVAILABLE' && fieldValue.pickedRequisitionNumbers.length !== 0) {
              const status = translate('react.stockMovement.enum.AvailableItemStatus.PICKED', 'PICKED');
              return status + (fieldValue.pickedRequisitionNumbers ? ` [${fieldValue.pickedRequisitionNumbers}]` : '');
            } else if (!fieldValue.status || fieldValue.status === 'AVAILABLE') {
              return '';
            }

            const status = translate(`react.stockMovement.enum.AvailableItemStatus.${fieldValue.status}`, fieldValue.status);
            return status + (fieldValue.pickedRequisitionNumbers ? ` [${fieldValue.pickedRequisitionNumbers}]` : '');
          },
        }),
      },
      lotNumber: {
        type: LabelField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        fixedWidth: '120px',
      },
      binLocation: {
        type: LabelField,
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin Location',
        getDynamicAttr: ({ hasBinLocationSupport }) => ({
          hide: !hasBinLocationSupport,
        }),
        attributes: {
          showValueTooltip: true,
          formatValue: fieldValue => fieldValue && (
            <div className="d-flex justify-content-center">
              {fieldValue.zoneName ? <div className="text-truncate" style={{ minWidth: 30, flexShrink: 20 }}>{fieldValue.zoneName}</div> : ''}
              <div className="text-truncate">{fieldValue.zoneName ? `: ${fieldValue.name}` : fieldValue.name}</div>
            </div>),
        },
      },
      quantityOnHand: {
        type: LabelField,
        label: 'react.stockMovement.onHand.label',
        defaultMessage: 'On Hand',
        fixedWidth: '150px',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.available.label',
        defaultMessage: 'Available',
        fixedWidth: '150px',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
        },
      },
      quantityPicked: {
        type: TextField,
        fieldKey: '',
        label: 'react.stockMovement.picked.label',
        defaultMessage: 'Picked',
        headerAlign: 'left',
        fixedWidth: '120px',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: fieldValue
              && !fieldValue.quantityAvailable
              && !fieldValue.quantityPicked,
        }),
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.availableItems = [];
  _.forEach(values.availableItems, (item, key) => {
    if (item.quantityPicked > item.quantityAvailable) {
      errors.availableItems[key] = { quantityPicked: 'react.stockMovement.errors.higherTyPicked.label' };
    }
    if (item.quantityPicked < 0) {
      errors.availableItems[key] = { quantityPicked: 'react.stockMovement.errors.negativeQtyPicked.label' };
    }
  });

  const pickedSum = _.reduce(
    values.availableItems, (sum, val) =>
      (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)),
    0,
  );


  if (_.some(values.availableItems, val => !_.isNil(val.quantityPicked)) &&
    !values.reasonCode && pickedSum !== values.quantityRequired) {
    errors.reasonCode = 'react.stockMovement.errors.differentTotalQty.label';
  }

  return errors;
}

/** Modal window where user can edit pick. */
/* eslint no-param-reassign: "error" */
class EditPickModal extends Component {
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

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  /**
   * Loads chosen items, required quantity and reason codes into modal's form.
   * @public
   */
  onOpen() {
    this.fetchPickPageItem();
  }

  /**
   * Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const picklistUrl = `/openboxes/api/stockMovementItems/${this.state.attr.itemId}/updatePicklist`;
    const itemsUrl = `/openboxes/api/stockMovementItems/${this.state.attr.itemId}?stepNumber=4`;
    const payload = {
      picklistItems: _.map(values.availableItems, avItem => ({
        id: avItem.id || '',
        'inventoryItem.id': avItem['inventoryItem.id'],
        'binLocation.id': avItem['binLocation.id'] || '',
        quantityPicked: _.isNil(avItem.quantityPicked) ? '' : avItem.quantityPicked,
      })),
      reasonCode: values.reasonCode.value || '',
    };

    apiClient.post(picklistUrl, payload)
      .then(() => {
        apiClient.get(itemsUrl)
          .then((resp) => {
            const pickPageItem = resp.data.data;

            this.state.attr.onResponse(pickPageItem);
            this.props.hideSpinner();
          })
          .catch(() => { this.props.hideSpinner(); });
      })
      .catch(() => { this.props.hideSpinner(); });
  }

  /**
   * Sums up quantity picked from all available items.
   * @param {object} values
   * @public
   */
  /* eslint-disable-next-line class-methods-use-this */
  calculatePicked(values) {
    return (
      <div>
        <div className="font-weight-bold pb-2">
          <Translate id="react.stockMovement.quantityPicked.label" defaultMessage="Qty Picked" />: {_.reduce(values.availableItems, (sum, val) =>
            (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)), 0)}
        </div>
        <hr />
      </div>
    );
  }

  fetchPickPageItem() {
    const itemsUrl = `/openboxes/api/stockMovementItems/${this.state.attr.itemId}/details?stepNumber=4`;

    apiClient.get(itemsUrl)
      .then((resp) => {
        const pickPageItem = resp.data.data;

        const availableItems = _.map(pickPageItem.availableItems, (avItem) => {
          // check if this picklist item already exists
          const picklistItem = _.find(pickPageItem.picklistItems, item => item['inventoryItem.id'] === avItem['inventoryItem.id'] && item['binLocation.id'] === avItem['binLocation.id']);

          if (picklistItem) {
            return {
              ...avItem,
              id: picklistItem.id,
              quantityPicked: picklistItem.quantityPicked,
              binLocation: {
                id: picklistItem['binLocation.id'],
                name: picklistItem['binLocation.name'],
                zoneName: picklistItem['binLocation.zoneName'],
              },
            };
          }

          return {
            ...avItem,
            binLocation: {
              id: avItem['binLocation.id'],
              name: avItem['binLocation.name'],
              zoneName: avItem['binLocation.zoneName'],
            },
          };
        });

        this.setState({
          formValues: {
            availableItems,
            reasonCode: '',
            quantityRequired: pickPageItem.quantityRequired,
            productCode: pickPageItem.productCode,
            productName: pickPageItem['product.name'],
          },
        });

        this.props.hideSpinner();
      })
      .catch(() => { this.props.hideSpinner(); });
  }

  render() {
    if (this.state.attr.subfield) {
      return null;
    }

    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        validate={validate}
        initialValues={this.state.formValues}
        formProps={{
          reasonCodes: this.state.attr.reasonCodes,
          hasBinLocationSupport: this.props.hasBinLocationSupport,
          translate: this.props.translate,
        }}
        renderBodyWithValues={this.calculatePicked}
      >
        <div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productCode.label" defaultMessage="Product code" />: {this.state.formValues.productCode}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productName.label" defaultMessage="Product name" />: {this.state.formValues.productName}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.quantityRequired.label" defaultMessage="Qty Required" />: {this.state.formValues.quantityRequired}
          </div>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(EditPickModal);

EditPickModal.propTypes = {
  /** Name of the field */
  fieldName: PropTypes.string.isRequired,
  /** Configuration of the field */
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};
