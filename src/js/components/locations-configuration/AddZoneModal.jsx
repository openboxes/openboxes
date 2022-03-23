import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import CheckboxField from 'components/form-elements/CheckboxField';
import TextField from 'components/form-elements/TextField';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'components/locations-configuration/AddZoneModal.scss';

const FIELDS = {
  active: {
    type: CheckboxField,
    label: 'react.locationsConfiguration.addZone.status.label',
    defaultMessage: 'Status',
    attributes: {
      withLabel: true,
      label: 'Active',
    },
  },
  name: {
    type: TextField,
    label: 'react.locationsConfiguration.name.label',
    defaultMessage: 'Name',
    attributes: {
      required: true,
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.addZone.name.tooltip.label',
    },
  },
};

const validate = (values) => {
  const requiredFields = ['name'];
  return Object.keys(FIELDS)
    .reduce((acc, fieldName) => {
      if (!values[fieldName] && requiredFields.includes(fieldName)) {
        return {
          ...acc,
          [fieldName]: 'react.default.error.requiredField.label',
        };
      }
      return acc;
    }, {});
};


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
    apiClient.post('/openboxes/api/locations/', {
      ...values,
      'parentLocation.id': `${this.props.locationId}`,
      'locationType.id': 'ZONE',
    })
      .then((res) => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.locationsConfiguration.addZone.success.label', 'Zone location has been created successfully!'), { timeout: 3000 });
        this.props.addLocation(res.data.data, 'ZONE');
        this.props.setShowAddZoneModal();
      })
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.addZone.error.label', 'Could not add zone location')));
      });
  }

  render() {
    return (
      <div className="add-zone-modal-main">
        <div className="add-zone-modal-content">
          <Form
            onSubmit={values => this.handleSubmit(values)}
            initialValues={this.state.values}
            validate={validate}
            render={({ handleSubmit }) => (
              <form onSubmit={handleSubmit} className="w-100">
                <div className="classic-form with-description add-zone-modal-form">
                  <div className="form-title">
                    <Translate id="react.locationsConfiguration.addZone.withoutPlus.label" defaultMessage="Add Zone Location" />
                  </div>
                  <div className="form-subtitle mb-lg-4">
                    <Translate
                      id="react.locationsConfiguration.addZone.additionalTitle.label"
                      defaultMessage="Zones are large areas within a depot encompassing multiple bin locations.
                                    They may represent different rooms or buildings within a depot space.
                                    To remove a zone from your depot, uncheck the box to mark it as inactive."
                    />
                  </div>
                  {_.map(FIELDS, (fieldConfig, fieldName) =>
                    renderFormField(fieldConfig, fieldName))}
                  <div className="submit-buttons d-flex justify-content-between">
                    <button type="button" className="btn btn-outline-primary" onClick={this.props.setShowAddZoneModal}>Cancel</button>
                    <button type="submit" className="btn btn-primary">Save</button>
                  </div>
                </div>
              </form>
            )}
          />
        </div>
      </div>
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
  setShowAddZoneModal: PropTypes.func.isRequired,
  locationId: PropTypes.string.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  addLocation: PropTypes.func.isRequired,
};

export default connect(mapStateToProps, mapDispatchToProps)(AddZoneModal);
