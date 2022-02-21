import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import ConfigureProductCategories from './ConfigureProductCategories';
import ReviewCategories from './ReviewCategories';
import ConfigureProducts from './ConfigureProducts';
import Wizard from '../wizard/Wizard';
import { fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

import '../stock-movement-wizard/StockMovement.scss';

const SUPPORT_LINKS = {
  configureCategories: 'Configure Categories',
  reviewCategories: 'Review Categories',
  configureProducts: 'Configure Products',
};

class ProductsConfigurationWizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };
  }

  componentDidMount() {
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'productsConfiguration');

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
      this.props.fetchTranslations(nextProps.locale, 'productsConfiguration');
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

  getStepList() {
    return [
      this.props.translate('react.productsConfiguration.configureCategories.label', 'Configure Product Categories'),
      this.props.translate('react.productsConfiguration.reviewCategories.label', 'Review Categories'),
      this.props.translate('react.productsConfiguration.configureProducts.label', 'Configure Products'),
    ];
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
          locationId, location, history, supportLinks: SUPPORT_LINKS,
        }}
      />
    );
  }
}

const mapStateToProps = state => ({
  breadcrumbsConfig: state.session.breadcrumbsConfig.returns,
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
    shipmentStatus: PropTypes.string,
  }),
  breadcrumbsConfig: PropTypes.shape({
    actionLabel: PropTypes.string.isRequired,
    defaultActionLabel: PropTypes.string.isRequired,
    listLabel: PropTypes.string.isRequired,
    defaultListLabel: PropTypes.string.isRequired,
    listUrl: PropTypes.string.isRequired,
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
