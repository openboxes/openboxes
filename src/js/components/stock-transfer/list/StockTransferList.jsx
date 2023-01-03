import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import filterFields from 'components/stock-transfer/list/FilterFields';
import StockTransferListFilters from 'components/stock-transfer/list/StockTransferListFilters';
import StockTransferListHeader from 'components/stock-transfer/list/StockTransferListHeader';
import StockTransferListTable from 'components/stock-transfer/list/StockTransferListTable';
import useStockTransferFilters from 'hooks/list-pages/stock-transfer/useStockTransferFilters';
import useTranslation from 'hooks/useTranslation';

const StockTransferList = (props) => {
  const { setFilterValues, defaultFilterValues, filterParams } = useStockTransferFilters();

  useTranslation('stockTransfer', 'reactTable');

  return (
    <div className="d-flex flex-column list-page-main">
      <StockTransferListHeader />
      <StockTransferListFilters
        filterFields={filterFields}
        setFilterParams={setFilterValues}
        defaultValues={defaultFilterValues}
        formProps={{ statuses: props.statuses }}
      />
      <StockTransferListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  currentUser: state.session.user,
  statuses: state.stockTransfer.statuses,
  currentLocation: state.session.currentLocation,
});

export default withRouter(connect(mapStateToProps)(StockTransferList));


StockTransferList.propTypes = {
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
};
