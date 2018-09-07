import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import moment from 'moment';
import Alert from 'react-s-alert';

import PartialReceivingPage from './PartialReceivingPage';
import ReceivingCheckScreen from './ReceivingCheckScreen';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

function validate(values) {
  const errors = {};

  if (!values.dateDelivered) {
    errors.dateDelivered = 'This field is required';
  } else {
    const date = moment(values.dateDelivered, 'MM/DD/YYYY');
    if (moment().diff(date) < 0) {
      errors.dateDelivered = 'The date cannot be in the future';
    }
  }

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
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
    this.save = this.save.bind(this);
  }

  componentDidMount() {
    this.fetchPartialReceiptCandidates();
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
        shipmentItems: _.filter(container.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== ''),
      }));
      const payload = {
        ...formValues, receiptStatus: 'CHECKING', containers: _.filter(containers, container => container.shipmentItems.length),
      };

      this.save(payload, this.nextPage);
    } else {
      this.save({ ...formValues, receiptStatus: 'COMPLETE' }, () => {
        this.setState({ completed: true });
        Alert.success('Shipment was received successfully!');
      });
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
      />,
      <ReceivingCheckScreen
        {...props}
        prevPage={this.prevPage}
        save={this.save}
        completed={this.state.completed}
      />,
    ];
  }

  /**
  * Sends all changes made by user in this step of partial receiving to API and updates data.
  * @param {function} callback
  * @param {object} formValues
  * @public
  */
  save(formValues, callback) {
    this.props.showSpinner();
    const url = `/openboxes/api/partialReceiving/${this.props.match.params.shipmentId}`;

    return apiClient.post(url, flattenRequest(formValues))
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
   * Takes user to the next page.
   * @public
   */
  nextPage() {
    this.setState({ page: this.state.page + 1 });
  }

  /**
   * Returns user to the previous page.
   * @public
   */
  prevPage() {
    this.setState({ page: this.state.page - 1, completed: false });
  }

  /**
   * Fetches available receipts from API.
   * @public
   */
  fetchPartialReceiptCandidates() {
    this.props.showSpinner();
    const url = `/openboxes/api/partialReceiving/${this.props.match.params.shipmentId}`;

    return apiClient.get(url)
      .then((response) => {
        this.setState({ formData: parseResponse(response.data.data) });
        this.fetchBins();
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    const url = '/openboxes/api/internalLocations';

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
    const { page, formData } = this.state;

    return (
      <Form
        onSubmit={values => this.onSubmit(values)}
        validate={validate}
        mutators={{ ...arrayMutators }}
        initialValues={formData}
        render={({ handleSubmit, values, form }) => (
          <div>
            {values.shipment && values.shipment.shipmentNumber &&
              <h2 className="my-2 text-center">{`${values.shipment.shipmentNumber} ${values.shipment.name}`}</h2>}
            <div className="align-self-center">
              <form onSubmit={handleSubmit}>
                {this.getFormList({
                  formValues: values, change: form.change,
                })[page]}
              </form>
            </div>
          </div>
        )}
      />
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(ReceivingPage);

ReceivingPage.propTypes = {
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ shipmentId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
};
