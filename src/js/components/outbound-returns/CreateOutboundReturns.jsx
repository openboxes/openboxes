import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { withRouter } from 'react-router-dom';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import { renderFormField } from '../../utils/form-utils';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import { debounceLocationsFetch } from '../../utils/option-utils';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

function validate(values) {
  const errors = {};
  if (!values.description) {
    errors.description = 'react.default.error.requiredField.label';
  }
  if (!values.origin) {
    errors.origin = 'react.default.error.requiredField.label';
  }
  if (!values.destination) {
    errors.destination = 'react.default.error.requiredField.label';
  }
  return errors;
}

const FIELDS = {
  description: {
    type: TextField,
    label: 'react.outboundReturns.description.label',
    defaultMessage: 'Description',
    attributes: {
      required: true,
      autoFocus: true,
    },
  },
  origin: {
    type: SelectField,
    label: 'react.outboundReturns.origin.label',
    defaultMessage: 'Origin',
    attributes: {
      required: true,
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      options: [],
      filterOptions: options => options,
    },
    getDynamicAttr: props => ({
      loadOptions: props.debouncedLocationsFetch,
      disabled: !props.isSuperuser,
    }),
  },
  destination: {
    type: SelectField,
    label: 'react.outboundReturns.destination.label',
    defaultMessage: 'Destination',
    attributes: {
      required: true,
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      options: [],
      filterOptions: options => options,
    },
    getDynamicAttr: props => ({
      loadOptions: props.debouncedLocationsFetch,
    }),
  },
};

class CreateOutboundReturns extends Component {
  constructor(props) {
    super(props);
    this.state = {
      setInitialValues: true,
      values: this.props.initialValues,
    };

    this.debouncedLocationsFetch =
      debounceLocationsFetch(this.props.debounceTime, this.props.minSearchLength);
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.match.params.outboundReturnId && this.state.setInitialValues
      && nextProps.location.id) {
      this.setInitialValues(nextProps.location);
    }
  }

  setInitialValues(location) {
    const { id, locationType, name } = location;

    const values = {
      origin: {
        id,
        type: locationType ? locationType.locationTypeCode : null,
        name,
        label: `${name} [${locationType ? locationType.description : null}]`,
      },
    };
    this.setState({ values, setInitialValues: false });
  }

  checkOutboundReturnsChange(newValues) {
    const { destination } = this.props.initialValues;
    return newValues.destination && destination ?
      newValues.destination.id !== destination.id : false;
  }

  saveOutboundReturns(values) {
    if (values.origin && values.destination && values.description) {
      this.props.showSpinner();

      const payload = {
        description: values.description,
        'origin.id': values.origin.id,
        'destination.id': values.destination.id,
        type: 'OUTBOUND_RETURNS',
      };

      const url = `/openboxes/api/stockTransfers/${this.props.match.params.outboundReturnId || ''}`;

      apiClient.post(url, payload)
        .then((response) => {
          if (response.data) {
            const resp = response.data.data;
            this.props.history.push(`/openboxes/stockTransfer/createReturns/${resp.id}`);
            this.props.nextPage({ ...resp });
          }
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.outboundReturns.error.createOutboundReturns.label', 'Could not create return')));
        });
    }
  }

  resetToInitialValues() {
    this.setState({
      values: {},
    }, () => this.setState({
      values: this.props.initialValues,
    }));
  }

  nextPage(values) {
    const showModal = this.checkOutboundReturnsChange(values);
    if (!showModal) {
      this.saveOutboundReturns(values);
    } else {
      confirmAlert({
        title: this.props.translate('react.outboundReturns.message.confirmChange.label', 'Confirm change'),
        message: this.props.translate(
          'react.outboundReturns.confirmChange.message',
          'Do you want to change stock movement data? Changing origin, destination or stock list can cause loss of your current work',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.no.label', 'No'),
            onClick: () => this.resetToInitialValues(),
          },
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => this.saveOutboundReturns(values),
          },
        ],
      });
    }
  }

  render() {
    return (
      <Form
        onSubmit={values => this.nextPage(values)}
        validate={validate}
        initialValues={this.state.values}
        mutators={{
          clearStocklist: (args, state, utils) => {
            utils.changeValue(state, 'stocklist', () => null);
          },
        }}
        render={({ handleSubmit, values }) => (
          <form onSubmit={handleSubmit}>
            <div className="classic-form with-description">
              {_.map(
                FIELDS,
                (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  origin: values.origin,
                  destination: values.destination,
                  isSuperuser: this.props.isSuperuser,
                  debouncedLocationsFetch: this.debouncedLocationsFetch,
                  values,
                }),
              )}
            </div>
            <div className="submit-buttons">
              <button type="submit" className="btn btn-outline-primary float-right btn-xs">
                <Translate id="react.default.button.next.label" defaultMessage="Next" />
              </button>
            </div>
          </form>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  location: state.session.currentLocation,
  isSuperuser: state.session.isSuperuser,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(CreateOutboundReturns));

CreateOutboundReturns.propTypes = {
  match: PropTypes.shape({
    params: PropTypes.shape({ outboundReturnId: PropTypes.string }),
  }).isRequired,
  initialValues: PropTypes.shape({
    origin: PropTypes.shape({
      id: PropTypes.string,
    }),
    destination: PropTypes.shape({
      id: PropTypes.string,
    }),
    stocklist: PropTypes.shape({}),
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
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
  isSuperuser: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};
