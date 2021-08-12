import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { withRouter } from 'react-router-dom';
import { getTranslate } from 'react-localize-redux';
import queryString from 'query-string';

import 'react-confirm-alert/src/react-confirm-alert.css';

import TextField from '../../form-elements/TextField';
import SelectField from '../../form-elements/SelectField';
import { renderFormField } from '../../../utils/form-utils';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';
import { debounceLocationsFetch } from '../../../utils/option-utils';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';

const { orderId } = queryString.parse(window.location.search);

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
    label: 'react.stockMovement.description.label',
    defaultMessage: 'Description',
    attributes: {
      required: true,
      autoFocus: true,
    },
  },
  origin: {
    type: SelectField,
    label: 'react.stockMovement.origin.label',
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
      loadOptions: props.debouncedOriginLocationsFetch,
      disabled: !_.isNil(props.stockMovementId),
    }),
  },
  destination: {
    type: SelectField,
    label: 'react.stockMovement.destination.label',
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
      loadOptions: props.debouncedDestinationLocationsFetch,
      disabled: (!props.isSuperuser || !_.isNil(props.stockMovementId)) &&
        !props.hasCentralPurchasingEnabled,
    }),
  },
};

/** The first step of stock movement where user can add all the basic information. */
class CreateStockMovement extends Component {
  constructor(props) {
    super(props);
    this.state = {
      setInitialValues: true,
      values: this.props.initialValues,
    };

    this.debouncedOriginLocationsFetch =
      debounceLocationsFetch(
        this.props.debounceTime,
        this.props.minSearchLength,
        null, // activityCodes
        false, // fetchAll
        true, // withOrgCode
        false, // withTypeDescription
      );

    this.debouncedDestinationLocationsFetch =
      debounceLocationsFetch(
        this.props.debounceTime,
        this.props.minSearchLength,
        null,
        true,
      );
  }

  componentDidMount() {
    if (orderId) {
      const url = `/openboxes/api/combineShipments/${orderId}`;
      apiClient.get(url)
        .then((resp) => {
          const { data } = resp.data;
          this.setInitialValues(data.origin, data.destination);
        })
        .catch(err => err);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.match.params.stockMovementId && this.state.setInitialValues
      && nextProps.location.id && !orderId && !this.props.hasCentralPurchasingEnabled) {
      this.setInitialValues(null, nextProps.location);
    }
  }

  setInitialValues(origin, destination) {
    let originLocation = [];
    let destinationLocation = [];
    if (origin) {
      originLocation = {
        id: origin.id,
        type: origin.locationType ? origin.locationType.locationTypeCode : null,
        name: origin.name,
        label: `${origin.organizationCode ? `${origin.organizationCode} - ` : ''}${origin.name}`,
      };
    }
    if (destination) {
      destinationLocation = {
        id: destination.id,
        type: destination.locationType ? destination.locationType.locationTypeCode : null,
        name: destination.name,
        label: `${destination.name} [${destination.locationType ? destination.locationType.description : null}]`,
      };
    }
    this.setState({
      values: {
        destination: destinationLocation,
        origin: originLocation,
      },
      setInitialValues: false,
    });
  }

  /**
   * Creates or updates stock movement with given data
   * @param {object} values
   * @public
   */
  saveStockMovement(values) {
    if (values.origin && values.destination && values.description) {
      this.props.showSpinner();

      let payload = {
        description: values.description,
        'origin.id': values.origin.id,
        'destination.id': values.destination.id,
      };
      let stockMovementUrl = '';

      if (values.stockMovementId) {
        stockMovementUrl = `/openboxes/api/stockMovements/${values.stockMovementId}/updateRequisition`;
        payload = {
          description: values.description,
          'destination.id': values.destination.id,
        };
      } else {
        stockMovementUrl = '/openboxes/api/stockMovements/createCombinedShipments';
      }

      apiClient.post(stockMovementUrl, payload)
        .then((response) => {
          if (response.data) {
            const resp = response.data.data;
            this.props.history.push(`/openboxes/stockMovement/createCombinedShipments/${resp.id}`);
            this.props.nextPage({
              ...values,
              stockMovementId: resp.id,
              lineItems: resp.lineItems,
              movementNumber: resp.identifier,
              name: resp.name,
            });
          }
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.stockMovement.error.createStockMovement.label', 'Could not create stock movement')));
        });
    }
  }

  render() {
    return (
      <Form
        onSubmit={values => this.saveStockMovement(values)}
        validate={validate}
        initialValues={this.state.values}
        render={({ handleSubmit, values }) => (
          <form onSubmit={handleSubmit}>
            <div className="classic-form with-description">
              {_.map(
                FIELDS,
                (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  isSuperuser: this.props.isSuperuser,
                  debouncedDestinationLocationsFetch: this.debouncedDestinationLocationsFetch,
                  debouncedOriginLocationsFetch: this.debouncedOriginLocationsFetch,
                  stockMovementId: values.stockMovementId,
                  hasCentralPurchasingEnabled: this.props.hasCentralPurchasingEnabled,
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
  hasCentralPurchasingEnabled: state.session.currentLocation.hasCentralPurchasingEnabled,
});

export default withRouter(connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(CreateStockMovement));

CreateStockMovement.propTypes = {
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ stockMovementId: PropTypes.string }),
  }).isRequired,
  /** Initial component's data */
  initialValues: PropTypes.shape({
    origin: PropTypes.shape({
      id: PropTypes.string,
    }),
    destination: PropTypes.shape({
      id: PropTypes.string,
    }),
    stocklist: PropTypes.shape({}),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  nextPage: PropTypes.func.isRequired,
  /** React router's object used to manage session history */
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  /** Current location */
  location: PropTypes.shape({
    name: PropTypes.string,
    id: PropTypes.string,
    locationType: PropTypes.shape({
      description: PropTypes.string,
      locationTypeCode: PropTypes.string,
    }),
  }).isRequired,
  /** Return true if current user is superuser */
  isSuperuser: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  /** Is true when currently selected location has central purchasing enabled */
  hasCentralPurchasingEnabled: PropTypes.bool.isRequired,
};
