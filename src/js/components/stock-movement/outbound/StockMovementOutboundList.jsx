import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchRequisitionStatusCodes } from 'actions';
import filterFields from 'components/stock-movement/outbound/FilterFields';
import StockMovementOutboundFilters from 'components/stock-movement/outbound/StockMovementOutboundFilters';
import StockMovementOutboundHeader from 'components/stock-movement/outbound/StockMovementOutboundHeader';
import StockMovementOutboundTable from 'components/stock-movement/outbound/StockMovementOutboundTable';
import useOutboundFilters from 'hooks/list-pages/outbound/useOutboundFilters';
import useTranslation from 'hooks/useTranslation';

const StockMovementOutboundList = (props) => {
  const {
    selectFiltersForMyStockMovements,
    defaultFilterValues,
    setFilterValues,
    filterParams,
  } = useOutboundFilters(props.isRequestsList);

  useTranslation('stockMovement', 'StockMovementType', 'reactTable');

  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementOutboundHeader
        isRequestsOpen={props.isRequestsList}
        showMyStockMovements={selectFiltersForMyStockMovements}
      />
      <StockMovementOutboundFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{
          requisitionStatuses: props.requisitionStatuses,
          shipmentTypes: props.shipmentTypes,
        }}
      />
      <StockMovementOutboundTable
        isRequestsOpen={props.isRequestsList}
        filterParams={filterParams}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
  requisitionStatuses: state.requisitionStatuses.data,
  isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
  shipmentTypes: state.stockMovementCommon.shipmentTypes,
});

export default withRouter(connect(mapStateToProps, {
  fetchStatuses: fetchRequisitionStatusCodes,
})(StockMovementOutboundList));

StockMovementOutboundList.propTypes = {
  isRequestsList: PropTypes.bool.isRequired,
  requisitionStatuses: PropTypes.arrayOf(PropTypes.shape({
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
