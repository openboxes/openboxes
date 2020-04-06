import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { withRouter } from 'react-router-dom';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';
import moment from 'moment';

import TextField from '../../form-elements/TextField';
import SelectField from '../../form-elements/SelectField';
import DateField from '../../form-elements/DateField';
import { renderFormField } from '../../../utils/form-utils';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';
import { debounceUsersFetch, debounceLocationsFetch } from '../../../utils/option-utils';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';

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
  if (!values.requestedBy) {
    errors.requestedBy = 'react.default.error.requiredField.label';
  }
  if (!values.dateRequested) {
    errors.dateRequested = 'react.default.error.requiredField.label';
  } else {
    const dateRequested = moment(values.dateRequested, 'MM/DD/YYYY');
    if (moment().diff(dateRequested) < 0) {
      errors.dateRequested = 'react.stockMovement.error.futureDate.label';
    }
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
      loadOptions: props.debouncedLocationsFetch,
      onChange: (value) => {
        if (value && props.destination && props.destination.id) {
          props.fetchStockLists(value, props.destination);
        }
      },
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
      loadOptions: props.debouncedLocationsFetch,
      onChange: (value) => {
        if (value && props.origin && props.origin.id) {
          props.fetchStockLists(props.origin, value);
        }
      },
      disabled: !props.isSuperuser,
    }),
  },
  stocklist: {
    label: 'react.stockMovement.stocklist.label',
    defaultMessage: 'Stocklist',
    type: SelectField,
    getDynamicAttr: ({ origin, destination, stocklists }) => ({
      disabled: !(origin && destination && origin.id && destination.id),
      options: stocklists,
      showValueTooltip: true,
      objectValue: true,
    }),
  },
  requestedBy: {
    type: SelectField,
    label: 'react.stockMovement.requestedBy.label',
    defaultMessage: 'Requested by',
    attributes: {
      async: true,
      required: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      options: [],
      labelKey: 'name',
      filterOptions: options => options,
    },
    getDynamicAttr: props => ({
      loadOptions: props.debouncedUsersFetch,
    }),
  },
  dateRequested: {
    type: DateField,
    label: 'react.stockMovement.dateRequested.label',
    defaultMessage: 'Date requested',
    attributes: {
      required: true,
      dateFormat: 'MM/DD/YYYY',
      autoComplete: 'off',
    },
  },
};

/** The first step of stock movement where user can add all the basic information. */
class CreateStockMovement extends Component {
  constructor(props) {
    super(props);
    this.state = {
      stocklists: [],
      setInitialValues: true,
      values: this.props.initialValues,
    };
    this.fetchStockLists = this.fetchStockLists.bind(this);

    this.debouncedUsersFetch =
      debounceUsersFetch(this.props.debounceTime, this.props.minSearchLength);

    this.debouncedLocationsFetch =
      debounceLocationsFetch(this.props.debounceTime, this.props.minSearchLength);
  }

  componentDidMount() {
    if (this.state.values.origin && this.state.values.destination) {
      this.fetchStockLists(this.state.values.origin, this.state.values.destination);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.match.params.stockMovementId && this.state.setInitialValues
      && nextProps.location.id) {
      this.setInitialValues(nextProps.location);
    }
  }

  setInitialValues(location) {
    const { id, locationType, name } = location;
    const values = {
      destination: {
        id,
        type: locationType ? locationType.locationTypeCode : null,
        name,
        label: `${name} [${locationType ? locationType.description : null}]`,
      },
    };
    this.setState({ values, setInitialValues: false });
  }

  checkStockMovementChange(newValues) {
    const { origin, destination, stocklist } = this.props.initialValues;

    const originLocs = newValues.origin && origin;
    const isOldSupplier = origin && origin.type === 'SUPPLIER';
    const isNewSupplier = newValues.origin && newValues.type === 'SUPPLIER';
    const checkOrigin = originLocs && (!isOldSupplier || (isOldSupplier && !isNewSupplier)) ?
      newValues.origin.id !== origin.id : false;

    const checkDest = stocklist && newValues.destination && destination ?
      newValues.destination.id !== destination.id : false;
    const checkStockList = newValues.stockMovementId ? _.get(newValues.stocklist, 'id', null) !== _.get(stocklist, 'id', null) : false;

    return (checkOrigin || checkDest || checkStockList);
  }

  /**
   * Fetches available stock lists from API with given origin and destination.
   * @param {object} origin
   * @param {object} destination
   * @param {function} clearStocklist
   * @public
   */
  fetchStockLists(origin, destination, clearStocklist) {
    this.props.showSpinner();
    const url = `/openboxes/api/stocklists?origin.id=${origin.id}&destination.id=${destination.id}`;

    return apiClient.get(url)
      .then((response) => {
        const stocklists = _.map(response.data.data, stocklist => (
          { value: { id: stocklist.id, name: stocklist.name }, label: stocklist.name }
        ));

        const stocklistChanged = !_.find(stocklists, item => item.value.id === _.get(this.state.values, 'stocklist.id'));

        if (stocklistChanged && clearStocklist) {
          clearStocklist();
        }

        this.setState({ stocklists }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
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
        stockMovementUrl = `/openboxes/api/stockMovements/${values.stockMovementId}/updateRequisition`;
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
        'stocklist.id': _.get(values.stocklist, 'id') || '',
      };

      apiClient.post(stockMovementUrl, payload)
        .then((response) => {
          if (response.data) {
            const resp = response.data.data;
            this.props.history.push(`/openboxes/stockMovement/createInbound/${resp.id}`);
            this.props.nextPage({
              ...values,
              stockMovementId: resp.id,
              lineItems: resp.lineItems,
              movementNumber: resp.identifier,
              name: resp.name,
              stocklist: resp.stocklist,
            });
          }
        })
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.stockMovement.error.createStockMovement.label', 'Could not create stock movement')));
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
        title: this.props.translate('react.stockMovement.message.confirmChange.label', 'Confirm change'),
        message: this.props.translate(
          'react.stockMovement.confirmChange.message',
          'Do you want to change stock movement data? Changing origin, destination or stock list can cause loss of your current work',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.no.label', 'No'),
            onClick: () => this.resetToInitialValues(),
          },
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
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
        mutators={{
          clearStocklist: (args, state, utils) => {
            utils.changeValue(state, 'stocklist', () => null);
          },
        }}
        render={({ form: { mutators }, handleSubmit, values }) => (
          <form className="create-form" onSubmit={handleSubmit}>
            {_.map(
              FIELDS,
              (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                stocklists: this.state.stocklists,
                fetchStockLists: (origin, destination) =>
                  this.fetchStockLists(origin, destination, mutators.clearStocklist),
                origin: values.origin,
                destination: values.destination,
                isSuperuser: this.props.isSuperuser,
                debouncedUsersFetch: this.debouncedUsersFetch,
                debouncedLocationsFetch: this.debouncedLocationsFetch,
              }),
            )}
            <div>
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
  user: state.session.user,
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
};
