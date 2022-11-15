import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import Translate from 'utils/Translate';

const StockListHeader = ({ isUserAdmin }) => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.stocklists.header.label" defaultMessage="Stock List" />
    </span>
    <div className="d-flex justify-content-end buttons align-items-center">
      {
        isUserAdmin && (
          <a className="primary-button" href="/openboxes/requisitionTemplate/create?type=STOCK">
            <Translate id="react.stocklists.addStockList.label" defaultMessage="Add stocklist" />
          </a>
        )
      }
    </div>
  </div>
);

const mapStateToProps = state => ({
  isUserAdmin: state.session.isUserAdmin,
});

export default connect(mapStateToProps)(StockListHeader);

StockListHeader.propTypes = {
  isUserAdmin: PropTypes.bool.isRequired,
};

