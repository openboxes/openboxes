import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchBreadcrumbsConfig, fetchTranslations, updateBreadcrumbs } from 'actions';
import ConfigureProductCategories from 'components/products-configuration/ConfigureProductCategories';
import ConfigureProducts from 'components/products-configuration/ConfigureProducts';
import ReviewCategories from 'components/products-configuration/ReviewCategories';
import Wizard from 'components/wizard/Wizard';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-movement-wizard/StockMovement.scss';


class ProductsConfigurationWizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: {
        categoriesImported: false,
      },
      currentPage: 1,
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'productsConfiguration');

    const {
      actionLabel, defaultActionLabel, actionUrl,
    } = this.props.breadcrumbsConfig;
    this.props.updateBreadcrumbs([
      { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
    ]);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'productsConfiguration');
    }

    if (nextProps.breadcrumbsConfig &&
      nextProps.breadcrumbsConfig !== this.props.breadcrumbsConfig) {
      const {
        actionLabel, defaultActionLabel, actionUrl,
      } = nextProps.breadcrumbsConfig;

      this.props.updateBreadcrumbs([
        { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
      ]);
    }
  }

  getStepList() {
    return [
      this.props.translate('react.productsConfiguration.configureCategories.label', 'Configure Product Categories'),
      this.props.translate('react.productsConfiguration.reviewCategories.label', 'Review Categories'),
      this.props.translate('react.productsConfiguration.configureProducts.label', 'Configure Products'),
    ];
  }

  updateWizardValues(currentPage, values) {
    if (values.categoriesImported) {
      this.setState({ currentPage, values });
    } else {
      this.setState({ currentPage, values: this.props.initialValues });
    }
  }

  render() {
    const { values, currentPage } = this.state;
    const pageList = [ConfigureProductCategories, ReviewCategories, ConfigureProducts];
    const stepList = this.getStepList();
    const { location, history } = this.props;
    const locationId = location.id;

    return (
      <Wizard
        pageList={pageList}
        stepList={stepList}
        initialValues={values}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
        additionalProps={{
          locationId, location, history,
        }}
        updateWizardValues={this.updateWizardValues}
        showStepNumber
      />
    );
  }
}

const mapStateToProps = state => ({
  breadcrumbsConfig: state.session.breadcrumbsConfig.productsConfiguration,
  locale: state.session.activeLanguage,
  location: state.session.currentLocation,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
})(ProductsConfigurationWizard);

ProductsConfigurationWizard.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    categoriesImported: PropTypes.bool,
  }),
  breadcrumbsConfig: PropTypes.shape({
    actionLabel: PropTypes.string.isRequired,
    defaultActionLabel: PropTypes.string.isRequired,
    actionUrl: PropTypes.string.isRequired,
  }),
  updateBreadcrumbs: PropTypes.func.isRequired,
  fetchBreadcrumbsConfig: PropTypes.func.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  location: PropTypes.shape({
    name: PropTypes.string,
    id: PropTypes.string,
    locationType: PropTypes.shape({
      description: PropTypes.string,
      locationTypeCode: PropTypes.string,
    }),
  }).isRequired,
};

ProductsConfigurationWizard.defaultProps = {
  initialValues: { categoriesImported: false },
  breadcrumbsConfig: {
    actionLabel: '',
    defaultActionLabel: '',
    actionUrl: '',
  },
};
