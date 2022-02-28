import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import LocationDetails from './LocationDetails';
import LocationAddress from './LocationAddress';
import ZoneAndBinLocations from './ZoneAndBinLocations';
import Forecasting from './Forecasting';
import Wizard from '../wizard/Wizard';
import { fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

import '../stock-movement-wizard/StockMovement.scss';

const SUPPORT_LINKS = {
  locationDetails: 'Location Details',
  locationAddress: 'Address',
  zoneAndBinLocations: 'Zone and Bin Locations',
  forecasting: 'Forecasting',
};

class LocationsConfigurationWizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      values: this.props.initialValues,
      currentPage: 1,
    };
  }

  componentDidMount() {
    this.props.fetchBreadcrumbsConfig();
    this.props.fetchTranslations('', 'locationsConfiguration');

    const {
      actionLabel, defaultActionLabel, actionUrl,
    } = this.props.breadcrumbsConfig;
    this.props.updateBreadcrumbs([
      { label: actionLabel, defaultLabel: defaultActionLabel, url: actionUrl },
    ]);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'locationsConfiguration');
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
      this.props.translate('react.locationsConfiguration.locationDetails.label', 'Details'),
      this.props.translate('react.locationsConfiguration.address.label', 'Address'),
      this.props.translate('react.locationsConfiguration.zoneAndBin.label', 'Zone and Bin Locations'),
      this.props.translate('react.locationsConfiguration.forecasting.label', 'Forecasting'),
    ];
  }

  render() {
    const { values, currentPage } = this.state;
    const pageList = [LocationDetails, LocationAddress, ZoneAndBinLocations, Forecasting];
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
  breadcrumbsConfig: state.session.breadcrumbsConfig.locationsConfiguration,
  locale: state.session.activeLanguage,
  location: state.session.currentLocation,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  fetchTranslations, updateBreadcrumbs, fetchBreadcrumbsConfig,
})(LocationsConfigurationWizard);

LocationsConfigurationWizard.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({
    shipmentStatus: PropTypes.string,
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

LocationsConfigurationWizard.defaultProps = {
  initialValues: {},
  breadcrumbsConfig: {
    actionLabel: '',
    defaultActionLabel: '',
    actionUrl: '',
  },
};
