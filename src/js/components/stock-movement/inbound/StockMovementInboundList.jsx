import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import StockMovementInboundFilters from 'components/stock-movement/inbound/StockMovementInboundFilters';
import StockMovementInboundHeader from 'components/stock-movement/inbound/StockMovementInboundHeader';
import StockMovementInboundTable from 'components/stock-movement/inbound/StockMovementInboundTable';

const StockMovementInboundList = (props) => {
  const [filterParams, setFilterParams] = useState({
    destination: {
      id: props.currentLocation.id,
      value: props.currentLocation.id,
      name: props.currentLocation.name,
      label: props.currentLocation.name,
    },
  });

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
  }, [props.locale]);

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
      <StockMovementInboundHeader showMyStockMovements={selectFiltersForMyStockMovements} />
      <StockMovementInboundFilters filterParams={filterParams} setFilterParams={setFilterParams} />
      <StockMovementInboundTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
});

export default connect(mapStateToProps, { fetchTranslations })(StockMovementInboundList);

StockMovementInboundList.propTypes = {
  locale: PropTypes.string.isRequired,
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
