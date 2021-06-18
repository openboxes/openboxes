import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import update from 'immutability-helper';

import { showSpinner, hideSpinner } from '../../actions';
import Translate from '../../utils/Translate';
import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import accountingFormat from '../../utils/number-utils';
import apiClient from '../../utils/apiClient';
import InvoiceItemsModal from './InvoiceItemsModal';
import ButtonField from '../form-elements/ButtonField';
import TextField from '../form-elements/TextField';

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
    addButton: ({ values, loadMoreRows, saveInvoiceItems }) => (
      <InvoiceItemsModal
        btnOpenText="react.default.button.addLines.label"
        btnOpenDefaultText="Add lines"
        onOpen={() => saveInvoiceItems(values)}
        invoiceId={values.id}
        onResponse={loadMoreRows}
      >
        <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
      </InvoiceItemsModal>
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
          return { url: orderId ? `/openboxes/order/show/${orderId}` : '' };
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
          return { url: shipmentId ? `/openboxes/stockMovement/show/${shipmentId}` : '' };
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
      },
      quantity: {
        type: TextField,
        label: 'react.invoice.qty.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
        required: true,
        attributes: {
          type: 'number',
          showError: true,
        },
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
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
          formatValue: value => (value ? accountingFormat(value) : value),
        },
      },
      totalAmount: {
        type: LabelField,
        label: 'react.invoice.totalPrice.label',
        defaultMessage: 'Total Price',
        flexWidth: '1',
        attributes: {
          formatValue: value => (value ? accountingFormat(value) : value),
        },
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
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

    this.setState({
      values: {
        ...this.state.values,
        invoiceItems,
        totalCount,
        totalValue,
      },
    }, () => {
      if (!_.isNull(startIndex) &&
        this.state.values.invoiceItems.length !== this.state.values.totalCount) {
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
    const url = `/openboxes/api/invoices/${this.state.values.id}/items?offset=${startIndex}&max=${this.props.pageSize}`;
    apiClient.get(url)
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
    this.setState({
      values: {
        ...this.state.values,
        totalCount: this.state.values.totalCount + value,
      },
    });
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
    const url = `/openboxes/api/invoices/${this.state.values.id}/items`;
    const payload = {
      id: values.id,
      invoiceItems: _.map(values.invoiceItems, item => ({
        id: item.id,
        quantity: _.toInteger(item.quantity),
      })),
    };
    if (payload.invoiceItems.length) {
      return apiClient.post(url, payload)
        .catch(() => Promise.reject(new Error('react.invoice.error.saveInvoiceItems.label')));
    }
    return Promise.resolve();
  }

  /**
   * Saves invoices items and goes to the next step.
   * @param {object} values
   * @public
   */
  nextPage(values) {
    this.saveInvoiceItems(values).then(() => {
      this.props.nextPage(values);
    });
  }

  /**
   * Saves invoices items and goes to the previous step.
   * @param {object} values
   * @public
   */
  previousPage(values) {
    this.saveInvoiceItems(values).then(() => {
      this.props.previousPage(values);
    });
  }

  /**
   * Removes chosen item from items list.
   * @param {string} itemId
   * @public
   */
  removeItem(itemId, values, index) {
    const removeItemsUrl = `/openboxes/api/invoices/${itemId}/removeItem`;
    const item = values.invoiceItems[index];
    const newTotalValue = parseFloat(this.state.values.totalValue) - parseFloat(item.totalAmount);
    return apiClient.delete(removeItemsUrl)
      .then(() => {
        this.setState({
          values: {
            ...this.state.values,
            invoiceItems: update(this.state.values.invoiceItems, {
              $splice: [
                [index, 1],
              ],
            }),
            totalValue: newTotalValue,
          },
        });
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.invoice.error.deleteInvoiceItem.label'));
      });
  }

  saveAndExit(formValues) {
    this.saveInvoiceItems(formValues)
      .then(() => {
        window.location = `/openboxes/invoice/show/${formValues.id}`;
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <Form
        onSubmit={() => {}}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
          <div className="d-flex flex-column">
            <span className="buttons-container">
              <button
                type="button"
                className="btn btn-outline-secondary float-right btn-form btn-xs"
                onClick={() => this.saveAndExit(values)}
              >
                <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
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
                    updateRow: this.updateRow,
                    saveInvoiceItems: this.saveInvoiceItems,
                  }))}
              </div>
              <div className="font-weight-bold float-right mr-5er e mt-1">
                <Translate id="react.default.total.label" defaultMessage="Total" />:&nbsp;
                {this.state.values.totalValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} {this.state.values.currencyUom.code}
              </div>
              &nbsp;
              <div className="submit-buttons">
                <button
                  onClick={() => this.previousPage(values)}
                  className="btn btn-outline-primary btn-form btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
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

const mapStateToProps = state => ({
  pageSize: state.session.pageSize,
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
};
