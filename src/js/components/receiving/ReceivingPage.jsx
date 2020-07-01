import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import moment from 'moment';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import PartialReceivingPage from './PartialReceivingPage';
import ReceivingCheckScreen from './ReceivingCheckScreen';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner, fetchTranslations } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

function validate(values) {
  const errors = {};
  errors.containers = [];

  if (!values.dateDelivered) {
    errors.dateDelivered = 'react.default.error.requiredField.label';
  } else {
    const dateDelivered = moment(values.dateDelivered, 'MM/DD/YYYY HH:mm Z');
    if (moment().diff(dateDelivered) < 0) {
      errors.dateDelivered = 'react.partialReceiving.error.futureDate.label';
    }
    const dateShipped = values.dateShipped ? moment(values.dateShipped, 'MM/DD/YYYY HH:mm Z') : null;
    if (dateShipped && dateDelivered < dateShipped) {
      errors.dateDelivered = 'react.partialReceiving.error.dateBeforeShipment.label';
    }
  }
  _.forEach(values.containers, (container, key) => {
    errors.containers[key] = { shipmentItems: [] };
    _.forEach(container.shipmentItems, (item, key2) => {
      if (item.quantityReceiving < 0) {
        errors.containers[key].shipmentItems[key2] = { quantityReceiving: 'react.partialReceiving.error.quantityToReceiveNegative.label' };
      }
    });
  });

  return errors;
}

/** Main partial receiving form's component. */
class ReceivingPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page: 0,
      bins: [],
      formData: {},
      completed: false,
      locationId: '',
      shipmentNumber: '',
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
    this.save = this.save.bind(this);
    this.saveAndExit = this.saveAndExit.bind(this);
    this.confirmReceive = this.confirmReceive.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'partialReceiving');

    if (this.props.partialReceivingTranslationsFetched) {
      this.dataFetched = true;

      this.fetchPartialReceiptCandidates();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'partialReceiving');
    }

    if (nextProps.partialReceivingTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;

      this.fetchPartialReceiptCandidates();
    }
  }

  /**
   * Saves all changes made by user, updates receipt status and if it's the last page of partial
   * receiving, it informs if the shipment was received successfully.
   * @param {object} formValues
   * @public
   */
  onSubmit(formValues) {
    if (this.state.page === 0) {
      const containers = _.map(formValues.containers, container => ({
        ...container,
        shipmentItems: _.chain(container.shipmentItems)
          .map((item) => {
            if (item.receiptItemId) {
              return {
                ...item, quantityReceiving: item.quantityReceiving ? item.quantityReceiving : 0,
              };
            }

            return item;
          })
          .filter(item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '').value(),
      }));

      const payload = {
        ...formValues, receiptStatus: 'CHECKING', containers: _.filter(containers, container => container.shipmentItems.length),
      };

      this.save(payload, this.nextPage);
    } else {
      const isBinLocationChosen = !_.some(formValues.containers, container =>
        _.some(container.shipmentItems, shipmentItem => _.isNull(shipmentItem.binLocation.id)));

      if (!isBinLocationChosen && this.props.hasBinLocationSupport && !(formValues.shipmentStatus === 'RECEIVED')) {
        this.confirmReceive(formValues);
      } else {
        this.save({
          ...formValues,
          receiptStatus: 'COMPLETED',
        }, () => {
          this.setState({ completed: true });
          const { requisition, shipmentId } = formValues;
          window.location = `/openboxes/stockMovement/show/${requisition || shipmentId}`;
        });
      }
    }
  }

  /**
   * Returns array of form's components.
   * @param {object} props
   * @public
   */
  getFormList(props) {
    return [
      <PartialReceivingPage
        {...props}
        bins={this.state.bins}
        save={this.save}
        saveAndExit={this.saveAndExit}
      />,
      <ReceivingCheckScreen
        {...props}
        prevPage={this.prevPage}
        save={this.save}
        saveAndExit={this.saveAndExit}
        completed={this.state.completed}
      />,
    ];
  }

  dataFetched = false;

  /**
   * Shows transition confirmation dialog if there are items with the same code.
   * @param {function} onConfirm
   * @public
   */
  confirmReceive(formValues) {
    confirmAlert({
      title: this.props.translate('react.partialReceiving.message.confirmReceive.label', 'Confirm receiving'),
      message: this.props.translate(
        'react.partialReceiving.confirmReceive.message',
        'Are you sure you want to receive? There are some lines with empty bin locations.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => this.save({
            ...formValues,
            receiptStatus: 'COMPLETED',
          }, () => {
            this.setState({ completed: true });
            const { requisition, shipmentId } = formValues;
            window.location = `/openboxes/stockMovement/show/${requisition || shipmentId}`;
          }),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  /**
  * Sends all changes made by user in this step of partial receiving to API and updates data.
  * @param {function} callback
  * @param {object} formValues
  * @public
  */
  save(formValues, callback) {
    this.saveValues(formValues)
      .then((response) => {
        this.props.hideSpinner();

        this.setState({ formData: {} }, () =>
          this.setState({ formData: parseResponse(response.data.data) }));
        if (callback) {
          callback();
        }
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Sends all changes made by user in this step of partial receiving to API
   * @param {object} formValues
   * @public
   */
  saveValues(formValues) {
    this.props.showSpinner();
    const url = `/openboxes/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=${this.state.page + 1}`;

    const payload = {
      ...formValues,
      containers: _.map(formValues.containers, container => ({
        ...container,
        shipmentItems: _.map(container.shipmentItems, (item) => {
          if (!_.get(item, 'recipient.id')) {
            return {
              ...item, recipient: '',
            };
          }

          return item;
        }),
      })),
    };

    return apiClient.post(url, flattenRequest(payload));
  }

  /**
   * Sends all changes made by user in this step of partial receiving to API and redirects
   * user to shipment page
   * @param {object} formValues
   * @public
   */
  saveAndExit(formValues) {
    this.saveValues(formValues)
      .then(() => {
        const { requisition, shipmentId } = formValues;
        window.location = `/openboxes/stockMovement/show/${requisition || shipmentId}`;
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Takes user to the next page.
   * @public
   */
  nextPage() {
    this.setState({ page: this.state.page + 1 }, () => this.fetchPartialReceiptCandidates());
  }

  /**
   * Returns user to the previous page.
   * @public
   */
  prevPage() {
    this.setState({ page: this.state.page - 1, completed: false }, () =>
      this.fetchPartialReceiptCandidates());
  }

  /**
   * Fetches available receipts from API.
   * @public
   */
  fetchPartialReceiptCandidates() {
    this.props.showSpinner();
    const url = `/openboxes/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=${this.state.page + 1}`;

    return apiClient.get(url)
      .then((response) => {
        const formData = parseResponse(response.data.data);
        this.setState({
          formData: {},
          locationId: formData.destination.id,
          shipmentNumber: formData.shipment.shipmentNumber,
        }, () => {
          this.fetchBins();
          this.setState({ formData });
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    const url = `/openboxes/api/internalLocations/receiving?location.id=${this.state.locationId}&shipmentNumber=${this.state.shipmentNumber}`;

    return apiClient.get(url)
      .then((response) => {
        const bins = _.map(response.data.data, bin => (
          { value: { id: bin.id, name: bin.name }, label: bin.name }
        ));
        this.setState({ bins }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const { page, formData, locationId } = this.state;

    if (locationId) {
      return (
        <Form
          onSubmit={values => this.onSubmit(values)}
          validate={validate}
          mutators={{ ...arrayMutators }}
          initialValues={formData}
          render={({ handleSubmit, values, form }) => (
            <div className="main-container">
              {values.shipment && values.shipment.shipmentNumber &&
              <h2 className="my-2 text-center">
                {`${values.shipment.shipmentNumber} ${values.shipment.name}`}
              </h2>}
              <div className="align-self-center">
                <form onSubmit={handleSubmit}>
                  {this.getFormList({
                    formValues: values,
                    change: form.change,
                    locationId,
                  })[page]}
                </form>
              </div>
            </div>
          )}
        />
      );
    }

    return null;
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  locale: state.session.activeLanguage,
  partialReceivingTranslationsFetched: state.session.fetchedTranslations.partialReceiving,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(ReceivingPage);

ReceivingPage.propTypes = {
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ shipmentId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  hasBinLocationSupport: PropTypes.bool.isRequired,
  locale: PropTypes.string.isRequired,
  partialReceivingTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};
