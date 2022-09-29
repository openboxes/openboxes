import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import StockTransferListFilters from 'components/stock-transfer/list/StockTransferListFilters';
import StockTransferListHeader from 'components/stock-transfer/list/StockTransferListHeader';
import StockTransferListTable from 'components/stock-transfer/list/StockTransferListTable';


const StockTransferList = (props) => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockTransfer');
  }, [props.locale]);

  return (
    <div className="d-flex flex-column list-page-main">
      <StockTransferListHeader />
      <StockTransferListFilters setFilterParams={setFilterParams} />
      <StockTransferListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

const mapDispatchToProps = {
  fetchTranslations,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockTransferList);


StockTransferList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};
