import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { change, reduxForm, formValueSelector } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import { renderFormField } from '../../../utils/form-utils';
import DateField from '../../form-elements/DateField';
import ValueSelectorField from '../../form-elements/ValueSelectorField';

const FIELDS = {
  adjustInventory: {
    addButton: 'Add new lot number',
    type: ArrayField,
    fields: {
      lot: {
        type: ValueSelectorField,
        label: 'Lot #',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `adjustInventory[${rowIndex}].disabled`,
        }),
        component: TextField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue }) => ({
            disabled: !!selectedValue,
          }),
        },
      },
      bin: {
        type: ValueSelectorField,
        label: 'Bin',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `adjustInventory[${rowIndex}].disabled`,
        }),
        component: TextField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue }) => ({
            disabled: !!selectedValue,
          }),
        },
      },
      expiryDate: {
        type: ValueSelectorField,
        label: 'Expiry Date',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `adjustInventory[${rowIndex}].disabled`,
        }),
        component: DateField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue }) => ({
            dateFormat: 'YYYY/MM/DD',
            inputProps: { disabled: !!selectedValue },
          }),
        },
      },
      qtyAvailable: {
        type: TextField,
        label: 'Qty Available',
      },
    },
  },
};

class AdjustInventoryModal extends Component {
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
    const { pickPage } = this.props;
    const inventoryItem =
      _.find(pickPage, item => item.product.code === this.state.attr.product.code);
    this.props.change(
      'stock-movement-wizard',
      'adjustInventory',
      inventoryItem ? _.map(inventoryItem.availableLots, lot => ({ ...lot, disabled: true })) : [],
    );
  }

  onSave() {
    // TODO: send new/changed availableLots to backend!
    const { pickPage } = this.props;
    const lotsToUpdate =
      _.find(pickPage, item => item.product.code === this.state.attr.product.code);
    lotsToUpdate.availableLots = _.map(this.props.adjustInventory, item => (
      {
        ...item,
        product: item.product ||
          { code: this.state.attr.product.code, name: this.state.attr.product.name },
      }
    ));
    this.props.change('stock-movement-wizard', 'pickPage', pickPage);
  }

  render() {
    return (
      <ModalWrapper {...this.state.attr} onOpen={this.onOpen} onSave={this.onSave}>
        <form className="print-mt">
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
        </form>
      </ModalWrapper>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  pickPage: selector(state, 'pickPage'),
  adjustInventory: selector(state, 'adjustInventory'),
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(connect(mapStateToProps, { change })(AdjustInventoryModal));

AdjustInventoryModal.propTypes = {
  pickPage: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  adjustInventory: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  change: PropTypes.func.isRequired,
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};
