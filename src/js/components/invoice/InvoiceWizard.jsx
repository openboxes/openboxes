import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import AddItemsPage from 'components/invoice/AddItemsPage';
import ConfirmInvoicePage from 'components/invoice/ConfirmInvoicePage';
import CreateInvoicePage from 'components/invoice/CreateInvoicePage';
import Wizard from 'components/wizard/Wizard';
import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/invoice/Invoice.scss';


class InvoiceWizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'invoice');

    if (this.props.invoiceTranslationsFetched) {
      this.dataFetched = true;
      this.fetchInitialValues();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'invoice');
    }

    if (nextProps.invoiceTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchInitialValues();
    }
  }

  get wizardTitle() {
    const { values } = this.state;
    if (!values.invoiceNumber) {
      return '';
    }
    return [
      {
        text: this.props.translate('react.invoice.label', 'Invoice'),
        color: '#000000',
        delimeter: ' | ',
      },
      {
        text: values.invoiceNumber,
        color: '#000000',
        delimeter: '',
      },
    ];
  }

  /**
   * Returns array of form steps.
   * @public
   */
  get stepList() {
    return [
      this.props.translate('react.invoice.create.label', 'Create'),
      this.props.translate('react.invoice.addItems.label', 'Add items'),
      this.props.translate('react.invoice.confirm.label', 'Confirm'),
    ];
  }

  updateWizardValues(currentPage, values) {
    this.setState({ currentPage, values });
  }

  /**
   * Returns array of form's components.
   * @public
   */
  pageList = [CreateInvoicePage, AddItemsPage, ConfirmInvoicePage];

  dataFetched = false;

  /**
   * Fetches initial values from API.
   * @public
   */
  fetchInitialValues() {
    if (this.props.match.params.invoiceId) {
      this.props.showSpinner();
      const url = `/openboxes/api/invoices/${this.props.match.params.invoiceId}`;
      apiClient.get(url)
        .then((response) => {
          const values = {
            ...response.data.data,
            vendor: {
              id: response.data.data.vendor,
              label: response.data.data.vendorName,
            },
          };

          let currentPage = 2;
          if (values.totalCount > 0) {
            currentPage = 3;
          }

          this.setState({ values, currentPage }, () => this.props.hideSpinner());
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  render() {
    const { values, currentPage } = this.state;

    return (
      <Wizard
        pageList={this.pageList}
        stepList={this.stepList}
        initialValues={values}
        title={this.wizardTitle}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
        updateWizardValues={this.updateWizardValues}
      />
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  invoiceTranslationsFetched: state.session.fetchedTranslations.invoice,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(InvoiceWizard);

InvoiceWizard.propTypes = {
  /** React router's object which contains information about url variables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ invoiceId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Initial components' data */
  initialValues: PropTypes.shape({}),
  locale: PropTypes.string.isRequired,
  invoiceTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};

InvoiceWizard.defaultProps = {
  initialValues: {},
};
