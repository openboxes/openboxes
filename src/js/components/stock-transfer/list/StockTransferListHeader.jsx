import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link, withRouter } from 'react-router-dom';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const StockTransferListHeader = ({ isUserManager }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.stockTransfer.list.label" defaultMessage="List Stock Transfers" />
    </span>
    {isUserManager &&
      <div className="d-flex justify-content-end buttons align-items-center">
        <Link to="/openboxes/stockTransfer/create">
          <Button
            defaultLabel="Create Stock Transfer"
            label="react.stockTransfer.createStockTransfer.label"
            variant="primary"
          />
        </Link>
      </div>
    }
  </div>
);

const mapStateToProps = state => ({
  isUserManager: state.session.isUserManager,
});

export default withRouter(connect(mapStateToProps)(StockTransferListHeader));

StockTransferListHeader.propTypes = {
  isUserManager: PropTypes.bool.isRequired,
};

