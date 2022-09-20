import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchBuyers, fetchPurchaseOrderStatuses } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import DateField from 'components/form-elements/DateField';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { debounceLocationsFetch, debounceUsersFetch } from 'utils/option-utils';

const filterFields = {
  status: {
    type: FilterSelectField,
    label: 'react.purchaseOrder.status.label',
    defaultMessage: 'Status',
    attributes: {
      multi: true,
      filterElement: true,
    },
    getDynamicAttr: ({ statuses }) => ({
      options: statuses,
    }),
  },
  statusStartDate: {
    type: DateField,
    label: 'react.purchaseOrder.lastUpdateAfter.label',
    defaultMessage: 'Last update after',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  statusEndDate: {
    type: DateField,
    label: 'react.purchaseOrder.lastUpdateBefore.label',
    defaultMessage: 'Last update before',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  origin: {
    type: FilterSelectField,
    label: 'react.purchaseOrder.supplier.label',
    defaultMessage: 'Supplier',
    attributes: {
      async: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'name',
      options: [],
      filterOptions: options => options,
      filterElement: true,
    },
    getDynamicAttr: ({
      debouncedOriginLocationsFetch,
    }) => ({
      loadOptions: debouncedOriginLocationsFetch,
    }),
  },
  destination: {
    type: FilterSelectField,
    label: 'react.purchaseOrder.destination.label',
    defaultMessage: 'Destination',
    attributes: {
      async: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'name',
      options: [],
      filterOptions: options => options,
      filterElement: true,
    },
    getDynamicAttr: ({
      debouncedDestinationLocationsFetch,
    }) => ({
      loadOptions: debouncedDestinationLocationsFetch,
    }),
  },
  destinationParty: {
    type: FilterSelectField,
    label: 'react.purchaseOrder.purchasingOrganization.label',
    defaultMessage: 'Purchasing organization',
    attributes: {
      valueKey: 'id',
      filterElement: true,
    },
    getDynamicAttr: ({ buyers, isCentralPurchasingEnabled }) => ({
      options: buyers,
      disabled: isCentralPurchasingEnabled,
    }),
  },
  orderedBy: {
    type: FilterSelectField,
    label: 'react.purchaseOrder.orderedBy.label',
    defaultMessage: 'Ordered by',
    attributes: {
      async: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'name',
      options: [],
      filterOptions: options => options,
      filterElement: true,
    },
    getDynamicAttr: ({
      debouncedUsersFetch,
    }) => ({
      loadOptions: debouncedUsersFetch,
    }),
  },
};

const PurchaseOrderListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  supportedActivities,
  currentLocation,
  statuses,
  fetchStatuses,
  buyers,
  fetchBuyerOrganizations,
}) => {
  // Purchasing organizations (organizations with ROLE_BUYER)
  const [defaultValues, setDefaultValues] = useState({});
  const isCentralPurchasingEnabled = supportedActivities.includes('ENABLE_CENTRAL_PURCHASING');

  const determineDefaultValues = () => {
    // If central purchasing is enabled, set default purchasing org as currentLocation's org
    if (isCentralPurchasingEnabled) {
      setDefaultValues({
        destinationParty: buyers.find(org => org.id === currentLocation.organization.id),
      });
      setFilterParams(prevState => ({
        ...prevState,
        destinationParty: buyers.find(org => org.id === currentLocation.organization.id),
      }));
      return;
    }
    // If central purchasing is not enabled, set default destination as currentLocation
    setDefaultValues({
      destination: currentLocation,
    });
    setFilterParams(prevState => ({
      ...prevState,
      destination: currentLocation,
    }));
  };

  useEffect(() => {
    // If statuses not yet in store, fetch them
    if (statuses.length === 0) {
      fetchStatuses();
    }

    if (!buyers || buyers.length === 0) {
      fetchBuyerOrganizations();
      return;
    }
    determineDefaultValues();
  }, [buyers]);

  const debouncedOriginLocationsFetch = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    ['FULFILL_ORDER'],
  );
  const debouncedDestinationLocationsFetch = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    ['RECEIVE_STOCK'],
  );
  const debouncedUsersFetch = debounceUsersFetch(debounceTime, minSearchLength);

  return (
    <div className="d-flex flex-column purchase-order-list-filters">
      <FilterForm
        filterFields={filterFields}
        onSubmit={values => setFilterParams(values)}
        formProps={{
          statuses,
          debouncedOriginLocationsFetch,
          debouncedDestinationLocationsFetch,
          debouncedUsersFetch,
          buyers,
          isCentralPurchasingEnabled,
        }}
        defaultValues={defaultValues}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  supportedActivities: state.session.supportedActivities,
  currentLocation: state.session.currentLocation,
  // All possible PO statuses from store
  statuses: state.purchaseOrder.statuses,
  buyers: state.organizations.buyers,
});

const mapDispatchToProps = {
  fetchStatuses: fetchPurchaseOrderStatuses,
  fetchBuyerOrganizations: fetchBuyers,
};

export default connect(mapStateToProps, mapDispatchToProps)(PurchaseOrderListFilters);

PurchaseOrderListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  currentLocation: PropTypes.shape({}).isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  buyers: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  fetchBuyerOrganizations: PropTypes.func.isRequired,
};
