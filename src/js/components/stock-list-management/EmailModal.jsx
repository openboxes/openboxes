import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';
import { getTranslate } from 'react-localize-redux';

import ModalWrapper from '../form-elements/ModalWrapper';
import TextField from '../form-elements/TextField';
import TextareaField from '../form-elements/TextareaField';
import SelectField from '../form-elements/SelectField';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';
import CheckboxField from '../form-elements/CheckboxField';


const FIELDS = {
  recipients: {
    type: SelectField,
    label: 'react.stockListManagement.recipients.label',
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
    label: 'react.stockListManagement.subject.label',
    defaultMessage: 'Subject',
    attributes: {
      required: true,
    },
  },
  text: {
    type: TextareaField,
    label: 'react.stockListManagement.message.label',
    defaultMessage: 'Message',
    attributes: {
      rows: 8,
      required: true,
    },
  },
  includePdf: {
    type: CheckboxField,
    label: 'react.stockListManagement.includePdf.label',
    defaultMessage: 'Include PDF document',
  },
  includeXls: {
    type: CheckboxField,
    label: 'react.stockListManagement.includeXls.label',
    defaultMessage: 'Include XLS document',
  },
};

function validate(values) {
  const errors = {};
  if (_.isEmpty(values.recipients)) {
    errors.recipients = 'react.default.error.requiredField.label';
  }
  if (!values.subject) {
    errors.subject = 'react.default.error.requiredField.label';
  }
  if (!values.text) {
    errors.text = 'react.default.error.requiredField.label';
  }
  return errors;
}

/** Modal window where user can send email with updated stocklist */
/* eslint no-param-reassign: "error" */
class EmailModal extends Component {
  constructor(props) {
    super(props);

    this.state = {
      formValues: {},
      showModal: false,
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.onClose = this.onClose.bind(this);
  }

  /**
   * Loads initial form values
   * @public
   */
  onOpen() {
    const { manager } = this.props;
    this.setState({
      formValues: {
        subject: this.props.translate('react.stockListManagement.emailSubject.label', 'STOCK LIST UPDATE'),
        text: this.props.translate('react.stockListManagement.emailMessage.label', 'Please find attached a new' +
          ' version of your stock list reflecting recent updates. Please use this version for your next replenishment request.'),
        recipients: manager ? [{ id: manager.id, email: manager.email, label: manager.name }] : [],
        includePdf: true,
        includeXls: true,
        showModal: true,
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

    const url = `/openboxes/api/stocklists/sendMail/${this.props.stocklistId}?includePdf=${this.state.formValues.includePdf}?includeXls=${this.state.formValues.includeXls}`;
    const payload = {
      ...values,
      recipients: _.map(_.filter(values.recipients, val => val.email), val => val.email),
    };
    const { manager } = this.props;

    if (!_.some(values.recipients, recipient => recipient.email === manager.email)) {
      this.props.hideSpinner();
      Alert.error(this.props.translate('react.stockListManagement.alert.noManagerSelected.label', 'Please add a manager as a recipient and resend.'), { timeout: 1000 });
      this.setState({ showModal: true });
    } else {
      apiClient.post(url, payload)
        .then(() => {
          this.props.hideSpinner();
          this.setState({ showModal: false });
          Alert.success(this.props.translate('react.stockListManagement.alert.emailSend.label', 'Email sent successfully'), { timeout: 1000 });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  /**
   * Changes value of showModal to false to close modal
   * @public
   */
  onClose() {
    this.setState({ showModal: false });
  }

  render() {
    return (
      <ModalWrapper
        title="react.stockListManagement.sendMailModalTitle.label"
        btnOpenText="react.default.button.email.label"
        btnOpenDefaultText="Email"
        btnSaveText="react.default.button.send.label"
        btnSaveDefaultText="Send"
        btnOpenClassName="btn btn-outline-secondary btn-xs mr-1"
        onOpen={this.onOpen}
        onSave={this.onSave}
        onClose={this.onClose}
        fields={FIELDS}
        initialValues={this.state.formValues}
        formProps={{ users: this.props.users }}
        validate={validate}
        showModal={this.state.showModal}
      />
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(EmailModal);

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
  /** Function used to translate static messages */
  translate: PropTypes.func.isRequired,
};

EmailModal.defaultProps = {
  users: [],
  manager: null,
};
