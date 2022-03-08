import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { hideSpinner, showSpinner } from 'actions';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import apiClient, { parseResponse } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { debounceLocationsFetch } from 'utils/option-utils';
import Translate from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

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
    getDynamicAttr: ({ outboundReturnId }) => ({
      disabled: !!outboundReturnId,
    }),
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
      disabled: !!props.outboundReturnId,
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
      disabled: !!props.outboundReturnId,
    }),
  },
};

class CreateOutboundReturn extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: this.props.initialValues,
    };

    this.debouncedLocationsFetch =
      debounceLocationsFetch(
        this.props.debounceTime,
        this.props.minSearchLength,
        [], // activityCodes
        true, // fetchAll
      );
  }

  componentDidMount() {
    if (this.props.outboundReturnsTranslationsFetched && this.props.location.id) {
      this.dataFetched = true;
      this.fetchOutboundReturn(this.props);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.outboundReturnsTranslationsFetched && nextProps.location.id) {
      if (!this.dataFetched) {
        this.dataFetched = true;

        this.fetchOutboundReturn(nextProps);
      } else if (this.props.location.id !== nextProps.location.id) {
        this.fetchOutboundReturn(nextProps);
      }
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
    this.setState({ values });
  }

  fetchOutboundReturn(props) {
    if (props.match.params.outboundReturnId) {
      props.showSpinner();
      const url = `/openboxes/api/stockTransfers/${props.match.params.outboundReturnId}`;
      apiClient.get(url)
        .then((resp) => {
          const values = parseResponse(resp.data.data);
          this.setState({
            values: {
              ...values,
              origin: {
                id: values.origin.id,
                name: values.origin.name,
                label: values.origin.name,
              },
              destination: {
                id: values.destination.id,
                name: values.destination.name,
                label: values.destination.name,
              },
            },
          }, () => props.hideSpinner());
        })
        .catch(() => props.hideSpinner());
    } else {
      this.setInitialValues(props.location);
    }
  }

  dataFetched = false;

  saveOutboundReturns(values) {
    if (
      values.origin &&
      values.destination &&
      values.description &&
      !this.props.match.params.outboundReturnId
    ) {
      this.props.showSpinner();

      const payload = {
        description: values.description,
        'origin.id': values.origin.id,
        'destination.id': values.destination.id,
        type: 'RETURN_ORDER',
      };

      const url = '/openboxes/api/stockTransfers/';

      apiClient.post(url, payload)
        .then((response) => {
          if (response.data) {
            const resp = parseResponse(response.data.data);
            this.setState({
              values: resp,
            }, () => {
              this.props.history.push(`/openboxes/stockTransfer/createOutboundReturn/${this.state.values.id}`);
              this.props.nextPage(this.state.values);
            });
          }
        })
        .catch(() => {
          this.props.hideSpinner();
        });
    } else {
      this.props.nextPage(values);
    }
  }

  render() {
    return (
      <Form
        onSubmit={values => this.saveOutboundReturns(values)}
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
                  outboundReturnId: this.props.match.params.outboundReturnId,
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
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  outboundReturnsTranslationsFetched: state.session.fetchedTranslations.outboundReturns,
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(CreateOutboundReturn));

CreateOutboundReturn.propTypes = {
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
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  outboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
};
