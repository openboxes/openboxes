import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ModalWrapper from '../form-elements/ModalWrapper';
import TextField from '../form-elements/TextField';
import TextareaField from '../form-elements/TextareaField';
import SelectField from '../form-elements/SelectField';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

const FIELDS = {
  recipients: {
    type: SelectField,
    label: 'stockListManagement.recipients.label',
    defaultMessage: 'Recipients',
    attributes: {
      required: true,
      showValueTooltip: true,
      multi: true,
      objectValue: true,
      style: { paddingBottom: 5 },
    },
    getDynamicAttr: ({ users }) => ({
      options: users,
    }),
  },
  subject: {
    type: TextField,
    label: 'stockListManagement.subject.label',
    defaultMessage: 'Subject',
    attributes: {
      required: true,
    },
  },
  text: {
    type: TextareaField,
    label: 'stockListManagement.message.label',
    defaultMessage: 'Message',
    attributes: {
      rows: 8,
      required: true,
    },
  },
};

/** Modal window where user can send email with updated stocklist */
/* eslint no-param-reassign: "error" */
class EmailModal extends Component {
  constructor(props) {
    super(props);

    this.state = {
      formValues: {},
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  /**
   * Loads initial form values
   * @public
   */
  onOpen() {
    const { manager } = this.props;
    this.setState({
      formValues: {
        subject: 'STOCK LIST UPDATE',
        text: '',
        recipients: manager ? [{ id: manager.id, email: manager.email, label: manager.name }] : [],
      },
    });
  }

  /**
   * Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const url = `/openboxes/api/stocklists/sendMail/${this.props.stocklistId}`;
    const payload = {
      ...values,
      recipients: _.map(_.filter(values.recipients, val => val.email), val => val.email),
    };

    apiClient.post(url, payload)
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <ModalWrapper
        title="stockListManagement.sendMailModalTitle.label"
        btnOpenText="default.button.email.label"
        btnSaveText="default.button.send.label"
        btnOpenClassName="btn btn-outline-secondary btn-xs mr-1"
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        initialValues={this.state.formValues}
        formProps={{ users: this.props.users }}
      />
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(EmailModal);

EmailModal.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Id of stocklist */
  stocklistId: PropTypes.string.isRequired,
  /** Array of available users  */
  users: PropTypes.arrayOf(PropTypes.shape({})),
  manager: PropTypes.shape({}),
};

EmailModal.defaultProps = {
  users: [],
  manager: null,
};
