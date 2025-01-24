import React from 'react';

import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';

import InvoiceItemsTable from 'components/invoice/create/InvoiceItemsTable';
import InvoiceOptionsForm from 'components/invoice/create/InvoiceOptionsForm';
import InvoicePrepayedItemsTable from 'components/invoice/create/InvoicePrepayedItemsTable';
import useConfirmInvoicePage from 'hooks/invoice/useConfirmInvoicePage';
import useInvoicePrepaidItemsTable from 'hooks/invoice/useInvoicePrepaidItemsTable';
import useSpinner from 'hooks/useSpinner';
import Translate from 'utils/Translate';

const PREPAYMENT_INVOICE = 'PREPAYMENT_INVOICE';

const ConfirmInvoicePage = ({ initialValues, previousPage }) => {
  const {
    isSuperuser,
    stateValues,
    totalValue,
    submitInvoice,
    postInvoice,
    updateInvoiceItemData,
    refetchData,
    loadMoreRows,
    invoiceItemsMap,
  } = useConfirmInvoicePage({ initialValues });

  const spinner = useSpinner();

  const invoicePrepaidItemsTableData = useInvoicePrepaidItemsTable({
    invoiceItems: stateValues.invoiceItems,
    invoiceId: stateValues.id,
    invoiceItemsMap,
    updateInvoiceItemData,
    refetchData,
  });

  const {
    isValid,
    updateRow,
    updateInvoiceItem,
    save,
  } = invoicePrepaidItemsTableData;

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
            <InvoiceOptionsForm
              values={values}
              updateInvoiceItem={updateInvoiceItem}
              refetchData={refetchData}
              save={save}
              canUpdateInvoiceItems={
                values.invoiceType !== PREPAYMENT_INVOICE
                && stateValues.hasPrepaymentInvoice
              }
              disableSaveButton={!isValid
                && values.invoiceType !== PREPAYMENT_INVOICE}
            />
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
                type="button"
                onClick={() => {
                  spinner.show();
                  if (
                    values.invoiceType !== PREPAYMENT_INVOICE
                    && stateValues.hasPrepaymentInvoice
                  ) {
                    updateInvoiceItem(submitInvoice);
                    return;
                  }
                  submitInvoice();
                }}
                className="btn btn-outline-success float-right btn-form btn-xs"
                disabled={
                values.dateSubmitted
                || values.datePosted
                || !stateValues.invoiceItems.length
                || (!isValid
                    && values.invoiceType !== PREPAYMENT_INVOICE)
              }
              >
                <Translate id="react.invoice.submit.label" defaultMessage="Submit for Approval" />
              </button>
              {isSuperuser
                  && (
                  <button
                    type="button"
                    onClick={() => {
                      spinner.show();
                      if (
                        values.invoiceType !== PREPAYMENT_INVOICE
                        && stateValues.hasPrepaymentInvoice
                      ) {
                        updateInvoiceItem(postInvoice);
                        return;
                      }
                      postInvoice();
                    }}
                    className="btn btn-outline-success float-right btn-form btn-xs"
                    disabled={
                    values.datePosted
                    || !stateValues.invoiceItems.length
                    || (!isValid
                        && values.invoiceType !== PREPAYMENT_INVOICE)
                  }
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
                  updateInvoiceItemData={
                    updateInvoiceItemData(updateRow)
                  }
                  invoicePrepaidItemsTableData={invoicePrepaidItemsTableData}
                  invoiceStatus={stateValues.status}
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
