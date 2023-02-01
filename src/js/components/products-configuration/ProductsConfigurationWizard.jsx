import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
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
    this.props.fetchTranslations('', 'productsConfiguration');
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'productsConfiguration');
    }
  }

  get stepList() {
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
    const { location, history } = this.props;
    const locationId = location.id;

    return (
      <Wizard
        pageList={pageList}
        stepList={this.stepList}
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
  locale: state.session.activeLanguage,
  location: state.session.currentLocation,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  fetchTranslations,
})(ProductsConfigurationWizard);

ProductsConfigurationWizard.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    categoriesImported: PropTypes.bool,
  }),
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
};
