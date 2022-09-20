import React from 'react';

import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const PurchaseOrderListHeader = ({ history }) => (
  <div className="d-flex purchase-order-list-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.purchaseOrder.list.label" defaultMessage="Purchase Order List" />
    </span>
    <div className="d-flex justify-content-end buttons align-items-center">
      <Button
        defaultLabel="Create Shipment from PO"
        label="react.purchaseOrder.createShipmentFromPo.label"
        onClickAction={() => history.push('/openboxes/stockMovement/createCombinedShipments?direction=INBOUND')}
      />
      <a href="/openboxes/order/create">
        <Button
          defaultLabel="Create Order"
          label="react.purchaseOrder.createOrder.label"
        />
      </a>
    </div>
  </div>
);

export default withRouter(PurchaseOrderListHeader);

PurchaseOrderListHeader.propTypes = {
  history: PropTypes.func.isRequired,
};
