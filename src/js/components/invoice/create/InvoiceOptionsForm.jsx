import React, { useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import DocumentButton from 'components/DocumentButton';
import DateField from 'components/form-elements/DateField';
import TextField from 'components/form-elements/TextField';
import { INVOICE_URL } from 'consts/applicationUrls';
import { renderFormField } from 'utils/form-utils';
import Translate from 'utils/Translate';

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

const InvoiceOptionsForm = ({
  values,
  disableSaveButton,
  updateInvoiceItem,
  canUpdateInvoiceItems,
  save,
}) => {
  const [isDropdownVisible, setIsDropdownVisible] = useState(false);

  const toggleDropdown = () => {
    setIsDropdownVisible((state) => !state);
  };

  const redirectToShowPage = () => {
    window.location = INVOICE_URL.show(values.id);
  };

  return (
    <div className="classic-form classic-form-condensed">
      <span className="buttons-container classic-form-buttons">
        <button
          type="button"
          className="btn btn-outline-secondary float-right btn-form btn-xs"
          disabled={disableSaveButton}
          onClick={() => {
            if (canUpdateInvoiceItems) {
              updateInvoiceItem(redirectToShowPage);
              return;
            }
            redirectToShowPage();
          }}
        >
          <span>
            <i className="fa fa-sign-out pr-2" />
            <Translate
              id="react.default.button.saveAndExit.label"
              defaultMessage="Save and exit"
            />
          </span>
        </button>
        {canUpdateInvoiceItems && (
        <button
          type="button"
          className="btn btn-outline-secondary float-right btn-form btn-xs"
          disabled={disableSaveButton}
          onClick={() => save()}
        >
          <span>
            <i className="fa fa-save pr-2" />
            <Translate
              id="react.default.button.save.label"
              defaultMessage="Save"
            />
          </span>
        </button>
        )}
        <span className="mr-3">
          <div className="dropdown">
            <button
              type="button"
              onClick={toggleDropdown}
              className="dropdown-button float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
            >
              <span>
                <i className="fa fa-sign-out pr-2" />
                <Translate
                  id="react.default.button.download.label"
                  defaultMessage="Download"
                />
              </span>
            </button>
            <div className={`dropdown-content print-buttons-container col-md-3 flex-grow-1
            ${isDropdownVisible ? 'visible' : ''}`}
            >
              {values.documents && values.documents.length > 0
              && _.map(values.documents, (document, idx) => {
                if (document.hidden) {
                  return null;
                }
                return (
                  <DocumentButton
                    link={document.link}
                    buttonTitle={document.name}
                    {...document}
                    key={idx}
                    disabled={false}
                  />
                );
              })}
            </div>
          </div>
        </span>
      </span>
      <div className="form-title">
        <Translate id="react.invoice.options.label" defaultMessage="Invoice options" />
      </div>
      {
        _.map(INVOICE_HEADER_FIELDS, (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, { values }))
      }
    </div>
  );
};

InvoiceOptionsForm.propTypes = {
  values: PropTypes.shape({
    id: PropTypes.string.isRequired,
    documents: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  }).isRequired,
  disableSaveButton: PropTypes.bool.isRequired,
  updateInvoiceItem: PropTypes.func.isRequired,
  canUpdateInvoiceItems: PropTypes.bool.isRequired,
  save: PropTypes.func.isRequired,
};

export default InvoiceOptionsForm;
