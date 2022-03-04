import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import apiClient, { parseResponse } from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';


const FIELDS = {
  pendingRequisitionDetails: {
    type: ArrayField,
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'requisitions',
    fields: {
      'destination.name': {
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        headerAlign: 'left',
        label: 'react.stockMovement.destination.label',
        defaultMessage: 'Destination',
        attributes: {
          showValueTooltip: true,
          className: 'text-left ml-1',
        },
      },
      averageMonthlyDemand: {
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'react.averageMonthlyDemand.lot.label',
        defaultMessage: 'Average Monthly Demand',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
          numberField: true,
        },
      },
      quantityOnHandAtDestination: {
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'react.stockMovement.qoh.label',
        defaultMessage: 'QoH',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
          numberField: true,
        },
      },
      requisition: {
        type: (params) => {
          const { fieldValue } = params;
          if (fieldValue) {
            return (
              <div className="d-flex align-items-center justify-content-center">
                <a target="_blank" rel="noopener noreferrer" href={`/openboxes/stockMovement/show/${fieldValue.id}`}>
                  {fieldValue.requestNumber}
                </a>
              </div>
            );
          }
          return null;
        },
        label: 'react.stockMovement.requestNumber.label',
        defaultMessage: 'Request number',
      },
      quantityRequested: {
        type: LabelField,
        label: 'react.stockMovement.requested.label',
        defaultMessage: 'Requested',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
          numberField: true,
        },
      },
      quantityPicked: {
        type: LabelField,
        label: 'react.stockMovement.picked.label',
        defaultMessage: 'Picked',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
          numberField: true,
        },
      },
    },
  },
};

class DetailsModal extends Component {
  constructor(props) {
    super(props);

    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      attr,
      formValues: {},
    };

    this.onOpen = this.onOpen.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  onOpen() {
    this.fetchPendingRequestsDetails();
  }

  fetchPendingRequestsDetails() {
    const url = `/openboxes/api/stockMovements/pendingRequisitionDetails?origin.id=${this.state.attr.originId}&product.id=${this.state.attr.productId}&stockMovementId=${this.state.attr.stockMovementId}`;

    apiClient.get(url)
      .then((resp) => {
        this.setState({ formValues: { pendingRequisitionDetails: parseResponse(resp.data.data) } });
        this.props.hideSpinner();
      })
      .catch(() => { this.props.hideSpinner(); });
  }

  render() {
    const { attr, formValues } = this.state;
    const { pendingRequisitionDetails } = formValues;
    const {
      productCode, productName, quantityRequested, quantityOnHand, quantityAvailable,
    } = attr;
    const averageMonthlyDemand = _.sumBy(pendingRequisitionDetails, 'averageMonthlyDemand');
    const totalQtyRequested = _.sumBy(pendingRequisitionDetails, item => item.quantityRequested + _.sumBy(item.requisitions, 'quantityRequested'));
    const totalQtyPicked = _.sumBy(pendingRequisitionDetails, item => item.quantityPicked + _.sumBy(item.requisitions, 'quantityPicked'));

    return (
      <ModalWrapper
        {...attr}
        onOpen={this.onOpen}
        fields={FIELDS}
        initialValues={formValues}
      >
        <div className="mb-2">
          <h5>{productCode} {productName}</h5>
          <div>
            <span className="font-weight-bold">
              <Translate id="react.stockMovement.requested.label" defaultMessage="Requested" />:&nbsp;&nbsp;
            </span>
            {quantityRequested ? (quantityRequested.toLocaleString('en-US')) : quantityRequested}
          </div>
          <div>
            <span className="font-weight-bold">
              <Translate id="react.stockMovement.onHand.label" defaultMessage="On Hand" />:&nbsp;&nbsp;
            </span>
            {quantityOnHand ? (quantityOnHand.toLocaleString('en-US')) : quantityOnHand}
          </div>
          <div>
            <span className="font-weight-bold">
              <Translate id="react.stockMovement.available.label" defaultMessage="Available" />:&nbsp;&nbsp;
            </span>
            {quantityAvailable ? (quantityAvailable.toLocaleString('en-US')) : quantityAvailable}
          </div>
          <div>
            <span className="font-weight-bold">
              <Translate id="react.stockMovement.totalMonthlyDemand.label" defaultMessage="Total Monthly Demand" />:&nbsp;&nbsp;
            </span>
            {averageMonthlyDemand ? (averageMonthlyDemand.toLocaleString('en-US')) : averageMonthlyDemand}
          </div>
          <Tooltip
            html={this.props.translate(
              'react.stockMovement.requestedInOtherRequests.label',
              'This is the quantity requested in other requests but not yet picked. Picked quantities have already been removed from the available quantity',
            )}
            theme="transparent"
            position="top-start"
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
          >
            <div>
              <span className="font-weight-bold">
                <Translate id="react.stockMovement.totalQuantityRequested.label" defaultMessage="Total Qty Requested" />:&nbsp;&nbsp;
              </span>
              {totalQtyRequested ? (totalQtyRequested.toLocaleString('en-US')) : totalQtyRequested}
            </div>
            <div>
              <span className="font-weight-bold">
                <Translate id="react.stockMovement.totalQuantityPicked.label" defaultMessage="Total Qty Picked" />:&nbsp;&nbsp;
              </span>
              {totalQtyPicked ? (totalQtyPicked.toLocaleString('en-US')) : totalQtyPicked}
            </div>
          </Tooltip>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(DetailsModal);

DetailsModal.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  hasBinLocationSupport: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};
