import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import fileDownload from 'js-file-download';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';

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
  flexWidth: '1',
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
          autoFocus: true,
          loadOptions: debouncedProductsFetch,
          cache: false,
          options: [],
          showValueTooltip: true,
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
        getDynamicAttr: ({
          fieldValue, addRow, rowCount, rowIndex,
        }) => ({
          disabled: fieldValue.statusCode === 'SUBSTITUTED' || _.isNil(fieldValue.product),
          onBlur: rowCount === rowIndex + 1 ? () => addRow() : null,
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
            autoFocus: true,
            loadOptions: debouncedProductsFetch,
            cache: false,
            options: [],
            showValueTooltip: true,
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
        getDynamicAttr: ({
          addRow, rowCount, rowIndex,
        }) => ({
          onBlur: rowCount === rowIndex + 1 ? () => addRow() : null,
        }),
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
      palletName: {
        type: TextField,
        label: 'Pallet',
        flexWidth: '1',
        attributes: {
          autoFocus: true,
        },
      },
      boxName: {
        type: TextField,
        label: 'Box',
        flexWidth: '1',
      },
      product: {
        type: SelectField,
        label: 'Item',
        flexWidth: '6',
        attributes: {
          className: 'text-left',
          async: true,
          openOnClick: false,
          autoload: false,
          loadOptions: debouncedProductsFetch,
          cache: false,
          options: [],
          showValueTooltip: true,
        },
      },
      lotNumber: {
        type: TextField,
        label: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: DateField,
        label: 'Expiry',
        flexWidth: '1',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
        },
      },
      quantityRequested: {
        type: TextField,
        label: 'QTY',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
      },
      recipient: {
        type: SelectField,
        label: 'Recipient',
        flexWidth: '1.5',
        getDynamicAttr: ({
          recipients, addRow, rowCount, rowIndex,
        }) => ({
          options: recipients,
          onBlur: rowCount === rowIndex + 1 ? () => addRow() : null,
        }),
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

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

/**
 * The second step of stock movement where user can add items to stock list.
 * This component supports three different cases: with or without stocklist
 * when movement is from a depot and when movement is from a vendor.
 */
class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentLineItems: [],
      statusCode: '',
      values: this.props.initialValues,
    };

    this.props.showSpinner();
    this.removeItem = this.removeItem.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
  }

  componentDidMount() {
    if (!this.props.recipientsFetched) {
      this.fetchData(this.props.fetchUsers);
    }

    this.fetchAndSetLineItems();
  }

  /**
   * Returns proper fields depending on origin type or if stock list is chosen.
   * @public
   */
  getFields() {
    if (this.state.values.origin.type === 'SUPPLIER') {
      return VENDOR_FIELDS;
    } else if (this.state.values.stockList) {
      return STOCKLIST_FIELDS;
    }

    return NO_STOCKLIST_FIELDS;
  }

  /**
   * Returns an array of new stock movement's items and items to be
   * updated (comparing to previous state of line items).
   * @param {object} lineItems
   * @public
   */
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

    if (this.state.values.origin.type === 'SUPPLIER') {
      return [].concat(
        _.map(lineItemsToBeAdded, item => ({
          'product.id': item.product.id,
          quantityRequested: item.quantityRequested,
          palletName: item.palletName,
          boxName: item.boxName,
          lotNumber: item.lotNumber,
          expirationDate: item.expirationDate,
          'recipient.id': item.recipient ? item.recipient.id : '',
        })),
        _.map(lineItemsToBeUpdated, item => ({
          id: item.id,
          'product.id': item.product.id,
          quantityRequested: item.quantityRequested,
          palletName: item.palletName,
          boxName: item.boxName,
          lotNumber: item.lotNumber,
          expirationDate: item.expirationDate,
          'recipient.id': item.recipient ? item.recipient.id : '',
        })),
      );
    }

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

  /**
   * Fetches stock movement's line items and sets them in redux form and in
   * state as current line items.
   * @public
   */
  fetchAndSetLineItems() {
    this.fetchLineItems().then((resp) => {
      const { statusCode, lineItems } = resp.data.data;
      let lineItemsData;
      if (!lineItems.length) {
        lineItemsData = new Array(1).fill({});
      } else {
        lineItemsData = _.map(
          lineItems,
          val => ({
            ...val,
            disabled: true,
            rowKey: _.uniqueId('lineItem_'),
            product: {
              ...val.product,
              label: `${val.productCode} ${val.product.name}`,
            },
          }),
        );
      }

      this.setState({
        currentLineItems: lineItems,
        statusCode,
        values: { ...this.state.values, lineItems: lineItemsData },
      });

      this.props.hideSpinner();
    }).catch(() => this.props.hideSpinner());
  }

  /**
   * Fetches 2nd step data from current stock movement.
   * @public
   */
  fetchLineItems() {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}?stepNumber=2`;

    return apiClient.get(url)
      .then(resp => resp)
      .catch(err => err);
  }

  /**
   * Fetches data using function given as an argument.
   * @param {function} fetchFunction
   * @public
   */
  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Saves current stock movement progress (line items) and goes to the next stock movement step.
   * @param {object} formValues
   * @public
   */
  nextPage(formValues) {
    const lineItems = _.filter(formValues.lineItems, val => !_.isEmpty(val));

    if (formValues.origin.type === 'SUPPLIER') {
      this.props.showSpinner();
      this.saveRequisitionItems(lineItems)
        .then((resp) => {
          let values = formValues;
          if (resp) {
            values = { ...formValues, lineItems: resp.data.data.lineItems };
          }
          if (this.state.statusCode === 'CREATED' || this.state.statusCode === 'EDITING') {
            this.transitionToNextStep('PICKED')
              .then(() => {
                this.props.goToPage(5, values);
              })
              .catch(() => this.props.hideSpinner());
          } else {
            this.props.goToPage(5, values);
          }
        })
        .catch(() => this.props.hideSpinner());
    } else {
      this.props.showSpinner();
      this.saveRequisitionItems(lineItems)
        .then((resp) => {
          let values = formValues;
          if (resp) {
            values = { ...formValues, lineItems: resp.data.data.lineItems };
          }
          if (this.state.statusCode === 'CREATED' || this.state.statusCode === 'EDITING') {
            this.transitionToNextStep('VERIFYING')
              .then(() => {
                this.props.onSubmit(values);
              })
              .catch(() => this.props.hideSpinner());
          } else {
            this.props.onSubmit(values);
          }
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Saves list of stock movement items with post method.
   * @param {object} lineItems
   * @public
   */
  saveRequisitionItems(lineItems) {
    const itemsToSave = this.getLineItemsToBeSaved(lineItems);
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      return apiClient.post(updateItemsUrl, payload)
        .catch(() => Promise.reject(new Error('Could not save requisition items')));
    }

    return Promise.resolve();
  }

  /**
   * Saves list of requisition items in current step (without step change). Used to export template.
   * @param {object} itemCandidatesToSave
   * @public
   */
  saveRequisitionItemsInCurrentStep(itemCandidatesToSave) {
    const itemsToSave = this.getLineItemsToBeSaved(itemCandidatesToSave);
    const updateItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    const payload = {
      id: this.state.values.stockMovementId,
      lineItems: itemsToSave,
    };

    if (payload.lineItems.length) {
      return apiClient.post(updateItemsUrl, payload)
        .then((resp) => {
          const { statusCode, lineItems } = resp.data.data;

          const lineItemsBackendData = _.map(
            lineItems,
            val => ({
              ...val,
              product: {
                ...val.product,
                label: `${val.productCode} ${val.product.name}`,
              },
            }),
          );
          this.setState({ values: { ...this.state.values, lineItems: lineItemsBackendData } });

          this.setState({
            currentLineItems: lineItemsBackendData,
            statusCode,
          });
        })
        .catch(() => Promise.reject(new Error('Could not save requisition items')));
    }

    return Promise.resolve();
  }

  /**
   * Removes chosen item from requisition's items list.
   * @param {string} itemId
   * @public
   */
  removeItem(itemId) {
    const removeItemsUrl = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}`;
    const payload = {
      id: this.state.values.stockMovementId,
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

  /**
   * Transition to next stock movement status:
   * - 'PICKED' if origin type is supplier.
   * - 'VERIFYING' if origin type is other than supplier.
   * @param {string} status
   * @public
   */
  transitionToNextStep(status) {
    const url = `/openboxes/api/stockMovements/${this.state.values.stockMovementId}/status`;
    const payload = { status };

    return apiClient.post(url, payload);
  }

  /**
   * Exports current state of stock movement's to csv file.
   * @param {object} formValues
   * @public
   */
  exportTemplate(formValues) {
    this.props.showSpinner();

    const lineItems = _.filter(formValues.lineItems, item =>
      !_.isEmpty(item) && !_.isNil(item.quantityRequested));

    const { movementNumber, stockMovementId } = formValues;
    const url = `/openboxes/stockMovement/exportCsv/${stockMovementId}`;
    return this.saveRequisitionItemsInCurrentStep(lineItems)
      .then(() => {
        apiClient.get(url, { responseType: 'blob' })
          .then((response) => {
            fileDownload(response.data, `ItemList${movementNumber ? `-${movementNumber}` : ''}.csv`, 'text/csv');
            this.props.hideSpinner();
          })
          .catch(() => this.props.hideSpinner());
      });
  }

  /**
   * Imports chosen file to backend and then fetches line items.
   * @param {object} event
   * @public
   */
  importTemplate(event) {
    this.props.showSpinner();
    const formData = new FormData();
    const file = event.target.files[0];
    const { stockMovementId } = this.state.values;

    formData.append('importFile', file);
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    const url = `/openboxes/stockMovement/importCsv/${stockMovementId}`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.props.hideSpinner();
        this.fetchAndSetLineItems();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  render() {
    const { previousPage } = this.props;
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        validate={validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
          <div className="d-flex flex-column">
            <span>
              <label
                htmlFor="csvInput"
                className="float-right py-1 mb-1 btn btn-outline-secondary align-self-end ml-1"
              >
                <span><i className="fa fa-download pr-2" />Import Template</span>
                <input
                  id="csvInput"
                  type="file"
                  style={{ display: 'none' }}
                  onChange={this.importTemplate}
                  accept=".csv"
                />
              </label>
              <button
                type="button"
                onClick={() => this.exportTemplate(values)}
                className="float-right py-1 mb-1 btn btn-outline-secondary align-self-end"
              >
                <span><i className="fa fa-upload pr-2" />Export Template</span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              {_.map(this.getFields(), (fieldConfig, fieldName) =>
                renderFormField(fieldConfig, fieldName, {
                  stockList: values.stockList,
                  recipients: this.props.recipients,
                  removeItem: this.removeItem,
                }))}
              <div>
                <button type="button" className="btn btn-outline-primary btn-form" onClick={() => previousPage(values)}>
                  Previous
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form float-right"
                  disabled={!_.some(values.lineItems, item => !_.isEmpty(item))}
                >Next
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  recipients: state.users.data,
  recipientsFetched: state.users.fetched,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(AddItemsPage));

AddItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function taking user to specified page */
  goToPage: PropTypes.func.isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  onSubmit: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching users */
  fetchUsers: PropTypes.func.isRequired,
  /** Array of available recipients  */
  recipients: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  /** Indicator if recipients' data is fetched */
  recipientsFetched: PropTypes.bool.isRequired,
};
