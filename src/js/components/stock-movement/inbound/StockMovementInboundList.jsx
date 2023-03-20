import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchShipmentStatusCodes } from 'actions';
import filterFields from 'components/stock-movement/inbound/FilterFields';
import StockMovementInboundFilters from 'components/stock-movement/inbound/StockMovementInboundFilters';
import StockMovementInboundHeader from 'components/stock-movement/inbound/StockMovementInboundHeader';
import StockMovementInboundTable from 'components/stock-movement/inbound/StockMovementInboundTable';
import useInboundFilters from 'hooks/list-pages/inbound/useInboundFilters';
import useTranslation from 'hooks/useTranslation';

const StockMovementInboundList = (props) => {
  const {
    selectFiltersForMyStockMovements,
    defaultFilterValues,
    setFilterValues,
    filterParams,
  } = useInboundFilters();

  useTranslation('stockMovement', 'reactTable');

  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementInboundHeader showMyStockMovements={selectFiltersForMyStockMovements} />
      <StockMovementInboundFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{
          shipmentStatuses: props.shipmentStatuses,
          shipmentTypes: props.shipmentTypes,
        }}
      />
      <StockMovementInboundTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  shipmentStatuses: state.shipmentStatuses.data,
  isShipmentStatusesFetched: state.shipmentStatuses.fetched,
  shipmentTypes: state.stockMovementCommon.shipmentTypes,
});

export default withRouter(connect(mapStateToProps, {
  fetchStatuses: fetchShipmentStatusCodes,
})(StockMovementInboundList));

StockMovementInboundList.propTypes = {
  shipmentStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
  shipmentTypes: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    label: PropTypes.string,
    description: PropTypes.string,
  })).isRequired,
};
