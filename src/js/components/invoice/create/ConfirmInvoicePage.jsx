import React, {
  useCallback, useEffect, useMemo, useState,
} from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { useSelector } from 'react-redux';

import invoiceApi from 'api/services/InvoiceApi';
import InvoiceItemsTable from 'components/invoice/create/InvoiceItemsTable';
import InvoiceOptionsForm from 'components/invoice/create/InvoiceOptionsForm';
import InvoicePrepayedItemsTable from 'components/invoice/create/InvoicePrepayedItemsTable';
import { INVOICE_URL } from 'consts/applicationUrls';
import useSpinner from 'hooks/useSpinner';
import accountingFormat from 'utils/number-utils';
import Translate from 'utils/Translate';

const PREPAYMENT_INVOICE = 'PREPAYMENT_INVOICE';

const ConfirmInvoicePage = ({ initialValues, previousPage }) => {
  const spinner = useSpinner();

  const {
    pageSize,
    isSuperuser,
  } = useSelector((state) => ({
    pageSize: state.session.pageSize,
    isSuperuser: state.session.isSuperuser,
  }));

  const [stateValues, setStateValues] = useState({
    ...initialValues,
    invoiceItems: [],
  });

  /**
   * Fetches invoice values from API.
   * @public
   */
  const fetchInvoiceData = useCallback(() => {
    spinner.show();
    invoiceApi.getInvoice(stateValues.id)
      .then((response) => {
        setStateValues((state) => ({
          ...state,
          documents: response.data.data.documents,
        }));
      })
      .finally(() => spinner.hide());
  }, [stateValues.id]);

  useEffect(() => {
    if (stateValues.id) {
      fetchInvoiceData();
    }
  }, [stateValues.id]);

  const totalValue = useMemo(() => {
    const value = _.reduce(stateValues.invoiceItems, (sum, val) =>
      (sum + (val.totalAmount ? parseFloat(val.totalAmount) : 0.0)), 0);
    return accountingFormat(value.toFixed(2));
  }, [stateValues.invoiceItems]);

  const submitInvoice = () => {
    invoiceApi.submitInvoice(stateValues.id)
      .then(() => {
        window.location = INVOICE_URL.show(stateValues.id);
      })
      .finally(() => spinner.hide());
  };

  const postInvoice = () => {
    invoiceApi.postInvoice(stateValues.id)
      .then(() => {
        window.location = INVOICE_URL.show(stateValues.id);
      })
      .finally(() => spinner.hide());
  };

  /**
   * Sets state of invoice items after fetch and calls method to fetch next items
   * @param {string} startIndex
   * @public
   */
  const setInvoiceItems = (response) => {
    spinner.show();
    const { data, totalCount } = response.data;

    setStateValues((state) => ({
      ...state,
      invoiceItems: [
        ...state.invoiceItems,
        ...data,
      ],
      totalCount,
    }));

    spinner.hide();
  };

  /**
   * Loads more rows, needed for pagination
   * @param {index} startIndex
   * @public
   */
  const loadMoreRows = useCallback(
    ({ startIndex }) => invoiceApi.getInvoiceItems(stateValues.id, {
      params: { offset: startIndex, max: pageSize },
    })
      .then((response) => {
        setInvoiceItems(response);
      }),
    [stateValues.id, pageSize],
  );

  return (
    <div>
      <Form
        onSubmit={() => {}}
        initialValues={{
          ...stateValues,
          totalValue,
        }}
        mutators={{ ...arrayMutators }}
        render={({ handleSubmit, values }) => (
          <form onSubmit={handleSubmit}>
            <InvoiceOptionsForm values={values} />
            <div className="submit-buttons">
              <button
                type="button"
                className="btn btn-outline-primary btn-form btn-xs"
                onClick={() => previousPage(values)}
                disabled={values.datePosted
                    || values.invoiceType === PREPAYMENT_INVOICE || values.hasPrepaymentInvoice}
              >
                <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
              </button>
              <button
                type="submit"
                onClick={() => submitInvoice()}
                className="btn btn-outline-success float-right btn-form btn-xs"
                disabled={values.dateSubmitted || values.datePosted}
              >
                <Translate id="react.invoice.submit.label" defaultMessage="Submit for Approval" />
              </button>
              {isSuperuser
                  && (
                  <button
                    type="submit"
                    onClick={() => postInvoice()}
                    className="btn btn-outline-success float-right btn-form btn-xs"
                    disabled={values.datePosted}
                  >
                    <Translate id="react.invoice.post.label" defaultMessage="Post Invoice" />
                  </button>
                  )}
            </div>
            {
              stateValues.hasPrepaymentInvoice || stateValues.isPrepaymentInvoice ? (
                <InvoicePrepayedItemsTable
                  invoiceId={values.id}
                  totalCount={stateValues.totalCount}
                  invoiceItems={stateValues.invoiceItems}
                  loadMoreRows={loadMoreRows}
                  isPrepaymentInvoice={stateValues.isPrepaymentInvoice}
                />
              )
                : (
                  <InvoiceItemsTable
                    invoiceId={values.id}
                    totalCount={stateValues.totalCount}
                    invoiceItems={stateValues.invoiceItems}
                    loadMoreRows={loadMoreRows}
                  />
                )
            }
          </form>
        )}
      />
    </div>
  );
};

export default ConfirmInvoicePage;

ConfirmInvoicePage.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({}).isRequired,
  /** Function returning user to the previous page */
  previousPage: PropTypes.func.isRequired,
};
