import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { hasMinimumRequiredRole } from 'utils/list-utils';
import Translate from 'utils/Translate';

const StockListHeader = ({ highestRole }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.stockMovement.stockList.label" defaultMessage="Stock List" />
    </span>
    <div className="d-flex justify-content-end buttons align-items-center">
      {
        hasMinimumRequiredRole('Admin', highestRole) && (
          <a className="primary-button" href="/openboxes/requisitionTemplate/create?type=STOCK">
            <Translate id="react.stockListManagement.addStockList.label" defaultMessage="Add stocklist" />
          </a>
        )
      }
    </div>
  </div>
);

const mapStateToProps = state => ({
  highestRole: state.session.highestRole,

});

export default connect(mapStateToProps)(StockListHeader);

StockListHeader.propTypes = {
  highestRole: PropTypes.string.isRequired,
};

