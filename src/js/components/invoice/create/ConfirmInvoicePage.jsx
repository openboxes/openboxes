import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import invoiceApi from 'api/services/InvoiceApi';
import InvoiceItemsTable from 'components/invoice/create/InvoiceItemsTable';
import InvoiceOptionsForm from 'components/invoice/create/InvoiceOptionsForm';
import { INVOICE_URL } from 'consts/applicationUrls';
import accountingFormat from 'utils/number-utils';
import Translate from 'utils/Translate';

const PREPAYMENT_INVOICE = 'PREPAYMENT_INVOICE';

class ConfirmInvoicePage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: { ...this.props.initialValues, invoiceItems: [], totalValue: 0 },
    };

    this.loadMoreRows = this.loadMoreRows.bind(this);
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
      invoiceApi.getInvoice(this.state.values.id)
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
   * Loads more rows, needed for pagination
   * @param {index} startIndex
   * @public
   */
  loadMoreRows({ startIndex }) {
    invoiceApi.getInvoiceItems(this.state.values.id, {
      params: { offset: startIndex, max: this.props.pageSize },
    })
      .then((response) => {
        this.setInvoiceItems(response, startIndex);
      });
  }

  submitInvoice() {
    invoiceApi.submitInvoice(this.state.values.id)
      .then(() => {
        window.location = INVOICE_URL.show(this.state.values.id);
      })
      .catch(() => this.props.hideSpinner());
  }

  postInvoice() {
    invoiceApi.postInvoice(this.state.values.id)
      .then(() => {
        window.location = INVOICE_URL.show(this.state.values.id);
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchPrepaymentItems() {
    invoiceApi.getInvoicePrepaymentItems(this.state.values.id)
      .then((response) => {
        const { data } = response.data;
        const lineItemsData = _.map(
          _.filter(data, val => val.orderNumber === this.state.values.vendorInvoiceNumber),
          val => ({
            ...val,
            totalAmount: val.totalPrepaymentAmount,
            isPrepaymentItem: true,
          }),
        );
        const invoiceItems = _.concat(this.state.values.invoiceItems, lineItemsData);
        const updatedTotalCount = this.state.values.totalCount + lineItemsData.length;
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
              <InvoiceOptionsForm values={values} />
              <div className="submit-buttons">
                <button
                  className="btn btn-outline-primary btn-form btn-xs"
                  onClick={() => this.props.previousPage(values)}
                  disabled={values.datePosted
                    || values.invoiceType === PREPAYMENT_INVOICE || values.hasPrepaymentInvoice}
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  onClick={() => { this.submitInvoice(); }}
                  className="btn btn-outline-success float-right btn-form btn-xs"
                  disabled={values.dateSubmitted || values.datePosted}
                >
                  <Translate id="react.invoice.submit.label" defaultMessage="Submit for Approval" />
                </button>
                {this.props.isSuperuser &&
                  <button
                    type="submit"
                    onClick={() => { this.postInvoice(); }}
                    className="btn btn-outline-success float-right btn-form btn-xs"
                    disabled={values.datePosted}
                  >
                    <Translate id="react.invoice.post.label" defaultMessage="Post Invoice" />
                  </button>}
              </div>
              <InvoiceItemsTable
                values={values}
                loadMoreRows={this.loadMoreRows}
              />
            </form>
          )}
        />
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
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
