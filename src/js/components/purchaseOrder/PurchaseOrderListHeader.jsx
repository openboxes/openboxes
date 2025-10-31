import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import { ORDER_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import Translate from 'utils/Translate';

const PurchaseOrderListHeader = ({ history, supportedActivities }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.purchaseOrder.list.label" defaultMessage="Purchase Order List" />
    </span>
    <div className="d-flex justify-content-end buttons align-items-center">
      <Button
        defaultLabel="Create Shipment from PO"
        label="react.purchaseOrder.createShipmentFromPo.label"
        onClick={() => history.push({ pathname: STOCK_MOVEMENT_URL.createCombinedShipments(), search: 'direction=INBOUND' })}
      />
      {supportedActivities.includes('PLACE_ORDER')
        && (
        <a href={ORDER_URL.create()}>
          <Button
            defaultLabel="Create Order"
            label="react.purchaseOrder.createOrder.label"
          />
        </a>
        )}

    </div>
  </div>
);

const mapStateToProps = (state) => ({
  supportedActivities: state.session.supportedActivities,
});

export default withRouter(connect(mapStateToProps)(PurchaseOrderListHeader));

PurchaseOrderListHeader.propTypes = {
  history: PropTypes.shape({
    push: PropTypes.func.isRequired,
  }).isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
};
