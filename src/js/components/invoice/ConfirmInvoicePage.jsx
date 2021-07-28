import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import { Tooltip } from 'react-tippy';

import { showSpinner, hideSpinner } from '../../actions';
import { renderFormField } from '../../utils/form-utils';
import accountingFormat from '../../utils/number-utils';
import DateField from '../form-elements/DateField';
import TextField from '../form-elements/TextField';
import LabelField from '../form-elements/LabelField';
import Translate from '../../utils/Translate';
import ArrayField from '../form-elements/ArrayField';
import apiClient from '../../utils/apiClient';
import DocumentButton from '../DocumentButton';

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
    getDynamicAttr: ({ values }) => ({
      className: values && values.totalValue && (values.totalValue < 0 || values.totalValue.startsWith('(')) ? 'negative-value' : '',
    }),
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
    getDynamicRowAttr: ({ rowValues }) => ({ className: rowValues && rowValues.totalAmount && rowValues.totalAmount < 0 ? 'negative-value' : '' }),
    fields: {
      prepaymentIcon: {
        type: (params) => {
          const { values } = params;
          const hasItems = values && values.invoiceItems;
          const isPrepLine = hasItems && (values.isPrepaymentInvoice ||
              values.invoiceItems[params.rowIndex].isPrepaymentItem);
          if (isPrepLine) {
            return (
              <div className="d-flex align-items-center justify-content-center">
                <Tooltip
                  html="Prepayment"
                  theme="transparent"
                  delay="150"
                  duration="250"
                  hideDelay="50"
                >
                  {/* &#x24C5; = hexadecimal circled letter P */}
                  <b>&#x24C5;</b>
                </Tooltip>
              </div>
            );
          }
          return null;
        },
        label: '',
        defaultMessage: '',
        flexWidth: '0.25',
      },
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
        type: LabelField,
        label: 'react.invoice.qty.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
        getDynamicAttr: params => ({
          formatValue: () => {
            const { values } = params;
            const hasItems = values && values.invoiceItems;
            const isPrepLine = hasItems && values.invoiceItems[params.rowIndex].isPrepaymentItem;
            if (isPrepLine) {
              return params.fieldValue * (-1);
            }
            return params.fieldValue;
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
    this.toggleDropdown = this.toggleDropdown.bind(this);
  }

  componentDidMount() {
    this.fetchInvoiceData();
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
        totalValue: accountingFormat(totalValue.toFixed(2)),
      },
    }, () => {
      if (!_.isNull(startIndex) &&
          this.state.values.invoiceItems.length !== this.state.values.totalCount) {
        this.loadMoreRows({ startIndex: startIndex + this.props.pageSize });
      } else if (this.state.values.invoiceItems.length === this.state.values.totalCount
        && !this.state.values.isPrepaymentInvoice) {
        this.fetchPrepaymentItems();
      }
      this.props.hideSpinner();
    });
  }

  /**
   * Fetches invoice values from API.
   * @public
   */
  fetchInvoiceData() {
    if (this.state.values.id) {
      this.props.showSpinner();
      const url = `/openboxes/api/invoices/${this.state.values.id}`;
      apiClient.get(url)
        .then((response) => {
          const values = {
            ...this.state.values,
            documents: response.data.data.documents,
          };

          this.setState({ values }, () => this.props.hideSpinner());
        });
    }
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

  postInvoice() {
    const url = `/openboxes/api/invoices/${this.state.values.id}/post`;
    apiClient.post(url)
      .then(() => {
        window.location = `/openboxes/invoice/show/${this.state.values.id}`;
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchPrepaymentItems() {
    const url = `/openboxes/api/invoices/${this.state.values.id}/prepaymentItems`;
    apiClient.get(url)
      .then((response) => {
        const { data, totalCount } = response.data;
        const lineItemsData = _.map(
          data,
          val => ({
            ...val,
            totalAmount: val.totalPrepaymentAmount,
            isPrepaymentItem: true,
          }),
        );
        const invoiceItems = _.concat(this.state.values.invoiceItems, lineItemsData);
        const updatedTotalCount = this.state.values.totalCount + totalCount;
        const totalValue = _.reduce(invoiceItems, (sum, val) =>
          (sum + (val.totalAmount ? parseFloat(val.totalAmount) : 0.0)), 0);

        this.setState({
          values: {
            ...this.state.values,
            invoiceItems,
            totalCount: updatedTotalCount,
            totalValue: accountingFormat(totalValue.toFixed(2)),
          },
        });
      });
  }

  /**
   * Toggle the downloadable files
   */
  toggleDropdown() {
    this.setState({
      isDropdownVisible: !this.state.isDropdownVisible,
    });
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
                <span className="buttons-container classic-form-buttons">
                  <button
                    type="button"
                    className="btn btn-outline-secondary float-right btn-form btn-xs"
                    onClick={() => { window.location = `/openboxes/invoice/show/${values.id}`; }}
                  >
                    <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
                  </button>
                  <span className="mr-3">
                    <div className="dropdown">
                      <button
                        type="button"
                        onClick={this.toggleDropdown}
                        className="dropdown-button float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                      >
                        <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.download.label" defaultMessage="Download" /></span>
                      </button>
                      <div className={`dropdown-content print-buttons-container col-md-3 flex-grow-1
                        ${this.state.isDropdownVisible ? 'visible' : ''}`}
                      >
                        {this.state.values.documents && this.state.values.documents.length > 0 &&
                        _.map(this.state.values.documents, (document, idx) => {
                          if (document.hidden) {
                            return null;
                          }
                          return (<DocumentButton
                            link={document.link}
                            buttonTitle={document.name}
                            {...document}
                            key={idx}
                            disabled={false}
                          />);
                        })}
                      </div>
                    </div>
                  </span>
                </span>
                <div className="form-title"><Translate id="react.invoice.options.label" defaultMessage="Invoice options" /></div>
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
                  disabled={this.state.values.datePosted ||
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
                  <Translate id="react.invoice.submit.label" defaultMessage="Submit for Approval" />
                </button>
                {this.props.isSuperuser &&
                  <button
                    type="submit"
                    onClick={() => { this.postInvoice(); }}
                    className="btn btn-outline-success float-right btn-form btn-xs"
                    disabled={this.state.values.datePosted}
                  >
                    <Translate id="react.invoice.post.label" defaultMessage="Post Invoice" />
                  </button>}
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
  isSuperuser: state.session.isSuperuser,
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
  isSuperuser: PropTypes.bool.isRequired,
};
