import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import apiClient, { flattenRequest } from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'components/locations-configuration/ZoneTable.scss';

class AddZoneModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: {
        active: true,
      },
    };
  }

  handleSubmit(values) {
    this.props.showSpinner();
    apiClient.post('/openboxes/api/locations/', flattenRequest({ ...values, parentLocation: { id: this.props.locationId }, locationType: { id: values.locationType.id } }))
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.locationsConfiguration.addZone.success.label', 'Zone location has been created successfully!'), { timeout: 3000 });
        this.props.addZoneLocation();
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.addZone.error.label', 'Could not add zone location')));
      });
  }

  render() {
    return (
      <ModalWrapper
        onSave={values => this.handleSubmit(values)}
        fields={this.props.FIELDS}
        validate={this.props.validate}
        initialValues={this.props.zoneTypes.length === 1 ?
          { ...this.state.values, locationType: this.props.zoneTypes[0] }
          : this.state.values}
        formProps={{
          zoneTypes: this.props.zoneTypes,
        }}
        title="react.locationsConfiguration.addZone.label"
        defaultTitleMessage="Add Zone Location"
        btnSaveDefaultText="Save"
        btnOpenClassName="btn btn-outline-primary add-zonebin-btn"
        btnOpenText="react.locationsConfiguration.addZone.label"
        btnOpenDefaultText="Add Zone Location"
        btnOpenIcon="fa-plus"
        btnContainerClassName="d-flex justify-content-end"
        btnContainerStyle={{ gap: '3px' }}
        btnSaveClassName="btn btn-primary"
        btnCancelClassName="btn btn-outline-primary"
      >
        <div className="form-subtitle mb-lg-4">
          <Translate
            id="react.locationsConfiguration.addZone.additionalTitle.label"
            defaultMessage="Zones are large areas within a depot encompassing multiple bin locations.
                                  They may represent different rooms or buildings within a depot space.
                                  To remove a zone from your depot, uncheck the box to mark it as inactive."
          />
        </div>
      </ModalWrapper>
    );
  }
}


const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
};

AddZoneModal.propTypes = {
  locationId: PropTypes.string.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  addZoneLocation: PropTypes.func.isRequired,
  FIELDS: PropTypes.shape({}).isRequired,
  validate: PropTypes.func.isRequired,
  zoneTypes: PropTypes.shape([]).isRequired,
};

export default connect(mapStateToProps, mapDispatchToProps)(AddZoneModal);
