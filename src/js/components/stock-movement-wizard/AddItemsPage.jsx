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
import { renderFormField, getMovementNumber } from '../../utils/form-utils';
import apiClient from '../../utils/apiClient';
import { STOCK_LIST_ITEMS_MOCKS } from '../../mockedData';

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
          openOnClick: false,
          objectValue: true,
        },
        getDynamicAttr: ({ products }) => ({
          options: products,
        }),
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
            openOnClick: false,
            objectValue: true,
          },
          getDynamicAttr: ({ selectedValue, products }) => ({
            disabled: !!selectedValue,
            options: products,
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
          openOnClick: false,
          objectValue: true,
        },
        getDynamicAttr: ({ products }) => ({
          options: products,
        }),
      },
      lot: {
        type: TextField,
        label: 'Lot',
      },
      expiry: {
        type: DateField,
        label: 'Expiry',
        attributes: {
          dateFormat: 'DD/MMM/YYYY',
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
  constructor(props) {
    super(props);

    this.state = {
      recipients: [],
      products: [],
    };
  }

  componentDidMount() {
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

    const movementNumber = getMovementNumber();

    this.props.initialize('stock-movement-wizard', {
      lineItems,
      pickPage: [],
      adjustInventory: [],
      editPick: [],
      substitutions: [],
      movementNumber,
    }, true);

    this.fetchRecipients();
    this.fetchProducts();
  }

  getFields() {
    if (this.props.origin.type === 'SUPPLIER') {
      return VENDOR_FIELDS;
    } else if (this.props.stockList) {
      return STOCKLIST_FIELDS;
    }

    return NO_STOCKLIST_FIELDS;
  }

  fetchRecipients() {
    const url = '/openboxes/api/generic/person';

    return apiClient.get(url)
      .then((response) => {
        const recipients = _.map(response.data.data, recipient => (
          { value: recipient.id, label: recipient.displayName }
        ));
        this.setState({ recipients });
      });
  }

  fetchProducts() {
    const url = '/openboxes/api/products';

    return apiClient.get(url)
      .then((response) => {
        const products = _.map(response.data.data, product => (
          { value: product, label: product.name }
        ));
        this.setState({ products });
      });
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
            recipients: this.state.recipients,
            products: this.state.products,
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
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(mapStateToProps, { initialize, change })(AddItemsPage));

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
};

AddItemsPage.defaultProps = {
  stockList: null,
};
