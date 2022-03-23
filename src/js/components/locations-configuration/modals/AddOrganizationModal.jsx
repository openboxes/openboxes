import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import Modal from 'react-modal';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import Alert from 'react-s-alert';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import TextareaField from 'components/form-elements/TextareaField';
import TextField from 'components/form-elements/TextField';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'components/locations-configuration/modals/ConfigurationModal.scss';

function validate(values) {
  const errors = {};

  if (!values.name) {
    errors.name = 'react.default.error.requiredField.label';
  }

  return errors;
}

const FIELDS = {
  name: {
    type: TextField,
    label: 'react.locationsConfiguration.name.label',
    defaultMessage: 'Name',
    attributes: {
      required: true,
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.organizationName.tooltip.label',
    },
  },
  description: {
    type: TextareaField,
    label: 'react.locationsConfiguration.description.label',
    defaultMessage: 'Description',
    attributes: {
      rows: 3,
      withTooltip: true,
      tooltip: 'react.locationsConfiguration.organizationDescription.tooltip.label',
    },
  },
};

class AddOrganizationModal extends Component {
  save(values) {
    if (values.name) {
      this.props.showSpinner();

      const locationUrl = '/openboxes/api/organizations';

      const payload = {
        name: values.name,
        description: values.description,
      };

      apiClient.post(locationUrl, payload)
        .then((response) => {
          this.props.hideSpinner();
          Alert.success(this.props.translate('react.locationsConfiguration.alert.organizationSaveCompleted.label', 'Organization was successfully saved!'), { timeout: 3000 });
          const resp = response.data.data;
          this.props.onResponse({ id: resp.id, name: values.name });
          this.props.onClose();
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.locationsConfiguration.error.createOrganization.label', 'Could not create organization')));
        });
    }
  }

  render() {
    return (
      <Modal
        isOpen={this.props.isOpen}
        overlayClassName="configuration-modal-overlay"
        className="configuration-modal-content"
        shouldCloseOnOverlayClick={false}
      >
        <div>
          <h4><Translate id="react.locationsConfiguration.organizationModal.title.label" defaultMessage="Add new Organization" /></h4>
          <div className="my-3">
            <Translate id="react.locationsConfiguration.organizationModal.subtitle.label" defaultMessage="Enter a name and description for your organization. This organization is the company or entity that owns and manages the location. Read more about organizations" />
            <a target="_blank" rel="noopener noreferrer" href="https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1291452471/Configure+Organizations+and+Locations">
              <Translate id="react.locationsConfiguration.here.label" defaultMessage="here" />
            </a>.&nbsp;
          </div>
          <Form
            onSubmit={values => this.save(values)}
            initialValues={this.initialValues}
            validate={validate}
            render={({ handleSubmit, values }) =>
              (
                <form id="modalForm" onSubmit={handleSubmit}>
                  <div className="classic-form with-description">
                    {_.map(
                      FIELDS,
                      (fieldConfig, fieldName) =>
                        renderFormField(
                          fieldConfig,
                          fieldName,
                          { ...this.formProps, values },
                        ),
                    )}
                  </div>
                  <div className="btn-toolbar justify-content-between pt-3">
                    <button type="button" className="btn btn-outline-primary ml-1" onClick={() => this.props.onClose()}>
                      <Translate id="Cancel" defaultMessage="Cancel" />
                    </button>
                    <button type="submit" className="btn btn-primary align-self-end">
                      <Translate id="Save" defaultMessage="Save" />
                    </button>
                  </div>
                </form>
              )
            }
          />
        </div>
      </Modal>
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(AddOrganizationModal));

AddOrganizationModal.propTypes = {
  hideSpinner: PropTypes.func.isRequired,
  isOpen: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  onResponse: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
