import React, { Component } from 'react';
import { reduxForm, formValueSelector, change } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import _ from 'lodash';

import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField, generateKey } from '../../utils/form-utils';
import ValueSelectorField from '../form-elements/ValueSelectorField';
import AdjustInventoryModal from './modals/AdjustInventoryModal';
import EditPickModal from './modals/EditPickModal';
import { AVAILABLE_LOTS } from '../../mockedData';

const FIELDS = {
  pickPage: {
    type: ArrayField,
    pickPage: true,
    fields: {
      product: {
        type: LabelField,
        label: 'Product Name',
        attributes: {
          formatValue: value => (`${value.code} - ${value.name}`),
        },
      },
      lot: {
        type: LabelField,
        label: 'Lot #',
      },
      expiryDate: {
        type: LabelField,
        label: 'Expiry Date',
      },
      bin: {
        type: LabelField,
        label: 'Bin',
      },
      quantity: {
        type: LabelField,
        label: 'Qty required',
      },
      qtyPicked: {
        type: LabelField,
        label: 'Qty picked',
      },
      recipient: {
        type: ValueSelectorField,
        label: 'Includes recipient',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `pickPage[${rowIndex}].recipient`,
        }),
        component: LabelField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue }) => ({
            className: selectedValue ? 'fa fa-user' : '',
          }),
        },
      },
      buttonEditPick: {
        label: 'Edit Pick',
        type: ValueSelectorField,
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `pickPage[${rowIndex}].product.code`,
        }),
        component: EditPickModal,
        componentConfig: {
          attributes: {
            btnOpenText: 'Edit',
            title: 'Edit Pick',
          },
          getDynamicAttr: ({ selectedValue }) => ({
            productCode: selectedValue,
          }),
        },
      },
      buttonAdjustInventory: {
        label: 'Adjust Inventory',
        type: ValueSelectorField,
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `pickPage[${rowIndex}].product`,
        }),
        component: AdjustInventoryModal,
        componentConfig: {
          attributes: {
            btnOpenText: 'Adjust',
            title: 'Adjust Inventory',
          },
          getDynamicAttr: ({ selectedValue }) => ({
            product: selectedValue,
          }),
        },
      },
    },
  },
};

/* eslint class-methods-use-this: ["error",{ "exceptMethods": ["print"] }] */
/* eslint no-param-reassign: "error" */
class PickPage extends Component {
  componentDidMount() {
    // TODO: once API will be ready, rewrite this to get data from backend
    if (!_.some(this.props.pickPageData, 'availableLots')) {
      const { lineItems } = this.props;
      let pickPage = [];
      _.forEach(_.filter(lineItems, item => !item.substituted), (line) => {
        // Get available lots for every product
        const availableLots =
          _.filter(
            AVAILABLE_LOTS,
            data => data.product.code === line.product.code,
          );
        // Get picked lots (out of all available lots)
        const lotsPicked =
          _.filter(
            availableLots,
            data => data.product.code === line.product.code && data.qtyPicked > 0,
          );
        // Create array for PickPage table: line items with lots picked for them
        // (rowKey = unique value, that will be used as row key inside rendered table)
        pickPage = _.concat(
          pickPage,
          {
            ...line,
            availableLots: _.map(availableLots, lot => (
              {
                ...lot,
                lotWithBin: `${lot.lot}-${lot.bin}`,
              }
            )),
            quantity: line.revisedQuantity || line.quantity,
            parent: true,
            qtyPicked: _.reduce(lotsPicked, (sum, lot) => sum + parseInt(lot.qtyPicked, 10), 0),
            rowKey: generateKey(),
          },
          // Add fields to picked lots:
          // - initialPick (Bool),
          // - lotWithBin (String made from lot and bin fields, assumed to be unique),
          _.map(lotsPicked, lot => ({
            ...lot,
            initialPick: true,
            lotWithBin: `${lot.lot}-${lot.bin}`,
            rowKey: generateKey(),
          })),
        );
      });
      // Update specfied field in redux form
      this.props.change('stock-movement-wizard', 'pickPage', pickPage);
    }
  }

  print() {
    window.print();
  }

  render() {
    return (
      <div>
        <button
          type="button"
          className="fa fa-print float-right p-2 mb-1 btn btn-secondary d-print-none"
          onClick={this.print}
        />
        <form onSubmit={this.props.onSubmit} className="print-mt">
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
          <div className="d-print-none">
            <button type="button" className="btn btn-outline-primary" onClick={this.props.previousPage}>
              Previous
            </button>
            <button type="submit" className="btn btn-outline-primary float-right">Next</button>
          </div>
        </form>
      </div>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({ pickPageData: selector(state, 'pickPage'), lineItems: selector(state, 'lineItems') });

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(connect(mapStateToProps, { change })(PickPage));

PickPage.propTypes = {
  pickPageData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  change: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  lineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
