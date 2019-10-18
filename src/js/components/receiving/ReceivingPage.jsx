import React, { Component } from 'react';

import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchTranslations, hideSpinner, showSpinner } from 'actions';
import PartialReceivingPage from 'components/receiving/PartialReceivingPage';
import ReceivingCheckScreen from 'components/receiving/ReceivingCheckScreen';
import Wizard from 'components/wizard/Wizard';
import apiClient, { parseResponse } from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';
import 'components/stock-movement-wizard/StockMovement.scss';


/** Main partial receiving form's component. */
class ReceivingPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      bins: [],
      formData: {},
      locationId: '',
      shipmentNumber: '',
      currentPage: 1,
    };
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

  getStepList() {
    const stepList = [this.props.translate('react.receiving.label', 'Receiving'),
      this.props.translate('react.requisition.wizard.confirm.label', 'Check')];

    return stepList;
  }

  /**
   * Returns shipment's name containing shipment's origin, destination, requisition date,
   * tracking number given by user on the last step, description and stock list if chosen.
   * @public
   */
  getWizardTitle() {
    const { formData } = this.state;
    if (!formData.shipment) {
      return '';
    }
    const dateShipped = moment(formData.dateShipped).format('MM/DD/YYYY');
    return [
      {
        text: 'Receiving',
        color: '#000000',
        delimeter: ' | ',
      },
      {
        text: formData.shipment.shipmentNumber,
        color: '#000000',
        delimeter: ' - ',
      },
      {
        text: formData.origin.name,
        color: '#004d40',
        delimeter: ' to ',
      },
      {
        text: formData.destination.name,
        color: '#01579b',
        delimeter: ', ',
      },
      {
        text: dateShipped,
        color: '#4a148c',
        delimeter: ', ',
      },
      {
        text: formData.description,
        color: '#770838',
        delimeter: '',
      },
    ];
  }

  dataFetched = false;

  /**
   * Fetches available receipts from API.
   * @public
   */
  fetchPartialReceiptCandidates() {
    this.props.showSpinner();
    const url = `/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=${this.state.currentPage}`;

    return apiClient.get(url)
      .then((response) => {
        const formData = parseResponse(response.data.data);
        this.setState({
          formData,
          locationId: formData.destination.id,
          shipmentNumber: formData.shipment.shipmentNumber,
        }, () => {
          this.fetchBins();
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    const url = `/api/internalLocations/receiving?location.id=${this.state.locationId}&shipmentNumber=${this.state.shipmentNumber}`;
    const mapBins = bins => (_.chain(bins)
      .orderBy(['name'], ['asc']).value()
    );

    return apiClient.get(url)
      .then((response) => {
        const binGroups = _.partition(response.data.data, bin => (bin.zoneName));
        const binsWithZone = _.chain(binGroups[0]).groupBy('zoneName')
          .map((value, key) => ({ name: key, options: mapBins(value) }))
          .orderBy(['label'], ['asc'])
          .value();
        const binsWithoutZone = mapBins(binGroups[1]);
        this.setState(
          { bins: [...binsWithZone, ...binsWithoutZone] },
          () => this.props.hideSpinner(),
        );
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const {
      formData, locationId, currentPage, bins,
    } = this.state;
    const { match, translate } = this.props;
    const title = this.getWizardTitle();
    const pageList = [PartialReceivingPage, ReceivingCheckScreen];
    const stepList = this.getStepList();

    if (locationId) {
      return (
        <Wizard
          pageList={pageList}
          stepList={stepList}
          initialValues={formData}
          title={title}
          currentPage={currentPage}
          prevPage={currentPage === 1 ? 1 : currentPage - 1}
          additionalProps={{
            bins, locationId, match, translate,
          }}
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
  hasPartialReceivingSupport: state.session.currentLocation.hasPartialReceivingSupport,
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
  locale: PropTypes.string.isRequired,
  partialReceivingTranslationsFetched: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};
