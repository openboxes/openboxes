import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { withRouter } from 'react-router-dom';
import { confirmAlert } from 'react-confirm-alert';

import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import DateField from '../form-elements/DateField';
import { renderFormField } from '../../utils/form-utils';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import { debouncedUsersFetch, debouncedLocationsFetch } from '../../utils/option-utils';

function validate(values) {
  const errors = {};
  if (!values.description) {
    errors.description = 'This field is required';
  }
  if (!values.origin) {
    errors.origin = 'This field is required';
  }
  if (!values.destination) {
    errors.destination = 'This field is required';
  }
  if (!values.requestedBy) {
    errors.requestedBy = 'This field is required';
  }
  if (!values.dateRequested) {
    errors.dateRequested = 'This field is required';
  }
  return errors;
}

const FIELDS = {
  description: {
    type: TextField,
    label: 'Description',
    attributes: {
      required: true,
      autoFocus: true,
    },
  },
  origin: {
    type: SelectField,
    label: 'Origin',
    attributes: {
      required: true,
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      loadOptions: debouncedLocationsFetch,
      cache: false,
      options: [],
    },
    getDynamicAttr: props => ({
      onChange: (value) => {
        if (value && props.destination && props.destination.id) {
          props.fetchStockLists(value, props.destination);
        }
      },
    }),
  },
  destination: {
    type: SelectField,
    label: 'Destination',
    attributes: {
      required: true,
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      loadOptions: debouncedLocationsFetch,
      cache: false,
      options: [],
    },
    getDynamicAttr: props => ({
      onChange: (value) => {
        if (value && props.origin && props.origin.id) {
          props.fetchStockLists(props.origin, value);
        }
      },
    }),
  },
  stockList: {
    label: 'Stock list',
    type: SelectField,
    getDynamicAttr: ({ origin, destination, stockLists }) => ({
      disabled: !(origin && destination && origin.id && destination.id),
      options: stockLists,
      showValueTooltip: true,
    }),
  },
  requestedBy: {
    type: SelectField,
    label: 'Requested-by',
    attributes: {
      async: true,
      required: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      loadOptions: debouncedUsersFetch,
      cache: false,
      options: [],
      labelKey: 'name',
    },
  },
  dateRequested: {
    type: DateField,
    label: 'Date requested',
    attributes: {
      required: true,
      dateFormat: 'MM/DD/YYYY',
    },
  },
};

  /** The first step of stock movement where user can add all the basic information. */
class CreateStockMovement extends Component {
  constructor(props) {
    super(props);
    this.state = {
      stockLists: [],
      values: this.props.initialValues,
    };
    this.fetchStockLists = this.fetchStockLists.bind(this);
  }

  componentDidMount() {
    if (this.state.values.origin && this.state.values.destination) {
      this.fetchStockLists(this.state.values.origin, this.state.values.destination);
    }
  }

  /**
   * Fetches available stock lists from API with given origin and destination.
   * @param {object} origin
   * @param {object} destination
   * @public
   */
  fetchStockLists(origin, destination) {
    this.props.showSpinner();
    const url = `/openboxes/api/stocklists?origin.id=${origin.id}&destination.id=${destination.id}`;

    return apiClient.get(url)
      .then((response) => {
        const stockLists = _.map(response.data.data, stockList => (
          { value: stockList.id, label: stockList.name }
        ));
        this.setState({ stockLists }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  checkStockMovementChange(newValues) {
    const checkOrigin = newValues.origin && this.props.initialValues.origin ?
      newValues.origin.id !== this.props.initialValues.origin.id : false;
    const checkDest = newValues.destination && this.props.initialValues.destination ?
      newValues.destination.id !== this.props.initialValues.destination.id : false;
    const checkStockList = newValues.stockMovementId ?
      newValues.stockList !== this.props.initialValues.stockList : false;

    return (checkOrigin || checkDest || checkStockList);
  }

  /**
   * Creates or updates stock movement with given data
   * @param {object} values
   * @public
   */
  saveStockMovement(values) {
    if (values.origin && values.destination && values.requestedBy &&
      values.dateRequested && values.description) {
      this.props.showSpinner();

      let stockMovementUrl = '';
      if (values.stockMovementId) {
        stockMovementUrl = `/openboxes/api/stockMovements/${values.stockMovementId}`;
      } else {
        stockMovementUrl = '/openboxes/api/stockMovements';
      }

      const payload = {
        name: '',
        description: values.description,
        dateRequested: values.dateRequested,
        'origin.id': values.origin.id,
        'destination.id': values.destination.id,
        'requestedBy.id': values.requestedBy.id,
        'stocklist.id': values.stockList || '',
      };

      apiClient.post(stockMovementUrl, payload)
        .then((response) => {
          if (response.data) {
            const resp = response.data.data;
            this.props.history.push(`/openboxes/stockMovement/index/${resp.id}`);
            this.props.onSubmit({
              ...values,
              stockMovementId: resp.id,
              lineItems: resp.lineItems,
              movementNumber: resp.identifier,
              shipmentName: resp.name,
            });
          }
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error('Could not create stock movement'));
        });
    }

    return new Promise(((resolve, reject) => {
      reject(new Error('Missing required parameters'));
    }));
  }

  resetToInitialValues() {
    this.setState({
      values: {},
    }, () => this.setState({
      values: this.props.initialValues,
    }));
  }

  /**
   * Calls method creating or saving stock movement and moves user to the next page.
   * @param {object} values
   * @public
   */
  nextPage(values) {
    const showModal = this.checkStockMovementChange(values);
    if (!showModal) {
      this.saveStockMovement(values);
    } else {
      confirmAlert({
        title: 'Confirm change',
        message: 'Do you want to change stock movement data? ' +
          'Changing origin, destination or stock list can cause loss of your current work.',
        buttons: [
          {
            label: 'No',
            onClick: () => this.resetToInitialValues(),
          },
          {
            label: 'Yes',
            onClick: () => this.saveStockMovement(values),
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
        render={({ handleSubmit, values }) => (
          <form className="create-form" onSubmit={handleSubmit}>
            {_.map(
              FIELDS,
              (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stockLists: this.state.stockLists,
                fetchStockLists: this.fetchStockLists,
                origin: values.origin,
                destination: values.destination,
              }),
            )}
            <div>
              <button type="submit" className="btn btn-outline-primary float-right">Next</button>
            </div>
          </form>
        )}
      />
    );
  }
}

export default withRouter(connect(null, {
  showSpinner, hideSpinner,
})(CreateStockMovement));

CreateStockMovement.propTypes = {
  /** Initial component's data */
  initialValues: PropTypes.shape({
    origin: PropTypes.shape({
      id: PropTypes.string,
    }),
    destination: PropTypes.shape({
      id: PropTypes.string,
    }),
    stockList: PropTypes.shape({}),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /**
   * Function called with the form data when the handleSubmit()
   * is fired from within the form component.
   */
  onSubmit: PropTypes.func.isRequired,
  /** React router's object used to manage session history */
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};
