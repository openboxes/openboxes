import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchBuyers, fetchPurchaseOrderStatuses } from 'actions';
import filterFields from 'components/purchaseOrder/FilterFields';
import PurchaseOrderListFilters from 'components/purchaseOrder/PurchaseOrderListFilters';
import PurchaseOrderListHeader from 'components/purchaseOrder/PurchaseOrderListHeader';
import PurchaseOrderListTable from 'components/purchaseOrder/PurchaseOrderListTable';
import usePurchaseOrderFilters from 'hooks/list-pages/purchase-order/usePurchaseOrderFilters';
import useTranslation from 'hooks/useTranslation';


const PurchaseOrderList = (props) => {
  const {
    defaultFilterValues, setFilterValues, filterParams, isCentralPurchasingEnabled,
  } = usePurchaseOrderFilters();

  useTranslation('purchaseOrder', 'reactTable');

  return (
    <div className="d-flex flex-column list-page-main">
      <PurchaseOrderListHeader />
      <PurchaseOrderListFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterValues}
        filterFields={filterFields}
        formProps={{
          paymentTerms: props.paymentTerms,
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
  buyers: state.organizations.buyers,
  statuses: state.purchaseOrder.statuses,
  paymentTerms: state.purchaseOrder.paymentTerms,
});

export default connect(mapStateToProps, {
  fetchStatuses: fetchPurchaseOrderStatuses,
  fetchBuyerOrganizations: fetchBuyers,
})(PurchaseOrderList);


PurchaseOrderList.propTypes = {
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  paymentTerms: PropTypes.arrayOf(PropTypes.shape({
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

