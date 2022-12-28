import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchBuyers, fetchPurchaseOrderStatuses, fetchTranslations } from 'actions';
import filterFields from 'components/purchaseOrder/FilterFields';
import PurchaseOrderListFilters from 'components/purchaseOrder/PurchaseOrderListFilters';
import PurchaseOrderListHeader from 'components/purchaseOrder/PurchaseOrderListHeader';
import PurchaseOrderListTable from 'components/purchaseOrder/PurchaseOrderListTable';
import usePurchaseOrderFilters from 'hooks/list-pages/purchase-order/usePurchaseOrderFilters';


const PurchaseOrderList = (props) => {
  const {
    defaultFilterValues, setFilterValues, filterParams, isCentralPurchasingEnabled,
  } = usePurchaseOrderFilters();

  useEffect(() => {
    props.fetchTranslations(props.locale, 'purchaseOrder');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);

  return (
    <div className="d-flex flex-column list-page-main">
      <PurchaseOrderListHeader />
      <PurchaseOrderListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{
          statuses: props.statuses,
          buyers: props.buyers,
          isCentralPurchasingEnabled,
        }}
      />
      <PurchaseOrderListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  buyers: state.organizations.buyers,
  statuses: state.purchaseOrder.statuses,
});

export default connect(mapStateToProps, {
  fetchTranslations,
  fetchStatuses: fetchPurchaseOrderStatuses,
  fetchBuyerOrganizations: fetchBuyers,
})(PurchaseOrderList);


PurchaseOrderList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  buyers: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
};

