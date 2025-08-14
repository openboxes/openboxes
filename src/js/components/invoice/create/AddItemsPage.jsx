import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import invoiceApi from 'api/services/InvoiceApi';
import invoiceItemApi from 'api/services/InvoiceItemApi';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import LabelField from 'components/form-elements/LabelField';
import TextField from 'components/form-elements/TextField';
import InvoiceItemsModal from 'components/invoice/create/InvoiceItemsModal';
import { INVOICE_URL, ORDER_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import { renderFormField } from 'utils/form-utils';
import { getInvoiceDescription } from 'utils/form-values-utils';
import accountingFormat from 'utils/number-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  label: 'react.default.button.delete.label',
  defaultMessage: 'Delete',
  flexWidth: '1',
  fieldKey: '',
  buttonLabel: 'react.default.button.delete.label',
  buttonDefaultMessage: 'Delete',
  getDynamicAttr: ({
    fieldValue, removeItem, updateTotalCount, values, rowIndex,
  }) => ({
    onClick: fieldValue && fieldValue.id ? () => {
      removeItem(fieldValue.id, values, rowIndex);
      updateTotalCount(-1);
    } : () => { updateTotalCount(-1); },
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const FIELDS = {
  invoiceItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    // eslint-disable-next-line react/prop-types
    addButton: ({ values, loadMoreRows, saveBeforeOpenInvoiceCandidates }) => (
      <InvoiceItemsModal
        onOpen={() => saveBeforeOpenInvoiceCandidates(values)}
        invoiceId={values.id}
        onResponse={loadMoreRows}
      />
    ),
    fields: {
      orderNumber: {
        type: LabelField,
        label: 'react.invoice.orderNumber.label',
        defaultMessage: 'PO Number',
        flexWidth: '1',
        getDynamicAttr: ({ values, rowIndex }) => {
          const orderId = values && values.invoiceItems
              && values.invoiceItems[rowIndex]
              && values.invoiceItems[rowIndex].orderId;
          return { url: orderId ? ORDER_URL.show(orderId) : '' };
        },
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
        flexWidth: '1',
        getDynamicAttr: ({ values, rowIndex }) => {
          const shipmentId = values && values.invoiceItems
              && values.invoiceItems[rowIndex]
              && values.invoiceItems[rowIndex].shipmentId;
          return { url: shipmentId ? STOCK_MOVEMENT_URL.show(shipmentId) : '' };
        },
      },
      budgetCode: {
        type: LabelField,
        label: 'react.invoice.budgetCode.label',
        defaultMessage: 'Budget Code',
        flexWidth: '1',
      },
      glCode: {
        type: LabelField,
        label: 'react.invoice.glCode.label',
        defaultMessage: 'GL Code',
        flexWidth: '1',
      },
      productCode: {
        type: LabelField,
        label: 'react.invoice.itemNo.label',
        defaultMessage: 'Item No',
        flexWidth: '1',
      },
      description: {
        type: LabelField,
        label: 'react.invoice.description.label',
        defaultMessage: 'Description',
        flexWidth: '5',
        attributes: {
          className: 'text-left',
        },
        getDynamicAttr: (params) => ({
          formatValue: () => {
            const { values, rowIndex } = params;
            const rowValue = values?.invoiceItems?.[rowIndex];
            // If it's not an adjustment, but product, and it has a synonym, display it
            // with a tooltip with the original name of the product
            return getInvoiceDescription(rowValue);
          },
        }),
      },
      quantity: {
        type: TextField,
        label: 'react.invoice.qty.label',
        defaultMessage: 'Qty',
        flexWidth: '1.1',
        required: true,
        attributes: {
          type: 'number',
          showError: true,
        },
        getDynamicAttr: ({
          rowIndex,
          values,
          updateRow,
          validateInvoiceItem,
          debouncedInvoiceItemValidation,
        }) => ({
          onBlur: () => {
            updateRow(values, rowIndex);
            validateInvoiceItem({
              invoiceItem: values.invoiceItems[rowIndex],
              rowIndex,
            });
          },
          onChange: (event) => {
            debouncedInvoiceItemValidation({
              invoiceItem: {
                ...values.invoiceItems[rowIndex],
                quantity: event,
              },
              rowIndex,
            });
          },
        }),
      },
      uom: {
        type: LabelField,
        label: 'react.invoice.uom.label',
        defaultMessage: 'UOM',
        flexWidth: '1',
      },
      unitPrice: {
        type: LabelField,
        label: 'react.invoice.unitPrice.label',
        defaultMessage: 'Unit Price',
        flexWidth: '1',
        attributes: {
          formatValue: (value) => (value ? accountingFormat(value) : value),
        },
      },
      amount: {
        type: LabelField,
        label: 'react.invoice.totalPrice.label',
        defaultMessage: 'Total Price',
        flexWidth: '1',
        attributes: {
          formatValue: (value) => (value ? accountingFormat(value) : value),
        },
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

const someItemsHaveZeroQuantity = (invoiceItems) =>
  _.some(invoiceItems, (item) => _.parseInt(item.quantity) === 0);

const allItemsHaveZeroQuantity = (invoiceItems) =>
  _.every(invoiceItems, (item) => _.parseInt(item.quantity) === 0);

const validate = (values) => {
  const errors = {};
  errors.invoiceItems = [];
  _.forEach(values?.invoiceItems, (item, key) => {
    if (_.isNil(item?.quantity)) {
      errors.invoiceItems[key] = { quantity: 'react.invoice.error.enterQuantity.label' };
    }
    if (_.has(item, 'isValid') && !item.isValid) {
      errors.invoiceItems[key] = { quantity: item?.errorMessage };
    }
  });

  return errors;
};

class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: { ...this.props.initialValues, invoiceItems: [], totalValue: 0 },
      isFirstPageLoaded: false,
    };

    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
    this.updateTotalCount = this.updateTotalCount.bind(this);
    this.removeItem = this.removeItem.bind(this);
    this.updateRow = this.updateRow.bind(this);
    this.saveInvoiceItems = this.saveInvoiceItems.bind(this);
    this.validateInvoiceItem = this.validateInvoiceItem.bind(this);
    this.confirmSave = this.confirmSave.bind(this);
    this.save = this.save.bind(this);
    this.saveBeforeOpenInvoiceCandidates = this.saveBeforeOpenInvoiceCandidates.bind(this);

    this.debouncedInvoiceItemValidation = _.debounce(this.validateInvoiceItem, 1000);
  }

  /**
   * Sets state of invoice items after fetch and calls method to fetch next items
   * @param {string} startIndex
   * @public
   */
  setInvoiceItems(response, startIndex) {
    this.props.showSpinner();
    const { data, totalCount } = response.data;

    const invoiceItems = _.isNull(startIndex) || startIndex === 0 ? data : _.uniqBy(_.concat(this.state.values.invoiceItems, data), 'id');
    const totalValue = _.reduce(invoiceItems, (sum, val) =>
      (sum + (val.totalAmount ? parseFloat(val.totalAmount) : 0.0)), 0);

    this.setState((prev) => ({
      values: {
        ...prev.values,
        invoiceItems,
        totalCount,
        totalValue,
      },
    }), () => {
      if (!_.isNull(startIndex)
        && this.state.values.invoiceItems.length !== this.state.values.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      }
      this.props.hideSpinner();
    });
  }

  /**
   * Checks if row is loaded, needed for pagination
   * @param {string} index
   * @public
   */
  isRowLoaded({ index }) {
    return !!this.state.values.invoiceItems[index];
  }

  /**
   * Loads more rows, needed for pagination
   * @param {index} startIndex
   * @public
   */
  loadMoreRows({ startIndex }) {
    this.setState({
      isFirstPageLoaded: true,
    });
    invoiceApi.getInvoiceItems(this.state.values.id, {
      params: {
        offset: startIndex,
        max: this.props.pageSize,
      },
    })
      .then((response) => {
        this.setInvoiceItems(response, startIndex);
      });
  }

  /**
   * Updates total count of items after removing item
   * @param {integer} value
   * @public
   */
  updateTotalCount(value) {
    this.setState((prev) => ({
      values: {
        ...prev.values,
        totalCount: prev.values.totalCount + value,
      },
    }));
  }

  /**
   * Updates row after changing value
   * @param {integer} value
   * @param {string} index
   * @public
   */
  updateRow(values, index) {
    const item = values.invoiceItems[index];
    this.setState({
      values: update(values, {
        invoiceItems: { [index]: { $set: item } },
      }),
    });
  }

  /**
   * Saves invoice items
   * @param {object} values
   * @public
   */
  saveInvoiceItems(values) {
    const payload = {
      id: values.id,
      invoiceItems: _.map(values.invoiceItems, (item) => ({
        id: item.id,
        quantity: _.toInteger(item.quantity),
      })),
    };
    if (payload.invoiceItems.length) {
      return invoiceApi.saveInvoiceItems(this.state.values.id, payload)
        .catch(() => Promise.reject(new Error('react.invoice.error.saveInvoiceItems.label')));
    }
    return Promise.resolve();
  }

  confirmSave(onConfirm) {
    confirmAlert({
      title: this.props.translate('react.invoice.message.confirmSave.label', 'Confirm save'),
      message: this.props.translate(
        'react.invoice.confirmSave.message',
        'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
   * Saves invoices items and goes to the next step.
   * @param {object} values
   * @public
   */
  nextPage(values) {
    if (someItemsHaveZeroQuantity(values.invoiceItems)) {
      this.confirmSave(() => {
        this.saveInvoiceItems(values).then(() => this.props.nextPage(values));
      });
    } else {
      this.saveInvoiceItems(values).then(() => this.props.nextPage(values));
    }
  }

  /**
   * Saves invoices items and goes to the previous step.
   * @param {object} values
   * @public
   */
  previousPage(values) {
    if (someItemsHaveZeroQuantity(values.invoiceItems)) {
      this.confirmSave(() => {
        this.saveInvoiceItems(values).then(() => this.props.previousPage(values));
      });
    } else {
      this.saveInvoiceItems(values).then(() => this.props.previousPage(values));
    }
  }

  /**
   * Removes chosen item from items list.
   * @param {string} itemId
   * @public
   */
  removeItem(itemId, values, index) {
    const item = values.invoiceItems[index];
    const newTotalValue = parseFloat(this.state.values.totalValue) - parseFloat(item.totalAmount);

    return invoiceApi.removeInvoiceItem(itemId)
      .then(() => {
        this.setState((prev) => ({
          values: {
            ...prev.values,
            invoiceItems: update(prev.values.invoiceItems, {
              $splice: [
                [index, 1],
              ],
            }),
            totalValue: newTotalValue,
          },
        }));
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.invoice.error.deleteInvoiceItem.label'));
      });
  }

  async validateInvoiceItem({
    invoiceItem,
    rowIndex,
  }) {
    this.debouncedInvoiceItemValidation.cancel();

    const { values } = this.state;
    try {
      await invoiceItemApi.validateInvoiceItem(_.pick(invoiceItem, ['quantity', 'id', 'shipmentId']));
      const updatedValues = update(values, {
        invoiceItems: {
          [rowIndex]: {
            isValid: { $set: true },
            quantity: { $set: invoiceItem.quantity },
          },
        },
      });

      this.setState({ values: updatedValues });
    } catch (err) {
      const updatedValues = update(values, {
        invoiceItems: {
          [rowIndex]: {
            isValid: { $set: false },
            errorMessage: { $set: err?.response?.data?.errorMessages?.[0] || '' },
            quantity: { $set: invoiceItem.quantity },
          },
        },
      });

      this.setState({ values: updatedValues });
    }
  }

  saveBeforeOpenInvoiceCandidates(values) {
    return new Promise((resolve) => {
      if (someItemsHaveZeroQuantity(values.invoiceItems)) {
        return this.confirmSave(() => {
          this.saveInvoiceItems(values).then(() => {
            this.loadMoreRows({ startIndex: 0 });
          }).finally(() => resolve());
        });
      }
      return this.saveInvoiceItems(values).finally(() => resolve());
    });
  }

  save(values) {
    if (someItemsHaveZeroQuantity(values.invoiceItems)) {
      this.confirmSave(() => {
        this.saveInvoiceItems(values).then(() => {
          this.loadMoreRows({ startIndex: 0 });
        });
      });

      return null;
    }
    return this.saveInvoiceItems(values).then(() => {
      // after saving invoice items, we need to reload the items list to see new total price
      this.loadMoreRows({ startIndex: 0 });
    });
  }

  saveAndExit(formValues) {
    if (someItemsHaveZeroQuantity(formValues.invoiceItems)) {
      this.confirmSave(() => {
        this.saveInvoiceItems(formValues).then(() => {
          window.location = INVOICE_URL.show(formValues.id);
        });
      });
    } else {
      this.saveInvoiceItems(formValues).then(() => {
        window.location = INVOICE_URL.show(formValues.id);
      });
    }
  }

  render() {
    return (
      <Form
        onSubmit={() => {}}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        validate={validate}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            <span className="buttons-container">
              <button
                type="button"
                className="btn btn-outline-secondary float-right btn-form btn-xs"
                disabled={invalid}
                onClick={() => this.save(values)}
              >
                <span>
                  <i className="fa fa-floppy-o pr-2" />
                  <Translate id="react.default.button.save.label" defaultMessage="Save" />
                </span>
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary float-right btn-form btn-xs"
                disabled={invalid}
                onClick={() => this.saveAndExit(values)}
              >
                <span>
                  <i className="fa fa-sign-out pr-2" />
                  <Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" />
                </span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    values,
                    totalCount: this.state.values.totalCount,
                    loadMoreRows: this.loadMoreRows,
                    isRowLoaded: this.isRowLoaded,
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                    updateTotalCount: this.updateTotalCount,
                    removeItem: this.removeItem,
                    saveBeforeOpenInvoiceCandidates: this.saveBeforeOpenInvoiceCandidates,
                    updateRow: this.updateRow,
                    validateInvoiceItem: this.validateInvoiceItem,
                    debouncedInvoiceItemValidation: this.debouncedInvoiceItemValidation,
                  }))}
              </div>
              <div className="font-weight-bold float-right mr-5er e mt-1">
                <Translate id="react.default.total.label" defaultMessage="Total" />
                :&nbsp;
                {this.state.values.totalValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                {' '}
                {this.state.values.currencyUom.code}
              </div>
              &nbsp;
              <div className="submit-buttons">
                <button
                  type="button"
                  onClick={() => this.previousPage(values)}
                  className="btn btn-outline-primary btn-form btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="button"
                  disabled={
                    invalid
                    || !values.invoiceItems?.length
                    || allItemsHaveZeroQuantity(values.invoiceItems)
                }
                  onClick={() => this.nextPage(values)}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                >
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = (state) => ({
  pageSize: state.session.pageSize,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default (connect(mapStateToProps, { showSpinner, hideSpinner })(AddItemsPage));

AddItemsPage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
  /** Number of page size needed for pagination */
  pageSize: PropTypes.number.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
