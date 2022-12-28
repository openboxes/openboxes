import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { fetchRequisitionStatusCodes, fetchTranslations } from 'actions';
import filterFields from 'components/stock-movement/outbound/FilterFields';
import StockMovementOutboundFilters from 'components/stock-movement/outbound/StockMovementOutboundFilters';
import StockMovementOutboundHeader from 'components/stock-movement/outbound/StockMovementOutboundHeader';
import StockMovementOutboundTable from 'components/stock-movement/outbound/StockMovementOutboundTable';
import useOutboundFilters from 'hooks/list-pages/outbound/useOutboundFilters';

const StockMovementOutboundList = (props) => {
  const {
    selectFiltersForMyStockMovements, defaultFilterValues, setFilterValues, filterParams,
  } = useOutboundFilters(props.isRequestsList);

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
    props.fetchTranslations(props.locale, 'StockMovementType');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);

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
        formProps={{ requisitionStatuses: props.requisitionStatuses }}
      />
      <StockMovementOutboundTable
        isRequestsOpen={props.isRequestsList}
        filterParams={filterParams}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
  requisitionStatuses: state.requisitionStatuses.data,
  isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
});

export default withRouter(connect(mapStateToProps, {
  fetchTranslations,
  fetchStatuses: fetchRequisitionStatusCodes,
})(StockMovementOutboundList));

StockMovementOutboundList.propTypes = {
  locale: PropTypes.string.isRequired,
  isRequestsList: PropTypes.bool.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  requisitionStatuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    variant: PropTypes.string,
    label: PropTypes.string,
  })).isRequired,
};
