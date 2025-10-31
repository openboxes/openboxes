import React, { Component } from 'react';

import update from 'immutability-helper';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { hideSpinner, showSpinner } from 'actions';
import userApi from 'api/services/UserApi';
import { STOCKLIST_API } from 'api/urls';
import DateField from 'components/form-elements/DateField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import ActivityCode from 'consts/activityCode';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import RoleType from 'consts/roleType';
import apiClient from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

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
  if (!values.requestedBy) {
    errors.requestedBy = 'react.default.error.requiredField.label';
  }
  if (!values.requestType) {
    errors.requestType = 'react.default.error.requiredField.label';
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

const DEFAULT_FIELDS = {
  description: {
    type: TextField,
    label: 'react.stockMovement.description.label',
    defaultMessage: 'Description',
    attributes: {
      required: true,
      autoFocus: true,
    },
  },
  destination: {
    type: SelectField,
    label: 'react.stockMovement.requestingLocation.label',
    defaultMessage: 'Requesting Location',
    attributes: {
      required: true,
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      options: [],
      disabled: true,
      filterOptions: (options) => options,
    },
    getDynamicAttr: (props) => ({
      loadOptions: props.debouncedLocationsFetch,
      onChange: (value) => {
        if (value && props.origin && props.origin.id) {
          props.fetchStockLists(props.origin, value);
        }
      },
    }),
  },
  origin: {
    type: SelectField,
    label: 'react.stockMovement.fulfillingLocation.label',
    defaultMessage: 'Fulfilling Location',
    attributes: {
      required: true,
      async: true,
      showValueTooltip: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      options: [],
      filterOptions: (options) => options,
    },
    getDynamicAttr: (props) => ({
      loadOptions: props.debouncedLocationsFetch,
      onChange: (value) => {
        if (value && props.destination && props.destination.id) {
          props.fetchStockLists(value, props.destination);
          if (value?.supportedActivities?.includes(ActivityCode.APPROVE_REQUEST)) {
            props.setSupportsApprover(true);
            props.fetchAvailableApprovers(value.id);
          } else {
            props.setSupportsApprover(false);
          }
        }
      },
      disabled: false,
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
      filterOptions: (options) => options,
    },
    getDynamicAttr: (props) => ({
      loadOptions: props.debouncedPeopleFetch,
    }),
  },
  dateRequested: {
    type: DateField,
    label: 'react.stockMovement.dateRequested.label',
    defaultMessage: 'Date requested',
    attributes: {
      required: true,
      localizeDate: true,
      localizedDateFormat: DateFormat.COMMON,
      autoComplete: 'off',
    },
  },
  requestType: {
    type: SelectField,
    label: 'react.stockMovement.requestType.label',
    defaultMessage: 'Request type',
    attributes: {
      valueKey: 'id',
      labelKey: 'name',
      required: true,
      showValueTooltip: true,
    },
    getDynamicAttr: ({ requestTypes }) => ({
      options: requestTypes,
    }),
  },
  stocklist: {
    label: 'react.stockMovement.stocklist.label',
    defaultMessage: 'Stocklist',
    type: SelectField,
    getDynamicAttr: ({
      origin, destination, stocklists, setRequestType, values,
    }) => ({
      disabled: !(origin && destination && origin.id && destination.id),
      options: stocklists,
      showValueTooltip: true,
      valueKey: 'id',
      labelKey: 'name',
      onChange: (value) => {
        if (value) {
          setRequestType(values, value);
        }
      },
    }),
  },
  dateDeliveryRequested: {
    label: 'react.stockMovement.desiredDateOfDelivery',
    defaultMessage: 'Desired date of delivery',
    type: DateField,
    attributes: {
      localizeDate: true,
      localizedDateFormat: DateFormat.COMMON,
      dateFormat: null,
      autoComplete: 'off',
    },
  },
};

const APPROVER_FIELDS = {
  ...DEFAULT_FIELDS,
  approvers: {
    label: 'react.stockMovement.request.approvers.label',
    defaultMessage: 'Approvers',
    attributes: {
      multi: true,
      showValueTooltip: true,
      valueKey: 'id',
      labelKey: 'name',
    },
    type: SelectField,
    getDynamicAttr: (props) => ({
      options: props.availableApprovers,
    }),
  },
};

const ELECTRONIC = 'ELECTRONIC';

/** The first step of stock movement where user can add all the basic information. */
class CreateStockMovement extends Component {
  constructor(props) {
    super(props);
    this.state = {
      stocklists: [],
      availableApprovers: [],
      supportsApprover: false,
      setInitialValues: true,
      values: this.props.initialValues,
      requestTypes: [],
    };
    this.fetchStockLists = this.fetchStockLists.bind(this);
    this.setRequestType = this.setRequestType.bind(this);
    this.fetchAvailableApprovers = this.fetchAvailableApprovers.bind(this);
    this.setSupportsApprover = this.setSupportsApprover.bind(this);

    this.debouncedPeopleFetch = debouncePeopleFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
    );

    this.debouncedLocationsFetch = debounceLocationsFetch(this.props.debounceTime, this.props.minSearchLength, ['FULFILL_REQUEST']);
  }

  componentDidMount() {
    if (this.state.values.origin && this.state.values.destination) {
      this.fetchStockLists(this.state.values.origin, this.state.values.destination);
    }
    if (this.state.values.origin) {
      if (this.state.values.origin?.supportedActivities?.includes(ActivityCode.APPROVE_REQUEST)) {
        this.setSupportsApprover(true);
        this.fetchAvailableApprovers(this.state.values.origin.id);
      } else {
        this.setSupportsApprover(false);
      }
    }
    this.fetchRequisitionTypes();
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.match.params.stockMovementId && this.state.setInitialValues
      && nextProps.location.id) {
      this.setInitialValues(nextProps.location, nextProps.user);
    }
  }

  setRequestType(values, stocklist) {
    const { requestTypes } = this.state;
    const requestType = _.find(requestTypes, (type) => type.id === 'STOCK');

    this.setState({
      values: update(values, {
        requestType: { $set: requestType },
        stocklist: { $set: stocklist },
      }),
    });
  }

  setInitialValues(location, user) {
    const { id, locationType, name } = location;

    const values = {
      destination: {
        id,
        type: locationType ? locationType.locationTypeCode : null,
        name,
        label: `${name} [${locationType ? locationType.description : null}]`,
      },
      requestedBy: {
        id: user.id,
        name: user.name,
        label: `${user.name}`,
      },
      dateRequested: moment(new Date()).format('MM/DD/YYYY'),
      approvers: undefined,
    };
    this.setState({ values, setInitialValues: false });
  }

  setSupportsApprover(value) {
    this.setState({
      supportsApprover: value,
    });
  }

  /**
   * Fetches available shipment types from API.
   * @public
   */
  fetchRequisitionTypes() {
    const url = '/api/getRequestTypes';

    return apiClient.get(url)
      .then((response) => {
        this.setState({ requestTypes: response.data.data }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  fetchAvailableApprovers(locationId) {
    return userApi.getUsersOptions({
      params: { roleTypes: RoleType.ROLE_REQUISITION_APPROVER, location: locationId },
    })
      .then((response) => {
        const options = response.data.data?.map((user) => ({
          id: user.id,
          value: user.id,
          label: user.name,
        }));

        this.setState({ availableApprovers: options });

        return options;
      })
      .finally(() => this.props.hideSpinner());
  }

  checkStockMovementChange(newValues) {
    const { origin, destination, stocklist } = this.props.initialValues;

    const originLocs = newValues.origin && origin;
    const isOldSupplier = origin && origin.type === 'SUPPLIER';
    const isNewSupplier = newValues.origin && newValues.type === 'SUPPLIER';
    const checkOrigin = originLocs && (!isOldSupplier || (isOldSupplier && !isNewSupplier))
      ? newValues.origin.id !== origin.id : false;

    const checkDest = stocklist && newValues.destination && destination
      ? newValues.destination.id !== destination.id : false;
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
    return apiClient.get(STOCKLIST_API, {
      params: {
        origin: origin.id,
        destination: destination.id,
      },
    })
      .then((response) => {
        const stocklists = _.map(response.data.data, (stocklist) => (
          {
            id: stocklist.id, name: stocklist.name, value: stocklist.id, label: stocklist.name,
          }
        ));

        const stocklistChanged = !_.find(stocklists, (item) => item.value.id === _.get(this.state.values, 'stocklist'));

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
    if (values.origin && values.destination && values.requestedBy
      && values.dateRequested && values.description) {
      this.props.showSpinner();

      let stockMovementUrl = '';
      if (values.stockMovementId) {
        stockMovementUrl = `/api/stockMovements/${values.stockMovementId}/updateRequisition`;
      } else {
        stockMovementUrl = '/api/stockMovements';
      }

      const payload = {
        name: '',
        description: values.description,
        dateRequested: values.dateRequested,
        origin: values.origin.id,
        destination: values.destination.id,
        requestedBy: values.requestedBy.id,
        stocklist: { id: _.get(values.stocklist, 'id', '') },
        requestType: values.requestType.id,
        sourceType: ELECTRONIC,
        approvers: values.approvers?.map((user) => user.id),
        dateDeliveryRequested: values.dateDeliveryRequested
          ? moment(values.dateDeliveryRequested).format('MM/DD/YYYY')
          : null,
      };

      apiClient.post(stockMovementUrl, payload)
        .then((response) => {
          if (response.data) {
            const resp = response.data.data;
            this.props.history.push(STOCK_MOVEMENT_URL.editRequest(resp.id));
            this.props.nextPage({
              ...values,
              stockMovementId: resp.id,
              lineItems: resp.lineItems,
              movementNumber: resp.identifier,
              name: resp.name,
              stocklist: resp.stocklist,
              replenishmentType: resp.replenishmentType,
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
    const FIELDS = this.state.supportsApprover ? APPROVER_FIELDS : DEFAULT_FIELDS;
    return (
      <Form
        onSubmit={(values) => this.nextPage(values)}
        validate={validate}
        initialValues={this.state.values}
        mutators={{
          clearStocklist: (args, state, utils) => {
            utils.changeValue(state, 'stocklist', () => null);
          },
          setApproversValues: (args, state, utils) => {
            const [selectedOptions] = args;
            utils.changeValue(state, 'approvers', () => selectedOptions);
          },
        }}
        render={({ form: { mutators }, handleSubmit, values }) => (
          <form onSubmit={handleSubmit}>
            <div className="classic-form with-description">
              {_.map(
                FIELDS,
                (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  stocklists: this.state.stocklists,
                  fetchStockLists: (origin, destination) =>
                    this.fetchStockLists(origin, destination, mutators.clearStocklist),
                  fetchAvailableApprovers: (locationId) => {
                    this.fetchAvailableApprovers(locationId).then((resp) => {
                    // if there is only one available approver to choose from
                    // then preselect this options by default
                      if (resp?.length === 1) {
                        mutators.setApproversValues(resp);
                      } else {
                        mutators.setApproversValues([]);
                      }
                    });
                  },
                  setSupportsApprover: this.setSupportsApprover,
                  origin: values.origin,
                  destination: values.destination,
                  isSuperuser: this.props.isSuperuser,
                  debouncedPeopleFetch: this.debouncedPeopleFetch,
                  debouncedLocationsFetch: this.debouncedLocationsFetch,
                  requestTypes: this.state.requestTypes,
                  setRequestType: this.setRequestType,
                  availableApprovers: this.state.availableApprovers,
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

const mapStateToProps = (state) => ({
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
  user: PropTypes.shape({
    username: PropTypes.string,
    id: PropTypes.string,
    name: PropTypes.string,
  }).isRequired,
};
