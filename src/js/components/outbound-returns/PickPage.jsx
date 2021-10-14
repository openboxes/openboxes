import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';

import { showSpinner, hideSpinner } from '../../actions';
import apiClient from '../../utils/apiClient';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const FIELDS = {
  picklistItems: {
    type: ArrayField,
    fields: {
      'product.productCode': {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Code',
        flexWidth: '0.5',
      },
      'product.name': {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        flexWidth: '2',
        headerAlign: 'left',
        attributes: {
          showValueTooltip: true,
          className: 'text-left ml-1',
        },
      },
      originZone: {
        type: LabelField,
        label: 'react.outboundReturn.zone.label',
        defaultMessage: 'Zone',
        flexWidth: '0.5',
        attributes: {
          showValueTooltip: true,
        },
      },
      'originBinLocation.name': {
        type: LabelField,
        label: 'react.outboundReturn.bin.label',
        defaultMessage: 'Bin Location',
        flexWidth: '1',
        attributes: {
          showValueTooltip: true,
        },
        getDynamicAttr: () => ({
          formatValue: value => value || 'DEFAULT',
        }),
      },
      lotNumber: {
        type: LabelField,
        label: 'react.outboundReturn.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.outboundReturn.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1',
      },
      quantity: {
        type: LabelField,
        label: 'react.outboundReturn.quantity.label',
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
    };

    this.fetchOutboundReturn = this.fetchOutboundReturn.bind(this);
    this.nextPage = this.nextPage.bind(this);
  }

  componentDidMount() {
    if (this.props.outboundReturnTranslationsFetched) {
      this.dataFetched = true;
      this.fetchOutboundReturn();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.outboundReturnTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchOutboundReturn();
    }
  }

  dataFetched = false;

  fetchOutboundReturn() {
    this.props.showSpinner();
    const url = `/openboxes/api/stockTransfers/${this.props.match.params.outboundReturnId}`;

    return apiClient.get(url)
      .then((resp) => {
        const outboundReturn = resp.data.data;
        this.setState({
          values: { outboundReturn },
        }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  // eslint-disable-next-line class-methods-use-this
  nextPage() {
    // TODO: Will be finished in OBAM-269
    // if (this.state.values.outboundReturn.status === 'APPROVED') {
    //   this.props.showSpinner();
    //   const url = `/openboxes/api/outboundReturns/${this.props.match.params.outboundReturnId}`;
    //   const payload = { status: 'PLACED' };
    //   apiClient.post(url, payload)
    //     .then((resp) => {
    //       const outboundReturn = resp.data.data;
    //       this.props.hideSpinner();
    //       this.props.nextPage(outboundReturn);
    //     })
    //     .catch(() => this.props.hideSpinner());
    // } else {
    //   this.props.nextPage(this.state.values);
    // }
  }

  previousPage(values) {
    this.props.previousPage(values);
  }

  render() {
    const { outboundReturn } = this.state.values;
    const picklistItems = _.flatten(_.map(outboundReturn.stockTransferItems, 'picklistItems'));

    return (
      <Form
        onSubmit={() => this.nextPage()}
        mutators={{ ...arrayMutators }}
        initialValues={{ picklistItems }}
        render={({ handleSubmit }) => (
          <div className="d-flex flex-column">
            <form onSubmit={handleSubmit} className="print-mt">
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
                  outboundReturnId: this.props.match.params.outboundReturnId,
                  refetchOutboundReturn: this.fetchOutboundReturn,
                  locationId: this.props.locationId,
                  translate: this.props.translate,
                }))}
              </div>
              <div className="submit-buttons d-flex justify-content-between">
                <button
                  type="button"
                  onClick={() => this.previousPage(outboundReturn)}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                ><Translate id="react.outboundReturn.previous.label" defaultMessage="Provious" />
                </button>
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                ><Translate id="react.outboundReturn.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  outboundReturnTranslationsFetched: state.session.fetchedTranslations.outboundReturn,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
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
  // eslint-disable-next-line react/no-unused-prop-types
  nextPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  initialValues: PropTypes.shape({}).isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      outboundReturnId: PropTypes.string,
    }),
  }).isRequired,
  locationId: PropTypes.string.isRequired,
  outboundReturnTranslationsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};
