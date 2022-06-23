import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import apiClient, { flattenRequest } from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';


class AddBinModal extends Component {
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
    apiClient.post('/openboxes/api/locations/', flattenRequest({
      ...values,
      parentLocation: { id: this.props.locationId },
      locationType: { id: values.locationType.id },
      zone: values.zoneLocation && { id: values.zoneLocation.id },
    }))
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.locationsConfiguration.addBin.success.label', 'Bin location has been created successfully!'), { timeout: 3000 });
        this.props.addBinLocation();
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.addBin.error.label', 'Could not add bin location')));
      });
  }

  render() {
    return (
      <ModalWrapper
        onSave={values => this.handleSubmit(values)}
        fields={this.props.FIELDS}
        validate={this.props.validate}
        initialValues={this.props.binTypes.length === 1 ?
          { ...this.state.values, locationType: this.props.binTypes[0] }
          : this.state.values}
        formProps={{
          binTypes: this.props.binTypes,
          zoneData: this.props.zoneData,
        }}
        title="react.locationsConfiguration.addBin.label"
        defaultTitleMessage="Add Bin Location"
        btnSaveDefaultText="Save"
        btnOpenClassName="btn btn-outline-primary add-zonebin-btn"
        btnOpenText="react.locationsConfiguration.addBin.label"
        btnOpenDefaultText="Add Bin Location"
        btnOpenIcon="fa-plus"
        btnContainerClassName="d-flex justify-content-end"
        btnContainerStyle={{ gap: '3px' }}
        btnSaveClassName="btn btn-primary"
        btnCancelClassName="btn btn-outline-primary"
      >
        <div className="form-subtitle mb-lg-4">
          <Translate
            id="react.locationsConfiguration.editBin.additionalTitle.label"
            defaultMessage="Bin locations represent a physical storage location within a depot.
                            Bins are defined by a unique name or code that indicates the position within the depot.
                            Common bin names might include a pallet position number, rack number, or shelf action."
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

AddBinModal.propTypes = {
  locationId: PropTypes.string.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  addBinLocation: PropTypes.func.isRequired,
  FIELDS: PropTypes.shape({}).isRequired,
  validate: PropTypes.func.isRequired,
  binTypes: PropTypes.shape([]).isRequired,
  zoneData: PropTypes.shape([]).isRequired,
};

export default connect(mapStateToProps, mapDispatchToProps)(AddBinModal);
