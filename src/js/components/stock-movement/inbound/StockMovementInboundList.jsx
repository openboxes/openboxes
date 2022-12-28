import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchShipmentStatusCodes, fetchTranslations } from 'actions';
import filterFields from 'components/stock-movement/inbound/FilterFields';
import StockMovementInboundFilters from 'components/stock-movement/inbound/StockMovementInboundFilters';
import StockMovementInboundHeader from 'components/stock-movement/inbound/StockMovementInboundHeader';
import StockMovementInboundTable from 'components/stock-movement/inbound/StockMovementInboundTable';
import useInboundFilters from 'hooks/list-pages/inbound/useInboundFilters';

const StockMovementInboundList = (props) => {
  const {
    selectFiltersForMyStockMovements, defaultFilterValues, setFilterValues, filterParams,
  } = useInboundFilters();

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);

  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementInboundHeader showMyStockMovements={selectFiltersForMyStockMovements} />
      <StockMovementInboundFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{ shipmentStatuses: props.shipmentStatuses }}
      />
      <StockMovementInboundTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  shipmentStatuses: state.shipmentStatuses.data,
  isShipmentStatusesFetched: state.shipmentStatuses.fetched,

});

export default withRouter(connect(mapStateToProps, {
  fetchTranslations,
  fetchStatuses: fetchShipmentStatusCodes,
})(StockMovementInboundList));

StockMovementInboundList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  shipmentStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
};
