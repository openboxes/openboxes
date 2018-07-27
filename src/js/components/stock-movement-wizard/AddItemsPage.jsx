import _ from 'lodash';
import React, { Component } from 'react';
import { reduxForm, initialize, formValueSelector, change } from 'redux-form';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import ArrayField from '../form-elements/ArrayField';
import ButtonField from '../form-elements/ButtonField';
import LabelField from '../form-elements/LabelField';
import DateField from '../form-elements/DateField';
import ValueSelectorField from '../form-elements/ValueSelectorField';
import { renderFormField } from '../../utils/form-utils';
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
  fieldKey: '',
  buttonLabel: 'Delete',
  getDynamicAttr: ({ fieldValue, removeItem, removeRow }) => ({
    onClick: fieldValue.id ? () => removeItem(fieldValue.id).then(() => removeRow()) : removeRow,
    disabled: fieldValue.statusCode === 'SUBSTITUTED',
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
        fieldKey: 'disabled',
        type: SelectField,
        label: 'Requisition items',
        attributes: {
          async: true,
          openOnClick: false,
          autoload: false,
          loadOptions: debouncedProductsFetch,
          cache: false,
          options: [],
        },
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: !!fieldValue,
        }),
      },
      quantityRequested: {
        type: TextField,
        label: 'Quantity',
        attributes: {
          type: 'number',
        },
        fieldKey: '',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: fieldValue.statusCode === 'SUBSTITUTED' || _.isNil(fieldValue.product),
        }),
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
            options: [],
          },
          getDynamicAttr: ({ selectedValue }) => ({
            disabled: !!selectedValue,
          }),
        },
      },
      quantityAllowed: {
        type: LabelField,
        label: 'Max QTY',
        attributes: {
          type: 'number',
        },
      },
      quantityRequested: {
        type: TextField,
        label: 'Needed QTY',
        attributes: {
          type: 'number',
        },
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
          options: [],
        },
      },
      lotNumber: {
        type: TextField,
        label: 'Lot',
      },
      expirationDate: {
        type: DateField,
        label: 'Expiry',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
        },
      },
      quantityRequested: {
        type: TextField,
        label: 'QTY',
        attributes: {
          type: 'number',
        },
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
      currentLineItems: [],
      statusCode: '',
    };

    this.props.showSpinner();
    this.removeItem = this.removeItem.bind(this);
  }

  componentDidMount() {
    if (!this.props.recipientsFetched) {
      this.fetchData(this.props.fetchUsers);
    }

    if (this.props.origin.type === 'SUPPLIER') {
      const lineItems = this.props.lineItems.length ? this.props.lineItems : new Array(10).fill({});
      this.props.change('stock-movement-wizard', 'lineItems', lineItems);
      this.props.hideSpinner();
    } else {
      this.fetchLineItems().then((resp) => {
        const { statusCode, lineItems } = resp.data.data;
        let lineItemsData;
        if (!lineItems.length) {
          lineItemsData = new Array(10).fill({});
        } else {
          lineItemsData = _.map(
            lineItems,
            val => ({
              ...val,
              quantityAllowed: val.quantityAllowed,
              disabled: true,
              rowKey: _.uniqueId('lineItem_'),
              product: {
                ...val.product,
                label: `${val.productCode} ${val.product.name}`,
              },
            }),
          );
        }

        this.props.change('stock-movement-wizard', 'lineItems', lineItemsData);

        this.setState({
          currentLineItems: lineItems,
          statusCode,
        });

        this.props.hideSpinner();
      }).catch(() => this.props.hideSpinner());
    }
  }

  getFields() {
    if (this.props.origin.type === 'SUPPLIER') {
      return VENDOR_FIELDS;
    } else if (this.props.stockList) {
      return STOCKLIST_FIELDS;
    }

    return NO_STOCKLIST_FIELDS;
  }

  getLineItemsToBeSaved(lineItems) {
    const lineItemsToBeAdded = _.filter(lineItems, item => !item.statusCode);

    const lineItemsWithStatus = _.filter(lineItems, item => item.statusCode);
    const lineItemsToBeUpdated = [];
    _.forEach(lineItemsWithStatus, (item) => {
      const oldItem = _.find(this.state.currentLineItems, old => old.id === item.id);
      if (parseInt(item.quantityRequested, 10) !== parseInt(oldItem.quantityRequested, 10)) {
        lineItemsToBeUpdated.push(item);
      }
    });

    return [].concat(
      _.map(lineItemsToBeAdded, item => ({
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
      })),
      _.map(lineItemsToBeUpdated, item => ({
        id: item.id,
        'product.id': item.product.id,
        quantityRequested: item.quantityRequested,
      })),
    );
  }

  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}?stepNumber=2`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
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
      this.props.showSpinner();
      this.saveRequisitionItems(lineItems)
        .then(() => {
          if (this.state.statusCode === 'CREATED' || this.state.statusCode === 'EDITING') {
            this.transitionToStep3()
              .then(() => {
                this.props.onSubmit();
              })
              .catch(() => this.props.hideSpinner());
          } else {
            this.props.onSubmit();
          }
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  saveRequisitionItems(lineItems) {
    const itemsToSave = this.getLineItemsToBeSaved(lineItems);
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.props.stockMovementId}`;
    const payload = {
      id: this.props.stockMovementId,
      name: '',
      description: this.props.description,
      identifier: this.props.movementNumber,
      'origin.id': this.props.origin.id,
      'destination.id': this.props.destination.id,
      dateRequested: this.props.dateRequested,
      'requestedBy.id': this.props.requestedBy,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      return apiClient.post(updateItemsUrl, payload)
        .then((resp) => {
          this.props.change('stock-movement-wizard', 'lineItems', resp.data.data.lineItems);
        })
        .catch(() => Promise.reject(new Error('Could not save requisition items')));
    }

    return Promise.resolve();
  }

  removeItem(itemId) {
    const removeItemsUrl = `/openboxes/api/stockMovements/${this.props.stockMovementId}`;
    const payload = {
      id: this.props.stockMovementId,
      name: '',
      description: this.props.description,
      identifier: this.props.movementNumber,
      'origin.id': this.props.origin.id,
      'destination.id': this.props.destination.id,
      dateRequested: this.props.dateRequested,
      'requestedBy.id': this.props.requestedBy,
      lineItems: [{
        id: itemId,
        delete: 'true',
      }],
    };

    return apiClient.post(removeItemsUrl, payload)
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('Could not delete requisition items'));
      });
  }

  transitionToStep3() {
    const url = `/openboxes/api/stockMovements/${this.props.stockMovementId}/status`;
    const payload = { status: 'VERIFYING' };

    return apiClient.post(url, payload);
  }

  render() {
    const { handleSubmit, previousPage } = this.props;
    return (
      <form onSubmit={handleSubmit(values => this.nextPage(values))}>
        {_.map(this.getFields(), (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, {
            stockList: this.props.stockList,
            recipients: this.props.recipients,
            removeItem: this.removeItem,
          }))}
        <div>
          <button type="button" className="btn  btn-outline-primary btnForm" onClick={previousPage}>
            Previous
          </button>
          <button
            type="submit"
            className="btn btn-outline-primary btnForm float-right"
            disabled={!_.some(this.props.lineItems, item => !_.isEmpty(item))}
          >Next
          </button>
        </div>
      </form>
    );
  }
}

function validate(values) {
  const errors = {};
  errors.lineItems = [];

  _.forEach(values.lineItems, (item, key) => {
    if (!_.isNil(item.product) && (item.quantityRequested <= 0
    || _.isNil(item.quantityRequested))) {
      errors.lineItems[key] = { quantityRequested: 'Enter proper quantity' };
    }
  });
  return errors;
}
const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({
  stockList: selector(state, 'stockList'),
  origin: selector(state, 'origin'),
  lineItems: selector(state, 'lineItems'),
  stockMovementId: selector(state, 'requisitionId'),
  destination: selector(state, 'destination'),
  requestedBy: selector(state, 'requestedBy'),
  description: selector(state, 'description'),
  dateRequested: selector(state, 'dateRequested'),
  movementNumber: selector(state, 'movementNumber'),
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
  change: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  goToPage: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  origin: PropTypes.shape({
    id: PropTypes.string,
    type: PropTypes.string,
  }).isRequired,
  stockList: PropTypes.string,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  fetchUsers: PropTypes.func.isRequired,
  recipients: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  recipientsFetched: PropTypes.bool.isRequired,
  lineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  stockMovementId: PropTypes.string.isRequired,
  destination: PropTypes.shape({
    id: PropTypes.string,
    type: PropTypes.string,
  }).isRequired,
  requestedBy: PropTypes.string.isRequired,
  description: PropTypes.string.isRequired,
  dateRequested: PropTypes.string.isRequired,
  movementNumber: PropTypes.string.isRequired,
};

AddItemsPage.defaultProps = {
  stockList: null,
};
