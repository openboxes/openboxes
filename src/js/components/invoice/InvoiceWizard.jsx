import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import CreateInvoicePage from './CreateInvoicePage';
import AddItemsPage from './AddItemsPage';
import ConfirmInvoicePage from './ConfirmInvoicePage';
import Wizard from '../wizard/Wizard';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

import './Invoice.scss';

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
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'invoice');

    if (this.props.invoiceTranslationsFetched) {
      this.dataFetched = true;
      this.fetchInitialValues();
    }

    const {
      actionLabel, defaultActionLabel, actionUrl, listLabel, defaultListLabel, listUrl,
    } = this.props.breadcrumbsConfig;
    this.props.updateBreadcrumbs([
      { label: listLabel, defaultLabel: defaultListLabel, url: listUrl },
      { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
    ]);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'invoice');
    }

    if (nextProps.invoiceTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchInitialValues();
    }

    if (nextProps.breadcrumbsConfig &&
      nextProps.breadcrumbsConfig !== this.props.breadcrumbsConfig) {
      const {
        actionLabel, defaultActionLabel, actionUrl, listLabel, defaultListLabel, listUrl,
      } = nextProps.breadcrumbsConfig;

      this.props.updateBreadcrumbs([
        { label: listLabel, defaultLabel: defaultListLabel, url: listUrl },
        { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
      ]);
    }
  }

  getWizardTitle() {
    const { values } = this.state;
    if (!values.invoiceNumber) {
      return '';
    }
    return `${translateWithDefaultMessage('react.invoice.label, "Invoice"')} - ${values.invoiceNumber}`;
  }

  updateWizardValues(currentPage, values) {
    this.setState({ currentPage, values });
    if (values.invoiceNumber && values.id) {
      const {
        actionLabel, defaultActionLabel, actionUrl, listLabel, defaultListLabel, listUrl,
      } = this.props.breadcrumbsConfig;
      this.props.updateBreadcrumbs([
        { label: listLabel, defaultLabel: defaultListLabel, url: listUrl },
        { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
        { label: values.invoiceNumber, url: actionUrl, id: values.id },
      ]);
    }
  }

  /**
   * Returns array of form steps.
   * @public
   */
  stepList = [
    this.props.translate('react.invoice.create.label', 'Create'),
    this.props.translate('react.invoice.addItems.label', 'Add items'),
    this.props.translate('react.invoice.confirm.label', 'Confirm'),
  ];

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
    const title = this.getWizardTitle();

    return (
      <Wizard
        pageList={this.pageList}
        stepList={this.stepList}
        initialValues={values}
        title={title}
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
  breadcrumbsConfig: state.session.breadcrumbsConfig.invoice,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
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
  // Labels and url with translation
  breadcrumbsConfig: PropTypes.shape({
    actionLabel: PropTypes.string.isRequired,
    defaultActionLabel: PropTypes.string.isRequired,
    listLabel: PropTypes.string.isRequired,
    defaultListLabel: PropTypes.string.isRequired,
    listUrl: PropTypes.string.isRequired,
    actionUrl: PropTypes.string.isRequired,
  }),
  // Method to update breadcrumbs data
  updateBreadcrumbs: PropTypes.func.isRequired,
  fetchBreadcrumbsConfig: PropTypes.func.isRequired,
};

InvoiceWizard.defaultProps = {
  initialValues: {},
  breadcrumbsConfig: {
    actionLabel: '',
    defaultActionLabel: '',
    listLabel: '',
    defaultListLabel: '',
    listUrl: '',
    actionUrl: '',
  },
};
