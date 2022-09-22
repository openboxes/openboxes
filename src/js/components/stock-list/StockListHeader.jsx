import React from 'react';

import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const StockListHeader = ({ history }) => (
  <div className="d-flex purchase-order-list-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.stockMovement.stocklistList.label" defaultMessage="Stocklist List" />
    </span>
    <div className="d-flex justify-content-end buttons align-items-center">
      <Button
        defaultLabel="Add stocklist"
        label="react.stockListManagement.addStockList.label"
        onClickAction={() => history.push('/openboxes/requisitionTemplate/create?type=STOCK')}
      />
    </div>
  </div>
);

export default withRouter(StockListHeader);

StockListHeader.propTypes = {
  history: PropTypes.func.isRequired,
};
