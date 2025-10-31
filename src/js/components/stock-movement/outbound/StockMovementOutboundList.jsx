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
  const isRequestsList = props.sourceType === 'ELECTRONIC';
  const {
    selectFiltersForMyStockMovements,
    defaultFilterValues,
    setFilterValues,
    filterParams,
  } = useOutboundFilters(props.sourceType);

  useTranslation('stockMovement', 'StockMovementType', 'reactTable');
  const filters = filterFields(isRequestsList);
  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementOutboundHeader
        isRequestsOpen={isRequestsList}
        showMyStockMovements={selectFiltersForMyStockMovements}
      />
      <StockMovementOutboundFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filters}
        formProps={{
          requisitionStatuses: props.requisitionStatuses,
          approvers: props.approvers,
          shipmentTypes: props.shipmentTypes,
        }}
      />
      <StockMovementOutboundTable
        isRequestsOpen={isRequestsList}
        filterParams={filterParams}
      />
    </div>
  );
};

const mapStateToProps = (state) => ({
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
  requisitionStatuses: state.requisitionStatuses.data,
  isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
  shipmentTypes: state.stockMovementCommon.shipmentTypes,
  approvers: state.approvers.data,
});

export default withRouter(connect(mapStateToProps, {
  fetchStatuses: fetchRequisitionStatusCodes,
})(StockMovementOutboundList));

StockMovementOutboundList.propTypes = {
  sourceType: PropTypes.string.isRequired,
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
  approvers: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    label: PropTypes.string,
    value: PropTypes.string,
  })).isRequired,
};
