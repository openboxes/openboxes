import React, { Component } from 'react';

import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchCurrencies, hideSpinner, showSpinner } from 'actions';
import invoiceApi from 'api/services/InvoiceApi';
import DateField from 'components/form-elements/DateField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import { INVOICE_URL } from 'consts/applicationUrls';
import { renderFormField } from 'utils/form-utils';
import { debounceOrganizationsFetch } from 'utils/option-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

function validate(values) {
  const errors = {};

  if (!values.vendor) {
    errors.vendor = 'react.default.error.requiredField.label';
  }

  if (!values.vendorInvoiceNumber) {
    errors.vendorInvoiceNumber = 'react.default.error.requiredField.label';
  }

  if (!values.dateInvoiced) {
    errors.dateInvoiced = 'react.default.error.requiredField.label';
  } else {
    const dateInvoiced = moment(values.dateInvoiced, 'MM/DD/YYYY', true);
    if (!dateInvoiced.isValid()) {
      errors.dateInvoiced = 'react.default.error.invalid.date';
    }
  }

  if (!values.currencyUom) {
    errors.currencyUom = 'react.default.error.requiredField.label';
  }

  return errors;
}

const FIELDS = {
  // Invoice Number will be auto-generated after first save request
  invoiceNumber: {
    type: TextField,
    label: 'react.invoice.invoiceNumber.label',
    defaultMessage: 'Invoice number',
    attributes: {
      disabled: true,
    },
  },
  vendor: {
    type: SelectField,
    label: 'react.Invoice.vendor.label',
    defaultMessage: 'Vendor',
    attributes: {
      required: true,
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      options: [],
      filterOptions: (options) => options,
    },
    getDynamicAttr: ({ debouncedOrganizationsFetch }) => ({
      loadOptions: debouncedOrganizationsFetch,
    }),
  },
  vendorInvoiceNumber: {
    type: TextField,
    label: 'react.invoice.vendorInvoiceNumber.label',
    defaultMessage: 'Vendor Invoice Number',
    attributes: {
      required: true,
    },
  },
  dateInvoiced: {
    type: DateField,
    label: 'react.invoice.invoiceDate.label',
    defaultMessage: 'Invoice Date',
    attributes: {
      required: true,
      dateFormat: 'MM/DD/YYYY',
      autoComplete: 'off',
    },
  },
  currencyUom: {
    label: 'react.invoice.currency.label',
    defaultMessage: 'Currency',
    type: SelectField,
    attributes: {
      required: true,
      showValueTooltip: true,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ currencies }) => ({
      options: currencies,
    }),
  },
};

class CreateInvoicePage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: this.props.initialValues,
    };

    this.debounceOrganizationsFetch = debounceOrganizationsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
    );
  }

  componentDidMount() {
    // TODO: If full React, fetch only if not fetched yet
    this.props.fetchCurrencies();
  }

  checkInvoiceChange(newValues) {
    if (!this.state.id) {
      return false;
    }

    const {
      vendor, vendorInvoiceNumber, dateInvoiced, currencyUom,
    } = this.props.initialValues;
    const vendorSelected = newValues.vendor && vendor;
    const vendorCheck = vendorSelected && newValues.vendor.id !== vendor.id;

    const vendorInvoiceNumberCheck = vendorInvoiceNumber !== newValues.vendorInvoiceNumber;
    const dateInvoicedCheck = dateInvoiced !== newValues.dateInvoiced;

    const currencyUomSelected = newValues.currencyUom && currencyUom;
    const currencyUomCheck = currencyUomSelected && newValues.vendor.id !== vendor.id;

    return (vendorCheck || vendorInvoiceNumberCheck || dateInvoicedCheck || currencyUomCheck);
  }

  saveInvoice(values) {
    if (values.vendor && values.vendorInvoiceNumber && values.dateInvoiced && values.currencyUom) {
      this.props.showSpinner();

      const payload = {
        vendor: values.vendor.id,
        vendorInvoiceNumber: values.vendorInvoiceNumber,
        dateInvoiced: values.dateInvoiced,
        currencyUom: {
          id: values.currencyUom.id,
        },
      };

      invoiceApi.saveInvoice(values.id || '', payload)
        .then((response) => {
          if (response.data) {
            const resp = response.data.data;
            this.props.history.push(INVOICE_URL.edit(resp.id));
            this.props.nextPage({
              ...values,
              id: resp.id,
              vendorName: resp.vendorName,
              invoiceNumber: resp.invoiceNumber,
              totalCount: resp.totalCount,
              currencyUom: resp.currencyUom,
            });
            this.props.hideSpinner();
          }
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.invoice.error.createInvoice.label', 'Could not create invoice')));
        });
    }
  }

  resetToInitialValues() {
    this.setState({
      values: {},
    }, () => this.setState({
      values: this.props.initialValues,
    }));
  }

  nextPage(values) {
    const showModal = this.checkInvoiceChange(values);
    if (!showModal) {
      this.saveInvoice(values);
    } else {
      confirmAlert({
        title: this.props.translate('react.invoice.confirmChange.label', 'Confirm change'),
        message: this.props.translate(
          'react.invoice.confirmChange.label',
          'Do you want to change invoice data?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.no.label', 'No'),
            onClick: () => this.resetToInitialValues(),
          },
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => this.saveInvoice(values),
          },
        ],
      });
    }
  }

  render() {
    return (
      <Form
        onSubmit={(values) => this.nextPage(values)}
        validate={validate}
        initialValues={this.state.values}
        render={({ form, handleSubmit }) => (
          <form onSubmit={handleSubmit}>
            <div className="classic-form with-description">
              {_.map(
                FIELDS,
                (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  currencies: this.props.currencies,
                  debouncedOrganizationsFetch: this.debounceOrganizationsFetch,
                }),
              )}
            </div>
            <div className="submit-buttons">
              <button type="submit" className="btn btn-outline-primary float-right btn-xs">
                <Translate id="react.default.button.next.label" defaultMessage="Next" />
              </button>
            </div>
          </form>
        )}
      />
    );
  }
}

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currencies: state.unitOfMeasure.currency,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchCurrencies,
})(CreateInvoicePage));

CreateInvoicePage.propTypes = {
  initialValues: PropTypes.shape({
    vendor: PropTypes.shape({}),
    vendorInvoiceNumber: PropTypes.string,
    dateInvoiced: PropTypes.string,
    currencyUom: PropTypes.shape({}),
  }).isRequired,
  fetchCurrencies: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  translate: PropTypes.func.isRequired,
  currencies: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};
