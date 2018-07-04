import _ from 'lodash';
import React, { Component } from 'react';
import { reduxForm, initialize, formValueSelector, change } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import { validate } from './validate';
import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import ArrayField from '../form-elements/ArrayField';
import ButtonField from '../form-elements/ButtonField';
import LabelField from '../form-elements/LabelField';
import DateField from '../form-elements/DateField';
import ValueSelectorField from '../form-elements/ValueSelectorField';
import { renderFormField } from '../../utils/form-utils';
import { STOCK_LIST_ITEMS_MOCKS } from '../../mockedData';
import { showSpinner, hideSpinner, fetchUsers } from '../../actions';
import apiClient from '../../utils/apiClient';

const debouncedProductsFetch = _.debounce((searchTerm, callback) => {
  if (searchTerm) {
    apiClient.get(`/openboxes/api/products?name=${searchTerm}&productCode=${searchTerm}`)
      .then(result => callback(
        null,
        {
          complete: true,
          options: _.map(result.data.data, obj => (
            {
              value: {
                id: obj.id,
                name: obj.name,
                productCode: obj.productCode,
                label: `${obj.productCode} - ${obj.name}`,
              },
              label: `${obj.productCode} - ${obj.name}`,
            }
          )),
        },
      ))
      .catch(error => callback(error, { options: [] }));
  } else {
    callback(null, { options: [] });
  }
}, 500);

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
      product: {
        type: SelectField,
        label: 'Requisition items',
        attributes: {
          async: true,
          openOnClick: false,
          autoload: false,
          loadOptions: debouncedProductsFetch,
          cache: false,
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
      product: {
        type: ValueSelectorField,
        label: 'Requisition items',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `lineItems[${rowIndex}].disabled`,
        }),
        component: SelectField,
        componentConfig: {
          attributes: {
            async: true,
            openOnClick: false,
            autoload: false,
            loadOptions: debouncedProductsFetch,
            cache: false,
          },
          getDynamicAttr: ({ selectedValue }) => ({
            disabled: !!selectedValue,
          }),
        },
      },
      maxQuantity: {
        type: LabelField,
        label: 'Max QTY',
      },
      quantity: {
        type: TextField,
        label: 'Needed QTY',
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const VENDOR_FIELDS = {
  lineItems: {
    type: ArrayField,
    addButton: 'Add line',
    fields: {
      pallet: {
        type: TextField,
        label: 'Pallet',
      },
      box: {
        type: TextField,
        label: 'Box',
      },
      product: {
        type: SelectField,
        label: 'Item',
        attributes: {
          async: true,
          openOnClick: false,
          autoload: false,
          loadOptions: debouncedProductsFetch,
          cache: false,
        },
      },
      lot: {
        type: TextField,
        label: 'Lot',
      },
      expiry: {
        type: DateField,
        label: 'Expiry',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
        },
      },
      quantity: {
        type: TextField,
        label: 'QTY',
      },
      recipient: {
        type: SelectField,
        label: 'Recipient',
        getDynamicAttr: ({ recipients }) => ({
          options: recipients,
        }),
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

class AddItemsPage extends Component {
  componentDidMount() {
    this.props.showSpinner();
    let lineItems;

    if (this.props.origin.type === 'SUPPLIER' || !this.props.stockList) {
      lineItems = new Array(5).fill({});
    } else {
      lineItems = _.map(
        STOCK_LIST_ITEMS_MOCKS[1],
        val => ({
          ...val, quantity: val.maxQuantity, disabled: true, rowKey: _.uniqueId('lineItem_'),
        }),
      );
    }

    this.props.initialize('stock-movement-wizard', {
      lineItems,
      pickPage: [],
      adjustInventory: [],
      editPick: [],
      substitutions: [],
    }, true);

    if (!this.props.recipientsFetched) {
      this.fetchData(this.props.fetchUsers);
    }

    this.props.hideSpinner();
  }

  getFields() {
    if (this.props.origin.type === 'SUPPLIER') {
      return VENDOR_FIELDS;
    } else if (this.props.stockList) {
      return STOCKLIST_FIELDS;
    }

    return NO_STOCKLIST_FIELDS;
  }

  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  nextPage(formValues) {
    const lineItems = _.filter(formValues.lineItems, val => !_.isEmpty(val));
    this.props.change('stock-movement-wizard', 'lineItems', lineItems);
    if (this.props.origin.type === 'SUPPLIER') {
      this.props.goToPage(5);
    } else {
      this.props.onSubmit();
    }
  }

  render() {
    const { handleSubmit, previousPage } = this.props;
    return (
      <form onSubmit={handleSubmit(values => this.nextPage(values))}>
        {_.map(this.getFields(), (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, {
            stockList: this.props.stockList,
            recipients: this.props.recipients,
          }))}
        <div>
          <button type="button" className="btn btn-outline-primary" onClick={previousPage}>
            Previous
          </button>
          <button type="submit" className="btn btn-outline-primary float-right">Next</button>
        </div>
      </form>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  stockList: selector(state, 'stockList'),
  origin: selector(state, 'origin'),
  recipients: state.users.data,
  recipientsFetched: state.users.fetched,
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(mapStateToProps, {
  initialize, change, showSpinner, hideSpinner, fetchUsers,
})(AddItemsPage));

AddItemsPage.propTypes = {
  initialize: PropTypes.func.isRequired,
  change: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  goToPage: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  origin: PropTypes.shape({
    type: PropTypes.string,
  }).isRequired,
  stockList: PropTypes.string,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  fetchUsers: PropTypes.func.isRequired,
  recipients: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  recipientsFetched: PropTypes.bool.isRequired,
};

AddItemsPage.defaultProps = {
  stockList: null,
};
