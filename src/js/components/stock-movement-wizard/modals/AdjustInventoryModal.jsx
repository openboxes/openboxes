import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import LabelField from '../../form-elements/LabelField';
import SelectField from '../../form-elements/SelectField';
import DateField from '../../form-elements/DateField';
import { showSpinner, hideSpinner } from '../../../actions';
import apiClient from '../../../utils/apiClient';
import Translate from '../../../utils/Translate';

const FIELDS = {
  adjustInventory: {
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow, productId }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({ productId })}
      ><Translate id="react.stockMovement.addLot.label" defaultMessage="Add new lot number" />
      </button>
    ),
    type: ArrayField,
    fields: {
      binLocation: {
        type: SelectField,
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin Location',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue, bins, hasBinLocationSupport }) => ({
          disabled: !!fieldValue || !hasBinLocationSupport,
          options: bins,
          labelKey: 'name',
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: !!fieldValue,
        }),
      },
      expirationDate: {
        type: DateField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
        fieldKey: 'inventoryItem.id',
        attributes: {
          autoComplete: 'off',
        },
        getDynamicAttr: ({ fieldValue }) => ({
          dateFormat: 'MM/DD/YYYY',
          disabled: !!fieldValue,
        }),
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.previousQuantity.label',
        defaultMessage: 'Previous Qty',
        fixedWidth: '150px',
        attributes: {
          formatValue: value => (value ? value.toLocaleString('en-US') : null),
        },
      },
      quantityAdjusted: {
        type: TextField,
        label: 'react.stockMovement.currentQuantity.label',
        defaultMessage: 'Current Qty',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
      comments: {
        type: TextField,
        label: 'react.stockMovement.comments.label',
        defaultMessage: 'Comments',
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.adjustInventory = [];

  _.forEach(values.adjustInventory, (item, key) => {
    if (item.quantityAdjusted < 0) {
      errors.adjustInventory[key] = { quantityAdjusted: 'react.stockMovement.errors.adjustedQty.label' };
    }
    if (!_.isNil(item.quantityAdjusted) && item.quantityAdjusted !== '' && !item.comments) {
      errors.adjustInventory[key] = { comments: 'react.stockMovement.errors.emptyField.label' };
    }
  });
  return errors;
}


/** Modal window where user can adjust existing inventory or add a new one. */
class AdjustInventoryModal extends Component {
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
   * Loads available inventories for chosen items into modal's form.
   * @public
   */
  onOpen() {
    this.setState({
      formValues: {
        adjustInventory: _.map(this.state.attr.fieldValue.availableItems, item => ({
          ...item,
          binLocation: {
            id: item['binLocation.id'],
            name: item['binLocation.name'],
          },
        })),
      },
    });
  }

  /**
   * Sends all changes made by user in this modal to API and update data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const url = `/openboxes/api/stockAdjustments?location.id=${this.props.locationId}`;
    const items = _.filter(values.adjustInventory, item => !_.isNil(item.quantityAdjusted) && item.quantityAdjusted !== '');
    const payload = _.map(items, (item) => {
      if (!item['inventoryItem.id']) {
        return {
          productId: item.productId,
          'binLocation.id': item.binLocation || '',
          lotNumber: item.lotNumber,
          expirationDate: item.expirationDate,
          quantityAdjusted: parseInt(item.quantityAdjusted, 10),
          comments: item.comments,
        };
      }
      return {
        'inventoryItem.id': item['inventoryItem.id'] || '',
        'binLocation.id': item['binLocation.id'] || '',
        quantityAdjusted: parseInt(item.quantityAdjusted, 10),
        comments: item.comments,
      };
    });

    apiClient.post(url, payload)
      .then(() => {
        this.state.attr.onResponse(this.state.attr.fieldValue.productCode);
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
          bins: this.props.bins,
          hasBinLocationSupport: this.props.hasBinLocationSupport,
          productId: this.state.attr.fieldValue.productId,
        }}
      >
        <div>
          <div className="font-weight-bold"><Translate id="react.stockMovement.productCode.label" defaultMessage="Product code" />: {this.state.attr.fieldValue.productCode}</div>
          <div className="font-weight-bold"><Translate id="react.stockMovement.productName.label" defaultMessage="Product name" />: {this.state.attr.fieldValue['product.name']} <hr /></div>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(AdjustInventoryModal);

AdjustInventoryModal.propTypes = {
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
  /** Available bin locations fetched from API. */
  bins: PropTypes.arrayOf(PropTypes.shape({})),
  /** Location ID (origin of stock movement). To be used in stockAdjustments request. */
  locationId: PropTypes.string.isRequired,
};

AdjustInventoryModal.defaultProps = {
  bins: [],
};
