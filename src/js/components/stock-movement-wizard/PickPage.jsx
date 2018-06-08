import React, { Component } from 'react';
import { reduxForm, arrayInsert, formValueSelector, change } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import _ from 'lodash';

import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import ValueSelectorField from '../form-elements/ValueSelectorField';
import AdjustInventoryModal from './modals/AdjustInventoryModal';
import EditPickModal from './modals/EditPickModal';
import {
  LOT_DATA,
  PRODUCTS_MOCKS,
} from '../../mockedData';

const FIELDS = {
  pickPage: {
    type: ArrayField,
    fields: {
      productCode: {
        type: LabelField,
        label: 'Code',
      },
      productName: {
        type: LabelField,
        label: 'Product Name',
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
        label: 'Qty',
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
        type: EditPickModal,
        label: 'Edit Pick',
        attributes: {
          btnOpenText: 'Edit',
          title: 'Edit Pick',
        },
      },
      buttonAdjustInventory: {
        type: AdjustInventoryModal,
        label: 'Adjust Inventory',
        attributes: {
          btnOpenText: 'Adjust',
          title: 'Adjust Inventory',
        },
      },
    },
  },
};

/* eslint class-methods-use-this: ["error",{ "exceptMethods": ["print"] }] */
/* eslint no-param-reassign: "error" */
class PickPage extends Component {
  constructor(props) {
    super(props);

    this.state = {};
  }

  componentDidMount() {
    let pickPage = [];
    _.forEach(this.props.lineItems, (line) => {
      pickPage = _.concat(
        pickPage,
        _.filter(LOT_DATA, data => data.productCode === line.product.code),
      );
    });
    _.forEach(pickPage, (pick) => {
      pick.productName = _.find(PRODUCTS_MOCKS, prod => prod.value.code === pick.productCode).label;
    });
    this.props.change('stock-movement-wizard', 'pickPage', pickPage);
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
})(connect(mapStateToProps, { arrayInsert, change })(PickPage));

PickPage.propTypes = {
  change: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  lineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
