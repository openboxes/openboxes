import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import ArrayField from '../../form-elements/ArrayField';
import apiClient, { parseResponse } from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';
import Translate, { translateWithDefaultMessage } from '../../../utils/Translate';

const FIELDS = {
  pendingRequisitionDetails: {
    type: ArrayField,
    fields: {
      'destination.name': {
        type: LabelField,
        label: 'react.stockMovement.destination.label',
        defaultMessage: 'Destination',
        attributes: {
          showValueTooltip: true,
          className: 'text-left ml-1',
        },
      },
      averageMonthlyDemand: {
        type: LabelField,
        label: 'react.averageMonthlyDemand.lot.label',
        defaultMessage: 'Average Monthly Demand',
      },
      quantityOnHandAtDestination: {
        type: LabelField,
        label: 'react.stockMovement.qoh.label',
        defaultMessage: 'QoH',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
        },
      },
      requestNumber: {
        type: LabelField,
        label: 'react.stockMovement.requestNumber.label',
        defaultMessage: 'Request number',
      },
      quantityRequested: {
        type: LabelField,
        label: 'react.stockMovement.requested.label',
        defaultMessage: 'Requested',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
        },
      },
      quantityPicked: {
        type: LabelField,
        label: 'react.stockMovement.picked.label',
        defaultMessage: 'Picked',
        attributes: {
          formatValue: value => (value || value === 0 ? value.toLocaleString('en-US') : null),
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
    const url = `/openboxes/api/stockMovements/pendingRequisitionDetails?origin.id=${this.state.attr.originId}&product.id=${this.state.attr.productId}`;

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

    return (
      <ModalWrapper
        {...attr}
        onOpen={this.onOpen}
        fields={FIELDS}
        initialValues={formValues}
      >
        <div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productCode.label" defaultMessage="Product code" />: {attr.productCode}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productName.label" defaultMessage="Product name" />: {attr.productName}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.totalMonthlyDemand.label" defaultMessage="Total Monthly Demand" />:
            {_.sumBy(_.uniqBy(pendingRequisitionDetails, 'destination.name'), 'averageMonthlyDemand')}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.totalQuantityRequired.label" defaultMessage="Total Qty Required" />:
            {_.sumBy(pendingRequisitionDetails, 'quantityRequested')}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.totalQuantityPicked.label" defaultMessage="Total Qty Picked" />:
            {_.sumBy(pendingRequisitionDetails, 'quantityPicked')}
          </div>
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
