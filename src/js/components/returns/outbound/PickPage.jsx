import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import DateFormat from 'consts/dateFormat';
import apiClient, { parseResponse } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { formatProductDisplayName } from 'utils/form-values-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import { formatDate } from 'utils/translation-utils';

import 'react-confirm-alert/src/react-confirm-alert.css';

const FIELDS = {
  picklistItems: {
    type: ArrayField,
    getDynamicRowAttr: ({ rowValues, translate }) => {
      let className = '';
      let tooltip = '';
      if (rowValues.recalled && rowValues.onHold) {
        className = 'recalled-and-on-hold';
        tooltip = translate('react.outboundReturns.recalledAndOnHold.label');
      } else if (rowValues.recalled) {
        className = 'recalled';
        tooltip = translate('react.outboundReturns.recalled.label');
      } else if (rowValues.onHold) {
        className = 'on-hold';
        tooltip = translate('react.outboundReturns.onHold.label');
      }
      return { className, tooltip };
    },
    fields: {
      'product.productCode': {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Code',
        flexWidth: '0.5',
      },
      product: {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        flexWidth: '2',
        headerAlign: 'left',
        getDynamicAttr: ({ fieldValue }) => ({
          tooltipValue: fieldValue?.name,
        }),
        attributes: {
          formatValue: formatProductDisplayName,
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      originZone: {
        type: LabelField,
        label: 'react.outboundReturns.zone.label',
        defaultMessage: 'Zone',
        flexWidth: '0.5',
        attributes: {
          showValueTooltip: true,
        },
      },
      'originBinLocation.name': {
        type: LabelField,
        label: 'react.outboundReturns.bin.label',
        defaultMessage: 'Bin Location',
        flexWidth: '1',
        attributes: {
          showValueTooltip: true,
        },
        getDynamicAttr: () => ({
          formatValue: (value) => value || 'DEFAULT',
        }),
      },
      lotNumber: {
        type: LabelField,
        label: 'react.outboundReturns.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.outboundReturns.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1',
        getDynamicAttr: ({ formatLocalizedDate }) => ({
          formatValue: (value) => formatLocalizedDate(value, DateFormat.COMMON),
        }),
      },
      quantity: {
        type: LabelField,
        label: 'react.outboundReturns.quantity.label',
        defaultMessage: 'Qty to Return',
        flexWidth: '1',
      },
    },
  },
};

class PickPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      values: { outboundReturn: { ...this.props.initialValues } },
      printPicksUrl: '',
    };

    this.fetchOutboundReturn = this.fetchOutboundReturn.bind(this);
    this.nextPage = this.nextPage.bind(this);
  }

  componentDidMount() {
    if (this.props.outboundReturnsTranslationsFetched) {
      this.dataFetched = true;
      this.fetchOutboundReturn();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.outboundReturnsTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchOutboundReturn();
    }
  }

  dataFetched = false;

  fetchOutboundReturn() {
    this.props.showSpinner();
    const url = `/api/stockTransfers/${this.props.match.params.outboundReturnId}`;

    return apiClient.get(url)
      .then((resp) => {
        const outboundReturn = resp.data.data;
        const printPicks = _.find(
          outboundReturn.documents,
          (doc) => doc.documentType === 'PICKLIST',
        );
        this.setState({
          values: { outboundReturn },
          printPicksUrl: printPicks ? printPicks.uri : '/',
        }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  nextPage() {
    this.props.showSpinner();
    const url = `/api/stockTransfers/${this.props.match.params.outboundReturnId}`;
    const payload = parseResponse({
      ...this.state.values.outboundReturn,
      status: 'PLACED',
    });

    apiClient.put(url, payload)
      .then((resp) => {
        const outboundReturn = resp.data.data;
        this.props.hideSpinner();
        this.props.nextPage(outboundReturn);
      })
      .catch(() => this.props.hideSpinner());
  }

  previousPage(values) {
    this.props.previousPage(values);
  }

  render() {
    // There is a parseResponse to avoid affecting outboundReturn in state
    const { outboundReturn } = parseResponse(this.state.values);
    const picklistItems = _.flatten(_.map(outboundReturn.stockTransferItems, 'picklistItems'));

    return (
      <Form
        onSubmit={() => this.nextPage()}
        mutators={{ ...arrayMutators }}
        initialValues={{ picklistItems }}
        render={({ handleSubmit, values }) => (
          <div className="d-flex flex-column">
            <span className="buttons-container">
              <a
                href={this.state.printPicksUrl}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
                target="_blank"
                rel="noopener noreferrer"
              >
                <span>
                  <i className="fa fa-print pr-2" />
                  <Translate id="react.stockMovement.printPicklist.label" defaultMessage="Print picklist" />
                </span>
              </a>
            </span>
            <form onSubmit={handleSubmit} className="print-mt">
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  outboundReturnId: this.props.match.params.outboundReturnId,
                  refetchOutboundReturn: this.fetchOutboundReturn,
                  locationId: this.props.locationId,
                  translate: this.props.translate,
                  formatLocalizedDate: this.props.formatLocalizedDate,
                  values,
                }))}
              </div>
              <div className="submit-buttons d-flex justify-content-between">
                <button
                  type="button"
                  onClick={() => this.previousPage(outboundReturn)}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                >
                  <Translate id="react.outboundReturns.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                >
                  <Translate id="react.outboundReturns.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = (state) => ({
  outboundReturnsTranslationsFetched: state.session.fetchedTranslations.outboundReturns,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  formatLocalizedDate: formatDate(state.localize),
});

export default connect(
  mapStateToProps,
  {
    showSpinner, hideSpinner,
  },
)(PickPage);

PickPage.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({}).isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      outboundReturnId: PropTypes.string,
    }),
  }).isRequired,
  locationId: PropTypes.string.isRequired,
  outboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
  formatLocalizedDate: PropTypes.func.isRequired,
};
