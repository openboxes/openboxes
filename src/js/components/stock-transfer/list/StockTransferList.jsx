import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchTranslations } from 'actions';
import filterFields from 'components/stock-transfer/list/FilterFields';
import StockTransferListFilters from 'components/stock-transfer/list/StockTransferListFilters';
import StockTransferListHeader from 'components/stock-transfer/list/StockTransferListHeader';
import StockTransferListTable from 'components/stock-transfer/list/StockTransferListTable';
import useStockTransferFilters from 'hooks/useStockTransferFilters';

const StockTransferList = (props) => {
  const { setFilterValues, defaultFilterValues, filterParams } = useStockTransferFilters();

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockTransfer');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);

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
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  statuses: state.stockTransfer.statuses,
  currentLocation: state.session.currentLocation,
});

const mapDispatchToProps = {
  fetchTranslations,
};
export default withRouter(connect(mapStateToProps, mapDispatchToProps)(StockTransferList));


StockTransferList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
};
