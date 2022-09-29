import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import StockMovementOutboundFilters from 'components/stock-movement/outbound/StockMovementOutboundFilters';
import StockMovementOutboundHeader from 'components/stock-movement/outbound/StockMovementOutboundHeader';
import StockMovementOutboundTable from 'components/stock-movement/outbound/StockMovementOutboundTable';

const StockMovementOutboundList = (props) => {
  const [filterParams, setFilterParams] = useState({
    origin: {
      id: props.currentLocation.id,
      value: props.currentLocation.id,
      name: props.currentLocation.name,
      label: props.currentLocation.name,
    },
  });

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
    props.fetchTranslations(props.locale, 'StockMovementType');
  }, [props.locale]);

  useEffect(() => {
    if (props.isRequestsList) {
      setFilterParams(filters => ({
        ...filters,
        sourceType: 'ELECTRONIC',
      }));
    }
  }, [props.isRequestsList]);


  const selectFiltersForMyStockMovements = () => {
    const currentUserValue = {
      id: props.currentUser.id,
      value: props.currentUser.id,
      label: props.currentUser.name,
      name: props.currentUser.name,
    };
    setFilterParams(filters => ({
      ...filters,
      requestedBy: currentUserValue,
      createdBy: currentUserValue,
    }));
  };

  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementOutboundHeader
        isRequestsOpen={props.isRequestsList}
        showMyStockMovements={selectFiltersForMyStockMovements}
      />
      <StockMovementOutboundFilters filterParams={filterParams} setFilterParams={setFilterParams} />
      <StockMovementOutboundTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
});

export default connect(mapStateToProps, { fetchTranslations })(StockMovementOutboundList);

StockMovementOutboundList.propTypes = {
  locale: PropTypes.string.isRequired,
  isRequestsList: PropTypes.bool.isRequired,
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};
