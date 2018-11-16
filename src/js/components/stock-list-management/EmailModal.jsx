import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ModalWrapper from '../form-elements/ModalWrapper';
import TextField from '../form-elements/TextField';
import TextareaField from '../form-elements/TextareaField';
import SelectField from '../form-elements/SelectField';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchUsers } from '../../actions';

const FIELDS = {
  recipient: {
    type: SelectField,
    label: 'Recipients',
    attributes: {
      required: true,
      showValueTooltip: true,
      multi: true,
      style: { paddingBottom: 5 },
    },
    getDynamicAttr: ({ users }) => ({
      options: users,
    }),
  },
  subject: {
    type: TextField,
    label: 'Subject',
    attributes: {
      required: true,
    },
  },
  text: {
    type: TextareaField,
    label: 'Message',
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

    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      attr,
      formValues: {},
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  componentDidMount() {
    if (!this.props.usersFetched) {
      this.fetchData(this.props.fetchUsers);
    }
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  /**
   * Loads initial form values
   * @public
   */
  onOpen() {
    this.setState({
      /* TODO add stocklist manager as default recipient */
      formValues: {
        subject: 'STOCK LIST UPDATE',
        text: '',
        recipient: '',
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

    /* TODO add endpoint */
    const url = '/openboxes/stockMovement/sendEmail';

    return apiClient.post(url, values);
  }

  /**
   * Fetches data using function given as an argument(reducers components).
   * @param {function} fetchFunction
   * @public
   */
  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        initialValues={this.state.formValues}
        formProps={{ users: this.props.users }}
      />
    );
  }
}

const mapStateToProps = state => ({
  usersFetched: state.users.fetched,
  users: state.users.data,
});

export default connect(mapStateToProps, {
  fetchUsers, showSpinner, hideSpinner,
})(EmailModal);

EmailModal.propTypes = {
  /** Name of the field */
  fieldName: PropTypes.string.isRequired,
  /** Configuration of the field */
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching users */
  fetchUsers: PropTypes.func.isRequired,
  /** Indicator if users' data is fetched */
  usersFetched: PropTypes.bool.isRequired,
  /** Array of available users  */
  users: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
};
