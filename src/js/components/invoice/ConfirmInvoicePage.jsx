import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';

import { showSpinner, hideSpinner } from '../../actions';
import { renderFormField } from '../../utils/form-utils';
import accountingFormat from '../../utils/number-utils';
import DateField from '../form-elements/DateField';
import TextField from '../form-elements/TextField';
import LabelField from '../form-elements/LabelField';
import Translate from '../../utils/Translate';
import ArrayField from '../form-elements/ArrayField';
import apiClient from '../../utils/apiClient';

const INVOICE_HEADER_FIELDS = {
  invoiceNumber: {
    type: TextField,
    label: 'react.invoice.invoiceNumber.label',
    defaultMessage: 'Invoice number',
    attributes: {
      disabled: true,
    },
  },
  vendorName: {
    type: TextField,
    label: 'react.Invoice.vendor.label',
    defaultMessage: 'Vendor',
    attributes: {
      disabled: true,
    },
  },
  vendorInvoiceNumber: {
    type: TextField,
    label: 'react.invoice.vendorInvoiceNumber.label',
    defaultMessage: 'Vendor Invoice Number',
    attributes: {
      disabled: true,
    },
  },
  dateInvoiced: {
    type: DateField,
    label: 'react.invoice.invoiceDate.label',
    defaultMessage: 'Invoice Date',
    attributes: {
      disabled: true,
      dateFormat: 'MM/DD/YYYY',
    },
  },
  'currencyUom.code': {
    type: TextField,
    label: 'react.invoice.currency.label',
    defaultMessage: 'Currency',
    attributes: {
      disabled: true,
    },
  },
  totalValue: {
    type: TextField,
    label: 'react.invoice.total.label',
    defaultMessage: 'Total',
    attributes: {
      disabled: true,
    },
  },
};

const INVOICE_ITEMS = {
  invoiceItems: {
    type: ArrayField,
    virtualized: true,
    totalCount: ({ totalCount }) => totalCount,
    isRowLoaded: ({ isRowLoaded }) => isRowLoaded,
    loadMoreRows: ({ loadMoreRows }) => loadMoreRows(),
    isFirstPageLoaded: ({ isFirstPageLoaded }) => isFirstPageLoaded,
    fields: {
      orderNumber: {
        type: LabelField,
        label: 'react.invoice.orderNumber.label',
        defaultMessage: 'PO Number',
        flexWidth: '1',
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
        flexWidth: '1',
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
        type: LabelField,
        label: 'react.invoice.qty.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
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
    },
  },
};

const PREPAYMENT_INVOICE = 'PREPAYMENT_INVOICE';

class ConfirmInvoicePage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: { ...this.props.initialValues, invoiceItems: [], totalValue: 0 },
      isFirstPageLoaded: false,
    };

    this.isRowLoaded = this.isRowLoaded.bind(this);
    this.loadMoreRows = this.loadMoreRows.bind(this);
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
        totalValue: totalValue.toFixed(2),
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

  submitInvoice() {
    const url = `/openboxes/api/invoices/${this.state.values.id}/submit`;
    apiClient.post(url)
      .then(() => {
        window.location = `/openboxes/invoice/show/${this.state.values.id}`;
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <div>
        <Form
          onSubmit={() => {}}
          validate={this.validate}
          initialValues={this.state.values}
          mutators={{ ...arrayMutators }}
          render={({ handleSubmit, values }) => (
            <form onSubmit={handleSubmit}>
              <div className="classic-form classic-form-condensed">
                {_.map(INVOICE_HEADER_FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    values,
                    totalCount: this.state.values.totalCount,
                    loadMoreRows: this.loadMoreRows,
                    isRowLoaded: this.isRowLoaded,
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                  }))}
              </div>
              <div className="submit-buttons">
                <button
                  className="btn btn-outline-primary btn-form btn-xs"
                  onClick={() => this.props.previousPage(this.state.values)}
                  disabled={this.state.values.dateSubmitted ||
                    this.state.values.invoiceType === PREPAYMENT_INVOICE ||
                    this.state.values.hasPrepaymentInvoice}
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  onClick={() => { this.submitInvoice(); }}
                  className="btn btn-outline-success float-right btn-form btn-xs"
                  disabled={this.state.values.dateSubmitted}
                >
                  <Translate id="react.invoice.post.label" defaultMessage="Post Invoice" />
                </button>
              </div>
              <div className="my-2 table-form">
                {_.map(INVOICE_ITEMS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    values: this.state.values,
                    totalCount: this.state.values.totalCount,
                    loadMoreRows: this.loadMoreRows,
                    isRowLoaded: this.isRowLoaded,
                    isFirstPageLoaded: this.state.isFirstPageLoaded,
                    updateTotalCount: this.updateTotalCount,
                  }))}
              </div>
            </form>
          )}
        />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  pageSize: state.session.pageSize,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(ConfirmInvoicePage);

ConfirmInvoicePage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  pageSize: PropTypes.number.isRequired,
};
