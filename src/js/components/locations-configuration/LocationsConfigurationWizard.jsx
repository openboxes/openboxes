import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import Forecasting from 'components/locations-configuration/Forecasting';
import LocationAddress from 'components/locations-configuration/LocationAddress';
import LocationDetails from 'components/locations-configuration/LocationDetails';
import ZoneAndBinLocations from 'components/locations-configuration/ZoneAndBinLocations';
import Wizard from 'components/wizard/Wizard';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/stock-movement-wizard/StockMovement.scss';

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
      values: {},
      currentPage: 1,
    };

    this.updateWizardValues = this.updateWizardValues.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'locationsConfiguration');
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'locationsConfiguration');
    }
  }

  get stepList() {
    return [
      this.props.translate('react.locationsConfiguration.locationDetails.label', 'Details'),
      this.props.translate('react.locationsConfiguration.address.label', 'Address'),
      this.props.translate('react.locationsConfiguration.zoneAndBin.label', 'Zone and Bin Locations'),
      this.props.translate('react.locationsConfiguration.forecasting.label', 'Forecasting'),
    ];
  }

  updateWizardValues(currentPage, values) {
    this.setState({ currentPage, values });
  }

  render() {
    const { values, currentPage } = this.state;
    const pageList = [LocationDetails, LocationAddress, ZoneAndBinLocations, Forecasting];
    const { history } = this.props;

    return (
      <Wizard
        pageList={pageList}
        stepList={this.stepList}
        initialValues={values}
        currentPage={currentPage}
        prevPage={currentPage === 1 ? 1 : currentPage - 1}
        updateWizardValues={this.updateWizardValues}
        additionalProps={{
          history, supportLinks: SUPPORT_LINKS,
        }}
      />
    );
  }
}

const mapStateToProps = (state) => ({
  locale: state.session.activeLanguage,
  location: state.session.currentLocation,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  fetchTranslations,
})(LocationsConfigurationWizard);

LocationsConfigurationWizard.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};
