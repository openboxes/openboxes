import _ from 'lodash';
import React, { Component } from 'react';
import { reduxForm, initialize, formValueSelector } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import validate from './validate';
import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import ArrayField from '../form-elements/ArrayField';
import ButtonField from '../form-elements/ButtonField';
import ValueSelectorField from '../form-elements/ValueSelectorField';
import { renderFormField } from '../../utils/form-utils';
import { PRODUCTS_MOCKS, STOCK_LIST_ITEMS_MOCKS } from '../../mockedData';

const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  label: 'Delete',
  buttonLabel: 'Delete',
  getDynamicAttr: ({ removeRow }) => ({
    onClick: removeRow,
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const NO_STOCKLIST_FIELDS = {
  lineItems: {
    type: ArrayField,
    addButton: 'Add line',
    fields: {
      productCode: {
        type: SelectField,
        label: 'Requisition items',
        attributes: {
          openOnClick: false,
          options: PRODUCTS_MOCKS,
        },
      },
      quantity: {
        type: TextField,
        label: 'Quantity',
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const STOCKLIST_FIELDS = {
  lineItems: {
    type: ArrayField,
    addButton: 'Add line',
    fields: {
      productCode: {
        type: ValueSelectorField,
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `lineItems[${rowIndex}].disabled`,
        }),
        component: SelectField,
        componentConfig: {
          label: 'Requisition items',
          attributes: {
            openOnClick: false,
            options: PRODUCTS_MOCKS,
          },
          getDynamicAttr: ({ selectedValue }) => ({
            disabled: !!selectedValue,
          }),
        },
      },
      maxQuantity: {
        type: TextField,
        label: 'Max QTY',
      },
      neededQuantity: {
        type: TextField,
        label: 'Needed QTY',
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

class AddItemsPage extends Component {
  componentDidMount() {
    let lineItems;

    if (this.props.stockList) {
      lineItems = _.map(
        STOCK_LIST_ITEMS_MOCKS[this.props.stockList],
        val => ({ ...val, disabled: true }),
      );
    } else {
      lineItems = new Array(20).fill({});
    }

    this.props.initialize('stock-movement-wizard', { lineItems }, true);
  }

  getFields() {
    if (this.props.stockList) {
      return STOCKLIST_FIELDS;
    }

    return NO_STOCKLIST_FIELDS;
  }

  render() {
    const { handleSubmit, previousPage } = this.props;

    return (
      <form onSubmit={handleSubmit}>
        {_.map(this.getFields(), (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, { stockList: this.props.stockList }))}
        <div>
          <button type="button" className="btn btn-outline-primary" onClick={previousPage}>
            Previous
          </button>
          <button type="submit" className="btn btn-outline-primary">Next</button>
        </div>
      </form>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({ stockList: selector(state, 'stockList') });

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(mapStateToProps, { initialize })(AddItemsPage));

AddItemsPage.propTypes = {
  initialize: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  stockList: PropTypes.string,
};

AddItemsPage.defaultProps = {
  stockList: null,
};
