import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const StockTransferListHeader = ({ history, isUserManager }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.stockTransfer.list.label" defaultMessage="List Stock Transfers" />
    </span>
    {isUserManager &&
      <div className="d-flex justify-content-end buttons align-items-center">
        <Button
          defaultLabel="Create Stock Transfer"
          label="react.stockTransfer.createStockTransfer.label"
          variant="primary"
          onClick={() => history.push('/openboxes/stockTransfer/create')}
        />
      </div>
    }
  </div>
);

const mapStateToProps = state => ({
  isUserManager: state.session.isUserManager,
});

export default withRouter(connect(mapStateToProps)(StockTransferListHeader));

StockTransferListHeader.propTypes = {
  history: PropTypes.func.isRequired,
  isUserManager: PropTypes.bool.isRequired,
};

